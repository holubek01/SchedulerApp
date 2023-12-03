package com.example.scheduler.controller

import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.LocationObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.LocationsModel
import com.example.scheduler.models.RoomsModel
import com.example.scheduler.objects.Room
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.filter.IntegerFilter
import io.github.palexdev.materialfx.filter.StringFilter
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.sql.SQLException
import java.util.function.Function
import kotlin.Comparator
import kotlin.reflect.KProperty1

/**
 * Klasa kontrolera modułu RoomModule do zarządzania danymi sal
 */
class RoomModuleController: IRoomModuleController, LocationObserver, AdminTabsObserver, TabsObserver {

    /**
     * Przycisk służący do wyświetlania listy sal w wybranej lokalizacji.
     */
    @FXML
    private lateinit var showRoomsButton: MFXButton

    /**
     * Stepper odpowiedzialny za proces dodawania sali wraz z walidacją danych.
     */
    @FXML
    lateinit var stepper: MFXStepper

    /**
     * Stepper odpowiedzialny za proces edycji sali wraz z walidacją danych.
     */
    var stepperEdit = MFXStepper()

    /**
     * Tabela służąca do wyświetlania listy sal w wybranej lokalizacji.
     */
    @FXML
    lateinit var roomsTableView: MFXTableView<Room>

    /**
     * Kontrolka umożliwiająca wybór lokalizacji do wyświetlenia sal
     */
    @FXML
    lateinit var locationChoiceBox: MFXComboBox<String>

    /**
     * Menu kontekstowe wyświetlające się po kliknięciu na wiersz tabeli z salami
     */
    lateinit var menu: MFXContextMenu

    /**
     * Flaga informująca o chęci edycji sali.
     */
    var wantToEdit = false

    /**
     * Pomocnicze okno dialogowe do menu kontekstowego.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Pomocnicze okno dialogowe do wyświetlania wiadomości.
     */
    lateinit var dialogMess: MFXStageDialog

    /**
     * Pole tekstowe do wprowadzania nazwy sali podczas dodawania nowej sali
     */
    private var roomNameTextField: MFXTextField = MFXTextField()

    /**
     * Kontrolka umożliwiająca wybór lokalizacji, do której chcemy dodać nową salę
     */
    private var locationStepperChoiceBox: MFXComboBox<String> = MFXComboBox()

    /**
     * Kontrolka umożliwiająca wybór pojemności sali (ile grup) podczas dodawania
     */
    private var volumeChoiceBox: MFXComboBox<Int> = MFXComboBox()

    /**
     * Kontrolka umożliwiająca wybór piętra dla sali podczas dodawania
     */
    private var floorChoiceBox: MFXComboBox<Int> = MFXComboBox()

    /**
     * Pole tekstowe do wprowadzania nazwy sali podczas edycji sali
     */
    private var roomNameTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Kontrolka umożliwiająca wybór pojemności sali (ile grup) podczas edycji sali
     */
    private var volumeChoiceBoxEdit: MFXComboBox<Int> = MFXComboBox()

    /**
     * Kontrolka umożliwiająca wybór piętra dla sali podczas edycji
     */
    private var floorChoiceBoxEdit: MFXComboBox<Int> = MFXComboBox()

    /**
     * Flaga informująca o tym czy sala jest w trybie edycji
     */
    var inEditMode = false

    /**
     * Flaga informująca o wyświetleniu okna dialogowego z komunikatem.
     */
    private var messageShown = false

    /**
     * Obiekt reprezentujący ostatnio wybraną salę z tabeli
     */
    lateinit var lastSelectedRoom: Room

    val roomsModel: RoomsModel = RoomsModel()
    val locationsModel: LocationsModel = LocationsModel()


    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize(){
        AdminTabObserver.addObserver(this)
        LocationsModel.addObserver(this)
        TabObserver.addObserver(this)

        setUpUI()
        setOnRoomSelected()
        onLocationsChanged()
        setConstraints(roomNameTextField)
        setConstraints(roomNameTextFieldEdit)

        setActions()

        createSteps(roomNameTextField, locationStepperChoiceBox, volumeChoiceBox, floorChoiceBox, stepper)
        createSteps(roomNameTextFieldEdit, locationStepperChoiceBox, volumeChoiceBoxEdit, floorChoiceBoxEdit, stepperEdit)
    }

