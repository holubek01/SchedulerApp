package com.example.scheduler.tests.controllersAdmin

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.FieldsModuleController
import com.example.scheduler.db.dao.FieldDAO
import com.example.scheduler.db.dao.FieldDAOImpl
import com.example.scheduler.db.dao.PlanDAOImpl
import com.example.scheduler.models.FieldsModel
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.TeachingPlan
import com.example.scheduler.objects.Subject
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.ActionType
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import com.example.scheduler.utils.ValidationWrapper
import io.github.palexdev.materialfx.controls.MFXComboBox
import io.github.palexdev.materialfx.controls.MFXContextMenuItem
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.application.Platform
import javafx.collections.FXCollections
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
import java.util.*

internal class FieldModuleControllerTest: ApplicationTest()
{
    lateinit var controller: FieldsModuleController
    lateinit var model:FieldsModel
    private val dao = FieldDAOImpl()

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/fieldModule.fxml"))
        val root: Parent = loader.load()
        controller = loader.getController()
        stage!!.scene = Scene(root)
        model = controller.fieldsModel
    }



    //Test sprawdzający duplikaty w bazie danych podczas edytowania kierunku
    @Test
    fun checkWhileEditingTest()
    {
        Platform.runLater {
            runBlocking {
                //field - kierunek, który będzie zaznaczony (pomijany przy sprawdzaniu)
                //field2 - kierunek w bazie danych, który będzie powodował duplikaty
                //Dodawanie potrzebnych danych
                val field = Field("Tymczasowy kierunek", 2, "TEMP")
                val field2 = Field("Tymczasowy kierunek 2", 2, "TEMP2")

                if (model.getFields().contains(field))
                {
                    model.deleteSPN(field.fieldName)
                    model.deleteField(field)
                }
                if (model.getFields().contains(field2))
                {
                    model.deleteSPN(field2.fieldName)
                    model.deleteField(field2)
                }

                val groupBox1 = MFXComboBox<Int>()
                groupBox1.value = 1
                val groupBox2 = MFXComboBox<Int>()
                groupBox2.value=1
                controller.groupNumberComboBoxArray = mutableListOf(groupBox1, groupBox2)

                controller.subjectsToAdd.add(Subject("Język polski", 1, 10))
                controller.subjectsToAdd.add(Subject("Język angielski", 1, 5))

                controller.addField(field)
                controller.addField(field2)
                dao.addGroups(field, 2, 1, "I")
                dao.addGroups(field2, 2, 1, "I")

                controller.lastSelectedField = field

                //Kierunek o tej nazwie już istnieje
                controller.fieldNameTextFieldEdit.text = "Tymczasowy kierunek 2"
                controller.shortcutTextFieldEdit.text = "TEMP2"
                controller.checkWhileEditing(controller.fieldNameTextFieldEdit.text, controller.shortcutTextFieldEdit.text)
                assertEquals(MessageBundle.getMess("warning.fieldNameExistsInEdit"), MessageUtil.content)

                //Kierunek o tym skrócie już istnieje
                controller.fieldNameTextFieldEdit.text = "Tymczasowy kierunek"
                controller.checkWhileEditing(controller.fieldNameTextFieldEdit.text, controller.shortcutTextFieldEdit.text)
                assertEquals(MessageBundle.getMess("warning.fieldShortcutExistsInEdit"), MessageUtil.content)

                model.deleteSPN(field.fieldName)
                model.deleteSPN(field2.fieldName)
                model.deleteField(field)
                model.deleteField(field2)
            }
        }
    }


    //Test dodawania, wyświetlania i usuwania kierunku
    @Test
    fun addAndDeleteFieldTest()
    {
        Platform.runLater {
            runBlocking {
                //Dodawanie kierunku z grupami i szkolnym planem nauczania
                val field = Field("Tymczasowy kierunek", 2, "TEMP")
                if (model.getFields().contains(field)) model.deleteField(field)

                val groupBox1 = MFXComboBox<Int>()
                groupBox1.value = 1
                val groupBox2 = MFXComboBox<Int>()
                groupBox2.value=1
                controller.groupNumberComboBoxArray = mutableListOf(groupBox1, groupBox2)

                controller.subjectsToAdd.add(Subject("Język polski", 1, 10))
                controller.subjectsToAdd.add(Subject("Język angielski", 1, 5))

                var fields = model.getFields()
                controller.showFields()

                val countBefore = controller.fieldsTableView.items.size

                //Narazie nie ma naszego kierunku
                assertFalse(fields.contains(field))
                controller.addField(field)

                fields = model.getFields()
                //Dodano kierunek
                assertTrue(fields.contains(field))

                val countAfter = controller.fieldsTableView.items.size
                assertEquals(countBefore+1, countAfter)

                //Zobacz czy istnieją grupy
                var groups = dao.getGroups(field.fieldName, 1)
                assertEquals(groups.size,1)

                model.deleteField(field)
                fields = model.getFields()
                //Usunięto poprawnie kierunek
                assertFalse(fields.contains(field))

                //Zobacz czy usunięto także grupy
                groups = dao.getGroups(field.fieldName, 1)
                assertTrue(groups.isEmpty())
            }
        }
    }


    //Test sprawdzający duplikaty w bazie danych podczas dodawania kierunku
    @Test
    fun checkDBWhileAddingTest()
    {
        Platform.runLater {
            runBlocking {
                //Dodawanie kierunku z grupami i szkolnym planem nauczania
                val field = Field("Tymczasowy kierunek", 2, "TEMP")
                if (model.getFields().contains(field)) model.deleteField(field)

                val groupBox1 = MFXComboBox<Int>()
                val groupBox2 = MFXComboBox<Int>()

                controller.groupNumberComboBoxArray = mutableListOf(groupBox1, groupBox2)

                //Błąd - nie wybrano liczby grup na semestrach
                controller.checkIfSemComboBoxEmpty()
                assertEquals(MessageBundle.getMess("warning.noGroupsNumber"), MessageUtil.content)

                groupBox2.value=1
                groupBox1.value=1
                controller.groupNumberComboBoxArray = mutableListOf(groupBox1, groupBox2)
                MessageUtil.content = ""

                //Wyrano liczby grup na semestrach
                controller.checkIfSemComboBoxEmpty()
                assertNotEquals(MessageBundle.getMess("warning.noGroupsNumber"), MessageUtil.content)

                //dodaj przedmioty
                controller.subjectsToAdd.add(Subject("Język polski", 1, 10))
                controller.subjectsToAdd.add(Subject("Język angielski", 1, 5))

                controller.addField(field)

                val fieldDupl = Field("Tymczasowy kierunek", 2, "TEMP")

                //Błąd - nie wybrano liczby semestrów
                controller.checkDbWhileAdding(fieldDupl.fieldName, fieldDupl.shortcut, controller.semChoiceBox)
                assertEquals(MessageBundle.getMess("warning.noSemCountSelected"), MessageUtil.content)

                //Błąd - istnieje duplikat
                controller.semChoiceBox.value = 2
                controller.checkDbWhileAdding(fieldDupl.fieldName, fieldDupl.shortcut, controller.semChoiceBox)
                assertEquals( MessageBundle.getMess("warning.fieldNameExistsInEdit"), MessageUtil.content)

                //Błąd - istnieje kierunek o tym skrócie
                fieldDupl.fieldName = "Tymczasowy kierunek 24"
                controller.checkDbWhileAdding(fieldDupl.fieldName, fieldDupl.shortcut, controller.semChoiceBox)
                assertEquals( MessageBundle.getMess("warning.fieldShortcutExistsInEdit"), MessageUtil.content)

                model.deleteSPN(field.fieldName)
                model.deleteField(field)
            }
        }
    }


    //Test sprawdzjący poprawność tworzenia i wyświetlania szkolnego planu nauczania dla kierunku
    @Test
    fun createAndShowSPNtableTest()
    {
        Platform.runLater {
            runBlocking {
                val field = Field("Tymczasowy kierunek", 2, "TEMPPP")
                if (model.getFields().contains(field)) model.deleteField(field)

                dao.addField(field)
                dao.addGroups(field, 2, 1, "I")

                model.addSubjectToSem("Język polski", field.fieldName, 1, 10)
                model.addSubjectToSem("Język angielski", field.fieldName, 1, 2)
                model.addSubjectToSem("Język polski", field.fieldName, 2, 20)
                model.addSubjectToSem("Język angielski", field.fieldName, 2, 30)

                //Stwórz tabele
                val spnTable = controller.createSPNtable(field)

                assertEquals(spnTable.isFooterVisible, false)
                assertEquals(spnTable.maxWidth, Double.MAX_VALUE)
                assertEquals(spnTable.maxHeight, Double.MAX_VALUE)

                assertEquals("table-header", spnTable.tableColumns[0].styleClass[1])
                assertEquals(spnTable.tableColumns[0].minWidth, 350.0)

                spnTable.items = model.showSPN(field)

                val subject1 = TeachingPlan("Język polski", FXCollections.observableArrayList(10,20))
                val subject2 = TeachingPlan("Język angielski", FXCollections.observableArrayList(2,30))

                assertEquals(spnTable.items[0], subject1)
                assertEquals(spnTable.items[1], subject2)

                model.deleteSPN(field.fieldName)
                model.deleteField(field)
            }
        }
    }

    //Test sprawdzający walidagory podczas edytowania kierunku
    @Test
    fun editFieldTest()
    {
        Platform.runLater {
            runBlocking {
                val field = Field("Tymczasowy kierunek", 3, "TMPPPP")
                val fi = Field("Kierunek edytowany", field.semsNumber,"KIER")

                if (model.getFields().contains(field)) model.deleteField(field)
                if (model.getFields().contains(fi)) model.deleteField(fi)
                dao.addField(field)
                dao.addGroups(field, 2, 1, "I")

                controller.lastSelectedField = field
                val oldID = model.getFieldID(field)

                controller.fieldNameTextFieldEdit.text = "kier@#unek"
                controller.shortcutTextFieldEdit.text = "kier"

                var errorFieldName = ValidationWrapper.createErrorLabel()
                var errorShortcut = ValidationWrapper.createErrorLabel()

                controller.onUpdateButtonPressed(errorFieldName, errorShortcut)

                //Błąd - nazwa kierunku nie może zawierać znaków specjalnych, skrót może zawierać jedynie duże litery
                assertEquals(errorFieldName.text, MessageBundle.getMess("fieldName.validation.noSpecialChars"))
                assertEquals(errorShortcut.text, MessageBundle.getMess("shortcut.onlyBigLetters"))

                //tu powinno się wykonać updateField()
                errorFieldName.text = ""
                errorShortcut.text= ""
                controller.fieldNameTextFieldEdit.text = fi.fieldName
                controller.shortcutTextFieldEdit.text = fi.shortcut
                assertFalse(model.getFields().contains(fi))

                errorFieldName = ValidationWrapper.createErrorLabel()
                errorShortcut = ValidationWrapper.createErrorLabel()

                controller.onUpdateButtonPressed(errorFieldName, errorShortcut)

                //Czy ID są takie same
                val newID = model.getFieldID(fi)
                assertTrue(model.getFields().contains(fi))
                assertEquals(oldID, newID)

                model.deleteField(fi)
            }
        }
    }


    //Test sprawdzający tworzenie menu kontekstowego
    @Test
    fun showMenuTest(){
        Platform.runLater {
            runBlocking {
                controller.showContextMenu(1, Field("Temp", 2, "TEMP"))

                for (item in controller.menu.items) assertTrue(item.styleClass.contains("mfx-menu-item"))
                assertEquals((controller.menu.items[0] as MFXContextMenuItem).text, MessageBundle.getMess("label.deleteField"))
                assertEquals((controller.menu.items[1] as MFXContextMenuItem).text,MessageBundle.getMess("label.editField"))
                assertEquals((controller.menu.items[2] as MFXContextMenuItem).text, MessageBundle.getMess("label.showSPN"))

                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-trash-can")
                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-wrench")
                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[2] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-paperclip")
                assertEquals(((controller.menu.items[2] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)
            }
        }
    }

    //Test sprawdzający tworzenie steppera (fomularz dodawania kierunku)
    @Test
    fun createStepperTest(){
        Platform.runLater {
            runBlocking {
                assertTrue(controller.groupNumberComboBoxArray.isEmpty())
                controller.semChoiceBox.value=2
                controller.createGroupNumberArray()

                assertFalse(controller.groupNumberComboBoxArray.isEmpty())
                assertEquals(controller.groupNumberComboBoxArray.size, 2)
                assertEquals(controller.groupNumberComboBoxArray[0].floatingText, "${MessageBundle.getMess("label.enterHoursNum")} 1")
                assertEquals(controller.groupNumberComboBoxArray[1].floatingText, "${MessageBundle.getMess("label.enterHoursNum")} 2")
                assertEquals(controller.groupNumberComboBoxArray[0].id, "comboWhite")
                assertEquals(controller.groupNumberComboBoxArray[0].items.size, 15)
            }
        }
    }

    //Test usuwania kierunku
    @Test
    fun deleteFieldTest()
    {
        Platform.runLater {
            runBlocking {
                val field = Field("Tymczasowy kierunekqwkjfn", 2, "TEMPPP")
                model.deleteField(field)
                assertFalse(model.getFields().contains(field))

                dao.addField(field)
                model.deleteField(field)
                assertFalse(model.getFields().contains(field))

                dao.addField(field)
                dao.addGroups(field, 2, 1, "I")
                model.deleteField(field)
                assertFalse(model.getFields().contains(field))

                dao.addField(field)
                model.addSubjectToSem("Język polski", field.fieldName, 1, 10)
                model.deleteField(field)
                assertFalse(model.getFields().contains(field))
            }
        }
    }


    @Test
    fun createFieldEditDialogTest()
    {
        Platform.runLater {
            runBlocking {
                val field = Field("Tymczasowy kierunek", 3, "TMPPPP")
                controller.editField(field)


                assertEquals(controller.fieldNameTextFieldEdit.text, field.fieldName)
                assertEquals(controller.shortcutTextFieldEdit.text, field.shortcut)

                assertEquals(controller.fieldNameTextFieldEdit.floatingText,  MessageBundle.getMess("label.enterFieldName"))
                assertEquals(controller.shortcutTextFieldEdit.floatingText, MessageBundle.getMess("label.enterFieldShortcut"))

                assertEquals(controller.fieldNameTextFieldEdit.prefWidth,  300.0)
                assertEquals(controller.shortcutTextFieldEdit.prefWidth, 300.0)

            }
        }
    }

    @Test
    fun messageTest()
    {
        Platform.runLater {
            runBlocking {
                controller.showDialogYesNoMessage("Are you sure?", ActionType.DELETE)
                val dialog = (controller.dialogMess.content as MFXGenericDialog)
                val vbox: VBox = dialog.children[2] as VBox
                assertEquals(vbox.children[0].javaClass, javafx.scene.control.Label::class.java)
                assertEquals((vbox.children[0] as Label).text, "Are you sure?")
            }
        }
    }
}







