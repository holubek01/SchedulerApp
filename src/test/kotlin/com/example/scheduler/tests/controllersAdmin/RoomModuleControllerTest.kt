package com.example.scheduler.tests.controllersAdmin

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.*
import com.example.scheduler.db.dao.LocationDAOImpl
import com.example.scheduler.db.dao.RoomDAOImpl
import com.example.scheduler.models.LocationsModel
import com.example.scheduler.models.RoomsModel
import com.example.scheduler.objects.Location
import com.example.scheduler.objects.Room
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import io.github.palexdev.materialfx.controls.MFXContextMenuItem
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
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
import java.util.*

internal class RoomModuleControllerTest: ApplicationTest()
{
    lateinit var controller: RoomModuleController
    lateinit var locationModel: LocationsModel
    lateinit var roomModel: RoomsModel

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/roomModule.fxml"))
        val root: Parent = loader.load()
        controller = loader.getController()
        stage!!.scene = Scene(root)
        locationModel = controller.locationsModel
        roomModel = controller.roomsModel
    }


    //Test sprawdzający duplikaty w bazie oraz edytowanie sali
    @Test
    fun addAndcheckDBAndEditTest()
    {
        Platform.runLater {
            runBlocking {
                val locationToAdd = Location("tmpLoc1", "Wro", "Ryn10", "22-222")

                val room1 = Room(
                    roomName = "roomTest1",
                    location = locationToAdd.locationName,
                    volume = 1,
                    floor = 1
                )
                controller.lastSelectedRoom = room1
                controller.checkDb("", "101", controller.stepper)

                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.noLocation"))

                controller.inEditMode = false

                val room2 = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                val room2Dupl = Room(
                    roomName = "roomTest2",
                    location = locationToAdd.locationName,
                    volume = 2,
                    floor = 2
                )

                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd)) locationModel.deleteLocation(locationToAdd)
                locationModel.addLocation(locationToAdd)

                var rooms = roomModel.getRooms(locationToAdd.locationName)
                assertTrue(rooms.isEmpty())
                val roomsToAdd = listOf(room1,room2)
                for (room in roomsToAdd) controller.addRoom(room)

                rooms = roomModel.getRooms(locationToAdd.locationName)
                assertFalse(rooms.isEmpty())
                assertEquals(rooms.size, 2)
                assertTrue(rooms.contains(room1) && rooms.contains(room2))

                controller.checkDb(locationToAdd.locationName, room2Dupl.roomName, controller.stepper)

                //ok
                assertEquals(MessageBundle.getMess("warning.roomAlreadyInDB"), MessageUtil.content)

                controller.inEditMode = true

                //Zaznaczmy room1 i sprawdźmy czy możemy edytować go na dane room2
                controller.lastSelectedRoom = room1

                val newName = room2.roomName
                controller.checkDb(locationToAdd.locationName, newName, controller.stepperEdit)
                assertEquals(MessageBundle.getMess("warning.roomAlreadyInDB"), MessageUtil.content)

                MessageUtil.content = ""

                //Usuwanie pokoju 1
                rooms = roomModel.getRooms(locationToAdd.locationName)
                assertEquals(rooms.size, 2)

                controller.wantToEdit = true

                controller.showRooms(locationToAdd.locationName)
                val countBefore = controller.roomsTableView.items.size
                controller.deleteRoom(room1)
                val countAfter = controller.roomsTableView.items.size


                assertEquals(countBefore-1, countAfter)

                rooms = roomModel.getRooms(locationToAdd.locationName)
                assertTrue(rooms.contains(room2))
                assertFalse(rooms.contains(room1))

                locationModel.deleteLocation(locationToAdd)
            }
        }
    }

    @Test
    fun showMenuTest(){
        Platform.runLater {
            runBlocking {
                controller.showContextMenu(1, Room("roomTest1", "TempLoc",1,1))

                for (item in controller.menu.items) assertTrue(item.styleClass.contains("mfx-menu-item"))
                assertEquals((controller.menu.items[0] as MFXContextMenuItem).text, MessageBundle.getMess("label.deleteRoom"))
                assertEquals((controller.menu.items[1] as MFXContextMenuItem).text, MessageBundle.getMess("label.editRoom"))

                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-trash-can")
                assertEquals(((controller.menu.items[0] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)

                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).description, "fas-wrench")
                assertEquals(((controller.menu.items[1] as MFXContextMenuItem).graphic as MFXFontIcon).size, 16.0)
            }
        }
    }

    @Test
    fun messageTest()
    {
        Platform.runLater {
            runBlocking {
                controller.showDialogYesNoMessage("Are you sure?")
                val dialog = (controller.dialogMess.content as MFXGenericDialog)
                val vbox: VBox = dialog.children[2] as VBox
                assertEquals(vbox.children[0].javaClass, javafx.scene.control.Label::class.java)
                assertEquals((vbox.children[0] as Label).text, "Are you sure?")
            }
        }
    }
}