    /**
     * Aktualizuje salę w bazie danych.
     *
     * @param roomID      Id edytowanej sali.
     * @param room        sala z nowymi danymi.
     */
    override fun updateRoom(roomID: Int, room: Room) {
        try {
            roomsModel.updateRoom(roomID, room)
            showRooms(room.location)

            MessageUtil.showInfoMessage(
                MessageBundle.getMess("label.roomUpdated"),
                MessageBundle.getMess("success.room.correctlyUpdated"),
            )
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.updateRoomError"))
        }

    }


    /**
     * Wyświetla listę sal z wybranej lokalizacji w tabeli
     * @param location Lokalizacja, z której sale mają zostać wyświetlone
     */
    override fun showRooms(location: String) {
        roomsTableView.items.clear()
        val rooms = roomsModel.getRooms(location)
        roomsTableView.items = rooms
        if (rooms.isEmpty()) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.noRooms"), MessageBundle.getMess("warning.noRoomsToShow"))
    }

    /**
     * Obsługuje akcje wywoływane po pierwszym kroku podczas dodawania lub edycji sali.
     *
     * @param roomName      Pole tekstowe do wprowadzania nazwy sali.
     * @param location      Kontrolka wyboru lokalizacji
     * @param stepper       Stepper dodawania lub edycji sali
     */
    override fun handleActionAfterFirstStep(roomName: MFXTextField, location: MFXComboBox<String>, stepper: MFXStepper)
    {
        if (stepper.currentStepperNode == stepper.stepperToggles[1])
        {
            if (this.stepper == stepper)
            {
                if (location.value.isNullOrEmpty())
                {
                    this.stepper.previous()
                    MessageUtil.showWarningMessage(MessageBundle.getMess("warning.fieldAddingError"), MessageBundle.getMess("warning.noLocation"))
                }
                else checkDb(location.value, roomName.text, stepper)
            }
            else checkDb(lastSelectedRoom.location, roomName.text, stepper)
        }
    }


    /**
     * Metoda sprawdzająca istnienie podobnych danych podczas dodawania lub edytowania sali
     * @param location Lokalizacja sali
     * @param roomName Nazwa sali
     * @param stepper Obiekt stepper (formularz) dodawania lub edytowania
     */
    fun checkDb(location: String, roomName: String, stepper: MFXStepper)
    {
        try {
            if (this.stepper == stepper) roomsModel.checkDB(location, roomName, inEditMode, messageShown)
            else roomsModel.checkDB(location, roomName, inEditMode, messageShown, lastSelectedRoom)
        }
        catch (e: DuplicatesException)
        {
            stepper.previous()
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.roomAddingError"), e.message!!)
        }
        catch (e: IdenticalObjectExistsException)
        {
            showDialogYesNoMessage(e.message!!)
            messageShown = true
            if (!wantToEdit) stepper.previous()
        }
    }


    /**
     * Wykonuje akcje między krokami formularza
     *
     * @param roomName      Pole tekstowe do wprowadzania nazwy sali.
     * @param location      Kontrolka wyboru lokalizacji
     * @param stepper       Stepper dodawania lub edycji sali
     */
    override fun performActionsBetweenSteps(stepper: MFXStepper, roomName: MFXTextField, location: MFXComboBox<String>)
    {
        stepper.addEventHandler(MFXStepper.MFXStepperEvent.NEXT_EVENT) {
            handleActionAfterFirstStep(roomName, location, stepper)
        }
    }


    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     */
    override fun showDialogYesNoMessage(content: String) {

        val stage: Stage = showRoomsButton.scene.window as Stage

        val buttons = listOf(
            MessageBundle.getMess("label.yes") to { wantToEdit = true },
            MessageBundle.getMess("label.no") to { wantToEdit = false}
        )

        dialogMess = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if (dialogMess.owner.isShowing) dialogMess.showAndWait()
    }


