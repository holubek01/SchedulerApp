package com.example.scheduler.tests.controllersDirector

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.CreatePlanController
import com.example.scheduler.db.dao.*
import com.example.scheduler.models.*
import com.example.scheduler.objects.*
import com.example.scheduler.utils.ActionType
import com.example.scheduler.utils.MessageBundle
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.time.LocalDate
import java.util.*

internal class CreatePlanControllerTest: ApplicationTest() {
    val dao  = ClassesDAOImpl()
    lateinit var planModel: PlansModel
    lateinit var classesModel: ClassesToWrite
    lateinit var controller: CreatePlanController
    private val teacherDAO = TeacherDAOImpl()
    private val fieldDAO = FieldDAOImpl()
    private val roomDAO = RoomDAOImpl()
    private val locationDAO = LocationDAOImpl()

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/createPlan.fxml"))
        loader.setController(CreatePlanController())
        val root: Parent = loader.load()
        controller = loader.getController()
        controller.stage = stage!!
        stage.scene = Scene(root)
        planModel = controller.plansModel
        classesModel = controller.classesModel
    }

    /**
     * Testowanie dodawania, zapisywania, przywracania planu oraz jego usuwania
     */
    @Test
    fun createSaveAndRetrievePlan()
    {
        //Do ułożenia planu są nam potrzebne dane, zatem musimy je dodać
        Platform.runLater {
            runBlocking {
                planModel.refillHours()

                val field = Field("Tymczasowy kierunek", 2, "TEMP2")
                val fieldNotToShow = Field("Tymczasowy kierunek 2", 2, "TEM3")

                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val teacherToAdd2 = Teacher("Jan2", "Kowalski2", "sSDFsrtg@s.pl", "777877777")
                val teacherToAdd3 = Teacher("Jan3", "Kowalski3", "ssasdfrtg@s.pl", "779777777")
                val teacherToAdd4 = Teacher("Jan4", "Kowalski4", "ssasdsadfdsfffrtg@s.pl", "779774777")

                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("LocTemp2", "Wro", "Ryn11", "22-222")
                val room1 = Room("roomTest1", locationToAdd.locationName, 1,1)
                val room2 = Room("roomTest2",locationToAdd.locationName,1,1)
                val room3 = Room("roomTest3",locationToAdd2.locationName,1,1)

                createAndAddObjects(teacherToAdd, teacherToAdd2, teacherToAdd3, teacherToAdd4, field, fieldNotToShow, locationToAdd, locationToAdd2,room1, room2, room3)

                //poprawne zajęcia do dodania
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

                controller.datepicker.value = LocalDate.parse("2000-09-01")

                val fields = controller.fieldOfStudyChoiceBox.items

                //Powinien pojawić się tylko kierunek field, fieldNotToShow nie powinien bo nie ma godzin do wykorzystania w group_hours_left
                assertTrue(fields.contains(field.fieldName))
                assertFalse(fields.contains(fieldNotToShow.fieldName))

                controller.fieldOfStudyChoiceBox.value = field.fieldName

                //Narazie powinny pojawić się 3 grupy, pozniej dodamy 2 x zajęcia dla IA i IA nie powinna się już pojawić
                var groups = controller.groupChoiceBox.items
                assertEquals(groups.size, 3)
                assertTrue(groups.contains("IA"))
                assertTrue(groups.contains("IB"))
                assertTrue(groups.contains("IC"))

                controller.groupChoiceBox.value = "IA"
                var subjects2 = controller.subjectChoiceBox.items
                assertEquals(subjects2.size, 2)

                //Język niemiecki nie powinien się pokazać, bo zostało 0 godzin do wykorzystania
                assertTrue(subjects2.contains("Język polski"))
                assertTrue(subjects2.contains("Język angielski"))
                assertFalse(subjects2.contains("Język niemiecki"))

                controller.subjectChoiceBox.value = "Język polski"

                var locations = controller.locationChoiceBox.items
                assertTrue(locations.contains(locationToAdd.locationName))

                controller.locationChoiceBox.value = locationToAdd.locationName
                controller.hourChoiceBox.value = "08.00-08.45"

                //Powinny sie pokazać wszystkie sale oprócz room3, bo jest w innej lokalizacji
                var rooms = controller.roomChoicebox.items
                assertTrue(rooms.contains(room1.roomName))
                assertTrue(rooms.contains(room2.roomName))
                assertFalse(rooms.contains(room3.roomName))

                controller.roomChoicebox.value = room2.roomName
                controller.roomChoicebox.value = room1.roomName

                //Powinien pokazać się nauczyciel teacher1 i teacher4, reszta nie uczy wybranego przedmiotu lub nie jest dyspozycyjna
                var teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.contains("Kowalski Jan"))
                assertFalse(teachers.contains("Kowalski2 Jan2"))
                assertFalse(teachers.contains("Kowalski3 Jan3"))
                assertTrue(teachers.contains("Kowalski4 Jan4"))

                assertTrue(!planModel.shouldSaveOldPlan())
                controller.classesModel = classes1.copy()
                controller.onAddClassesPressed()

                //Sprawdzanie czy zajęcia się dodały
                val classesList = planModel.getPlanGroup(field.fieldName, "IA", "Piątek")
                assertEquals(classesList.size,1)
                assertEquals(classesList[0].room, "${classes1.room}, ${classes1.location}")
                assertEquals(classesList[0].teacher, classes1.teacher)
                assertEquals(classesList[0].hour, classes1.hour)
                assertEquals(classesList[0].subject, classes1.subject)
                assertEquals(classesList[0].date, classes1.date)
                assertEquals(classesList[0].group, "${classes1.group}, ${classes1.fieldOfStudy}")

                //Ponownie wybieramy wartości
                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język angielski",
                    "LocTemp",
                    "08.45-09.30",
                    "roomTest1",
                    "Kowalski Jan"
                )

                controller.datepicker.value = LocalDate.parse("2000-09-01")
                controller.fieldOfStudyChoiceBox.value = field.fieldName

                groups = controller.groupChoiceBox.items
                assertEquals(groups.size, 3)
                assertTrue(groups.contains("IA"))
                assertTrue(groups.contains("IB"))
                assertTrue(groups.contains("IC"))

                controller.groupChoiceBox.value = "IA"
                subjects2 = controller.subjectChoiceBox.items

                //nie powinien pojawić się już język polski (do wykorzystania była tylko 1 godzina)
                assertEquals(subjects2.size, 1)
                assertFalse(subjects2.contains("Język polski"))
                assertTrue(subjects2.contains("Język angielski"))
                assertFalse(subjects2.contains("Język niemiecki"))

                controller.subjectChoiceBox.value = "Język angielski"

                locations = controller.locationChoiceBox.items
                assertTrue(locations.contains(locationToAdd.locationName))

                controller.locationChoiceBox.value = locationToAdd.locationName

                //Nie powinna pokazać się godzina 08.00-08.45, bo grupa IA jest już w tym czasie zajęta
                val hours = controller.hourChoiceBox.items
                assertFalse(hours.contains("08.00-08.45"))
                assertTrue(hours.contains("08.45-09.30"))
                controller.hourChoiceBox.value = "08.45-09.30"

                //Powinny pokazać się wszsytkie sale oprócz room3
                rooms = controller.roomChoicebox.items
                assertTrue(rooms.contains(room1.roomName))
                assertTrue(rooms.contains(room2.roomName))
                assertFalse(rooms.contains(room3.roomName))

                controller.roomChoicebox.value = room1.roomName

                //Tym razem powinni pokazać się wszyscy nauczyciele, bo zmieniliśmy przedmiot i godzinę
                teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.contains("Kowalski Jan"))
                assertTrue(teachers.contains("Kowalski2 Jan2"))
                assertTrue(teachers.contains("Kowalski3 Jan3"))

                controller.classesModel = classes2.copy()
                controller.onAddClassesPressed()

                val classes3 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy kierunek",
                    "IB",
                    "Język angielski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest2",
                    "Kowalski4 Jan4"
                )

                controller.datepicker.value = LocalDate.parse("2000-09-01")

                controller.fieldOfStudyChoiceBox.value = field.fieldName

                //Teraz nie powinna się już pokazać IA, bo wykorzystano wszystkie godziny
                groups = controller.groupChoiceBox.items
                assertEquals(groups.size, 2)
                assertFalse(groups.contains("IA"))
                assertTrue(groups.contains("IB"))
                assertTrue(groups.contains("IC"))

                controller.groupChoiceBox.value = "IB"
                subjects2 = controller.subjectChoiceBox.items

                assertEquals(subjects2.size, 2)
                assertTrue(subjects2.contains("Język polski"))
                assertTrue(subjects2.contains("Język angielski"))
                assertFalse(subjects2.contains("Język niemiecki"))

                controller.subjectChoiceBox.value = "Język polski"

                locations = controller.locationChoiceBox.items
                assertTrue(locations.contains(locationToAdd.locationName))

                controller.locationChoiceBox.value = locationToAdd.locationName
                controller.hourChoiceBox.value = "08.00-08.45"

                //Nie powinna pokazać się sala room1 bo jest zajęta przez IA
                rooms = controller.roomChoicebox.items
                assertFalse(rooms.contains(room1.roomName))
                assertTrue(rooms.contains(room2.roomName))
                assertFalse(rooms.contains(room3.roomName))

                controller.roomChoicebox.value = room2.roomName

                //Teacher1 jest już zajęty przez grupę IA, teacher2 i teacher 3 nie mają uczą przedmiotu lub nie mają dyspo
                teachers = controller.teacherChoiceBox.items
                assertFalse(teachers.contains("Kowalski Jan"))
                assertFalse(teachers.contains("Kowalski2 Jan2"))
                assertFalse(teachers.contains("Kowalski3 Jan3"))
                assertTrue(teachers.contains("Kowalski4 Jan4"))

                controller.classesModel = classes3.copy()
                controller.onAddClassesPressed()

                //Zapisz plan
                controller.saveAndClosePlan()
                assertTrue(planModel.checkIfPlanExists(classes1.date!!))

                //Sprwadźmy czy po zapisaniu wykonało się refillHours (czyli czy grupa IA się pojawi) i czy plan jest pusty
                val isPlanEmpty = !planModel.shouldSaveOldPlan()
                assertTrue(isPlanEmpty)

                controller.datepicker.value = LocalDate.parse("2000-09-01")
                controller.fieldOfStudyChoiceBox.value = field.fieldName

                groups = controller.groupChoiceBox.items
                assertEquals(groups.size, 3)
                assertTrue(groups.contains("IA"))

                //Teraz spróbujmy przywrócić plan
                controller.getPlanChoiceBox.value = "plan_2000-09-01"
                controller.getPlanFromBox()

                //Zobaczmy czy plan się dobrze wyświeli (2 x zajęcia dla IA i 1 x dla IB)
                val IAclasses = planModel.getPlanGroup(field.fieldName, "IA", "Cały plan")
                assertEquals(IAclasses.size, 2)

                val IBclasses = planModel.getPlanGroup(field.fieldName, "IB", "Cały plan")
                assertEquals(IBclasses.size, 1)
                controller.saveAndClosePlan()

                assertTrue(planModel.checkIfPlanExists(classes1.date!!))
                controller.wantToEdit = true
                controller.getPlanFromDatepicker(LocalDate.parse("2000-09-01"))
                val IAclassess = planModel.getPlanGroup(field.fieldName, "IA", "Cały plan")
                assertEquals(IAclasses.size, 2)
                controller.saveAndClosePlan()

                //Trzeba usunąć tabele w planem
                val planName = "plan_2000-09-01"
                val hoursLeftName = "group_subject_hours_left_2000-09-01"
                planModel.deletePlan(planName, hoursLeftName)
                planModel.refillHours()
                assertFalse(planModel.checkIfPlanExists(classes1.date!!))

                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationToAdd2)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteSPN(fieldNotToShow.fieldName)
                fieldDAO.deleteField(field)
                fieldDAO.deleteField(fieldNotToShow)
                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                teacherDAO.deleteTeacher(teacherToAdd3)
                teacherDAO.deleteTeacher(teacherToAdd4)
                planModel.refillHours()
            }
        }
    }


    //Testowanie przypadku braku nauczycieli do wyboru (wyświetlanie podpowiedzi z godzinami lub nauczycielami)
    @Test
    fun getHintWhileNoTeachers()
    {
        Platform.runLater {
            runBlocking {
                //Najpiew przetestuj normalny scenariusz kiedy brak nauczyciela do wyboru, system powinien zaproponować inną godzinę
                //Stwórz nauczyciela i przypisz mu zajęcia dla IB o 08.00-08.45. Po wybraniu zajęć o tej samej godzinie dla IA nie powinien się on pojawić
                //ale powinno zaproponować godzinę 08.45-09.30
                planModel.refillHours()

                val field = Field("Tymczasowy kierunek", 2, "TEMP2")
                val teacherToAdd = Teacher("Jan", "Kowalski", "ssrtg@s.pl", "777777777")
                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("LocTemp2", "Wro", "Ryn10", "22-222")
                val room1 = Room("roomTest1", locationToAdd.locationName, 1,1)
                val room2 = Room("roomTest2",locationToAdd.locationName,1,1)
                val room3 = Room("roomTest3",locationToAdd2.locationName,1,1)
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

                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                val subjects = mutableListOf("Język polski", "Język angielski")
                val objectMapper = ObjectMapper()
                var subjectsJSON = objectMapper.writeValueAsString(subjects)
                val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30"))
                val hoursJSON2 = objectMapper.writeValueAsString(listOf("09.35-10.20", "08.45-09.30"))
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)
                assertTrue(teacherDAO.getTeachers().contains(teacherToAdd))
                fieldDAO.deleteSPN(field.fieldName)
                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)
                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 3, 1, "I")
                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 1)
                fieldDAO.addSubjectToSem("Język angielski", field.fieldName, 1, 1)
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                locationDAO.addLocation(locationToAdd)
                roomDAO.addRoom(room1)
                roomDAO.addRoom(room2)
                controller.clearAllBoxes()

                controller.datepicker.value = LocalDate.parse("2000-09-01")
                var fields = controller.fieldOfStudyChoiceBox.items
                assertTrue(fields.contains(field.fieldName))

                controller.fieldOfStudyChoiceBox.value = field.fieldName

                var groups = controller.groupChoiceBox.items
                assertTrue(groups.contains("IB"))

                controller.groupChoiceBox.value = "IB"
                var subjects2 = controller.subjectChoiceBox.items
                assertTrue(subjects2.contains("Język polski"))

                controller.subjectChoiceBox.value = "Język polski"

                var locations = controller.locationChoiceBox.items
                assertTrue(locations.contains("LocTemp"))

                controller.locationChoiceBox.value = "LocTemp"
                controller.hourChoiceBox.value = "08.00-08.45"

                var rooms = controller.roomChoicebox.items
                assertTrue(rooms.contains(room1.roomName))

                controller.roomChoicebox.value = "roomTest1"

                var teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.contains("Kowalski Jan"))

                assertTrue(!planModel.shouldSaveOldPlan())

                //dodaj zajęcia
                controller.classesModel = classes1.copy()
                controller.onAddClassesPressed()


                controller.datepicker.value = LocalDate.parse("2000-09-01")
                fields = controller.fieldOfStudyChoiceBox.items
                assertTrue(fields.contains(field.fieldName))

                controller.fieldOfStudyChoiceBox.value = "Tymczasowy kierunek"

                groups = controller.groupChoiceBox.items
                assertTrue(groups.contains("IA"))

                controller.groupChoiceBox.value = "IA"
                controller.subjectChoiceBox.value = "Język polski"

                locations = controller.locationChoiceBox.items
                assertTrue(locations.contains("LocTemp"))

                controller.locationChoiceBox.value = "LocTemp"
                controller.hourChoiceBox.value = "08.00-08.45"

                rooms = controller.roomChoicebox.items
                assertTrue(rooms.contains(room2.roomName))

                controller.roomChoicebox.value = "roomTest2"

                //Tu nie powinno pokazać żadnego nauczyciela
                teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.isEmpty())

                //Proponowana lista godzin
                assertTrue(controller.suggestedHours.contains("08.45-09.30"))

                //Spróbuj zmienić na sugerowaną godzinę
                controller.hourChoiceBox.value = "08.45-09.30"

                controller.roomChoicebox.value = "roomTest1"
                controller.roomChoicebox.value = "roomTest2"
                teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.contains("Kowalski Jan"))

                controller.classesModel = classes1.copy()
                controller.classesModel.hour = "08.45-09.30"
                controller.classesModel.group = "IA"
                controller.classesModel.room = "roomTest2"

                controller.onAddClassesPressed()

                //Przypadek gdy brak godzin (istnieje nauczyciel, który jest wolny o 08.45-09.30 ale grupa ma w tym czasie zajęcia, natomiast poprzedni nauczyciel ma dyspo tylko 2 godziny )
                controller.datepicker.value = LocalDate.parse("2000-09-01")
                fields = controller.fieldOfStudyChoiceBox.items
                assertTrue(fields.contains(field.fieldName))

                controller.fieldOfStudyChoiceBox.value = field.fieldName

                groups = controller.groupChoiceBox.items
                assertTrue(groups.contains("IA"))

                controller.groupChoiceBox.value = "IA"
                controller.subjectChoiceBox.value = "Język polski"

                locations = controller.locationChoiceBox.items
                assertTrue(locations.contains("LocTemp"))

                controller.locationChoiceBox.value = "LocTemp"
                controller.hourChoiceBox.value = "09.35-10.20"

                rooms = controller.roomChoicebox.items
                assertTrue(rooms.contains(room2.roomName))

                controller.roomChoicebox.value = room2.roomName

                //Tu nie powinno pokazać żadnego nauczyciela
                teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.isEmpty())

                //Brak proponowanych godzin
                assertTrue(controller.suggestedHours.isEmpty())

                //Brak innego nauczyciela uczącego tego przedmiotu, bo jest dodany tylko 1
                assertTrue(controller.suggestedBusyTeachers.isEmpty())

                //Przypadek gdy nauczyciel nie zdąży zmienić lokalizacji na czas (ma zajęcia o 09.35-10.20 w innej lokalizacji)
                val teacherToAdd2 = Teacher("Jann", "Kowalskii", "sssdrtg@s.pl", "772377777")
                if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)
                if (locationDAO.getLocations().contains(locationToAdd2)) locationDAO.deleteLocation(locationToAdd2)
                locationDAO.addLocation(locationToAdd2)
                roomDAO.addRoom(room3)

                controller.classesModel = classes1.copy()
                controller.classesModel.group = "IC"
                controller.classesModel.location = "LocTemp2"
                controller.classesModel.room = "roomTest3"
                controller.classesModel.hour = "09.35-10.20"
                controller.classesModel.teacher= "Kowalskii Jann"
                controller.onAddClassesPressed()
                controller.clearAllBoxes()

                controller.classesModel.group = null
                controller.classesModel.location = null
                controller.classesModel.fieldOfStudy = null
                controller.classesModel.subject = null
                controller.classesModel.date= null
                controller.classesModel.room = null
                controller.classesModel.hour = null
                controller.classesModel.teacher= null

                controller.datepicker.value = null

                controller.datepicker.value = LocalDate.parse("2000-09-02")
                controller.datepicker.value = LocalDate.parse("2000-09-01")
                fields = controller.fieldOfStudyChoiceBox.items
                assertTrue(fields.contains(field.fieldName))

                controller.fieldOfStudyChoiceBox.value = field.fieldName

                groups = controller.groupChoiceBox.items
                assertTrue(groups.contains("IB"))

                controller.groupChoiceBox.value = "IB"
                controller.subjectChoiceBox.value = "Język polski"

                locations = controller.locationChoiceBox.items
                assertTrue(locations.contains("LocTemp"))

                controller.locationChoiceBox.value = "LocTemp"
                controller.hourChoiceBox.value = "08.00-08.45"
                controller.hourChoiceBox.value = "08.45-09.30"

                rooms = controller.roomChoicebox.items
                assertTrue(rooms.contains(room1.roomName))

                controller.roomChoicebox.value = room1.roomName

                teachers = controller.teacherChoiceBox.items
                assertTrue(teachers.isEmpty())
                assertTrue(controller.suggestedHours.isEmpty())

                //Kowalski Jan uczy tego przedmiotu ale jest w tym czasie zajęty, Kowalskii Jann nie powinien
                //się pokazać jako proponowany zajęty nauczyciel bo nie zdąży zmienić lokalizacji
                assertTrue(controller.suggestedBusyTeachers.contains("Kowalski Jan"))

                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationToAdd2)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                planModel.refillHours()
            }
        }
    }


    //Testowanie mechanizmu sprawdzania czy grupa zdąży zmienić lokalizację
    @Test
    fun testBreakBetweenClasses() {
        Platform.runLater {
            runBlocking {
                planModel.refillHours()
                controller.resetPlan()
                //Najpierw musimy dodać 3 klasy, 1 kierunek i 1 nauczyciela oraz 2 lokalizacje
                val field = Field("Tymczasowy kierunek", 2, "TEMP2")
                val teacherToAdd = Teacher("Jan", "Kowalski1", "ssrqwetg@s.pl", "774577777")
                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("LocTemp2", "Wro", "Ryn10", "22-222")
                val room1 = Room("roomTest1", locationToAdd.locationName, 1, 1)
                val room2 = Room("roomTest2", locationToAdd.locationName, 1, 1)
                val room3 = Room("roomTest3", locationToAdd2.locationName, 1, 1)

                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                val subjects = mutableListOf("Język polski")
                val objectMapper = ObjectMapper()
                var subjectsJSON = objectMapper.writeValueAsString(subjects)
                val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30", "09.35-10.20"))
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)
                fieldDAO.deleteSPN(field.fieldName)
                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)
                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 3, 1, "I")
                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 1)
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                if (locationDAO.getLocations().contains(locationToAdd2)) locationDAO.deleteLocation(locationToAdd2)
                locationDAO.addLocation(locationToAdd)
                locationDAO.addLocation(locationToAdd2)
                roomDAO.addRoom(room1)
                roomDAO.addRoom(room2)
                roomDAO.addRoom(room3)


                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 wolne
                //10.20-11.05 LocTemp2
                //Nie można ustawić ani LocTemp1, ani LocTemp2 ani Platform
                var classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                var classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "09.35-10.20", "roomTest1", "Kowalski1 Jan")
                var classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp2", "10.20-11.05", "roomTest3", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes3)

                controller.classesModel = classes2.copy()
                var canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                var canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 wolne
                //10.20-11.05 LocTemp
                //Nie można ustawić LocTemp2, ale można Platform lub LocTemp
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "09.35-10.20", "roomTest1", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "10.20-11.05", "roomTest1", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes3)

                controller.classesModel = classes2.copy()
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())

                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)


                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 wolne
                //10.20-11.05 Platform
                //Nie można ustawić LocTemp2, ale można Platform lub LocTemp
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "09.35-10.20", "roomTest1", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "10.20-11.05", "Virtual", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes3)

                controller.classesModel = classes2.copy()
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())

                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)


                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 wolne
                //10.20-11.05 Platform
                //11.10-11.55 Platform
                //11.55-12.40 LocTemp
                //Nie można ustawić LocTemp2, ale można Platform lub LocTemp
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "09.35-10.20", "roomTest1", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "10.20-11.05", "Virtual", "Kowalski1 Jan")
                var classes4 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "11.10-11.55", "Virtual", "Kowalski1 Jan")
                var classes5 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "11.55-12.40", "roomTest1", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes3)
                dao.addToPlan(classes4)
                dao.addToPlan(classes5)

                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes4))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes5))


                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 wolne
                //10.20-11.05 Platform
                //11.10-11.55 Platform
                //11.55-12.40 LocTemp2
                //Nie można ustawić ani LocTemp2 ani Platform ani LocTemp
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "09.35-10.20", "roomTest1", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "10.20-11.05", "Virtual", "Kowalski1 Jan")
                classes4 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "11.10-11.55", "Virtual", "Kowalski1 Jan")
                classes5 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp2", "11.55-12.40", "roomTest3", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes3)
                dao.addToPlan(classes4)
                dao.addToPlan(classes5)

                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes4))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes5))


                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 Platform
                //10.20-11.05 wolne
                //Nie można ustawić LocTemp2, ale można Platform lub LocTemp
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "09.35-10.20", "Virtual", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "10.20-11.05", "roomTest1", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes2)

                controller.classesModel = classes3.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes3.location!!, classes3.hour!!, classes3.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes3.location = "Platform"
                controller.classesModel = classes3.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes3.location!!, classes3.hour!!, classes3.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes3.location = "LocTemp2"
                controller.classesModel = classes3.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes3.location!!, classes3.hour!!, classes3.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)


                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes2))


                //Przypadki do przetestowania
                //08.45-09.30 wolne
                //09.35-10.20 Platform
                //10.20-11.05 LocTemp
                //Nie można ustawić LocTemp2, ale można Platform lub LocTemp
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "09.35-10.20", "Virtual", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "10.20-11.05", "roomTest1", "Kowalski1 Jan")

                dao.addToPlan(classes2)
                dao.addToPlan(classes3)

                controller.classesModel = classes1.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes1.location!!, classes1.hour!!, classes1.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes1.location = "Platform"
                controller.classesModel.location = "Platform"
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes1.location!!, classes1.hour!!, classes1.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes1.location = "LocTemp2"
                controller.classesModel.location = "LocTemp2"
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes1.location!!, classes1.hour!!, classes1.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)


                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes2))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))


                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 Platform
                //10.20-11.05 Platform
                //11.10-11.55 wolne
                //11.55-12.40 wolne
                //12.50-13.35 LocTemp2
                //Na 11.10-11.55 można ustawić LocTemp lub Platform ale nie LocTemp2
                //Na 11.55-12.40 można ustawić LocTemp2 lub Platform ale nie LocTemp

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "09.35-10.20", "Virtual", "Kowalski1 Jan")
                classes3 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "10.20-11.05", "Virtual", "Kowalski1 Jan")
                classes4 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "11.10-11.55", "roomTest1", "Kowalski1 Jan")
                classes5 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp2", "11.55-12.40", "roomTest1", "Kowalski1 Jan")
                var classes6 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp2", "12.50-13.35", "roomTest3", "Kowalski1 Jan")

                dao.addToPlan(classes1)
                dao.addToPlan(classes2)
                dao.addToPlan(classes3)
                //dao.addToPlan(classes5)
                dao.addToPlan(classes6)

                controller.classesModel = classes4.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes4.location!!, classes4.hour!!, classes4.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes4.location = "Platform"
                controller.classesModel = classes4.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes4.location!!, classes4.hour!!, classes4.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes4.location = "LocTemp2"
                controller.classesModel = classes4.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes4.location!!, classes4.hour!!, classes4.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)


                controller.classesModel = classes5.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes5.location!!, classes5.hour!!, classes5.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes5.location = "Platform"
                controller.classesModel = classes5.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes5.location!!, classes5.hour!!, classes5.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes5.location = "LocTemp"
                controller.classesModel = classes5.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes5.location!!, classes5.hour!!, classes5.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)


                //gdy dodamy do pierwszych wolnych zajęć Platform to już nic na 2 wolne nie możemy dodać
                classes4 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "11.10-11.55", "Virtual", "Kowalski1 Jan")
                dao.addToPlan(classes4)

                controller.classesModel = classes5.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes5.location!!, classes5.hour!!, classes5.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                classes5.location = "LocTemp2"
                controller.classesModel = classes5.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes5.location!!, classes5.hour!!, classes5.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                classes5.location = "Platform"
                controller.classesModel = classes5.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes5.location!!, classes5.hour!!, classes5.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes2))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes3))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes6))
                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes4))



                //Przypadki do przetestowania
                //08.45-09.30 LocTemp
                //09.35-10.20 wolne
                //Możemy ustawić LocTemp oraz Platform ale nie LocTemp2

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "09.35-10.20", "roomTest1", "Kowalski1 Jan")

                dao.addToPlan(classes1)

                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))


                //Przypadek skrajny
                //Przypadki do przetestowania
                //08.00-08.45 LocTemp
                //08.45-09.30 wolne
                //Możemy ustawić LocTemp oraz Platform ale nie LocTemp2

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.00-08.45", "roomTest1", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")

                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes1.location!!, classes1.hour!!, classes1.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)
                dao.addToPlan(classes1)


                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertFalse(canGroupMoveBetweenClasses)
                assertFalse(canTeacherMoveBetweenClasses)

                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))


                //08.00-08.45 Platform
                //08.45-09.30 wolne
                //Możemy ustawić wszystko

                classes1 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "Platform", "08.00-08.45", "Virtual", "Kowalski1 Jan")
                classes2 = ClassesToWrite(LocalDate.parse("2000-09-01"), "Tymczasowy kierunek", "IA", "Język polski", "LocTemp", "08.45-09.30", "roomTest1", "Kowalski1 Jan")

                controller.classesModel = classes1.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes1.location!!, classes1.hour!!, classes1.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)
                dao.addToPlan(classes1)

                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                classes2.location = "Platform"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)


                classes2.location = "LocTemp2"
                controller.classesModel = classes2.copy()
                canTeacherMoveBetweenClasses = controller.classesModel.canTeacherMoveBetweenClasses(teacherToAdd.firstname, teacherToAdd.lastname, classes2.location!!, classes2.hour!!, classes2.date.toString())
                canGroupMoveBetweenClasses = controller.classesModel.canGroupMoveBetweenClasses()
                assertTrue(canGroupMoveBetweenClasses)
                assertTrue(canTeacherMoveBetweenClasses)

                dao.deleteClasses(convertClassesToWriteToClassesToRead(classes1))


                locationDAO.deleteLocation(locationToAdd)
                locationDAO.deleteLocation(locationToAdd2)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                planModel.refillHours()
            }
        }
    }

    @Test
    fun messageTest()
    {
        Platform.runLater {
            runBlocking {
                controller.showDialogYesNoMessage("Are you sure?", ActionType.DELETE)
                val dialog = (controller.dialog.content as MFXGenericDialog)
                val vbox: VBox = dialog.children[2] as VBox
                assertEquals(vbox.children[0].javaClass, javafx.scene.control.Label::class.java)
                assertEquals((vbox.children[0] as Label).text, "Are you sure?")
            }
        }
    }

    private fun createAndAddObjects(
        teacherToAdd: Teacher,
        teacherToAdd2: Teacher,
        teacherToAdd3: Teacher,
        teacherToAdd4: Teacher,
        field: Field,
        fieldNotToShow: Field,
        locationToAdd: Location,
        locationToAdd2: Location,
        room1: Room,
        room2: Room,
        room3: Room
    ) {
        if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
        if (teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)
        if (teacherDAO.getTeachers().contains(teacherToAdd3)) teacherDAO.deleteTeacher(teacherToAdd3)
        if (teacherDAO.getTeachers().contains(teacherToAdd4)) teacherDAO.deleteTeacher(teacherToAdd4)

        val subjects = mutableListOf("Język polski", "Język angielski")
        val subjectss = mutableListOf("Język angielski")

        val objectMapper = ObjectMapper()
        var subjectsJSON = objectMapper.writeValueAsString(subjects)

        val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30"))
        val hoursJSON2 = objectMapper.writeValueAsString(listOf("08.45-09.30"))

        teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
        teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)

        teacherDAO.addTeacher(teacherToAdd4, subjectsJSON)
        teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)

        teacherDAO.addTeacher(teacherToAdd2, subjectsJSON)
        teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON2)

        subjectsJSON = objectMapper.writeValueAsString(subjectss)
        teacherDAO.addTeacher(teacherToAdd3, subjectsJSON)
        teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)

        fieldDAO.deleteSPN(field.fieldName)
        fieldDAO.deleteSPN(fieldNotToShow.fieldName)

        if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)
        if (fieldDAO.getFields().contains(fieldNotToShow)) fieldDAO.deleteField(fieldNotToShow)

        fieldDAO.addField(field)
        fieldDAO.addField(fieldNotToShow)
        fieldDAO.addGroups(field, 3, 1, "I")
        fieldDAO.addGroups(fieldNotToShow, 1, 1, "I")


        fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 1)
        fieldDAO.addSubjectToSem("Język angielski", field.fieldName, 1, 1)
        fieldDAO.addSubjectToSem("Język niemiecki", field.fieldName, 1, 0)

        //Do 2 kierunku dodajmy 0 liczbe godzin aby nie pojawił się w wyświetlanych kierunkach
        fieldDAO.addSubjectToSem("Język angielski", fieldNotToShow.fieldName, 1, 0)

        if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
        if (locationDAO.getLocations().contains(locationToAdd2)) locationDAO.deleteLocation(locationToAdd2)

        //Dodawanie lokalizacji z salami
        locationDAO.addLocation(locationToAdd)
        locationDAO.addLocation(locationToAdd2)

        roomDAO.addRoom(room1)
        roomDAO.addRoom(room2)
        roomDAO.addRoom(room3)
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
}