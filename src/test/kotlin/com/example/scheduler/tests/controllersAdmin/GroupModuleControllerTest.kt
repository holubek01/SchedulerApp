package com.example.scheduler.tests.controllersAdmin

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.GroupsModuleController
import com.example.scheduler.db.dao.FieldDAOImpl
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Group
import com.example.scheduler.objects.Room
import com.example.scheduler.utils.ActionType
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
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

internal class GroupModuleControllerTest: ApplicationTest()
{
    private val fieldDAO = FieldDAOImpl()
    lateinit var controller: GroupsModuleController

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/groupModule.fxml"))
        val root: Parent = loader.load()
        controller = loader.getController()
        stage!!.scene = Scene(root)
    }


    //Test sprawdzający wyświetlanie ostrzeżeń
    @Test
    fun showWarningsTest()
    {
        Platform.runLater {

            val field = Field("Tempo", 1, "TMPPPPP")
            fieldDAO.addField(field)
            fieldDAO.addGroups(field,2 ,1 ,"I")

            controller.showGroups()
            assertEquals(MessageUtil.content, MessageBundle.getMess("warning.showGroupsError.noField"))

            controller.fieldChoiceBox.value = field.fieldName
            controller.showGroups()
            assertEquals(MessageUtil.content, MessageBundle.getMess("warning.showGroupsError.noSem"))

            controller.semesterChoiceBox.value = 10
            controller.showGroups()
            assertEquals(MessageUtil.content, MessageBundle.getMess("warning.noGroupsInGivenField"))

            fieldDAO.deleteField(field)
        }
    }


    //Test dodawania i usuwania grupy
    @Test
    fun addAndDeleteGroupTest()
    {
        Platform.runLater {
            runBlocking {
                //Najpierw dodaj kierunek
                val field = Field("Tymczasowy kierunek", 2, "TEMP")
                if (fieldDAO.getFields().contains(field))
                {
                    fieldDAO.deleteSPN(field.fieldName)
                    fieldDAO.deleteField(field)
                }
                fieldDAO.addField(field)

                assertTrue(fieldDAO.getGroups(field.fieldName, 1).isEmpty())
                controller.fieldChoiceBox.value = "Tymczasowy kierunek"
                controller.semesterChoiceBox.value = 1

                //Brak grup
                controller.showGroups()
                assertTrue(controller.groupsListView.items.isEmpty())

                //Dodaj grupy
                var expectedAddedGroupName = "I${(65+controller.groupsListView.items.size).toChar()}"
                controller.addGroup()
                assertEquals(controller.groupsListView.items.size,1)
                assertEquals(controller.groupsListView.items[0],expectedAddedGroupName)

                expectedAddedGroupName = "I${(65+controller.groupsListView.items.size).toChar()}"
                controller.addGroup()
                assertEquals(controller.groupsListView.items.size,2)
                assertEquals(controller.groupsListView.items[1],expectedAddedGroupName)

                //czy posortowane
                controller.addGroup()
                controller.addGroup()

                val sortedGroups = FXCollections.observableArrayList("IA", "IB", "IC", "ID")
                assertEquals(sortedGroups, fieldDAO.getGroups(field.fieldName,1))
                controller.wantToDelete = true

                //usuń grupę IC (środkową) a nasępnie dodaj grupę i zobacz czy dodała się zgodnie z oczekiwaniami IC
                val groupToDelete = Group(controller.groupsListView.items[2], controller.fieldChoiceBox.value, controller.semesterChoiceBox.value)
                controller.deleteGroup(groupToDelete)

                assertFalse(fieldDAO.getGroups(field.fieldName, 1).contains(groupToDelete.fieldName))

                controller.addGroup()

                assertTrue(fieldDAO.getGroups(field.fieldName, 1).contains(groupToDelete.groupName))
                assertEquals(sortedGroups, fieldDAO.getGroups(field.fieldName,1))

                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                assertTrue(fieldDAO.getGroups(field.fieldName, field.semsNumber).isEmpty())
                assertFalse(fieldDAO.getFields().contains(field))
            }
        }

    }

    //Test sprawdzający tworzenie menu kontekstowego
    @Test
    fun showMenuTest(){
        Platform.runLater {
            runBlocking {
                controller.showContextMenu(1, "IA")

                for (item in controller.menu.items) assertTrue(item.styleClass.contains("mfx-menu-item"))
                assertEquals((controller.menu.items[0] as MFXContextMenuItem).text, MessageBundle.getMess("label.deleteGroup"))

                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-trash-can")
                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)
            }
        }
    }


    //Test czyszczenia panelu podczas zmiany zakładek
    @Test
    fun setOnTabsChangedTest()
    {
        Platform.runLater {
            runBlocking {
                controller.semesterChoiceBox.value = 12323
                controller.groupsListView.items = FXCollections.observableArrayList("Nowa Grupa")

                assertFalse(controller.semesterChoiceBox.value == null)
                assertFalse(controller.groupsListView.items.isEmpty())

                controller.onTabsChanged()

                assertTrue(controller.groupsListView.items.isEmpty())
            }
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
                assertEquals((vbox.children[0] as Label).text, "Are you sure?")
            }
        }
    }

    @Test
    fun clearBoxesTest()
    {
        controller.semesterChoiceBox.value = 1
        controller.semesterChoiceBox.text = "1"

        controller.onTabsChanged()

        assertTrue(controller.semesterChoiceBox.value == null)
        assertTrue(controller.semesterChoiceBox.text.isEmpty())
    }




}