    /**
     * Tworzy i konfiguruje tabelę z salami.
     */
    override fun setupTable() {
        val columns = mapOf<MFXTableColumn<Room>, KProperty1<Room, *>>(
            MFXTableColumn(MessageBundle.getMess("label.roomName"), false, Comparator.comparing(Room::roomName)) to Room::roomName,
            MFXTableColumn(MessageBundle.getMess("label.volume"), false, Comparator.comparing(Room::volume)) to Room::volume,
            MFXTableColumn(MessageBundle.getMess("label.floor"), false, Comparator.comparing(Room::floor)) to Room::floor)


        columns.forEach{ column ->
            column.key.rowCellFactory = Function<Room, MFXTableRowCell<Room?, *>>
            {
                val cell = MFXTableRowCell<Room?, Any?>(column.value)
                cell.styleClass.add("table-cell")
                cell
            }
        }

        roomsTableView.filters.addAll(
            StringFilter(MessageBundle.getMess("label.roomName"), Room::roomName),
            IntegerFilter(MessageBundle.getMess("label.volume"), Room::volume),
            IntegerFilter(MessageBundle.getMess("label.floor"), Room::floor)
        )

        roomsTableView.tableColumns.addAll(columns.keys)

        for (i in 0 until roomsTableView.tableColumns.size) {
            roomsTableView.tableColumns[i].styleClass.add("table-header")
        }

        roomsTableView.tableColumns[1].minWidth = 200.0
        roomsTableView.isFooterVisible = true
    }

    /**
     * Dodaje nową salę do bazy danych
     * @param room     Sala do dodania.
     */
    override fun addRoom(room: Room) {
        try {
            roomsModel.addRoom(room)
            showRooms(room.location)
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.roomAdded"), MessageBundle.getMess("success.room.correctlyAdded"))

        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.addRoomError"))
        }

    }

