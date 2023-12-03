package com.example.scheduler.tests.controllersAdmin

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.TeacherModuleController
import com.example.scheduler.db.dao.FieldDAOImpl
import com.example.scheduler.models.TeachersModel
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXContextMenuItem
import io.github.palexdev.materialfx.controls.MFXListView
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.util.*

internal class TeacherModuleControllerTest: ApplicationTest()
{
    lateinit var controller: TeacherModuleController
    lateinit var teacherModel: TeachersModel
    lateinit var stage:Stage

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("pl", "PL"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/teacherModule.fxml"))
        val root: Parent = loader.load()
        controller = loader.getController()
        stage!!.scene = Scene(root)
        this.stage = stage
        teacherModel = controller.teachersModel
    }

    //Testowanie wyświetlania przedmiotów oraz dyspozycyjności nauczyciela
    @Test
    fun showTeacherSubjectsAndAvailability()
    {
        Platform.runLater {
            runBlocking {
                val availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Podstawy przedsiębiorczości", "Język migowy")

                //nie był zmieniny
                val teacherToAdd = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987654321",
                    email = "ss@s.pl"
                )

                val expetedAvailabilityList = mutableListOf("Piątek 08.00-08.45", "Sobota 08.00-08.45", "Sobota 08.45-09.30")

                if (teacherModel.getTeachers().contains(teacherToAdd)) teacherModel.deleteTeacher(teacherToAdd)
                teacherModel.addTeacher(teacherToAdd, subjects, availabilityList)

                controller.showSubjects(teacherToAdd)
                var dialog = (controller.dialog.content as MFXGenericDialog)
                assertEquals(dialog.children.size,3)
                var vBox = dialog.children[2] as VBox
                var listView = vBox.children[1] as MFXListView<*>
                var label = vBox.children[0] as javafx.scene.control.Label

                //Przedmioty się zgadzają
                assertEquals(subjects.sorted(), listView.items.sorted())
                assertEquals(label.text, MessageBundle.getMess("label.subjects"))

                controller.showAvailability(teacherToAdd)
                dialog = (controller.dialog.content as MFXGenericDialog)
                assertEquals(dialog.children.size,3)
                vBox = dialog.children[2] as VBox
                listView = vBox.children[1] as MFXListView<*>
                label = vBox.children[0] as javafx.scene.control.Label
                assertEquals(expetedAvailabilityList, listView.items)

                //Dyspo się zgadza
                assertEquals(label.text, MessageBundle.getMess("label.availability"))

                teacherModel.deleteTeacher(teacherToAdd)
            }
        }
    }

    //Testowanie tworzenia okna edycji nauczyciela
    @Test
    fun createTeacherEditDialogTest()
    {
        Platform.runLater {
            runBlocking {
                //Najpierw dodajmy nauczyciela, potem sprawdzimy czy zaznaczane są dobre przedmioty
                //w formularzu oraz dobra dyspozycyjnosć
                val availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Podstawy przedsiębiorczości", "Język migowy")

                //nie był zmieniny
                val teacherToAdd = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987654321",
                    email = "ss@s.pl"
                )

                if (teacherModel.getTeachers().contains(teacherToAdd)) teacherModel.deleteTeacher(teacherToAdd)
                teacherModel.addTeacher(teacherToAdd, subjects, availabilityList)
                controller.lastSelectedTeacher = teacherToAdd
                controller.editTeacher(teacherToAdd)

                assertEquals(controller.firstNameTextFieldEdit.text, teacherToAdd.firstname)
                assertEquals(controller.lastNameTextFieldEdit.text, teacherToAdd.lastname)
                assertEquals(controller.phoneTextFieldEdit.text, teacherToAdd.phone)
                assertEquals(controller.emailTextFieldEdit.text, teacherToAdd.email)

                //lista przedmiotów odpowiednio się zaznacza
                assertEquals(controller.subjectCheckListEdit.selectionModel.selectedValues.sorted(), subjects.sorted())
                assertEquals(controller.availabilityList["Friday"], mutableListOf("08.00-08.45"))
                assertEquals(controller.availabilityList["Saturday"], mutableListOf("08.00-08.45", "08.45-09.30"))

                teacherModel.deleteTeacher(teacherToAdd)
            }
        }
    }


    //Testowanie dodawania nauczyciela (wraz ze sprawdzaniem walidatorów i duplikatów)
    @Test
    fun checkDBAddAndEditTeacherTest()
    {
        Platform.runLater {
            runBlocking {
                val availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Podstawy przedsiębiorczości", "Język migowy")

                //nie był zmieniny
                val teacherToAdd = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987654321",
                    email = "ss@s.pl"
                )

                var teacherToAdd3 = Teacher(
                    firstname = "Jann",
                    lastname = "Kowalskii",
                    phone = "987651234",
                    email = "wp@s.pl"
                )

                var teacherToAdd2 = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987654321",
                    email = "ss@s.pl"
                )

                if (teacherModel.getTeachers().contains(teacherToAdd)) teacherModel.deleteTeacher(teacherToAdd)
                if (teacherModel.getTeachers().contains(teacherToAdd3)) teacherModel.deleteTeacher(teacherToAdd3)

                assertTrue(controller.teacherTableView.items.isEmpty())

                //dodawanie nauczyciela
                controller.availabilityList = availabilityList

                controller.addTeacher(teacherToAdd, subjects)
                val teacherToAddId = teacherModel.getTeacherID(teacherToAdd)

                controller.addTeacher(teacherToAdd3, subjects)

                assertTrue(controller.availabilityList.all { it.value.isEmpty() })

                controller.showTeachers()
                assertFalse(controller.teacherTableView.items.isEmpty())

                val filteredTeachers = controller.teacherTableView.items.filter { it.firstname == "Jan" && it.lastname == "Kowalski" && it.phone == "987654321" && it.email=="ss@s.pl"}

                //nauczyciel istnieje już w bazie
                controller.checkDBWhileAdding(teacherToAdd2)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.teacherAlreadyInDB"))

                teacherToAdd2 = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987654320",
                    email = "wwwwww@s.pl"
                )

                //imię i nazwisko istnieje już w bazie
                controller.checkDBWhileAdding(teacherToAdd2)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.teacherExists"))


                teacherToAdd2 = Teacher(
                    firstname = "Jannnnn",
                    lastname = "Kowalskiiiii",
                    phone = "987654321",
                    email = "wwwwww@s.pl"
                )

                //numer telefonu istnieje już w bazie
                controller.checkDBWhileAdding(teacherToAdd2)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.phoneOccupied"))


                teacherToAdd2 = Teacher(
                    firstname = "Jannnnn",
                    lastname = "Kowalskiiiii",
                    phone = "987654320",
                    email = "ss@s.pl"
                )
                //email istnieje już w bazie
                controller.checkDBWhileAdding(teacherToAdd2)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.emailOccupied"))

                //Teraz sprawdzanie edytowania
                controller.lastSelectedTeacher = filteredTeachers[0]

                //edytujemy teacherToAdd3
                controller.checkDBWhileEditing(teacherToAdd3)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.teacherAlreadyInDB"))

                teacherToAdd3.phone = "010101010"
                controller.checkDBWhileEditing(teacherToAdd3)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.teacherExists"))

                teacherToAdd3.firstname = "Ewa"
                teacherToAdd3.lastname = "Nowak"
                teacherToAdd3.phone = "987651234"
                controller.checkDBWhileEditing(teacherToAdd3)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.phoneOccupied"))

                teacherToAdd3.firstname = "Ewa"
                teacherToAdd3.lastname = "Nowak"
                teacherToAdd3.phone = "999999999"
                teacherToAdd3.email = "wp@s.pl"
                controller.checkDBWhileEditing(teacherToAdd3)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.emailOccupied"))


                //Teraz możemy spróbować edytować nauczyciela na dobre dane
                teacherToAdd.firstname = "John"
                teacherToAdd.lastname = "Paul"
                teacherToAdd.phone = "888888888"
                teacherToAdd.email = "woekfvmokqwedfewfmc@wp.pl"


                var deletedAvailability= mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf()
                    this["Saturday"] = mutableListOf()
                    this["Sunday"] = mutableListOf()
                }

                //zaktualizuj nauczyciela i zobacz czy id się zgadzają
                controller.updateTeacher(teacherToAdd, subjects, deletedAvailability)
                val teacherToAddId2 = teacherModel.getTeacherID(teacherToAdd)
                assertEquals(teacherToAddId, teacherToAddId2)

                var teachers = teacherModel.getTeachers()
                assertTrue(teachers.contains(teacherToAdd))

                teacherToAdd3 = Teacher(
                    firstname = "Jann",
                    lastname = "Kowalskii",
                    phone = "987651234",
                    email = "wp@s.pl"
                )

                controller.wantToDelete = true
                controller.deleteTeacher(teacherToAdd)
                controller.deleteTeacher(teacherToAdd3)

                teachers = teacherModel.getTeachers()
                assertFalse(teachers.contains(teacherToAdd))
                assertFalse(teachers.contains(teacherToAdd3))

            }
        }
    }

    //Testowanie tworzenia formularza dodawania nauczyciela, resetowania oraz walidacji kroku 3
    @Test
    fun createStepperTest(){
        Platform.runLater {
            runBlocking {
                var teacherToAdd = Teacher("Jan", "Kowalskiii", "987654321", "ss@s.pl")
                if (teacherModel.getTeachers().contains(teacherToAdd)) teacherModel.deleteTeacher(teacherToAdd)

                val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.teacherAdded"))
                val resetButton = CommonUtils.createResetButton()
                val step3 = controller.createStep3(controller.availabilityLabel, controller.dayChoiceBox, controller.hourCheckList, controller.allHoursCheckBox)
                controller.createCompletedStep(controller.firstNameTextField, controller.lastNameTextField, controller.phoneTextField, controller.emailTextField, controller.subjectCheckList, controller.dayChoiceBox, controller.hourCheckList, controller.teacherLabel, controller.subjectLabel, controller.availabilityLabel, controller.allHoursCheckBox, step3, controller.stepper)

                controller.firstNameTextField.text = "Lorem"
                controller.lastNameTextField.text = "Lorem"
                controller.dayChoiceBox.items = FXCollections.observableArrayList("Friday")
                controller.phoneTextField.text = "111222333"

                assertFalse(controller.firstNameTextField.text.isEmpty())
                controller.setOnResetButton(controller.firstNameTextField, controller.lastNameTextField, controller.phoneTextField, controller.emailTextField, controller.subjectCheckList, controller.dayChoiceBox, controller.hourCheckList, controller.teacherLabel, controller.subjectLabel, controller.availabilityLabel, controller.allHoursCheckBox, controller.stepper)
                assertTrue(controller.firstNameTextField.text.isEmpty())

                //Nie wybrano żadnej dyspozycyjności
                controller.checkLastStep(
                    controller.firstNameTextField,
                    controller.lastNameTextField,
                    controller.phoneTextField,
                    controller.emailTextField,
                    controller.subjectCheckList,
                    controller.dayChoiceBox,
                    controller.hourCheckList,
                    completedLabel,
                    step3,
                    resetButton,
                    controller.stepper
                )

                //Bład dodawania nauczyciela - nie wybrano dyspozycyjności
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.noAvailabilityChosen"))

                //Wybrano dzień
                controller.dayChoiceBox.value = "Piątek"
                controller.hourCheckList.selectionModel.selectItem("08.00-08.45")
                controller.hourCheckList.selectionModel.selectItem("08.45-09.30")

                assertFalse(controller.availabilityList["Friday"]!!.contains("08.00-08.45"))
                controller.firstNameTextField.text = teacherToAdd.firstname
                controller.lastNameTextField.text = teacherToAdd.lastname
                controller.phoneTextField.text = teacherToAdd.phone
                controller.emailTextField.text = teacherToAdd.email
                controller.checkLastStep(
                    controller.firstNameTextField,
                    controller.lastNameTextField,
                    controller.phoneTextField,
                    controller.emailTextField,
                    controller.subjectCheckList,
                    controller.dayChoiceBox,
                    controller.hourCheckList,
                    completedLabel,
                    step3,
                    resetButton,
                    controller.stepperEdit
                )

                //Zobaczmy czy dodał się nauczyciel z dyspozycyjnością
                var avail = teacherModel.getAvailability(teacherToAdd)
                assertTrue(avail.contains("Piątek 08.00-08.45"))
                assertTrue(avail.contains("Piątek 08.45-09.30"))
                assertEquals(avail.size,2)

                avail = teacherModel.getAvailabilityByDay(teacherToAdd, "FRIDAY")
                assertTrue(avail.contains("08.00-08.45"))
                assertTrue(avail.contains("08.45-09.30"))
                assertEquals(avail.size,2)

                teacherModel.deleteTeacher(teacherToAdd)
            }
        }
    }

    //Test dodawania nauczycieli z pliku
    @Test
    fun addTeachersFromFile()
    {
        Platform.runLater {
            runBlocking {
                val fieldDAO = FieldDAOImpl()
                val teacher1 = Teacher("Tomasz", "Nowakiewicz",  "tomasz@wp.pl", "987898989")
                val teacher2 = Teacher("Adam", "Pusiarski", "adam@wp.pl","121212333")
                if (teacherModel.getTeachers().contains(teacher1)) teacherModel.deleteTeacher(teacher1)
                if (teacherModel.getTeachers().contains(teacher2)) teacherModel.deleteTeacher(teacher2)

                //Należy dodać jeszcze kierunek z przedmiotami
                val field = Field("Tymczasowy kierunek", 2, "TEMP")
                if (fieldDAO.getFields().contains(field))
                {
                    fieldDAO.deleteSPN(field.fieldName)
                    fieldDAO.deleteField(field)
                }

                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 3, 1, "I")

                fieldDAO.addSubjectToSem("Anatomia z fizjologią", field.fieldName, 1, 10)
                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 2)

                assertFalse(teacherModel.getTeachers().contains(teacher1))
                assertFalse(teacherModel.getTeachers().contains(teacher2))

                //Plik z poprawnymi danymi
                var path = "src/test/kotlin/com/example/scheduler/tests/testResources/teachers.xlsx"
                controller.uploadFileTeachers(path)

                assertTrue(teacherModel.getTeachers().contains(teacher1))
                assertTrue(teacherModel.getTeachers().contains(teacher2))

                assertTrue(teacherModel.getTeacherSubjects(teacher1).contains("Anatomia z fizjologią"))
                assertTrue(teacherModel.getTeacherSubjects(teacher1).contains("Język polski"))
                assertTrue(teacherModel.getTeacherSubjects(teacher2).contains("Język polski"))
                assertFalse(teacherModel.getTeacherSubjects(teacher2).contains("Anatomia z fizjologią"))

                teacherModel.deleteTeacher(teacher1)
                teacherModel.deleteTeacher(teacher2)

                //Plik ze zduplikowanymi nauczycielami - nie powinno dodać
                path = "src/test/kotlin/com/example/scheduler/tests/testResources/teachersbad1.xlsx"
                controller.uploadFileTeachers(path)
                assertFalse(teacherModel.getTeachers().contains(teacher1))
                assertFalse(teacherModel.getTeachers().contains(teacher2))
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.teacher.excel.duplicatedNames"))

                //Plik ze zduplikowanymi numerami telefonu - nie powinno dodać
                path = "src/test/kotlin/com/example/scheduler/tests/testResources/teachersbad2.xlsx"
                controller.uploadFileTeachers(path)
                assertFalse(teacherModel.getTeachers().contains(teacher1))
                assertFalse(teacherModel.getTeachers().contains(teacher2))
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.teacher.excel.duplicatedPhones"))

                //Plik z danymi, które nie przeszły walidacji - nie powinno dodać
                path = "src/test/kotlin/com/example/scheduler/tests/testResources/teachersbad4.xlsx"
                controller.uploadFileTeachers(path)
                assertFalse(teacherModel.getTeachers().contains(teacher1))
                assertFalse(teacherModel.getTeachers().contains(teacher2))
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.incorrectExcelForm"))

                fieldDAO.deleteField(field)
            }
        }
    }


    @Test
    fun createAddTeacherFromFileVBoxTest()
    {
        Platform.runLater {
            runBlocking {
                val vbox = controller.createAddTeacherFromFileVBox()
                assertTrue(vbox.children[0] is javafx.scene.control.Label)
                assertTrue(vbox.children[0].styleClass.contains("header-label_white"))
                assertEquals((vbox.children[0] as javafx.scene.control.Label).text, MessageBundle.getMess("label.loadFileWithTeachers"))

                assertTrue(vbox.children[1] is MFXRectangleToggleNode)
                assertTrue(vbox.children[1].id == "comboWhite")
                assertEquals((vbox.children[1] as MFXRectangleToggleNode).text, MessageBundle.getMess("label.chooseFile"))

                assertTrue(vbox.children[2] is MFXButton)
                assertTrue(vbox.children[2].id == "customButton")
                assertEquals((vbox.children[2] as MFXButton).text, MessageBundle.getMess("label.upload"))
            }
        }
    }

    @Test
    fun showMenuTest(){
        Platform.runLater {
            runBlocking {
                controller.showContextMenu(1,Teacher("Jan", "Kowal", "wef@w.pl", "111111456"))

                for (item in controller.menu.items) assertTrue(item.styleClass.contains("mfx-menu-item"))
                assertEquals((controller.menu.items[0] as MFXContextMenuItem).text, MessageBundle.getMess("label.delete"))
                assertEquals((controller.menu.items[1] as MFXContextMenuItem).text,MessageBundle.getMess("label.editTeacher"))
                assertEquals((controller.menu.items[2] as MFXContextMenuItem).text, MessageBundle.getMess("label.showAvailability"))
                assertEquals((controller.menu.items[3] as MFXContextMenuItem).text, MessageBundle.getMess("label.showSubjects"))

                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-trash-can")
                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-wrench")
                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[2] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-clock")
                assertEquals(((controller.menu.items[2] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[3] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-file-pen")
                assertEquals(((controller.menu.items[3] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)
            }
        }
    }

    //Test czyszczenia panelu podczas zmiany zakładek
    @Test
    fun onTabsChanged()
    {
        Platform.runLater {
            val teacher = Teacher("Jan", "Kowal", "123456789", "sdfg@wp.pl")
            controller.teacherTableView.items = FXCollections.observableArrayList(teacher)
            assertFalse(controller.teacherTableView.items.isEmpty())
            controller.onTabsChanged()
            assertTrue(controller.teacherTableView.items.isEmpty())
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
                assertEquals((vbox.children[0] as javafx.scene.control.Label).text, "Are you sure?")
            }
        }
    }


    @Test
    fun validateTeacherFromFile()
    {
        var teacher = Teacher("TOM", "Nowak", "tom@w.pl", "121212129")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("firstname.validation.allLettersLowercaseExceptFirst")}: ${teacher.firstname}")

        teacher = Teacher("T", "Nowak", "tom@w.pl", "121212129")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("firstname.validation.moreThanOneLetter")}: ${teacher.firstname}")

        teacher = Teacher("Tom#", "Nowak", "tom@w.pl", "121212129")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("firstname.validation.noSpecialChars")}: ${teacher.firstname}")

        teacher = Teacher("tom", "Nowak", "tom@w.pl", "121212129")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("firstname.validation.startWithUppercase")}: ${teacher.firstname}")

        teacher = Teacher("Tom", "N", "tom@w.pl", "121212129")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("lastname.validation.moreThanOneLetter")}: ${teacher.lastname}")

        teacher = Teacher("Tom", "nowak", "tom@w.pl", "121212129")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("lastname.validation.startWithUppercase")}: ${teacher.lastname}")

        teacher = Teacher("Tom", "Nowak", "tom@w.pl", "12121229")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("phone.validation.nineDigits")}: ${teacher.phone}")

        teacher = Teacher("Tom", "Nowak", "tom@w.pl", "121NA2291")
        assertEquals(controller.teachersModel.validateTeacher(teacher), "${MessageBundle.getMess("phone.validation.onlyNumbers")}: ${teacher.phone}")
    }

    @Test
    fun showAddDeleteTeacherTest()
    {
        Platform.runLater {
            runBlocking {
                var availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Podstawy przedsiębiorczości", "Język migowy")

                val teacherToAdd = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987654321",
                    email = "ss@s.pl"
                )

                if (teacherModel.getTeachers().contains(teacherToAdd)) teacherModel.deleteTeacher(teacherToAdd)

                assertTrue(controller.teacherTableView.items.isEmpty())

                //dodawanie nauczyciela
                controller.availabilityList = availabilityList
                controller.addTeacher(teacherToAdd, subjects)
                assertTrue(controller.availabilityList.all { it.value.isEmpty() })

                controller.showTeachers()
                assertFalse(controller.teacherTableView.items.isEmpty())

                val filteredTeachers = controller.teacherTableView.items.filter { it.firstname == "Jan" && it.lastname == "Kowalski" && it.phone == "987654321" && it.email=="ss@s.pl"}

                assertTrue(filteredTeachers.size==1)

                val selectedTeacher = filteredTeachers[0]

                //Upewnij się że ma dobre przedmioty
                val teachersSubjects = teacherModel.getTeacherSubjects(selectedTeacher)
                assertEquals(teachersSubjects.sorted(), subjects.sorted())

                val av = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = teacherModel.getAvailabilityByDay(teacherToAdd, "FRIDAY")
                    this["Saturday"] = teacherModel.getAvailabilityByDay(teacherToAdd, "SATURDAY")
                    this["Sunday"] = teacherModel.getAvailabilityByDay(teacherToAdd, "SUNDAY")
                }

                availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                assertEquals(av, availabilityList)


                //usuwanie wcześniej dodanego nauczyciela
                val teachersCountBefore = controller.teacherTableView.items.size
                controller.wantToDelete = true
                controller.deleteTeacher(teacherToAdd)
                val teachersCountAfter = controller.teacherTableView.items.size
                assertEquals(teachersCountBefore-1, teachersCountAfter)
            }
        }
    }
    @Test
    fun setOnDaySelectedTest()
    {
        Platform.runLater {
            runBlocking {
                controller.availabilityList.forEach { assertTrue(it.value.isEmpty()) }
                controller.availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }


                controller.setOnDaySelected(controller.dayChoiceBoxEdit, controller.hourCheckListEdit)
                assertTrue(controller.hourCheckListEdit.selectionModel.selectedValues.isEmpty())
                controller.dayChoiceBoxEdit.value = "Piątek"
                assertEquals(controller.hourCheckListEdit.selectionModel.selectedValues.sorted(), mutableListOf("08.00-08.45"))

                //Dodaj nowa godzine do piatku
                controller.hourCheckListEdit.selectionModel.selectItem("08.45-09.30")

                //Zmień na sobotę
                controller.dayChoiceBoxEdit.value = "Sobota"

                //zobacz czy dobrze się zaznaczyła sobota
                assertEquals(controller.hourCheckListEdit.selectionModel.selectedValues.sorted(), mutableListOf("08.00-08.45", "08.45-09.30"))

                //sprawdz czy nowa godzina przypisała się do piątku
                assertEquals(controller.availabilityList["Friday"], mutableListOf("08.00-08.45", "08.45-09.30"))
            }
        }
    }

    @Test
    fun checkSubjectWasDeletedTest()
    {
        Platform.runLater {
            runBlocking {

                val availabilityList = mutableMapOf<String, MutableList<String>>().apply {
                    this["Friday"] = mutableListOf("08.00-08.45")
                    this["Saturday"] = mutableListOf("08.00-08.45", "08.45-09.30")
                    this["Sunday"] = mutableListOf()
                }

                val subjects = mutableListOf("Podstawy przedsiębiorczości", "Język migowy")

                //nie był zmieniny
                val teacherToAdd = Teacher(
                    firstname = "Jan",
                    lastname = "Kowalski",
                    phone = "987224321",
                    email = "sshtr@s.pl"
                )

                if (teacherModel.getTeachers().contains(teacherToAdd)) teacherModel.deleteTeacher(teacherToAdd)
                teacherModel.addTeacher(teacherToAdd, subjects, availabilityList)
                controller.lastSelectedTeacher = teacherToAdd
                controller.editTeacher(teacherToAdd)

                controller.subjectCheckListEdit.selectionModel.clearSelection()
                controller.subjectCheckListEdit.selectionModel.selectItem("Język migowy")
                controller.wantToEdit = false
                controller.checkIfOldSubjectWasDeleted(controller.subjectCheckListEdit)

                assertTrue(controller.subjectCheckListEdit.selectionModel.selectedValues.sorted() == teacherModel.getTeacherSubjects(controller.lastSelectedTeacher))

                teacherModel.deleteTeacher(teacherToAdd)
            }
        }
    }







}