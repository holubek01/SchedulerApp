package com.example.scheduler.tests.dao

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.ShowPlanController
import com.example.scheduler.controller.TeacherModuleController
import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.db.dao.*
import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.models.ClassesToWrite
import com.example.scheduler.objects.*
import com.example.scheduler.utils.MessageBundle
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.time.LocalDate
import java.util.*


internal class DaoTest: ApplicationTest()
{
    private val teacherDAO = TeacherDAOImpl()
    private val locationDAO = LocationDAOImpl()
    private val fieldDAO = FieldDAOImpl()
    private val roomDAO = RoomDAOImpl()
    private val planDAO = PlanDAOImpl()
    private var classesDAO = ClassesDAOImpl()
    private lateinit var teacherController: TeacherModuleController


    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/teacherModule.fxml"))
        val root: Parent = loader.load()
        teacherController = loader.getController()
        stage!!.scene = Scene(root)
    }


    /**
     * Testowanie przypadku chęci zmiany sali na wybranych zajęciach
     * Przed testem należy dodać zajęcia do planu (do tego potrzebne jest wprowadzenie
     * takich danych jak nauczyciel, kierunek, grupa itp)
     */
    @Test
    fun changeRoomTest()
    {
        Platform.runLater {
            runBlocking {
                planDAO.refillHours()

                val field = Field("Tymczasowy kierunek", 2, "TEMP")

                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val teacherToAdd2 = Teacher("Jan2", "Kowalski2", "ssr23Rtg@s.pl", "777777767")

                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationNotToShow = Location("LocTemp2", "Wro", "Ryn11", "22-222")

                val room1 = Room(
                    roomName = "roomTest1",
                    location = locationToAdd.locationName,
                    volume = 1,
                    floor = 1
                )

                val room2 = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                val room3 = Room(
                    roomName = "roomTest3",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )


                val room4 = Room(
                    roomName = "roomTest4",
                    location = locationNotToShow.locationName,
                    volume = 2,
                    floor = 2
                )

                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest1",
                    "Kowalski Jan"
                )

                //Dodaj potrzebne dane
                addObjects(room1, room2, room3, room4, locationToAdd, locationNotToShow, teacherToAdd, teacherToAdd2, field)

                //Przed dodaniem zajęć plan powinien być pusty
                assertTrue(planDAO.getPlanGroup(field.fieldName, "IA", "Cały plan").isEmpty())
                classesDAO.addToPlan(classes1)

                //Po dodaniu zajęć w planue powinny być 1 zajęcia
                assertEquals(planDAO.getPlanGroup(field.fieldName, "IA", "Piątek").size, 1)

                val classesRead = ClassesToRead(classes1.date!!, classes1.hour!!, classes1.subject!!, "${classes1.room}, ${classes1.location}", classes1.teacher!!, "${classes1.group}, ${classes1.fieldOfStudy}")
                var freeRooms = classesRead.getFreeRooms()
                var busyRooms = classesRead.getBusyRooms()

                //Sala room1 nie powinna się wyświetlać bo to ta sama sala co zmieniana
                //Sala room4 powinna się wyświetlić bo pomimo że jest w innej lokalizacji to można
                // zmienić lokalizację bo nie koliduje to z innymi zajęciami
                assertFalse(freeRooms.contains("${room1.roomName}, ${room1.location}"))
                assertTrue(freeRooms.contains("${room2.roomName}, ${room2.location}"))
                assertTrue(freeRooms.contains("${room3.roomName}, ${room3.location}"))
                assertTrue(freeRooms.contains("${room4.roomName}, ${room4.location}"))

                //lista zajętych sal powinna być pusta
                assertFalse(busyRooms.contains("${room1.roomName}, ${room1.location}"))
                assertFalse(busyRooms.contains("${room2.roomName}, ${room2.location}"))
                assertFalse(busyRooms.contains("${room3.roomName}, ${room3.location}"))
                assertFalse(busyRooms.contains("${room4.roomName}, ${room4.location}"))

                var classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertEquals(classesList[0].room, "${room1.roomName}, ${room1.location}")

                //zamieńmy salę room1 na room2
                classesRead.setAnotherRoom("${room2.roomName}, ${room2.location}")

                classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")

                //Sprawdź czy zmiana się dokonała
                assertEquals(classesList[0].room, "${room2.roomName}, ${room2.location}")
                assertNotEquals(classesList[0].room, "${room1.roomName}, ${room1.location}")

                classesRead.room = "${room2.roomName}, ${room2.location}"

                //Zamień znowu
                classesRead.setAnotherRoom("${room1.roomName}, ${room1.location}")

                classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")

                //Sprawdź czy zmieniło się
                assertNotEquals(classesList[0].room, "${room2.roomName}, ${room2.location}")
                assertEquals(classesList[0].room, "${room1.roomName}, ${room1.location}")


                //Dodajmy zajęcia do room2 w tym samym czasie oraz zajęcia do room3 ale w innym czasie
                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IB",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest2",
                    "Kowalski2 Jan2"
                )

                val classes3 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IB",
                    "Język polski",
                    "LocTemp",
                    "08.45-09.30",
                    "roomTest3",
                    "Kowalski2 Jan2"
                )

                classesDAO.addToPlan(classes2)
                classesDAO.addToPlan(classes3)


                classesRead.room = "${room1.roomName}, ${room1.location}"

                freeRooms = classesRead.getFreeRooms()
                busyRooms = classesRead.getBusyRooms()
                busyRooms.removeIf { it == classesRead.room }

                //teraz tylko room3 oraz room4 powinny byc wolne
                assertFalse(freeRooms.contains("${room1.roomName}, ${room1.location}"))
                assertFalse(freeRooms.contains("${room2.roomName}, ${room2.location}"))
                assertTrue(freeRooms.contains("${room3.roomName}, ${room3.location}"))
                assertTrue(freeRooms.contains("${room4.roomName}, ${room4.location}"))

                //room2 jest zajęta w tym czasie
                assertFalse(busyRooms.contains("${room1.roomName}, ${room1.location}"))
                assertTrue(busyRooms.contains("${room2.roomName}, ${room2.location}"))
                assertFalse(busyRooms.contains("${room3.roomName}, ${room3.location}"))
                assertFalse(busyRooms.contains("${room4.roomName}, ${room4.location}"))

                //Spróbujmy zamienić teraz sale na zajęciach (przenosimy zajęcia do innych sal)
                //Zmiana na room2
                classesDAO.changeRooms("${room2.roomName}, ${room2.location}", classesRead)

                classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertEquals(classesList[0].room, "${room2.roomName}, ${room2.location}")

                classesList = planDAO.getPlanGroup(field.fieldName, "IB", "Piątek")
                assertEquals(classesList[0].room, "${room1.roomName}, ${room1.location}")

                //Usuń wcześniej dodane obiekty
                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationNotToShow)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                planDAO.refillHours()
            }
        }
    }


    //Test sprawdzający czy podczas zmiany nauczyciela wyświetlają się tylko ci zajęci nauczyciele
    //którzy zdążą zmienić lokalizację aby dojechać na zajęcia
    @Test
    fun canTeacherMoveBetweenClassesTest()
    {
        Platform.runLater {
            runBlocking {
                planDAO.refillHours()

                //Dodawanie 3 nauczycieli
                //Pierwszy zostanie przypisany do do zajęć
                //Drugi, który zdąży zmienić lokalizację bo ma zajęcia w tej samej lokalizacji
                //Trzeci który nie zdąży zmienić (zajęcia zaraz po sobie w innych lokalizacjach)
                //Wszyscy mają takie samo dyspo i przedmioty
                var availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45", "08.45-09.30", "09.35-10.20")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Język polski", "Język angielski")

                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val teacherToAdd2 = Teacher("Jan2", "Kowalski2", "sss@s.pl", "444444444")
                val teacherToAdd3 = Teacher("Jan3", "Kowalski3", "sssplpl@s.pl", "555555555")

                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)
                if (teacherDAO.getTeachers().contains(teacherToAdd3)) teacherDAO.deleteTeacher(teacherToAdd3)


                //dodawanie nauczyciela
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd, subjects)

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45", "08.45-09.30", "09.35-10.20")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd2, subjects)

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45", "08.45-09.30", "09.35-10.20")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd3, subjects)

                //dodawanie kierunku i grup
                val field = Field("Tymczasowy kierunek", 2, "TEMP")

                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)

                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 4, 1, "I")

                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 10)
                fieldDAO.addSubjectToSem("Język angielski", field.fieldName, 1, 2)

                assertTrue(fieldDAO.getFields().contains(field))

                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("LocTemp2", "Wro", "Ryn11", "22-222")
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                if (locationDAO.getLocations().contains(locationToAdd2)) locationDAO.deleteLocation(locationToAdd2)

                //Dodawanie lokalizacji z salami
                locationDAO.addLocation(locationToAdd)
                locationDAO.addLocation(locationToAdd2)

                val room1 = Room(
                    roomName = "roomTest1",
                    location = locationToAdd.locationName,
                    volume = 1,
                    floor = 1
                )

                val room2 = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                val room3 = Room(
                    roomName = "roomTest3",
                    location = locationToAdd2.locationName,
                    volume = 2,
                    floor = 2
                )

                roomDAO.addRoom(room1)
                roomDAO.addRoom(room2)
                roomDAO.addRoom(room3)

                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest1",
                    "Kowalski Jan"
                )

                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IB",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest2",
                    "Kowalski2 Jan2"
                )

                val classes3 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IC",
                    "Język polski",
                    "LocTemp2",
                    "08.00-08.45",
                    "roomTest3",
                    "Kowalski3 Jan3"
                )

                classesDAO.addToPlan(classes1)
                classesDAO.addToPlan(classes2)
                classesDAO.addToPlan(classes3)

                val firstTeacher = "${teacherToAdd.lastname} ${teacherToAdd.firstname}"
                val classesRead = ClassesToRead(
                    classes1.date!!,
                    classes1.hour!!,
                    classes1.subject!!,
                    "${classes1.room}, ${classes1.location}",
                    firstTeacher,
                    "${classes1.group}, ${classes1.fieldOfStudy}"
                )

                val controller = ShowPlanController()
                val busyTeachers = classesRead.getBusyTeachers(
                    controller.teachersModel.getTeacherSubjectsByName(classes1.teacher!!)
                )

                //teacher2 powinien się pojawić (jest w tej samej lokalizacji)
                assertTrue(busyTeachers.contains("${teacherToAdd2.lastname} ${teacherToAdd2.firstname}"))

                //teacher3 nie powinien się pojawić (Za mało czasu na zmianę lokalizacji)
                assertFalse(busyTeachers.contains("${teacherToAdd3.lastname} ${teacherToAdd3.firstname}"))

                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationToAdd2)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                teacherDAO.deleteTeacher(teacherToAdd3)
                planDAO.refillHours()
            }
        }

    }

    /**
     * Testowanie przypadku chęci zmiany nauczyciela na wybranych zajęciach
     * Przed testem należy dodać zajęcia do planu (do tego porzebne jest wprowadznenie
     * takich danych jak nauczyciel, kierunek, grupa itp).
     * Test sprawdza np czy na liście wolnych nauczycieli nie pojawiają się nauczyciele, którzy nie uczą przemdiotu,
     * mają złe dyspo itp
     */
    @Test
    fun changeTeacherTest()
    {
        Platform.runLater {
            runBlocking {
                planDAO.refillHours()

                var availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val availabilityList2 = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf()
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Język polski", "Język angielski")
                val subjects2 = mutableListOf("Podstawy przedsiębiorczości", "Język migowy")
                val subjects3 = mutableListOf("Język polski", "Język migowy")


                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val teacherToAdd2 = Teacher("Jan2", "Kowalski2", "sss@s.pl", "444444444")
                val teacherToAdd3 = Teacher("Jan3", "Kowalski3", "sssplpl@s.pl", "555555555")
                val teacherToAdd4 = Teacher("Jan4", "Kowalski4", "ss@ss.pl", "666666666")
                val teacherToAdd5 = Teacher("Jan5", "Kowalski5", "sssadfsplpl@s.pl", "555552555")
                val teacherToAdd6 = Teacher("Jan6", "Kowalski6", "sssawetdfsplpl@s.pl", "555522555")

                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)
                if (teacherDAO.getTeachers().contains(teacherToAdd3)) teacherDAO.deleteTeacher(teacherToAdd3)
                if (teacherDAO.getTeachers().contains(teacherToAdd4)) teacherDAO.deleteTeacher(teacherToAdd4)
                if (teacherDAO.getTeachers().contains(teacherToAdd5)) teacherDAO.deleteTeacher(teacherToAdd5)
                if (teacherDAO.getTeachers().contains(teacherToAdd6)) teacherDAO.deleteTeacher(teacherToAdd6)


                //dodawanie nauczyciela
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd, subjects)

                assertTrue(teacherDAO.getTeachers().contains(teacherToAdd))

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }
                //Nauczyciel, który powinien się pojawić na liście wolnych nauczycieli
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd2, subjects)

                //Nauczyciel, który nie powinien się pojawić na liście wolnych nauczycieli (zła dyspo)
                teacherController.availabilityList = availabilityList2
                teacherController.addTeacher(teacherToAdd3, subjects)

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                //Nauczyciel, który nie powinien się pojawić na liście wolnych nauczycieli (nie uczy przedmiotu)
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd4, subjects2)

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd6, subjects3)

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45","08.45-09.30","09.35-10.20")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd5, subjects)

                //dodawanie kierunku i grup
                val field = Field("Tymczasowy kierunek", 2, "TEMP")

                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)

                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 4, 1, "I")

                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 10)
                fieldDAO.addSubjectToSem("Język angielski", field.fieldName, 1, 2)
                fieldDAO.addSubjectToSem("Język migowy", field.fieldName, 1, 2)

                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("LocTemp2", "Wro", "Ryn11", "22-222")
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                if (locationDAO.getLocations().contains(locationToAdd2)) locationDAO.deleteLocation(locationToAdd2)

                //Dodawanie lokalizacji z salami
                locationDAO.addLocation(locationToAdd)
                locationDAO.addLocation(locationToAdd2)

                val room1 = Room(
                    roomName = "roomTest1",
                    location = locationToAdd.locationName,
                    volume = 1,
                    floor = 1
                )

                val room2 = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                val room3 = Room(
                    roomName = "roomTest3",
                    location = locationToAdd2.locationName,
                    volume = 2,
                    floor = 2
                )

                val room4 = Room(
                    roomName = "roomTest4",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                roomDAO.addRoom(room1)
                roomDAO.addRoom(room2)
                roomDAO.addRoom(room3)
                roomDAO.addRoom(room4)

                assertTrue(roomDAO.getRooms(locationToAdd.locationName).contains(room1))
                assertTrue(roomDAO.getRooms(locationToAdd.locationName).contains(room2))


                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest1",
                    "Kowalski Jan"
                )

                //zajęcia w innej lokalizacji godzinę później (teacher4 nie zdąży dojechać na zajęcia)
                val classes4 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "ID",
                    "Język polski",
                    "LocTemp2",
                    "08.45-09.30",
                    "roomTest3",
                    "Kowalski5 Jan5"
                )

                assertTrue(planDAO.getPlanGroup(field.fieldName, "IA", "Piątek").isEmpty())
                classesDAO.addToPlan(classes1)
                classesDAO.addToPlan(classes4)

                //sprawdzenie czy zajęcia czy na pewno się pobrały
                planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertEquals(planDAO.getPlanGroup(field.fieldName, "IA", "Piątek").size, 1)


                var classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertEquals(classesList[0].teacher, "Kowalski Jan")

                val firstTeacher = "${teacherToAdd.lastname} ${teacherToAdd.firstname}"
                val secondTeacher = "${teacherToAdd2.lastname} ${teacherToAdd2.firstname}"
                var classesRead = ClassesToRead(classes1.date!!, classes1.hour!!, classes1.subject!!, "${classes1.room}, ${classes1.location}", firstTeacher, "${classes1.group}, ${classes1.fieldOfStudy}")

                var freeTeachers = classesRead.getFreeTeachers()
                assertTrue(freeTeachers.contains("${teacherToAdd2.lastname} ${teacherToAdd2.firstname}"))

                //teacher3 nie powinien się pojawić - zła dyspo
                assertFalse(freeTeachers.contains("${teacherToAdd3.lastname} ${teacherToAdd3.firstname}"))

                //teacher4 nie powinien się pojawić - nie uczy przedmiotu
                assertFalse(freeTeachers.contains("${teacherToAdd4.lastname} ${teacherToAdd4.firstname}"))

                //teacher5 powinien nie pokazać bo nie zdąży zmienić lokalizacji (Na następnych zajęciach jest w innej lokalizacji)
                assertFalse(freeTeachers.contains("${teacherToAdd5.lastname} ${teacherToAdd5.firstname}"))

                classesRead = ClassesToRead(classes1.date!!, classes1.hour!!, classes1.subject!!, "${classes1.room}, ${classes1.location}", firstTeacher, "${classes1.group}, ${classes1.fieldOfStudy}")

                //Zmiana na teacher2
                classesRead.setAnotherTeacher(firstTeacher, secondTeacher)

                classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertNotEquals(classesList[0].teacher, "Kowalski Jan")
                assertEquals(classesList[0].teacher, "Kowalski2 Jan2")

                classesRead.teacher = "Kowalski2 Jan2"

                //zamień znowu
                classesRead.setAnotherTeacher(secondTeacher, firstTeacher)

                classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertEquals(classesList[0].teacher, "Kowalski Jan")
                assertNotEquals(classesList[0].teacher, "Kowalski2 Jan2")

                //Teraz dodajmy w tym o tej samej godzinie zajęcia 2 nauczycielowi przedmiotu "język angielski" i zobaczmy czy będzie można zamienić
                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IB",
                    "Język angielski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest2",
                    "Kowalski2 Jan2"
                )

                val classes3 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IC",
                    "Język migowy",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest4",
                    "Kowalski6 Jan6"
                )

                classesDAO.addToPlan(classes2)
                classesDAO.addToPlan(classes3)
                planDAO.getPlanGroup(field.fieldName, "IB", "Piątek")
                assertEquals(planDAO.getPlanGroup(field.fieldName, "IB", "Piątek").size, 1)

                val controller = ShowPlanController()
                freeTeachers = classesRead.getFreeTeachers()

                //Teacher2 nie powinien pojawić się już na liście wolnych nauczycieli, powinien się pojawić na liście zajętych nauczycieli
                assertFalse(freeTeachers.contains("${teacherToAdd2.lastname} ${teacherToAdd2.firstname}"))
                assertFalse(freeTeachers.contains("${teacherToAdd3.lastname} ${teacherToAdd3.firstname}"))
                assertFalse(freeTeachers.contains("${teacherToAdd4.lastname} ${teacherToAdd4.firstname}"))

                classes1.teacher = "Kowalski Jan"
                classesRead.teacher = "Kowalski Jan"

                //teacher1 prowadził w tym czasie zajęcia z j.polskiego (którego uczy również teacher2),
                //natomiast teacher2 prowadził w tym czasie zajęcia z j.angielskiego (którego uczy również teacher1)
                //Zapewniliśmy że na liście zajętych nauczycieli nie pojawiają się nauczyciele, którzy nie uczę przedmiotu
                //z zajęc do modyfikacji. A co w przypadku gdy nauczyciel z modyfikowanych zajęc nie uczy przedmiotu
                //który w tym czasie prowadzi zajęty nauczyciel? Taki nauczyciel nie powinien się wtedy pojawić na liście!

                val busyTeachers = classesRead.getBusyTeachers(controller.teachersModel.getTeacherSubjectsByName(classes1.teacher!!))
                assertTrue(busyTeachers.contains("${teacherToAdd2.lastname} ${teacherToAdd2.firstname}"))
                assertFalse(busyTeachers.contains("${teacherToAdd3.lastname} ${teacherToAdd3.firstname}"))
                assertFalse(busyTeachers.contains("${teacherToAdd4.lastname} ${teacherToAdd4.firstname}"))

                //Nie powinien się pojawić, bo nauczyciel z modyfikowanych zajęc nie uczy przedmiotu, który uczy
                //w tym czasie teacher6 czyli migowego
                assertFalse(busyTeachers.contains("${teacherToAdd6.lastname} ${teacherToAdd6.firstname}"))

                //zamiana teacher1 i teacher2 na zajęciach
                classesRead.changeTeachers(firstTeacher, secondTeacher)

                classesList = planDAO.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertNotEquals(classesList[0].teacher, "Kowalski Jan")
                assertEquals(classesList[0].teacher, "Kowalski2 Jan2")

                classesList = planDAO.getPlanGroup(field.fieldName, "IB", "Piątek")
                assertNotEquals(classesList[0].teacher, "Kowalski2 Jan2")
                assertEquals(classesList[0].teacher, "Kowalski Jan")

                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationToAdd2)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                teacherDAO.deleteTeacher(teacherToAdd3)
                teacherDAO.deleteTeacher(teacherToAdd4)
                teacherDAO.deleteTeacher(teacherToAdd5)
                teacherDAO.deleteTeacher(teacherToAdd6)
                planDAO.refillHours()
            }
        }
    }

    //Test, sprawdzjący czy metoda getTeacherID działa poprawnie
    @Test
    fun getTeacherIDTest()
    {
        Platform.runLater {
            runBlocking {
                val availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Język polski", "Język angielski")
                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val teacherToAdd2 = Teacher("Jan2", "Kowalski2", "ssr22tg@s.pl", "777727777")
                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)

                //dodawanie nauczyciela
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd, subjects)

                //Pobierz max id
                val expectedID = getMaxIDFromTable(Type.TEACHER) + 1

                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd2, subjects)

                val teacherID = teacherDAO.getTeacherID(teacherToAdd2)
                assertEquals(expectedID, teacherID)

                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
            }
        }
    }

    //Test, sprawdzjący czy metoda getRoomID działa poprawnie
    @Test
    fun getRoomIDTest()
    {
        Platform.runLater {
            runBlocking {
                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                locationDAO.addLocation(locationToAdd)

                val room1 = Room(
                    roomName = "roomTest1",
                    location = locationToAdd.locationName,
                    volume = 1,
                    floor = 1
                )

                val room2 = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                roomDAO.addRoom(room1)

                //Pobierz max id
                val expectedID = getMaxIDFromTable(Type.ROOM) + 1

                roomDAO.addRoom(room2)


                val roomID = roomDAO.getRoomID(room2)
                assertEquals(expectedID, roomID)

               locationDAO.deleteLocation(locationToAdd)
            }
        }
    }

    //Test, sprawdzjący czy metoda getLocationID działa poprawnie
    @Test
    fun locationIDTest()
    {
        Platform.runLater {
            runBlocking {
                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("LocTemp2", "Wro", "Ryn11", "22-222")
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                if (locationDAO.getLocations().contains(locationToAdd2)) locationDAO.deleteLocation(locationToAdd2)
                locationDAO.addLocation(locationToAdd)

                //Pobierz max id
                val expectedID = getMaxIDFromTable(Type.LOCATION) + 1
                locationDAO.addLocation(locationToAdd2)


                val locationID = locationDAO.getLocationID(locationToAdd2)
                assertEquals(expectedID, locationID)

                //Teraz sprawdźmy czy podając dobre ID otrzymamy dobrą lokalizację
                val loc = locationDAO.getLocationById(locationID)
                assertEquals(loc.locationName, locationToAdd2.locationName)
                assertEquals(loc.city, locationToAdd2.city)
                assertEquals(loc.street, locationToAdd2.street)
                assertEquals(loc.postcode, locationToAdd2.postcode)


                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationToAdd2)
            }
        }
    }

    //Test, sprawdzjący czy metoda getFieldID działa poprawnie
    @Test
    fun getFieldIDTest()
    {
        Platform.runLater {
            runBlocking {
                val field = Field("Tymczasowy kierunek", 2, "TEMP")
                val field2 = Field("Tymczasowy kierunek 2", 2, "TEMP2")

                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)
                if (fieldDAO.getFields().contains(field2)) fieldDAO.deleteField(field2)

                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 2, 1, "I")

                assertTrue(fieldDAO.getGroups(field.fieldName, 1).contains("IA"))
                assertTrue(fieldDAO.getGroups(field.fieldName, 1).contains("IB"))

                //Pobierz max id
                val expectedID = getMaxIDFromTable(Type.FIELD) + 1
                fieldDAO.addField(field2)
                fieldDAO.addGroups(field2, 2, 1, "I")

                val fieldID = fieldDAO.getFieldID(field2)
                assertEquals(expectedID, fieldID)

                fieldDAO.deleteField(field)
                fieldDAO.deleteField(field2)
            }
        }
    }



    enum class Type {TEACHER, ROOM, LOCATION, FIELD}

    private fun getMaxIDFromTable(type: Type):Int
    {
        val query = when (type) {
            Type.TEACHER -> "select max(teacherID) FROM teachers"
            Type.ROOM -> "select max(roomID) FROM rooms"
            Type.LOCATION -> "select max(locationID) FROM location"
            Type.FIELD -> "select max(fieldID) FROM fields"
        }

        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getInt(1) }.first()
    }


    /**
     * Testowanie przypadku chęci zmiany godziny i sali na wybranych zajęciach
     * Przed testem należy dodać zajęcia do planu (do tego porzebne jest wprowadznenie
     * takich danych jak nauczyciel, kierunek, grupa itp)
     */
    @Test
    fun changeHourAndRoomTest()
    {
        Platform.runLater {
            runBlocking {
                planDAO.refillHours()

                val availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45", "08.45-09.30", "09.30-10.20")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Język polski", "Język angielski")

                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val teacherToAdd2 = Teacher("Jan2", "Kowalski2", "ssr22tg@s.pl", "772777777")

                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)

                //dodawanie nauczyciela
                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd, subjects)

                teacherController.availabilityList = availabilityList
                teacherController.addTeacher(teacherToAdd2, subjects)

                //dodawanie kierunku i grup
                val field = Field("Tymczasowy kierunek", 2, "TEMP")

                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)

                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 3, 1, "I")

                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 10)
                fieldDAO.addSubjectToSem("Język angielski", field.fieldName, 1, 2)

                //dodawanie lokalizacji
                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)

                //Dodawanie lokalizacji z salami
                locationDAO.addLocation(locationToAdd)


                val room1 = Room(
                    roomName = "roomTest1",
                    location = locationToAdd.locationName,
                    volume = 1,
                    floor = 1
                )

                val room2 = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                //ta sala będzie zajęta o godzinie 08.45-09.30
                val room3 = Room(
                    roomName = "roomTest3",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                val room4 = Room(
                    roomName = "roomTest4",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )


                roomDAO.addRoom(room1)
                roomDAO.addRoom(room2)
                roomDAO.addRoom(room3)
                roomDAO.addRoom(room4)

                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest1",
                    "Kowalski Jan"
                )

                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IB",
                    "Język polski",
                    "LocTemp",
                    "10.20-11.05",
                    "roomTest3",
                    "Kowalski2 Jan2"
                )

                //Godzina 09.35-10.20 nie powinna się wyświetlić jako wolna ponieważ grupa jest zajęta w tym czasie
                val classes3 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język polski",
                    "LocTemp",
                    "09.35-10.20",
                    "roomTest3",
                    "Kowalski Jan"
                )

                ///ta sala nie powinna sie pojawić do wyboru, bo jest zajęta o nowo wybranej godzinie
                val classes4 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IC",
                    "Język polski",
                    "LocTemp",
                    "08.45-09.30",
                    "roomTest4",
                    "Kowalski2 Jan2"
                )

                classesDAO.addToPlan(classes1)
                classesDAO.addToPlan(classes2)
                classesDAO.addToPlan(classes3)
                classesDAO.addToPlan(classes4)

                val classesRead = ClassesToRead(classes1.date!!, classes1.hour!!, classes1.subject!!, "${classes1.room}, ${classes1.location}", classes1.teacher!!, "${classes1.group}, ${classes1.fieldOfStudy}")

                val freeHours = classesRead.getFreeHours()
                assertFalse(freeHours.contains("09.35-10.20"))   //Grupa jest zajęta w tym czasie
                assertTrue(freeHours.contains("08.45-09.30"))

                //teraz pobieramy sale
                val freeRooms = convertClassesToWriteToClassesToRead(classes1).getFreeRoomsByHour("08.45-09.30")
                assertTrue(freeRooms.contains("roomTest3, LocTemp"))
                assertFalse(freeRooms.contains("roomTest4, LocTemp"))

                //Pobierz plan dla IA
                var classes = planDAO.getPlanGroup(classes1.fieldOfStudy!!, classes1.group!!, "Piątek")
                assertEquals(classes[0].hour, "08.00-08.45")
                assertEquals(classes[0].room, "${room1.roomName}, ${room1.location}")

                //Teraz zmieńmy godzinę zajęć classes1 na 08.45-09.30 oraz salę roomTest3
                classesRead.changeHours("${room3.roomName}, ${room3.location}", "08.45-09.30")

                classes = planDAO.getPlanGroup(classes1.fieldOfStudy!!, classes1.group!!, "Piątek")

                //Sprawdźmy czy się zmieniło
                assertEquals(classes[0].hour, "08.45-09.30")
                assertEquals(classes[0].room, "${room3.roomName}, ${room3.location}")

                locationDAO.deleteLocation(locationToAdd)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                planDAO.refillHours()

            }
        }
    }

    fun convertClassesToWriteToClassesToRead(classesToEdit: ClassesToWrite): ClassesToRead
    {
        val classes = ClassesToRead(
            classesToEdit.date!!,
            classesToEdit.hour!!,
            classesToEdit.subject!!,
            "${classesToEdit.room}, ${classesToEdit.location}",
            classesToEdit.teacher!!,
            "${classesToEdit.group}, ${classesToEdit.fieldOfStudy}"
        )
        return classes
    }

    private fun addObjects(
        room1: Room,
        room2: Room,
        room3: Room,
        room4: Room,
        locationToAdd: Location,
        locationNotToShow: Location,
        teacherToAdd: Teacher,
        teacherToAdd2: Teacher,
        field: Field
    )
    {
        var availabilityList = mutableMapOf<String, MutableList<String>>().apply {
            this["Friday"] = mutableListOf("08.00-08.45")
            this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
            this["Sunday"] = mutableListOf()
        }

        val subjects = mutableListOf("Język polski", "Język angielski")

        if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
        if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)

        //dodawanie nauczyciela
        teacherController.availabilityList = availabilityList
        teacherController.addTeacher(teacherToAdd, subjects)

        availabilityList = mutableMapOf<String, MutableList<String>>().apply {
            this["Friday"] = mutableListOf("08.00-08.45")
            this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
            this["Sunday"] = mutableListOf()
        }

        teacherController.availabilityList = availabilityList
        teacherController.addTeacher(teacherToAdd2, subjects)

        //dodawanie kierunku i grup

        if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)

        fieldDAO.addField(field)
        fieldDAO.addGroups(field, 2, 1, "I")

        //Dodawanie przemdiotów do szkolnego planu nauczania
        fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 10)
        fieldDAO.addSubjectToSem("Język angielski", field.fieldName, 1, 2)

        //dodawanie lokalizacji

        if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
        if (locationDAO.getLocations().contains(locationNotToShow)) locationDAO.deleteLocation(locationNotToShow)

        //Dodawanie lokalizacji z salami
        locationDAO.addLocation(locationToAdd)
        locationDAO.addLocation(locationNotToShow)


        roomDAO.addRoom(room1)
        roomDAO.addRoom(room2)
        roomDAO.addRoom(room3)
        roomDAO.addRoom(room4)
    }



}