    /**
     * Usuwa salę z bazy danych
     * @param room sala do usunięcia
     */
    override fun deleteRoom(room: Room) {
        roomsTableView.selectionModel.clearSelection()
        menu.hide()

        if (wantToEdit) {
            try {
                roomsModel.deleteRoom(room)
                if (roomsTableView.items.size>1) showRooms(room.location) else roomsTableView.items.clear()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedRoom"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteRoomError"))
            }
        }
    }

    /**
     * Wyświetla formularz pozwalający na edycję sali.
     *
     * @param room   Wybrana sala.
     */
    override fun editRoom(room: Room) {

        val vbox = VBox()
        vbox.children.addAll(stepperEdit)

        roomNameTextFieldEdit.text = room.roomName
        floorChoiceBoxEdit.text = room.floor.toString()
        floorChoiceBoxEdit.value = room.floor
        volumeChoiceBoxEdit.value = room.volume
        volumeChoiceBoxEdit.text = room.volume.toString()

        createAndShowDialog(vbox)
    }

    /**
     * Tworzy i wyświetla customowe okno dialogowe.
     *
     * @param vBox Kontener z zawartością okna dialogowego.
     */
    private fun createAndShowDialog(vBox: VBox) {
        vBox.alignment = Pos.CENTER

        val stage: Stage = showRoomsButton.scene.window as Stage
        val shouldBeBigger = vBox.children.contains(stepperEdit)
        dialog = DialogUtils.showCustomDialog(stage, vBox, shouldBeBigger)
        {
            roomsTableView.selectionModel.clearSelection()
            menu.hide()
            messageShown=false
            inEditMode=false

            CommonUtils.removeStepDependencies(stepperEdit, roomNameTextFieldEdit)
            createSteps(roomNameTextFieldEdit, locationStepperChoiceBox, volumeChoiceBoxEdit, floorChoiceBoxEdit, stepperEdit)
        }

        dialog.showAndWait()
    }

    /**
     * Tworzy i wyświetla menu kontekstowe dla wybranej sali z tabeli.
     *
     * @param selectedRowIndex Indeks zaznaczonego wiersza w tabeli.
     * @param selectedItem       Wybrana sala.
     */
    override fun showContextMenu(selectedRowIndex: Int, selectedItem: Room) {

        val deleteButton = MFXContextMenuItem(MessageBundle.getMess("label.deleteRoom"))
        val editButton = MFXContextMenuItem(MessageBundle.getMess("label.editRoom"))

        deleteButton.graphic =  MFXFontIcon("fas-trash-can", 16.0, Color.BLACK)
        editButton.graphic = MFXFontIcon("fas-wrench", 16.0, Color.BLACK)

        deleteButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.room.askBeforeDelete"))
            deleteRoom(selectedItem)
        }

        editButton.setOnAction {
            inEditMode = true
            editRoom(selectedItem)
        }

        CommonUtils.showContextMenu(selectedRowIndex, roomsTableView, menu, listOf(deleteButton,editButton))
    }

    /**
     * Konfiguruje pierwszy krok formularza dodawania lub edycji sali.
     * @param roomName          Pole tekstowe do wprowadzania nazwy sali.
     * @param locationBox       Kontrolka wyboru lokalizacji
     */
    override fun setUpFirstStepForm(roomName: MFXTextField, locationBox: MFXComboBox<String>)
    {
        //step 1: podaj nazwę sali i lokalizację
        roomName.floatingText = MessageBundle.getMess("label.enterRoomName")
        locationBox.floatingText = MessageBundle.getMess("label.enterLocation")
        locationBox.items = locationsModel.getLocationsNamesExceptPlatform()
    }

    /**
     * Konfiguruje drugi krok formularza dodawania lub edycji sali.
     * @param volumeBox          Kontrolka wyboru pojemności sali
     * @param floorBox           Kontrolka wyboru piętra
     */
    override fun setUpSecondStepForm(volumeBox: MFXComboBox<Int>, floorBox: MFXComboBox<Int>)
    {
        //step 2: podaj piętro i pojemność
        volumeBox.floatingText = MessageBundle.getMess("label.enterRoomVolume")
        floorBox.floatingText = MessageBundle.getMess("label.enterFloor")

        volumeBox.items = FXCollections.observableArrayList((1..6).toList())
        floorBox.items = FXCollections.observableArrayList((1..10).toList())
    }

    /**
     *  Konfiguruje formularz dodawania lub edycji sali.
     *  @param roomName       Pole tekstowe do wprowadzania nazwy sali.
     *  @param locationBox    Kontrolka wyboru lokalizacji
     *  @param volumeBox      Kontrolka wyboru pojemności sali
     *  @param floorBox       Kontrolka wyboru piętra
     */
    override fun setupRoomForm(
        roomName: MFXTextField,
        locationBox: MFXComboBox<String>,
        floorBox: MFXComboBox<Int>,
        volumeBox: MFXComboBox<Int>
    ) {
        setUpFirstStepForm(roomName, locationBox)
        setUpSecondStepForm(volumeBox, floorBox)

        CommonUtils.setTextFieldStyle(roomNameTextField, locationStepperChoiceBox, volumeChoiceBox, floorChoiceBox)
        CommonUtils.setTextFieldStyle(roomNameTextFieldEdit, volumeChoiceBoxEdit, floorChoiceBoxEdit)
    }

    /**
     * Ustawia ograniczenia na pola tekstowe walidujące poprawność danych.
     *
     * @param roomName         Pole tekstowe do wprowadzania nazwy lokalizacji.
     */
    override fun setConstraints(roomName: MFXTextField) {
        roomName.validator.constraint(ValidatorUtil.createNotEmptyConstraint(roomName.textProperty(), MessageBundle.getMess("roomName.validation.notEmpty")))
        roomName.validator.constraint(ValidatorUtil.createNoLongerThanThirtyLettersConstraint(roomName.textProperty(), MessageBundle.getMess("roomName.validation.noLongerThan30") ))
        roomName.validator.constraint(ValidatorUtil.createNoSpecialCharsExceptDigitsConstraint(roomName.textProperty(), MessageBundle.getMess("roomName.validation.noSpecialChars")))
    }

    /**
     * Obsługuje akcje wywoływane po kliknięciu na salę w tabeli.
     */
    override fun setOnRoomSelected() {
        CommonUtils.setOnItemSelected(roomsTableView, menu) { selectedRowIndex ->
            lastSelectedRoom = roomsTableView.items[selectedRowIndex]
            showContextMenu(selectedRowIndex,roomsTableView.items[selectedRowIndex])
        }
    }

    /**
     * Metoda wywoływana podczas zmiany tabeli lokalizacji w bazie danych.
     */
    override fun onLocationsChanged() {
        locationChoiceBox.items = locationsModel.getLocationsNamesExceptPlatform()
        locationStepperChoiceBox.items = locationChoiceBox.items
    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przez użytkownika - czyści panel.
     */
    override fun onTabsChanged() {

        locationChoiceBox.clear()
        locationChoiceBox.clearSelection()
        roomsTableView.items.clear()

        CommonUtils.removeStepDependencies(stepper, roomNameTextField)
        createSteps(roomNameTextField, locationStepperChoiceBox, volumeChoiceBox, floorChoiceBox, stepper)
        clearBoxes()
    }

    /**
     * Konfiguruje komponenty GUI
     */
    override fun setUpUI()
    {
        menu = MFXContextMenu(roomsTableView)
        setupRoomForm(roomNameTextField, locationStepperChoiceBox, floorChoiceBox, volumeChoiceBox)
        setupRoomForm(roomNameTextFieldEdit, locationStepperChoiceBox, floorChoiceBoxEdit, volumeChoiceBoxEdit)
        setupTable()
        roomsTableView.prefWidth = 300.0
        stepper.styleClass.add("stepper")
        stepperEdit.styleClass.add("stepper")
        locationChoiceBox.floatingText = MessageBundle.getMess("label.chooseLocation")
        showRoomsButton.text = MessageBundle.getMess("label.showRooms")
    }

    /**
     * Ustawia akcje na przyciskach
     */
    override fun setActions()
    {
        showRoomsButton.setOnAction {
            if(!locationChoiceBox.value.isNullOrBlank()) showRooms(locationChoiceBox.value)
            else MessageUtil.showInfoMessage(MessageBundle.getMess("warning.showRoomsError"), MessageBundle.getMess("warning.noLocation"))
        }
    }

    /**
     * Tworzy pierwszy krok formularza dodawania lub edycji sali.
     *
     * @param roomName       Pole tekstowe do wprowadzania nazwy sali.
     * @param stepperr       Stepper dodawania lub edycji sali.
     * @param locationBox    Kontrolka wyboru lokalizacji
     * @return               Pierwszy krok formularza.
     */
    override fun createStep1(roomName: MFXTextField, stepperr: MFXStepper, locationBox: MFXComboBox<String>): MFXStepperToggle {
        val step1 = MFXStepperToggle("${MessageBundle.getMess("label.data")} 1", MFXFontIcon("fas-paperclip", 16.0, Color.web("#40F6F6")))

        val step1Box = if(stepperr==stepper) VBox(20.0, ValidationWrapper.wrapNodeForValidationStepper(roomName, stepperr), locationBox) else VBox(10.0, ValidationWrapper.wrapNodeForValidationStepper(roomName, stepperr))
        locationBox.prefWidth = 300.0

        step1Box.alignment = Pos.CENTER
        step1.content = step1Box

        step1.validator.dependsOn(roomName.validator)

        return step1
    }

    /**
     * Tworzy drugi krok formularza dodawania lub edycji sali.
     *
     * @param volumeBox      Kontrolka wyboru pojemności sali.
     * @param floorBox       Kontrolka wyboru piętra /
     * @return               Drugi krok formularza.
     */
    override fun createStep2(volumeBox: MFXComboBox<Int>, floorBox: MFXComboBox<Int>): MFXStepperToggle {
        val step2 = MFXStepperToggle("${MessageBundle.getMess("label.data")} 2", MFXFontIcon("fas-pen", 16.0, Color.web("#40F6F6")))
        val step2Box = VBox(30.0, volumeBox, floorBox)
        floorBox.prefWidth = 300.0
        volumeBox.prefWidth = 300.0
        step2Box.alignment = Pos.CENTER
        step2.content = step2Box
        step2Box.padding = Insets(30.0)

        return step2
    }

    /**
     * Tworzy layout informujący o powodzeniu dodania lub edycji nauczyciela
     *  @param roomName       Pole tekstowe do wprowadzania nazwy sali.
     *  @param locationBox    Kontrolka wyboru lokalizacji
     *  @param volumeBox      Kontrolka wyboru pojemności sali
     *  @param floorBox       Kontrolka wyboru piętra
     *  @param stepperr       Stepper dodawania lub edycji sali.
     *  @param step2          Drugi krok formularza.
     */
    override fun createCompletedStep(
        roomName: MFXTextField,
        locationBox: MFXComboBox<String>,
        volumeBox: MFXComboBox<Int>,
        floorBox: MFXComboBox<Int>,
        stepperr: MFXStepper,
        step2: MFXStepperToggle
    ) {
        val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.roomAdded"))
        val resetButton = CommonUtils.createResetButton()

        resetButton.setOnAction {
            CommonUtils.removeStepDependencies(stepperr, roomName)
            createSteps(roomName, locationBox, volumeBox, floorBox, stepperr)
            clearBoxes()
            if (inEditMode){
                dialog.close()
                inEditMode=false
            }
        }


        stepperr.setOnLastNext {

            if (volumeBox.value!=null && floorBox.value!=null )
            {
                val room = Room(
                    roomName = roomName.text,
                    volume = volumeBox.value,
                    floor = floorBox.value,
                    location = if (stepperr==stepper) locationBox.value else lastSelectedRoom.location
                )

                if (inEditMode)
                {
                    val roomID = roomsModel.getRoomID(lastSelectedRoom)
                    completedLabel.text = MessageBundle.getMess("label.roomUpdated")
                    updateRoom(roomID, room)
                }
                else
                {
                    addRoom(room)
                }

                val vbox = step2.content as VBox
                vbox.children.setAll(completedLabel, resetButton)
            }
            else{
                stepperr.previous()
                MessageUtil.showWarningMessage(MessageBundle.getMess("warning.fieldAddingError"), MessageBundle.getMess("warning.floorOrVolumeEmpty"))
            }
        }
    }

    /**
     * Tworzy kroki dla stepper procesu dodawania lub edycji sali.
     *  @param roomName       Pole tekstowe do wprowadzania nazwy sali.
     *  @param locationBox    Kontrolka wyboru lokalizacji
     *  @param volumeBox      Kontrolka wyboru pojemności sali
     *  @param floorBox       Kontrolka wyboru piętra
     *  @param stepperr       Stepper dodawania lub edycji sali.
     */
    override fun createSteps(
        roomName: MFXTextField,
        locationBox: MFXComboBox<String>,
        volumeBox: MFXComboBox<Int>,
        floorBox: MFXComboBox<Int>,
        stepperr: MFXStepper){

        val step1 = createStep1(roomName, stepperr, locationBox)
        val step2 = createStep2(volumeBox, floorBox)
        createCompletedStep(roomName, locationBox, volumeBox, floorBox, stepperr, step2)

        stepperr.stepperToggles.addAll(listOf(step1, step2))
        performActionsBetweenSteps(stepperr, roomName, locationBox)
    }


    /**
     * Czyści pola tekstowe formularza.
     */
    override fun clearBoxes()
    {
        roomNameTextField.clear()
        CommonUtils.clearBox(locationChoiceBox)
        CommonUtils.clearBox(volumeChoiceBox)
        CommonUtils.clearBox(floorChoiceBox)
        CommonUtils.clearBox(locationStepperChoiceBox)
    }


}