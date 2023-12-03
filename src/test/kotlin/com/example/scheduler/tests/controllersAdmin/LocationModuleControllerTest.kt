package com.example.scheduler.tests.controllersAdmin

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.LocationsModuleController
import com.example.scheduler.db.dao.LocationDAOImpl
import com.example.scheduler.db.dao.RoomDAOImpl
import com.example.scheduler.models.LocationsModel
import com.example.scheduler.models.RoomsModel
import com.example.scheduler.objects.Location
import com.example.scheduler.objects.Room
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.ActionType
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import com.example.scheduler.utils.ValidationWrapper
import io.github.palexdev.materialfx.controls.MFXContextMenuItem
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
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

internal class LocationModuleControllerTest: ApplicationTest()
{
    lateinit var controller: LocationsModuleController
    lateinit var locationModel:LocationsModel
    lateinit var roomModel:RoomsModel

    override fun start(stage: Stage?) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/locationModule.fxml"))
        val root: Parent = loader.load()
        controller = loader.getController()
        stage!!.scene = Scene(root)
        locationModel = controller.locationsModel
        roomModel = controller.roomsModel
    }


    //Test sprawdzający poprawność wczytywania pliku z salami do lokalizacji
    @Test
    fun roomsUploadFromFileTest()
    {
        Platform.runLater {
            runBlocking {
                val room1 = Room("sala10", "TempLoc",1,1)
                val room2 = Room("sala20", "TempLoc",2,3)
                val room3 = Room("sala30", "TempLoc",3,50)
                val roomsExpected: MutableList<Room> = mutableListOf(room1, room2, room3)

                //Poprawne wczytanie
                var filePath = "src/test/kotlin/com/example/scheduler/tests/testResources/roomsExample.xlsx"
                controller.uploadFileRooms(filePath, "TempLoc")
                assertEquals(controller.roomsToAdd, roomsExpected)

                //Błąd - jedna z wartości jest pusta
                filePath = "src/test/kotlin/com/example/scheduler/tests/testResources/roomsExampleNullCell.xlsx"
                controller.uploadFileRooms(filePath, "TempLoc")
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.incorrectExcelForm"))

                //Bład - niepoprawne dane zawierające znaki specjalne (błąd walidacji)
                filePath = "src/test/kotlin/com/example/scheduler/tests/testResources/roomsExampleIllegalChar.xlsx"
                controller.uploadFileRooms(filePath, "TempLoc")
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.incorrectExcelForm"))
            }
        }
    }


    //Test sprawdzający poprawność usuwania duplikatów i dodawania sal do lokalizacji
    @Test
    fun removeDuplicatesAndAddRoomsTest()
    {
        val locationToAdd = Location("TempLoc", "Wro", "Ryn10", "22-222")
        if (locationModel.getLocationsExceptPlatform().contains(locationToAdd)) locationModel.deleteLocation(locationToAdd)
        locationModel.addLocation(locationToAdd)

        var rooms = roomModel.getRooms(locationToAdd.locationName)
        assertTrue(rooms.isEmpty())

        val room1 = Room("sala10", "TempLoc",1,1)
        val room2 = Room("sala20", "TempLoc",2,3)
        val room3 = Room("sala30", "TempLoc",3,50)
        val room3Dupl = Room("sala30", "TempLoc",3,50)
        val roomsExpected: MutableList<Room> = mutableListOf(room1, room2, room3)
        val roomsWithDupl: MutableList<Room> = mutableListOf(room1, room2, room3, room3Dupl)

        controller.roomsToAdd = roomsWithDupl
        //Usuń duplikaty i dodaj sale
        assertTrue(controller.roomsModel.removeDuplicatesAndAddRooms(controller.roomsToAdd))

        rooms = roomModel.getRooms(locationToAdd.locationName)
        assertEquals(roomsExpected, rooms)

        locationModel.deleteLocation(locationToAdd)
    }

    //Test sprawdzający poprawność tworzenia przycisku do aktualizacji (oraz test dodawania)
    @Test
    fun createUpdateButtonTest()
    {
        Platform.runLater {
            runBlocking {
                val errorLocation = ValidationWrapper.createErrorLabel()
                val errorCity = ValidationWrapper.createErrorLabel()
                val errorStreet = ValidationWrapper.createErrorLabel()
                val errorPostcode = ValidationWrapper.createErrorLabel()

                val button = controller.createUpdateButton(errorLocation, errorCity, errorStreet, errorPostcode)

                assertEquals(MessageBundle.getMess("label.update"), button.text)
                assertEquals("customButton", button.id)
                assertEquals(200.0, button.prefWidth)

                controller.locationNameTextFieldEdit.text = "Lok@!"
                controller.cityTextFieldEdit.text = "Wroclaw"
                controller.streetTextFieldEdit.text = "Rynek 10"
                controller.postcodeTextFieldEdit.text = "11-111"

                //Błąd - nazwa lokalizacji zawiera znaki specjalne
                controller.onUpdateButtonPressed(errorLocation, errorCity ,errorStreet, errorPostcode)
                assertEquals(errorLocation.text, MessageBundle.getMess("location.validation.noSpecialChars"))

                //Dodajmy dobrą lokalizację o tych danych a potem spróbujmy wstawić podobną
                val locationToAdd = Location("Temploc", "Wro", "Ryn10", "22-222")
                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd)) locationModel.deleteLocation(locationToAdd)
                locationModel.addLocation(locationToAdd)

                val locationToAdd2 = Location("Inna", "Wroclaw", "Rynek10", "11-111")
                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd2)) locationModel.deleteLocation(locationToAdd2)
                locationModel.addLocation(locationToAdd2)
                controller.lastSelectedLocation = locationToAdd2


                controller.locationNameTextFieldEdit.text = locationToAdd.locationName
                controller.cityTextFieldEdit.text = locationToAdd.city
                controller.streetTextFieldEdit.text = locationToAdd.street
                controller.postcodeTextFieldEdit.text = locationToAdd.postcode

                //Błąd - lokalizacja o tych danych już istnieje
                controller.onUpdateButtonPressed(errorLocation, errorCity ,errorStreet, errorPostcode)
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.locationAlreadyInDB"))


                locationModel.deleteLocation(locationToAdd)
                locationModel.deleteLocation(locationToAdd2)
            }
        }
    }

    //Test sprawdzający wystepowanie duplikatów w bazie
    @Test
    fun checkDBAddAndEditLocationTest()
    {
        Platform.runLater {
            runBlocking {
                var locationToAdd = Location("tmpLoc1", "Wro", "Ryn10", "22-222")
                var locationToAdd3 = Location("tmpLoc2", "Wro", "Ryn11", "22-222")
                val locationToAdd3Dupl = Location("tmpLoc2", "Wro", "Ryn11", "22-222")
                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd)) locationModel.deleteLocation(locationToAdd)
                if (locationModel.getLocationsExceptPlatform().contains(locationToAdd3)) locationModel.deleteLocation(locationToAdd3)

                //Dodawanie lokalizacji z salami
                locationModel.addLocation(locationToAdd)
                locationModel.addLocation(locationToAdd3)
                assertTrue(controller.locationTableView.items.isEmpty())
                controller.showLocations()
                assertFalse(controller.locationTableView.items.isEmpty())

                //Sprawdzenie możliwości dodania 3 lokalizacji, której dane już istnieją
                controller.checkDbWhileAdding(locationToAdd3Dupl)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.locationExists"))

                //Lokalizacja istnieje
                locationToAdd3Dupl.locationName = "ChangedLocationName"
                controller.checkDbWhileAdding(locationToAdd3Dupl)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.similarLocationExists"))

                locationToAdd3Dupl.locationName = locationToAdd3.locationName
                locationToAdd3Dupl.city = "ChangedCity"
                locationToAdd3Dupl.street = "ChangedStreet"
                controller.checkDbWhileAdding(locationToAdd3Dupl)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.similarLocationExists"))


                //teraz sprawdzenie bazy podczas edycji (zaznaczam locationToAdd i próbuje zmienić jej dane na locationToAdd3)
                controller.lastSelectedLocation = locationToAdd
                locationToAdd = Location("tmpLoc2", "Wro", "Ryn11", "22-222")

                controller.checkDbWhileEditing(locationToAdd)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.locationAlreadyInDB"))

                locationToAdd.locationName = "AnotherName"
                controller.checkDbWhileEditing(locationToAdd)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.similarLocationExists"))

                locationToAdd.locationName = locationToAdd3.locationName
                locationToAdd.city = "AnotherCity"
                controller.checkDbWhileEditing(locationToAdd)
                assertTrue(MessageUtil.content==MessageBundle.getMess("warning.similarLocationExists"))

                locationToAdd = Location("tmpLoc1", "Wro", "Ryn10", "22-222")
                locationToAdd3 = Location("tmpLoc2", "Wro", "Ryn11", "22-222")

                controller.showLocations()
                val countBeforeDelete = controller.locationTableView.items.size

                //Po wszystkim usuń lokalizacje
                controller.wantToEdit = true
                controller.deleteLocation(locationToAdd)
                controller.deleteLocation(locationToAdd3)

                val countAfterDelete = controller.locationTableView.items.size
                assertEquals(countBeforeDelete-2, countAfterDelete)
            }
        }
    }

    //Test sprawdzający tworzenie menu kontekstowego
    @Test
    fun showMenuTest(){
        Platform.runLater {
            runBlocking {
                controller.showContextMenu(1, Location("LocTemp", "Wro", "Rynek 10", "11-111"))

                for (item in controller.menu.items) assertTrue(item.styleClass.contains("mfx-menu-item"))
                assertEquals((controller.menu.items[0] as MFXContextMenuItem).text, MessageBundle.getMess("label.deleteLocation"))
                assertEquals((controller.menu.items[1] as MFXContextMenuItem).text,MessageBundle.getMess("label.editLocation"))

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

    @Test
    fun createFormeditLocationTest()
    {
        Platform.runLater {
            runBlocking {
                val location = Location("TempLoc", "Wro", "Ryn10", "22-222")
                controller.editLocation(location)
                assertEquals(controller.locationNameTextFieldEdit.text, location.locationName)
                assertEquals(controller.cityTextFieldEdit.text, location.city)
                assertEquals(controller.streetTextFieldEdit.text, location.street)
                assertEquals(controller.postcodeTextFieldEdit.text, location.postcode)

                val dialog = (controller.dialog.content as MFXGenericDialog)
                val vbox:VBox = dialog.children[2] as VBox
                assertEquals((vbox.children[0] as Label).text, MessageBundle.getMess("label.updateLocation"))
            }
        }

    }
    }









