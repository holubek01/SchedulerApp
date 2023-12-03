package com.example.scheduler.tests.controllersDirector

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.ShowPlanController
import com.example.scheduler.db.dao.*
import com.example.scheduler.models.*
import com.example.scheduler.objects.*
import com.example.scheduler.utils.*
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXComboBox
import io.github.palexdev.materialfx.controls.MFXContextMenuItem
import io.github.palexdev.materialfx.controls.MFXListView
import io.github.palexdev.materialfx.controls.MFXTableView
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.util.*
import java.util.function.Consumer

/**
 * Klasa testująca metody z ShowPlanController
 */
class ShowPlanControllerTest: ApplicationTest() {

    lateinit var controller: ShowPlanController
    private val teacherDAO = TeacherDAOImpl()
    private val fieldDAO = FieldDAOImpl()
    private val locationModel = LocationsModel()
    private val roomModel = RoomsModel()
    private val planModel = PlansModel()


    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/showPlan.fxml"))
        val contr = ShowPlanController()
        contr.stage = stage!!
        loader.setController(contr)
        val root: Parent = loader.load()
        controller = loader.getController()
        controller.stage = stage
        stage.scene = Scene(root)
    }



    //Test sprawdzający poprawność eksportowania planu dla grupy
    @Test
    fun exportPlanForGroupTest()
    {
        Platform.runLater {
            val items = FXCollections.observableArrayList(
                ClassesToRead(LocalDate.parse("2000-09-01"), "08.00-08.45", "Język polski", "sala1", "Jan Kowalski", "IA, Przykładowy"),
                ClassesToRead(LocalDate.parse("2000-09-01"), "08.45-09.30", "Język angielski", "sala2", "Anna Nowak", "IA, Przykładowy"),
            )

            controller.planTableView.items.setAll(items)
            controller.wantToExport = true

            runBlocking {
                //Wykonaj eksport
                controller.exportGroupPlan()
                delay(3000)

                //Odczytaj plik i zobacz czy się zgadza
                val prop = ExcelUtils.loadConfigProps()
                val path = System.getProperty("user.home") + prop.getProperty("excel.plans.groups.path")
                val filePath = "$path/Przykładowy.xlsx"

                val file = File(filePath)
                if (file.exists())
                {
                    val fis = FileInputStream(file)

                    val workbook = XSSFWorkbook(fis)
                    val planName = ExcelUtils.createPlanName(LocalDate.parse("2000-09-01"))
                    val sheet = workbook.getSheet(planName)

                    //Wcześniej nic nie było w planie zatem w 3 wierszu powinien być tytuł, w 4 wierszu nagłówki, a w reszcie items
                    assertEquals(sheet.getRow(2).getCell(1).stringCellValue, "IA Przykładowy")
                    assertEquals(sheet.getRow(3).getCell(1).stringCellValue, "Dzień")
                    assertEquals(sheet.getRow(4).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(4).getCell(2).stringCellValue, "08.00-08.45")
                    assertEquals(sheet.getRow(4).getCell(3).stringCellValue, "Język polski")
                    assertEquals(sheet.getRow(4).getCell(4).stringCellValue, "sala1")
                    assertEquals(sheet.getRow(4).getCell(5).stringCellValue, "Jan Kowalski")
                    assertEquals(sheet.getRow(4).getCell(6).stringCellValue, "IA, Przykładowy")

                    assertEquals(sheet.getRow(5).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(5).getCell(2).stringCellValue, "08.45-09.30")
                    assertEquals(sheet.getRow(5).getCell(3).stringCellValue, "Język angielski")
                    assertEquals(sheet.getRow(5).getCell(4).stringCellValue, "sala2")
                    assertEquals(sheet.getRow(5).getCell(5).stringCellValue, "Anna Nowak")
                    assertEquals(sheet.getRow(5).getCell(6).stringCellValue, "IA, Przykładowy")

                    fis.close()
                }

            }
        }
    }


    //Test sprawdzający poprawność wyświetlania planu dla grup
    @Test
    fun showGroupTest()
    {
        Platform.runLater{
            runBlocking {
                val fieldToAdd = Field(
                    fieldName = "Tymczasowy",
                    semsNumber = 2,
                    shortcut = "TYM"
                )

                val teacherToAdd = Teacher(
                    firstname = "Anna",
                    lastname = "Tymczasowa",
                    phone = "121978121",
                    email = "anna@wp.pl"
                )

                val classessDAO = ClassesDAOImpl()

                if(teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (fieldDAO.getFields().contains(fieldToAdd))
                {
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

                //dodawanie planu dla grupy
                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.00-08.45",
                    "Virtual",
                    "Tymczasowa Anna"
                )

                classessDAO.addToPlan(classes1)

                controller.fieldOfStudyChoiceBox.value = "Tymczasowy"
                controller.groupChoiceBox.value = "IA"
                controller.dayChoiceBoxGroup.value = "Cały plan"

                //Wyświetl plan dla grupy i zobacz czy się zgadza
                controller.showGroupPlan()
                assertEquals(controller.planTableView.items.size, 1)
                assertEquals(controller.planTableView.items[0].date, LocalDate.parse("2000-09-01"))
                assertEquals(controller.planTableView.items[0].group, "IA, Tymczasowy")
                assertEquals(controller.planTableView.items[0].hour, "08.00-08.45")
                assertEquals(controller.planTableView.items[0].room, "Wirtualna, Platforma")
                assertEquals(controller.planTableView.items[0].teacher, "Tymczasowa Anna")
                assertEquals(controller.planTableView.items[0].subject, "Przedmiot tymczasowy")

                teacherDAO.deleteTeacher(teacherToAdd)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
            }
        }
    }

    //Test sprawdzający poprawność wyświetlania planu dla nauczyciela
    @Test
    fun showTeacherTest()
    {
        Platform.runLater{
            runBlocking {
                val fieldToAdd = Field(
                    fieldName = "Tymczasowy",
                    semsNumber = 2,
                    shortcut = "TYM"
                )

                val teacherToAdd = Teacher(
                    firstname = "Anna",
                    lastname = "Tymczasowa",
                    phone = "112412121",
                    email = "anna@wp.pl"
                )

                val classessDAO = ClassesDAOImpl()
                if(teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (fieldDAO.getFields().contains(fieldToAdd))
                {
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
                    "Platform",
                    "08.00-08.45",
                    "Virtual",
                    "Tymczasowa Anna"
                )

                classessDAO.addToPlan(classes1)

                controller.teacherChoiceBox.value = "Tymczasowa Anna"
                controller.dayChoiceBoxTeacher.value = "Cały plan"

                controller.showTeacherPlan()

                assertEquals(controller.planTableView.items.size, 1)
                assertEquals(controller.planTableView.items[0].date, LocalDate.parse("2000-09-01"))
                assertEquals(controller.planTableView.items[0].group, "IA, Tymczasowy")
                assertEquals(controller.planTableView.items[0].hour, "08.00-08.45")
                assertEquals(controller.planTableView.items[0].room, "Wirtualna, Platforma")
                assertEquals(controller.planTableView.items[0].teacher, "Tymczasowa Anna")
                assertEquals(controller.planTableView.items[0].subject, "Przedmiot tymczasowy")

                teacherDAO.deleteTeacher(teacherToAdd)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
            }
        }
    }


    //Test sprawdzający poprawność usuwania zajęć z planu
    @Test
    fun deleteFromPlanTest()
    {
        Platform.runLater{
            runBlocking {
                val fieldToAdd = Field(
                    fieldName = "Tymczasowy",
                    semsNumber = 2,
                    shortcut = "TYM"
                )

                val teacherToAdd = Teacher(
                    firstname = "Anna",
                    lastname = "Tymczasowa",
                    phone = "121002120",
                    email = "ana@wp.pl"
                )

                val objectMapper = ObjectMapper()
                val subjectsJSON = objectMapper.writeValueAsString(listOf("Przedmiot tymczasowy"))

                val classessDAO = ClassesDAOImpl()
                if(teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (fieldDAO.getFields().contains(fieldToAdd))
                {
                    fieldDAO.deleteSPN(fieldToAdd.fieldName)
                    fieldDAO.deleteField(fieldToAdd)
                }

                fieldDAO.addField(fieldToAdd)
                fieldDAO.addGroups(fieldToAdd, 2, 1, "I")
                fieldDAO.addSubjectToSem("Przedmiot tymczasowy", fieldToAdd.fieldName, 1, 1)

                val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30"))
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)


                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.00-08.45",
                    "Virtual",
                    "Tymczasowa Anna"
                )

                val hours_counter_before = classessDAO.getHowManyHoursLeft("IA","Tymczasowy", "Przedmiot tymczasowy")
                //Dodamy zajęcia i zaraz spróbujemy je usunąć
                classessDAO.addToPlan(classes1)
                val hours_counter_after = classessDAO.getHowManyHoursLeft("IA","Tymczasowy", "Przedmiot tymczasowy")

                //Sprawdz czy godziny poprawnie się odjęły
                assertEquals(hours_counter_before-1, hours_counter_after)

                controller.fieldOfStudyChoiceBox.value = "Tymczasowy"
                controller.groupChoiceBox.value = "IA"
                controller.dayChoiceBoxGroup.value = "Cały plan"

                controller.showGroupPlan()

                //Sprawdzenie czy zajęcia się dodały
                assertEquals(controller.planTableView.items.size, 1)
                controller.wantToDelete = true

                //Usuwanie zajęć
                controller.deleteFromPlan(controller.planTableView.items[0])
                assertEquals(controller.planTableView.items.size, 0)

                //Sprawdz czy godziny poprawnie się dodały
                assertEquals(hours_counter_before, classessDAO.getHowManyHoursLeft("IA","Tymczasowy", "Przedmiot tymczasowy"))

                teacherDAO.deleteTeacher(teacherToAdd)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
            }
        }
    }

    //Test sprawdzający poprawność eksportowania planów dla wszystkich nauczycieli
    @Test
    fun exportAllTeachersTest()
    {
        //export planów
        Platform.runLater {
            runBlocking {
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

                val teacherToAdd2 = Teacher(
                    firstname = "Jan1",
                    lastname = "Kowalski1",
                    phone = "121232321",
                    email = "jano@wp.pl"
                )


                if(teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if(teacherDAO.getTeachers().contains(teacherToAdd2)) teacherDAO.deleteTeacher(teacherToAdd2)

                val objectMapper = ObjectMapper()
                val subjectsJSON = objectMapper.writeValueAsString(listOf("Przedmiot tymczasowy"))

                val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30"))
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)
                assertTrue(teacherDAO.getTeachers().contains(teacherToAdd))

                teacherDAO.addTeacher(teacherToAdd2, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)

                delay(2000)

                if (fieldDAO.getFields().contains(fieldToAdd))
                {
                    fieldDAO.deleteSPN(fieldToAdd.fieldName)
                    fieldDAO.deleteField(fieldToAdd)
                }


                fieldDAO.addField(fieldToAdd)   //dodawanie kierunku
                fieldDAO.addGroups(fieldToAdd, 2, 1, "I")   //dodawanie grupy
                fieldDAO.addSubjectToSem("Przedmiot tymczasowy", fieldToAdd.fieldName, 1, 1)


                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.00-08.45",
                    "Virtual",
                    "Tymczasowa Anna"
                )

                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IB",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.45-09.30",
                    "Virtual",
                    "Kowalski1 Jan1"
                )


                val classesDAO = ClassesDAOImpl()
                classesDAO.addToPlan(classes1)
                classesDAO.addToPlan(classes2)

                controller.wantToExport = true
                controller.exportAllTeachers()

                val prop = ExcelUtils.loadConfigProps()
                val path = System.getProperty("user.home") + prop.getProperty("excel.plans.teachers.path")
                var filePath = "$path/${classes1.teacher}.xlsx"
                var file = File(filePath)

                if (file.exists())
                {
                    var fis = FileInputStream(file)

                    var workbook = XSSFWorkbook(fis)
                    var sheet = workbook.getSheetAt(0)

                    assertEquals(sheet.getRow(2).getCell(1).stringCellValue, "Tymczasowa Anna")
                    assertEquals(sheet.getRow(3).getCell(1).stringCellValue, "Dzień")
                    assertEquals(sheet.getRow(4).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(4).getCell(2).stringCellValue, "08.00-08.45")
                    assertEquals(sheet.getRow(4).getCell(3).stringCellValue, "Przedmiot tymczasowy")
                    assertEquals(sheet.getRow(4).getCell(4).stringCellValue, "Wirtualna, Platforma")
                    assertEquals(sheet.getRow(4).getCell(5).stringCellValue, "Tymczasowa Anna")
                    assertEquals(sheet.getRow(4).getCell(6).stringCellValue, "IA, Tymczasowy")

                    workbook.close()
                    fis.close()
                }

                filePath = "$path/${classes2.teacher}.xlsx"
                if (file.exists())
                {
                    file = File(filePath)
                    val fis = FileInputStream(file)

                    val workbook = XSSFWorkbook(fis)
                    val sheet = workbook.getSheetAt(0)

                    assertEquals(sheet.getRow(2).getCell(1).stringCellValue, "Kowalski1 Jan1")
                    assertEquals(sheet.getRow(3).getCell(1).stringCellValue, "Dzień")
                    assertEquals(sheet.getRow(4).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(4).getCell(2).stringCellValue, "08.45-09.30")
                    assertEquals(sheet.getRow(4).getCell(3).stringCellValue, "Przedmiot tymczasowy")
                    assertEquals(sheet.getRow(4).getCell(4).stringCellValue, "Wirtualna, Platforma")
                    assertEquals(sheet.getRow(4).getCell(5).stringCellValue, "Kowalski1 Jan1")
                    assertEquals(sheet.getRow(4).getCell(6).stringCellValue, "IB, Tymczasowy")

                    workbook.close()
                    fis.close()
                }

                teacherDAO.deleteTeacher(teacherToAdd)
                teacherDAO.deleteTeacher(teacherToAdd2)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
            }
        }
    }



    //Test sprawdzający poprawność eksportowania planów dla wszystkich grup z wybranym kierunku
    @Test
    fun exportAllGroupsFromFieldTest()
    {
        Platform.runLater {
            runBlocking {
                val fieldToAdd = Field(
                    fieldName = "Tymczasowy",
                    semsNumber = 2,
                    shortcut = "TYM"
                )

                val teacherToAdd = Teacher(
                    firstname = "Anna",
                    lastname = "Tymczasowa",
                    phone = "129802121",
                    email = "anna@wp.pl"
                )

                val classessDAO = ClassesDAOImpl()
                if(teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (fieldDAO.getFields().contains(fieldToAdd))
                {
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
                    "Platform",
                    "08.00-08.45",
                    "Virtual",
                    "Tymczasowa Anna"
                )

                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IB",
                    "Przedmiot tymczasowy",
                    "Platform",
                    "08.45-09.30",
                    "Virtual",
                    "Tymczasowa Anna"
                )


                classessDAO.addToPlan(classes1)
                classessDAO.addToPlan(classes2)

                controller.wantToExport = true
                controller.fieldOfStudyChoiceBox.value = "Tymczasowy"

                //export planów
                controller.exportAllGroupsFromField()
                delay(3000)

                val prop = ExcelUtils.loadConfigProps()
                val path = System.getProperty("user.home") + prop.getProperty("excel.plans.groups.path")
                val filePath = "$path/Tymczasowy.xlsx"
                val file = File(filePath)

                if (file.exists())
                {
                    val fis = FileInputStream(file)

                    val workbook = XSSFWorkbook(fis)
                    val sheet = workbook.getSheetAt(0)

                    assertEquals(sheet.getRow(2).getCell(1).stringCellValue, "IA Tymczasowy")
                    assertEquals(sheet.getRow(3).getCell(1).stringCellValue, "Dzień")
                    assertEquals(sheet.getRow(4).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(4).getCell(2).stringCellValue, "08.00-08.45")
                    assertEquals(sheet.getRow(4).getCell(3).stringCellValue, "Przedmiot tymczasowy")
                    assertEquals(sheet.getRow(4).getCell(4).stringCellValue, "Wirtualna, Platforma")
                    assertEquals(sheet.getRow(4).getCell(5).stringCellValue, "Tymczasowa Anna")
                    assertEquals(sheet.getRow(4).getCell(6).stringCellValue, "IA, Tymczasowy")

                    assertEquals(sheet.getRow(7).getCell(1).stringCellValue, "IB Tymczasowy")
                    assertEquals(sheet.getRow(8).getCell(1).stringCellValue, "Dzień")
                    assertEquals(sheet.getRow(9).getCell(1).stringCellValue, "2000-09-01")
                    assertEquals(sheet.getRow(9).getCell(2).stringCellValue, "08.45-09.30")
                    assertEquals(sheet.getRow(9).getCell(3).stringCellValue, "Przedmiot tymczasowy")
                    assertEquals(sheet.getRow(9).getCell(4).stringCellValue, "Wirtualna, Platforma")
                    assertEquals(sheet.getRow(9).getCell(5).stringCellValue, "Tymczasowa Anna")
                    assertEquals(sheet.getRow(9).getCell(6).stringCellValue, "IB, Tymczasowy")

                    fis.close()
                }


                teacherDAO.deleteTeacher(teacherToAdd)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
            }
        }
    }

    //Testowanie zmiany godziny i sali zajęć oraz zmiany samej sali
    //w planie dodamy zajęcia na LocTemp na 08.00-08.45 i na 08.45-09.30,
    //pobierzemy plan nauczyciela i sprawdzimy na jakie godziny i sale możemy zmienić 2 zajęcia w obu przypadkach
    //Następnie usuniemy zajęcia o 8.00 i sprawdzimy znowu itp
    @Test
    fun getPlanTeacherAndChangeHourTest()
    {
        Platform.runLater {
            runBlocking {
                val fieldToAdd = Field(
                    fieldName = "Tymczasowy",
                    semsNumber = 2,
                    shortcut = "TYM"
                )

                val teacherToAdd = Teacher(
                    firstname = "Anna",
                    lastname = "Tymczasowa",
                    phone = "001212120",
                    email = "anna@wp.pk"
                )


                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                if (fieldDAO.getFields().contains(fieldToAdd)) {
                    fieldDAO.deleteSPN(fieldToAdd.fieldName)
                    fieldDAO.deleteField(fieldToAdd)
                }

                val objectMapper = ObjectMapper()
                val subjectsJSON = objectMapper.writeValueAsString(mutableListOf("Przedmiot tymczasowy"))

                val hoursJSON = objectMapper.writeValueAsString(mutableListOf("08.00-08.45", "08.45-09.30", "09.35-10.20"))

                fieldDAO.addField(fieldToAdd)   //dodawanie kierunku
                fieldDAO.addGroups(fieldToAdd, 2, 1, "I")   //dodawanie grupy
                fieldDAO.addSubjectToSem("Przedmiot tymczasowy", fieldToAdd.fieldName, 1, 1)

                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)

                val locationToAdd = Location("TempLoc", "Wro", "Ryn10", "22-222")
                val locationToAdd2 = Location("TempLoc2", "Wro", "Ryn10", "22-222")
                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd)) locationModel.deleteLocation(locationToAdd)
                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd2)) locationModel.deleteLocation(locationToAdd2)
                locationModel.addLocation(locationToAdd)
                locationModel.addLocation(locationToAdd2)

                assertTrue(locationModel.getLocationsNames().contains("TempLoc"))

                var rooms = roomModel.getRooms(locationToAdd.locationName)
                assertTrue(rooms.isEmpty())

                val room1 = Room("sala10", "TempLoc",1,1)
                val room3 = Room("sala30", "TempLoc",1,1)
                val room2 = Room("sala20", "TempLoc2",2,3)

                roomModel.addRoom(room1)
                roomModel.addRoom(room2)
                roomModel.addRoom(room3)

                val classes1 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IA",
                    "Przedmiot tymczasowy",
                    "TempLoc",
                    "08.00-08.45",
                    "sala10",
                    "Tymczasowa Anna"
                )

                val classes2 = ClassesToWrite(
                    LocalDate.parse("2000-09-01"),
                    "Tymczasowy",
                    "IB",
                    "Przedmiot tymczasowy",
                    "TempLoc",
                    "08.45-09.30",
                    "sala30",
                    "Tymczasowa Anna"
                )


                val classesDAO = ClassesDAOImpl()
                classesDAO.addToPlan(classes1)
                classesDAO.addToPlan(classes2)

                assertTrue(controller.planTableView.items.isEmpty())
                controller.teacherChoiceBox.value = classes1.teacher
                controller.dayChoiceBoxTeacher.value = MessageBundle.getMess("label.wholePlan")
                controller.showTeacherPlan()
                assertTrue(controller.planTableView.items.size==2)
                assertEquals(controller.planTableView.items[0].date, classes1.date)
                assertEquals(controller.planTableView.items[0].hour, classes1.hour)
                assertEquals(controller.planTableView.items[0].teacher, classes1.teacher)
                assertEquals(controller.planTableView.items[0].group, "${classes1.group}, ${classes1.fieldOfStudy}")

                assertEquals(controller.planTableView.items[1].date, classes2.date)
                assertEquals(controller.planTableView.items[1].hour, classes2.hour)
                assertEquals(controller.planTableView.items[1].teacher, classes2.teacher)
                assertEquals(controller.planTableView.items[1].group, "${classes2.group}, ${classes2.fieldOfStudy}")

                //Pobierz wolne godziny na jakie można zmienić
                var hours = convertClassesToWriteToClassesToRead(classes2).getFreeHours()

                //Nie powinno pozwolić na zmianę na 8.00, bo nauczyciel jest zajęty i grupa też
                //Powinno wyświetlić jedynie 09.35-10.20 bo na inne godziny nauczyciel nie zgłosił już dyspo
                assertEquals(hours.size, 1)
                assertFalse(hours.contains(classes1.hour))
                assertFalse(hours.contains(classes2.hour))
                assertTrue(hours.contains("09.35-10.20"))

                //Z wolnych sal o tej godzinie powinno pokazać wszystkie sale z LocTemp (ale nie LocTemp2 bo to nie ta lokalizacja)
                var roomsHour = convertClassesToWriteToClassesToRead(classes2).getFreeRoomsByHour("09.35-10.20")
                assertTrue(roomsHour.contains("sala30, TempLoc"))
                assertTrue(roomsHour.contains("sala10, TempLoc"))
                assertFalse(roomsHour.contains("sala20, TempLoc2"))

                //Przypadek gdy zmiana sali a nie zmiana godziny i sali (inna opcja w menu)
                //w żadnym nie może pokazać LocTemp2, bo nauczyciel i grupa nie zdążą zmienić lokalizacji (ale wirtualną powinno pokazać)
                var freeRooms = convertClassesToWriteToClassesToRead(classes2).getFreeRooms()
                var busyRooms = convertClassesToWriteToClassesToRead(classes2).getBusyRooms()

                freeRooms.forEach { assertFalse { it.contains(locationToAdd2.locationName) } }
                busyRooms.forEach { assertFalse { it.contains(locationToAdd2.locationName) } }

                assertTrue(freeRooms.contains("${classes1.room}, ${classes1.location}"))
                assertTrue(freeRooms.contains("Wirtualna, Platforma"))
                assertFalse(freeRooms.contains("${classes2.room}, ${classes2.location}"))

                //Usuńmy pierwsze zajęcia - wtedy powinno się już móc zmienić na 2 lokalizację podczas zmiany tylko sali
                //ale podczas zmiany godziny i sali już nie (znowu jedynie tylko sale z LocTemp)
                convertClassesToWriteToClassesToRead(classes1).deleteClasses()

                //Powinna pojawić się 08.00-08.45 teraz
                hours = convertClassesToWriteToClassesToRead(classes2).getFreeHours()
                assertEquals(hours.size, 2)
                assertTrue(hours.contains(classes1.hour))
                assertFalse(hours.contains(classes2.hour))
                assertTrue(hours.contains("09.35-10.20"))

                //Nadal powinno wyświetlać te same sale
                roomsHour = convertClassesToWriteToClassesToRead(classes2).getFreeRoomsByHour("09.35-10.20")
                assertTrue(roomsHour.contains("sala30, TempLoc"))
                assertTrue(roomsHour.contains("sala10, TempLoc"))
                assertFalse(roomsHour.contains("sala20, TempLoc2"))


                //Opcja zmiany samej sali bez godziny (można zmienić na inną lokalizację już)
                freeRooms = convertClassesToWriteToClassesToRead(classes2).getFreeRooms()

                assertTrue(freeRooms.contains("${classes1.room}, ${classes1.location}"))
                assertTrue(freeRooms.contains("${room2.roomName}, ${room2.location}"))

                //Nie można zmienić na tą samą
                assertFalse(freeRooms.contains("${classes2.room}, ${classes2.location}"))

                //Sprawdz czy po zmianie wyświetlony został plan nauczyciela jeśli był wyświetlany przed zmianą
                controller.dialog = MFXStageDialog()
                controller.showTeacherPlanPressed = true
                controller.showPlanAfterChange(convertClassesToWriteToClassesToRead(classes2))
                controller.planTableView.items.forEach { assertTrue(it.teacher == classes2.teacher) }

                controller.showTeacherPlanPressed = false
                controller.showGroupPlanPressed = true
                controller.showPlanAfterChange(convertClassesToWriteToClassesToRead(classes2))
                controller.planTableView.items.forEach { assertTrue(it.group.contains(classes2.group!!)) }

                //zamieńmy godzinę na inną dla platformy
                hours = convertClassesToWriteToClassesToRead(classes2).getFreeHours()
                assertTrue(hours.contains(classes1.hour))
                convertClassesToWriteToClassesToRead(classes2).changeHoursPlatform(classes1.hour!!)

                //Pobierzmy plan dla IB i zobaczmy czy zmieniła się godzina
                val plan = planModel.getPlanGroup(classes2.fieldOfStudy!!, classes2.group!!, MessageBundle.getMess("label.wholePlan"))
                assertFalse(plan.isEmpty())
                assertEquals(plan.size,1)
                assertEquals(plan[0].hour, classes1.hour)

                teacherDAO.deleteTeacher(teacherToAdd)
                locationModel.deleteLocation(locationToAdd)
                locationModel.deleteLocation(locationToAdd2)
                fieldDAO.deleteSPN(fieldToAdd.fieldName)
                fieldDAO.deleteField(fieldToAdd)
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

    //Testowanie tworzenia menu kontekstowego
    @Test
    fun showMenuTest(){
        Platform.runLater {
            runBlocking {
                controller.showContextMenu(1,
                    ClassesToRead(LocalDate.parse("2000-01-01"), "08.00-08.45", "Język polski", "testroom", "Jan Kowalski", "IA")
                )

                for (item in controller.menu.items) assertTrue(item.styleClass.contains("mfx-menu-item"))
                assertEquals((controller.menu.items[0] as MFXContextMenuItem).text, MessageBundle.getMess("label.deleteFromPlan"))
                assertEquals((controller.menu.items[1] as MFXContextMenuItem).text,MessageBundle.getMess("label.changeTeacher"))
                assertEquals((controller.menu.items[2] as MFXContextMenuItem).text, MessageBundle.getMess("label.changeRoom"))
                assertEquals((controller.menu.items[3] as MFXContextMenuItem).text, MessageBundle.getMess("label.changeHour"))

                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-trash-can")
                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-arrows-rotate")
                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[2] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-arrows-rotate")
                assertEquals(((controller.menu.items[2] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[3] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-arrows-rotate")
                assertEquals(((controller.menu.items[3] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)
            }
        }
    }


    //Test sprawdzający poprawność tworzenia zawartości okna dialogowego z formularzem zmiany sali na zajęciach
    @Test
    fun createChangeRoomVBox()
    {
        Platform.runLater {
            val freeRooms = MFXListView<String>()
            val busyRooms = MFXListView<String>()

            freeRooms.items.addAll("Room1", "Room2", "Room3")
            busyRooms.items.addAll("Room4", "Room5")

            val vbox = controller.createChangeRoomVbox(freeRooms, busyRooms)

            val freeRoomsLabel = vbox.lookup(".header-label-big") as Label

            assertEquals(MessageBundle.getMess("label.freeRooms"), freeRoomsLabel.text)

            // Sprawdź, czy zawiera odpowiednie dzieci w zależności od danych wejściowych
            if (freeRooms.items.isEmpty() && busyRooms.items.isEmpty()) {
                assertEquals(1, vbox.children.size)
                val noRoomsLabel = vbox.children[0] as Label
                assertEquals("Brak sal", noRoomsLabel.text)
            } else {
                assertEquals(1, vbox.children.size)
                val hBox = vbox.children[0] as HBox
                assertEquals(2, hBox.children.size)

                val v1 = hBox.children[0] as VBox
                val v2 = hBox.children[1] as VBox

                assertEquals(2, v1.children.size)
                assertEquals(2, v2.children.size)

                // Sprawdź, czy etykiety są ustawione poprawnie
                val v1Label = v1.lookup(".header-label-big") as Label
                assertEquals("Wolne sale", v1Label.text)
            }
        }
    }


    //Test sprawdzający poprawność ustawiania tabeli
    @Test
    fun setUpTableTest()
    {
        Platform.runLater {
            controller.planTableView = MFXTableView<ClassesToRead>()
            controller.setupTable()

            val widths = arrayOf(2,2,4,2,3,2)

            var screenWidth = 10.0
            val screenSizes: ObservableList<Screen> = Screen.getScreens()
            screenSizes.forEach(Consumer { screen: Screen -> screenWidth = screen.bounds.width })

            assertNotNull(controller.planTableView)

            for (i in 0 until controller.planTableView.tableColumns.size) {
                assertEquals(controller.planTableView.tableColumns[i].prefWidth, (widths[i].toDouble()/15)*screenWidth)
            }

            assertEquals("table-header", controller.planTableView.tableColumns[0].styleClass[1])
        }
    }

    //Test sprawdzjący poprawność tworzenia zawartości okna dialogowego z formularzem zmiany nauczyciela na zajęciach
    @Test
    fun testCreateChangeTeacherVbox() {
        Platform.runLater {
            val freeTeachers = MFXListView<String>()
            val busyTeachers = MFXListView<String>()

            // Wypełnij dane dla list widoku
            freeTeachers.items.addAll("Teacher1", "Teacher2")
            busyTeachers.items.addAll("Teacher3", "Teacher4", "Teacher5")

            val resultVBox = controller.createChangeTeacherVbox(freeTeachers, busyTeachers)

            // Sprawdź, czy tworzony VBox zawiera oczekiwane elementy
            val freeTeachersLabel = resultVBox.lookup(".header-label-big") as Label

            assertEquals(MessageBundle.getMess("label.freeTeachers"), freeTeachersLabel.text)

            // Sprawdź, czy zawiera odpowiednie dzieci w zależności od danych wejściowych
            if (freeTeachers.items.isEmpty() && busyTeachers.items.isEmpty()) {
                assertEquals(1, resultVBox.children.size)
                val noTeachersLabel = resultVBox.children[0] as Label
                assertEquals(MessageBundle.getMess("label.noTeachers"), noTeachersLabel.text)
            } else {
                assertEquals(1, resultVBox.children.size)
                val hBox = resultVBox.children[0] as HBox
                assertEquals(2, hBox.children.size)

                val v1 = hBox.children[0] as VBox
                val v2 = hBox.children[1] as VBox

                assertEquals(2, v1.children.size)
                assertEquals(2, v2.children.size)

                val v1Label = v1.lookup(".header-label-big") as Label
                val v2Label = v2.lookup(".header-label-big") as Label

                assertEquals(MessageBundle.getMess("label.freeTeachers"), v1Label.text)
                assertEquals(MessageBundle.getMess("label.busyTeachers"), v2Label.text)
            }
        }
    }

    //Testowanie tworzenia okna zmiany godziny
    @Test
    fun createChangeHourVBox()
    {
        Platform.runLater {
            val hourChoicebox = MFXComboBox<String>()
            val freeRooms = MFXListView<String>()
            val classes = ClassesToRead(LocalDate.parse("2000-01-01"), "08.00-08.45", "Język polski", "testroom", "Jan Kowalski", "IA")

            hourChoicebox.items.addAll("08.00-08.45", "08.45-09.30")
            val vbox = controller.createChangeHourVbox(freeRooms, hourChoicebox, classes)
            val freeRoomsLabel = vbox.lookup(".header-label-big") as Label
            assertEquals(MessageBundle.getMess("label.freeRooms"), freeRoomsLabel.text)

            assertEquals(VBox::class.java, vbox.javaClass)
            assertEquals(15.0, vbox.spacing)

            assertEquals(3, vbox.children.size)
            assertEquals(vbox.alignment, Pos.CENTER)
            assertEquals(freeRooms.prefWidth, 350.0)
            assertEquals(hourChoicebox.prefWidth, 300.0)
        }
    }

    @Test
    fun messageTest()
    {
        Platform.runLater {
            runBlocking {
                controller.showDialogYesNoMessage("Are you sure?", ActionType.DELETE)
                val dialog = (controller.dialogMess.content as MFXGenericDialog)
                val vbox:VBox = dialog.children[2] as VBox
                assertEquals(vbox.children[0].javaClass, javafx.scene.control.Label::class.java)
                assertEquals((vbox.children[0] as Label).text, "Are you sure?")
            }
        }
    }

    //Testowanie tworzenia okna zmiany godziny dla platformy
    @Test
    fun createChangeHourPlatformVbox()
    {
        Platform.runLater {
            val hourChoicebox = MFXComboBox<String>()
            val classes = ClassesToRead(LocalDate.parse("2000-01-01"), "08.00-08.45", "Język polski", "testroom", "Jan Kowalski", "IA")

            val vbox:VBox = controller.createChangeHourPlatformVbox(hourChoicebox, classes)

            assertEquals(VBox::class.java, vbox.javaClass)
            assertEquals(15.0, vbox.spacing)

            assertEquals(2, vbox.children.size)
            val button = vbox.children[1]
            assertEquals(button.javaClass, MFXButton::class.java)
            assertEquals((button as MFXButton).text, MessageBundle.getMess("label.changeHour"))
            assertEquals(button.id, "customButton")
            assertEquals(vbox.alignment, Pos.CENTER)
            assertEquals(hourChoicebox.prefWidth, 300.0)
            assertEquals(hourChoicebox.promptText, MessageBundle.getMess("label.chooseHour"))
        }
    }
}