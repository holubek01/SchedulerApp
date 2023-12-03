package com.example.scheduler.tests.controllersDirector

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.ShowPlanRoomsController
import com.example.scheduler.db.dao.*
import com.example.scheduler.models.ClassesToWrite
import com.example.scheduler.objects.*
import com.example.scheduler.utils.ExcelUtils
import com.example.scheduler.utils.MessageBundle
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.palexdev.materialfx.controls.MFXTableView
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.*
import org.testfx.framework.junit5.ApplicationTest
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.util.*
import kotlin.test.Test


class ShowPlanForRoomsControllerTest: ApplicationTest() {

    lateinit var controller: ShowPlanRoomsController
    private val teacherDAO = TeacherDAOImpl()
    private val locationDAO = LocationDAOImpl()
    private val fieldDAO = FieldDAOImpl()
    private val roomDAO = RoomDAOImpl()

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/showPlanForRooms.fxml"))
        val contr = ShowPlanRoomsController()
        contr.stage = stage!!
        loader.setController(contr)
        val root: Parent = loader.load()
        controller = loader.getController()
        controller.stage = stage
        stage.scene = Scene(root)
    }


    //Test sprawdzający czy dodane zajęcia zostaną poprawnie wyświetlone w formie rozpiski dla sal
    //czyli jaka grupa o jakiej godzinie zajmuje jaką salę
    @Test
    fun showPlanForRoomsTest()
    {
        Platform.runLater {
            runBlocking {
                controller.locationChoiceBox.value = "LocTemp"

                //Najpierw trzeba utworzyć lokalizację i dodać sale
                val locationToAdd = Location("LocTemp", "Wro", "Rynek 10", "22-222")
                val roomToAdd1 = Room("room1", "LocTemp", 1,1)
                val roomToAdd2 = Room("room2", "LocTemp", 2,2)
                val roomToAdd3 = Room("room3", "LocTemp", 1,1)

                if(locationDAO.getLocationsNames().contains("LocTemp")) locationDAO.deleteLocation(locationToAdd)

                locationDAO.addLocation(locationToAdd)
                roomDAO.addRoom(roomToAdd1)
                roomDAO.addRoom(roomToAdd2)
                roomDAO.addRoom(roomToAdd3)


                val fieldToAdd = Field(
                    fieldName = "Tymczasowy",
                    semsNumber = 2,
                    shortcut = "TYM"
                )

                val teacherToAdd = Teacher(
                    firstname = "Anna",
                    lastname = "Tymczasowa",
                    phone = "121212121",
                    email = "anna@wp.pl"
                )

                if(teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (fieldDAO.getFields().contains(fieldToAdd)) {
                    fieldDAO.deleteSPN(fieldToAdd.fieldName)
                    fieldDAO.deleteField(fieldToAdd)
                }


                val objectMapper = ObjectMapper()
                val subjectsJSON = objectMapper.writeValueAsString(listOf("Przedmiot tymczasowy"))
                val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30"))

                fieldDAO.addField(fieldToAdd)   //dodawanie kierunku
                fieldDAO.addGroups(fieldToAdd, 2, 1, "I")   //dodawanie grupy
                fieldDAO.addSubjectToSem("Przedmiot tymczasowy", fieldToAdd.fieldName, 1, 1)
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)

                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "LocTemp",
                    "08.00-08.45",
                    "room1",
                    "Tymczasowa Anna"
                )

                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "LocTemp",
                    "08.45-09.30",
                    "room2",
                    "Tymczasowa Anna"
                )

                val classesDao = ClassesDAOImpl()
                classesDao.addToPlan(classes1)
                classesDao.addToPlan(classes2)

                controller.locationChoiceBox.value = "LocTemp"
                assertTrue(controller.planTableView.items.isEmpty())
                controller.showPlan()

                //Po dodaniu zajęć i wyświetleniu rozpiski tabela nie powinna być pusta
                assertFalse(controller.planTableView.items.isEmpty())

                assertEquals(controller.planTableView.items[0].date.toString(), "2000-09-01")
                assertEquals(controller.planTableView.items[0].hour, "08.00-08.45")
                assertEquals(controller.planTableView.items[0].rooms, FXCollections.observableArrayList("ITYMA", "", ""))

                assertEquals(controller.planTableView.items[1].date.toString(), "2000-09-01")
                assertEquals(controller.planTableView.items[1].hour, "08.45-09.30")
                assertEquals(controller.planTableView.items[1].rooms, FXCollections.observableArrayList("", "ITYMA", ""))


                val classes3 = ClassesToWrite(
                    LocalDate.parse("2000-09-08"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.45-09.30",
                    "Virtual",
                    "Tymczasowa Anna"
                )

                val classes4 = ClassesToWrite(
                    LocalDate.parse("2000-09-08"),
                    "Tymczasowy",
                    "IB",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.45-09.30",
                    "Virtual",
                    "Tymczasowa Anna"
                )
                classesDao.addToPlan(classes3)
                classesDao.addToPlan(classes4)
                controller.locationChoiceBox.value = "Platforma"
                controller.showPlan()

                assertEquals(controller.planTableView.items[0].date.toString(), "2000-09-08")
                assertEquals(controller.planTableView.items[0].hour, "08.45-09.30")
                assertEquals(controller.planTableView.items[0].rooms, FXCollections.observableArrayList("ITYMA, ITYMB"))

                teacherDAO.deleteTeacher(teacherToAdd)
                locationDAO.deleteLocation(locationToAdd)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
            }
        }
    }



    //Test sprawdzający poprawność eksportowania rozpiski dla sal do excela
    @Test
    fun exportPlanForRoomsTest()
    {
        Platform.runLater {
            //Najpierw ustaw tabelę
            val items = FXCollections.observableArrayList(
                PlanForRooms(LocalDate.parse("2000-09-01"), "08.00-08.45", FXCollections.observableArrayList("2PKB", "", "1FLOA")),
                PlanForRooms(LocalDate.parse("2000-09-01"), "08.45-09.30", FXCollections.observableArrayList("1PKA", "2PKB", ""))
            )

            controller.planTableView.items = items
            controller.locationChoiceBox.value = "LocTemp"
            controller.wantToExport = true

            val locationToAdd = Location("LocTemp", "Wro", "Rynek 10", "22-222")
            val roomToAdd1 = Room("room1", "LocTemp", 1,1)
            val roomToAdd2 = Room("room2", "LocTemp", 2,2)
            val roomToAdd3 = Room("room3", "LocTemp", 1,1)

            if(locationDAO.getLocationsNames().contains("LocTemp")) locationDAO.deleteLocation(locationToAdd)

            var locations = locationDAO.getLocationsNames()

            assertFalse(locations.contains("LocTemp"))
            locationDAO.addLocation(locationToAdd)

            locations = locationDAO.getLocationsNames()
            assertTrue(locations.contains("LocTemp"))

            var rooms = roomDAO.getRooms("LocTemp")
            assertTrue(rooms.isEmpty())

            roomDAO.addRoom(roomToAdd1)
            roomDAO.addRoom(roomToAdd2)
            roomDAO.addRoom(roomToAdd3)

            rooms = roomDAO.getRooms("LocTemp")
            assertFalse(rooms.isEmpty())

            runBlocking {
                controller.setupTable(FXCollections.observableArrayList(roomToAdd1.roomName, roomToAdd2.roomName, roomToAdd3.roomName))
                controller.exportPlan()
                delay(3000)

                //Odczytaj plik i zobacz czy się zgadza (nazwa pliku to Przykładowy.fxml, nazwa zakładki to "")
                val prop = ExcelUtils.loadConfigProps()
                val path = System.getProperty("user.home") + prop.getProperty("excel.plans.locations.path")
                val filePath = "$path/saleLocTemp.xlsx"

                val file = File(filePath)

                if (file.exists())
                {
                    val fis = FileInputStream(file)

                    val workbook = XSSFWorkbook(fis)
                    val planName = ExcelUtils.createPlanName(LocalDate.parse("2000-09-01"))
                    val sheet = workbook.getSheet(planName)

                    //Wartości z pliku Excel powinny zgadzać się z tabelą
                    assertEquals(sheet.getRow(2).getCell(1).stringCellValue, "LocTemp")
                    assertEquals(sheet.getRow(3).getCell(1).stringCellValue, "Dzień")
                    assertEquals(sheet.getRow(4).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(4).getCell(2).stringCellValue, "08.00-08.45")
                    assertEquals(sheet.getRow(4).getCell(3).stringCellValue, "2PKB")
                    assertEquals(sheet.getRow(4).getCell(4).stringCellValue, "")
                    assertEquals(sheet.getRow(4).getCell(5).stringCellValue, "1FLOA")
                    assertEquals(sheet.getRow(5).getCell(2).stringCellValue, "08.45-09.30")
                    assertEquals(sheet.getRow(5).getCell(3).stringCellValue, "1PKA")
                    assertEquals(sheet.getRow(5).getCell(4).stringCellValue, "2PKB")
                    assertEquals(sheet.getRow(5).getCell(5).stringCellValue, "")

                    fis.close()

                }

                locationDAO.deleteLocation(locationToAdd)
                assertFalse(locationDAO.getLocationsNames().contains("LocTemp"))
                assertTrue(roomDAO.getRooms("LocTemp").isEmpty())
            }
        }
    }

    //Test sprawdzjący czy odpowiedni styl został przypisany do nagłówków tabeli
    @Test
    fun setUpTableTest()
    {
        Platform.runLater {
            controller.planTableView = MFXTableView<PlanForRooms>()
            val rooms = FXCollections.observableArrayList("Room1", "Room2", "Room3")
            controller.setupTable(rooms)

            assertNotNull(controller.planTableView)
            assertEquals("table-header", controller.planTableView.tableColumns[0].styleClass[1])
        }
    }

    @Test
    fun messageTest()
    {
        Platform.runLater {
            runBlocking {
                controller.showDialogYesNoMessage("Are you sure?")
                val dialog = (controller.dialog.content as MFXGenericDialog)
                val vbox: VBox = dialog.children[2] as VBox
                assertEquals(vbox.children[0].javaClass, javafx.scene.control.Label::class.java)
                assertEquals((vbox.children[0] as javafx.scene.control.Label).text, "Are you sure?")
            }
        }
    }
}