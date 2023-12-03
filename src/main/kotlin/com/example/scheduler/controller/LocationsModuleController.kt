package com.example.scheduler.controller

import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.exceptions.IllegalValueException
import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.LocationsModel
import com.example.scheduler.models.RoomsModel
import com.example.scheduler.objects.Location
import com.example.scheduler.objects.Room
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.controls.MFXStepper.MFXStepperEvent
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.filter.StringFilter
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.function.Function
import kotlin.reflect.KProperty1

/**
 * Klasa kontrolera modułu LocationModule do zarządzania lokalizacjami
 */
class LocationsModuleController: ILocationsModuleController, AdminTabsObserver, TabsObserver {

    /**
     * Przycisk służący do wyświetlania listy lokalizacji.
     */
    @FXML
    private lateinit var showLocationsButton: MFXButton

    /**
     * Tabela służąca do wyświetlania listy lokalizacji.
     */
    @FXML
    lateinit var locationTableView: MFXTableView<Location>

    /**
     * Stepper odpowiedzialny za proces dodawania lokalizacji wraz z walidacją danych.
     */
    @FXML
    private lateinit var stepper: MFXStepper

    /**
     * Flaga informująca o chęci edycji lokalizacji.
     */
    var wantToEdit = false

    /**
     * Flaga informująca o tym czy lokalizacja jest w trybie edycji
     */
    private var inEditMode = false

    /**
     * Flaga informująca o wyświetleniu okna dialogowego z komunikatem.
     */
    private var messageShown = false

    /**
     * Scena aplikacji
     */
    lateinit var stage: Stage

    /**
     * Etykieta informująca o kroku 1 podczas dodawania lokalizacji
     */
    private var fillDataLabel: Label = Label()

    /**
     * Pole tekstowe do wprowadzania nazwy lokalizacji podczas dodawania lokalizacji
     */
    private var locationNameTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania nazwy miasta podczas dodawania lokalizacji
     */
    private var cityTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania nazwy ulicy podczas dodawania lokalizacji
     */
    private var streetTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania kodu pocztowego podczas dodawania lokalizacji
     */
    private var postcodeTextField: MFXTextField = MFXTextField()

    /**
     * Etykieta informująca o kroku 2 podczas dodawania lokalizacji
     */
    private var uploadRoomsLabel: Label = Label()

    /**
     * Przycisk służący do ładowania pliku z salami
     */
    private var uploadButton: MFXButton = MFXButton(MessageBundle.getMess("label.loadFile"))

    /**
     * Przycisk typu toggle umożliwiający wybranie pliku z salami
     */
    private var uploadRoomsToggle: MFXRectangleToggleNode = MFXRectangleToggleNode()

    /**
     * Lista sal, które należy przypisać do dodawanej lokalizacji
     */
    var roomsToAdd: MutableList<Room> = mutableListOf()

    /**
     * Pole tekstowe do wprowadzania nazwy lokalizacji podczas edycji lokalizacji
     */
    var locationNameTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania nazwy miasta podczas edycji lokalizacji
     */
    var cityTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania nazwy ulicy podczas edycji lokalizacji
     */
    var streetTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania kodu pocztowego lokalizacji podczas edycji lokalizacji
     */
    var postcodeTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Etykieta informująca o edycji lokalizacji
     */
    private var editDataLabel: Label = Label()

    /**
     * Menu kontekstowe wyświetlające się po kliknięciu na wiersz tabeli z lokalizacjami
     */
    internal lateinit var menu: MFXContextMenu

    /**
     * Obiekt reprezentujący ostatnio wybraną lokalizację z tabeli
     */
    lateinit var lastSelectedLocation: Location

    /**
     * Pomocnicze okno dialogowe do menu kontekstowego.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Pomocnicze okno dialogowe do wyświetlania wiadomości.
     */
    lateinit var dialogMess: MFXStageDialog

    val locationsModel = LocationsModel()
    val roomsModel = RoomsModel()


    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize()
    {
        setupTable()
        AdminTabObserver.addObserver(this)
        TabObserver.addObserver(this)
        menu = MFXContextMenu(locationTableView)

        setOnLocationSelected()
        setupLocationForm()

        setConstraints(locationNameTextField, cityTextField, streetTextField, postcodeTextField)
        setConstraints(locationNameTextFieldEdit, cityTextFieldEdit, streetTextFieldEdit, postcodeTextFieldEdit)

        createSteps()
        stepper.styleClass.add("stepper")

        uploadRoomsToggle.setOnAction { ExcelUtils.searchFile(uploadRoomsToggle, uploadButton.scene.window as Stage) }
        showLocationsButton.setOnAction { showLocations() }
        uploadButton.setOnAction { uploadFileRooms(uploadRoomsToggle.text, locationNameTextField.text) }

        showLocationsButton.text = MessageBundle.getMess("label.showLocations")
    }


    /**
     * Obsługuje akcje wywoływane po kliknięciu na lokalizację w tabeli.
     */
    override fun setOnLocationSelected() {
        CommonUtils.setOnItemSelected(locationTableView, menu) { selectedRowIndex ->
            lastSelectedLocation = locationTableView.items[selectedRowIndex]
            showContextMenu(selectedRowIndex, lastSelectedLocation)
        }
    }

    /**
     * Konfiguruje pierwszy krok formularza dodawania lub edycji lokalizacji.
     */
    override fun setUpFirstStepForm() {
        fillDataLabel.text = MessageBundle.getMess("label.fillNameAndAddress")
        fillDataLabel.styleClass.add("header-label")

        editDataLabel.styleClass.add("header-label-big")
        editDataLabel.text = MessageBundle.getMess("label.updateLocation")

        locationNameTextField.floatingText = MessageBundle.getMess("label.enterLocationName")
        cityTextField.floatingText = MessageBundle.getMess("label.enterCity")
        streetTextField.floatingText = MessageBundle.getMess("label.enterStreet")
        postcodeTextField.floatingText = MessageBundle.getMess("label.enterPostcode")
        postcodeTextField.textLimit = 6

        locationNameTextFieldEdit.floatingText = locationNameTextField.floatingText
        cityTextFieldEdit.floatingText = cityTextField.floatingText
        streetTextFieldEdit.floatingText =  streetTextField.floatingText
        postcodeTextFieldEdit.floatingText = postcodeTextField.floatingText
        postcodeTextFieldEdit.textLimit = postcodeTextField.textLimit

        CommonUtils.setTextFieldStyle(locationNameTextField, cityTextField, streetTextField, postcodeTextField)
        CommonUtils.setTextFieldStyle(locationNameTextFieldEdit, cityTextFieldEdit, streetTextFieldEdit, postcodeTextFieldEdit)
    }

    /**
     * Konfiguruje drugi krok formularza dodawania lokalizacji.
     */
    override fun setUpSecondStepForm() {
        //step 2
        uploadRoomsLabel.text = MessageBundle.getMess("label.loadRoomsNowOrLater")
        uploadRoomsLabel.styleClass.add("header-label-small")
        uploadRoomsToggle.labelTrailingIcon = MFXFontIcon("fas-list-ul", 16.0, Color.BLACK)
        uploadRoomsToggle.text = MessageBundle.getMess("label.chooseFile")

        MFXTooltip.of(uploadRoomsToggle, MessageBundle.getMess("label.patternInfo")).install()

        uploadRoomsLabel.prefWidth = 380.0
        uploadRoomsToggle.prefWidth = 380.0

        uploadButton.id = "customButton"
    }

    /**
     * Konfiguruje formularz dodawania lub edycji lokalizacji.
     */
    override fun setupLocationForm() {
        setUpFirstStepForm()
        setUpSecondStepForm()
    }


    /**
     * Ustawia ograniczenia na pola tekstowe walidujące poprawność danych.
     *
     * @param locationName         Pole tekstowe do wprowadzania nazwy lokalizacji.
     * @param city                 Pole tekstowe do wprowadzania nazwy miasta.
     * @param street               Pole tekstowe do wprowadzania nazwy ulicy.
     * @param postcode             Pole tekstowe do wprowadzania kodu pocztowego.
     */
    override fun setConstraints(locationName: MFXTextField, city: MFXTextField, street: MFXTextField, postcode: MFXTextField) {
        locationName.validator.constraint(ValidatorUtil.createMoreThanOneLetterConstraint(locationName.textProperty(), MessageBundle.getMess("location.validation.moreThanOneLetter")))
        locationName.validator.constraint(ValidatorUtil.createLettersSmallExceptFirstConstraint(locationName.textProperty(), MessageBundle.getMess("location.validation.allLettersLowercaseExceptFirst")))
        locationName.validator.constraint(ValidatorUtil.createFirstLetterBigConstraint(locationName.textProperty(), MessageBundle.getMess("location.validation.startWithUppercase")))
        locationName.validator.constraint(ValidatorUtil.createNoSpecialCharsConstraint(locationName.textProperty(), MessageBundle.getMess("location.validation.noSpecialChars")))

        city.validator.constraint(ValidatorUtil.createMoreThanOneLetterConstraint(city.textProperty(), MessageBundle.getMess("city.validation.moreThanOneLetter")))
        city.validator.constraint(ValidatorUtil.createFirstLetterBigConstraint(city.textProperty(), MessageBundle.getMess("city.validation.startWithUppercase")))
        city.validator.constraint(ValidatorUtil.createNoSpecialCharsConstraintSpceAllowed(city.textProperty(),MessageBundle.getMess("city.validation.noSpecialChars") ))


        street.validator.constraint(ValidatorUtil.createMoreThanOneLetterConstraint(street.textProperty(), MessageBundle.getMess("street.validation.moreThanOneLetter")))
        street.validator.constraint(ValidatorUtil.createFirstLetterBigConstraint(street.textProperty(), MessageBundle.getMess("street.validation.startWithUppercase")))

        postcode.validator.constraint(ValidatorUtil.createPostalCodeConstraint(postcode.textProperty(), MessageBundle.getMess("postcode.validation.correctForm")))
        postcode.validator.constraint(ValidatorUtil.createNotEmptyConstraint(postcode.textProperty(), MessageBundle.getMess("postcode.validation.notEmpty")))
    }


    /**
     * Wczytuje sale z pliku Excel i dodaje je do listy sal do późniejszego dodania do lokalizacji.
     *
     * @param filePath Ścieżka do pliku Excel zawierającego dane o salach.
     * @param location Nazwa lokalizacji, do której należy przypisać wczytywane sale
     */
    override fun uploadFileRooms(filePath: String, location: String)
    {
        roomsToAdd.clear()
        if (filePath.isNotEmpty())
        {
            val file = File(filePath)
            var myWorkBook: XSSFWorkbook?=null
            var fis:FileInputStream?=null

            try {
                fis = FileInputStream(file)
                myWorkBook = XSSFWorkbook(fis)
                val sheet = myWorkBook.getSheetAt(0)

                var startRowNr = 1
                val startCellNr = 1

                while (sheet.getRow(startRowNr)!=null)
                {
                    val roomName = sheet.getRow(startRowNr).getCell(startCellNr).stringCellValue
                    val volume = sheet.getRow(startRowNr).getCell(startCellNr+1).numericCellValue.toInt()
                    val floor = sheet.getRow(startRowNr).getCell(startCellNr+2).numericCellValue.toInt()

                    if (roomName.isEmpty() || volume==0 || floor==0) throw java.lang.NullPointerException()
                    if (volume < 0 || floor<0) throw IllegalValueException(MessageBundle.getMess("warning.shouldBeGreaterThanZero"))

                    val roomToAdd = Room(
                        roomName = roomName,
                        volume = volume,
                        floor = floor,
                        location = location
                    )

                    roomsToAdd.add(roomToAdd)
                    startRowNr++
                }

                MessageUtil.showInfoMessage(MessageBundle.getMess("label.fileLoaded"), MessageBundle.getMess("success.rooms.fileLoadedCorrectly"))
            }
            //nieprawidłowa wartość
            catch (e: IllegalStateException)
            {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), MessageBundle.getMess("warning.incorrectExcelForm"))
            }
            //Pusta komórka
            catch (e: NullPointerException)
            {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), MessageBundle.getMess("warning.incorrectExcelForm"))
            }
            catch (e: IllegalValueException)
            {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), e.message!!)
            }
            catch (e: IOException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), MessageBundle.getMess("warning.fileAlreadyOpened"))
            }
            finally {
                myWorkBook!!.close()
                fis!!.close()
            }
        }
        else{
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.fileLoadingError"), MessageBundle.getMess("warning.shouldChooseFileFirst"))
        }
    }


    /**
     * Wyświetla listę lokalizacji w tabeli
     */
    override fun showLocations() {
        stage = showLocationsButton.scene.window as Stage

        val locations = locationsModel.getLocationsExceptPlatform()

        if (locations.isEmpty())
        {
            MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noLocations"), MessageBundle.getMess("warning.noLocationsToShow"))
            locationTableView.items.clear()
        }
        else locationTableView.items = locations
    }


    /**
     * Tworzy pierwszy krok formularza dodawania lokalizacji.
     */
    override fun createFirstStep(): MFXStepperToggle {
        val step1 = MFXStepperToggle(MessageBundle.getMess("label.data"), MFXFontIcon("fas-paperclip", 16.0, Color.web("#40F6F6")))

        val step1Box = VBox(
            15.0,
            fillDataLabel,
            ValidationWrapper.wrapNodeForValidationStepper(locationNameTextField, stepper),
            ValidationWrapper.wrapNodeForValidationStepper(cityTextField, stepper),
            ValidationWrapper.wrapNodeForValidationStepper(streetTextField, stepper),
            ValidationWrapper.wrapNodeForValidationStepper(postcodeTextField, stepper)
        )

        step1Box.alignment = Pos.CENTER
        step1.content = step1Box


        step1.validator
            .dependsOn(locationNameTextField.validator)
            .dependsOn(cityTextField.validator)
            .dependsOn(streetTextField.validator)
            .dependsOn(postcodeTextField.validator)

        return step1
    }

    /**
     * Tworzy drugi krok formularza dodawania lokalizacji.
     */
    override fun createSecondStep(): MFXStepperToggle {
        val step2 = MFXStepperToggle(MessageBundle.getMess("label.rooms"), MFXFontIcon("fas-pen", 16.0, Color.web("#40F6F6")))
        val step2Box = VBox(30.0, uploadRoomsLabel, uploadRoomsToggle, uploadButton)
        uploadRoomsToggle.text = ""

        step2Box.alignment = Pos.CENTER
        step2.content = step2Box

        return step2
    }

    /**
     * Tworzy kroki dla stepper procesu dodawania lokalizacji.
     */
    override fun createSteps() {
        val step1 = createFirstStep()
        val step2 = createSecondStep()

        stepper.stepperToggles.addAll(listOf(step1, step2))
        performActionsBetweenSteps()
    }

    /**
     * Obsługuje akcje wywoływane po pierwszym kroku podczas dodawania lokalizacji.
     */
    override fun handleActionsAfterFirstStep() {
        if (stepper.currentStepperNode == stepper.stepperToggles[1])
        {
            val location = Location(
                locationName = locationNameTextField.text,
                city = cityTextField.text,
                street = streetTextField.text,
                postcode = postcodeTextField.text
            )
            checkDbWhileAdding(location)
        }
    }


    /**
     * Metoda sprawdza czy w bazie nie istnieją już lokalizacje o podobnych danych podczas dodawania
     * @param location  Lokalizacja do dodania
     */
    fun checkDbWhileAdding(location: Location)
    {
        try {
            locationsModel.checkDBwhileAdding(location)
        }catch (e: DuplicatesException)
        {
            stepper.previous()
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.locationAddingError"), e.message!!)
        }
    }

    /**
     * Obsługuje akcje wywoływane po ostatnim kroku podczas dodawania lokalizacji.
     */
    override fun handleActionsAfterLastStep() {
        stepper.setOnLastNext {
            val location = Location(
                locationName = locationNameTextField.text,
                city = cityTextField.text,
                street = streetTextField.text,
                postcode = postcodeTextField.text
            )

            try {
                locationsModel.addLocation(location)
                val duplicatesExists = roomsModel.removeDuplicatesAndAddRooms(roomsToAdd)
                if (duplicatesExists) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.deleteDuplicates"), MessageBundle.getMess("warning.roomExistsInDB"))

                createCompletedStep()
                showLocations()

                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.location.correctlyAdded"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.addLocationError"))
                locationsModel.deleteLocation(location)
            }

        }
    }


    /**
     * Tworzy layout informujący o powodzeniu dodania lokalizacji
     */
    override fun createCompletedStep() {
        val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.locationAdded"))
        val resetButton = CommonUtils.createResetButton()

        resetButton.setOnAction {
            setOnResetButton()
        }

        val vbox = VBox(20.0, completedLabel, resetButton)
        vbox.alignment = Pos.CENTER
        (stepper.stepperToggles[1].content as VBox).children.setAll(vbox)
    }

    /**
     * Obsługuje akcje wywoływane po naciśnięciu przycisku "Reset" na formularzu dodawania lub edycji lokalizacji.
     */
    override fun setOnResetButton()
    {
        CommonUtils.removeStepDependencies(stepper, locationNameTextField, cityTextField, streetTextField, postcodeTextField)
        createSteps()
        clearAllLocationForm()
        roomsToAdd.clear()
    }


    /**
     * Wykonuje akcje między krokami formularza.
     */
    override fun performActionsBetweenSteps() {

        stepper.addEventHandler(MFXStepperEvent.NEXT_EVENT) {
            handleActionsAfterFirstStep()
            handleActionsAfterLastStep()
        }
    }

    /**
     * Czyści pola formularza
     */
    override fun clearAllLocationForm() {
        locationNameTextField.clear()
        cityTextField.clear()
        streetTextField.clear()
        postcodeTextField.clear()
    }

    /**
     * Tworzy i wyświetla menu kontekstowe dla wybranej lokalizacji z tabeli (oprócz Platformy).
     *
     * @param selectedRowIndex Indeks zaznaczonego wiersza w tabeli.
     * @param selectedItem       Wybrana lokalizacja.
     */
    override fun showContextMenu(selectedRowIndex: Int, selectedItem: Location) {

        val deleteButton = MFXContextMenuItem(MessageBundle.getMess("label.deleteLocation"))
        val editButton = MFXContextMenuItem(MessageBundle.getMess("label.editLocation"))

        deleteButton.graphic =  MFXFontIcon("fas-trash-can", 16.0, Color.BLACK)
        editButton.graphic = MFXFontIcon("fas-wrench", 16.0, Color.BLACK)

        deleteButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.location.askBeforeDelete"))
            deleteLocation(selectedItem)
        }

        editButton.setOnAction {
            inEditMode = true
            editLocation(selectedItem)
        }

        CommonUtils.showContextMenu(selectedRowIndex, locationTableView, menu, listOf(deleteButton,editButton))
    }


    /**
     * Usuwa lokalizację z bazy danych wraz z przypisanymi do niej salami.
     *
     * @param locationToDelete  lokalizacja do usunięcia z bazy danych.
     */
    override fun deleteLocation(locationToDelete: Location) {
        locationTableView.selectionModel.clearSelection()
        menu.hide()

        if (wantToEdit) {
            try {
                locationsModel.deleteLocation(locationToDelete)
                if (locationTableView.items.size>1) showLocations()
                else locationTableView.items.clear()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedLocation"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteLocationError"))
            }
        }

    }

    /**
     * Metoda sprawdza czy w bazie nie istnieją już lokalizacje o podobnych danych podczas edycji
     * @param location  Lokalizacja do edycji
     */
    fun checkDbWhileEditing(location: Location)
    {
        try {
            locationsModel.checkDBWhileEditing(location, lastSelectedLocation, messageShown)
            updateLocation(location)

        }catch (e: DuplicatesException)
        {
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.locationEditingError"), e.message!!)
        }
        catch (e: IdenticalObjectExistsException)
        {
            showDialogYesNoMessage(e.message!!)
            if (wantToEdit) dialog.close()
            inEditMode=false
        }
    }

    /**
     * Tworzy, konfiguruje i zwraca przycisk aktualizacji lokalizacji.
     *
     * @param errorLocation Etykieta do wyświetlania błędów związanych z polem lokalizacji.
     * @param errorCity Etykieta do wyświetlania błędów związanych z polem miasta.
     * @param errorStreet Etykieta do wyświetlania błędów związanych z polem ulicy.
     * @param errorPostcode Etykieta do wyświetlania błędów związanych z polem kodu pocztowego.
     * @return Przycisk aktualizacji lokalizacji.
     */
    override fun createUpdateButton(errorLocation: Label, errorCity: Label, errorStreet: Label, errorPostcode: Label): MFXButton {

        val updateButton = MFXButton(MessageBundle.getMess("label.update"))
        updateButton.id = "customButton"
        updateButton.prefWidth = 200.0

        updateButton.setOnAction {
            onUpdateButtonPressed(errorLocation, errorCity, errorStreet, errorPostcode)
        }

        return updateButton
    }

    /**
     * Ustawia walidacje na przycisku aktualizacji
     */
    fun onUpdateButtonPressed(errorLocation: Label, errorCity: Label, errorStreet: Label, errorPostcode: Label)
    {
        ValidationWrapper.validationAction(locationNameTextFieldEdit, errorLocation)
        ValidationWrapper.validationAction(cityTextFieldEdit, errorCity)
        ValidationWrapper.validationAction(streetTextFieldEdit, errorStreet)
        ValidationWrapper.validationAction(postcodeTextFieldEdit, errorPostcode)

        if (errorLocation.text.isEmpty() && errorCity.text.isEmpty() && errorStreet.text.isEmpty() &&errorPostcode.text.isEmpty()) {

            val location = Location(
                locationName = locationNameTextFieldEdit.text,
                city = cityTextFieldEdit.text,
                street = streetTextFieldEdit.text,
                postcode = postcodeTextFieldEdit.text
            )

            //Sprawdź czy lokalizacja o podanych danych istnieje w bazie (oprócz samej siebie)
            checkDbWhileEditing(location)
        }
    }

    /**
     * Tworzy i wyświetla formularz pozwalający na edycję nauczyciela.
     *
     * @param locationToEdit Wybrana lokalizacja.
     */
    override fun editLocation(locationToEdit: Location) {
        locationNameTextFieldEdit.text = locationToEdit.locationName
        cityTextFieldEdit.text = locationToEdit.city
        streetTextFieldEdit.text = locationToEdit.street
        postcodeTextFieldEdit.text = locationToEdit.postcode

        val errorLocation = ValidationWrapper.createErrorLabel()
        val errorCity = ValidationWrapper.createErrorLabel()
        val errorStreet = ValidationWrapper.createErrorLabel()
        val errorPostcode = ValidationWrapper.createErrorLabel()

        val updateButton = createUpdateButton(errorLocation, errorCity, errorStreet, errorPostcode)

        val vbox = VBox(20.0,
            editDataLabel,
            ValidationWrapper.createWrapper(locationNameTextFieldEdit, errorLocation),
            ValidationWrapper.createWrapper(cityTextFieldEdit, errorCity),
            ValidationWrapper.createWrapper(streetTextFieldEdit, errorStreet),
            ValidationWrapper.createWrapper(postcodeTextFieldEdit, errorPostcode),
            updateButton)

        vbox.alignment = Pos.CENTER
        createAndShowDialog(vbox)
    }

    /**
     * Aktualizuje lokalizację w bazie danych.
     *
     * @param location     lokalizacja do aktualizacji.
     */
    override fun updateLocation(location: Location) {
        val locationID = locationsModel.getLocationID(lastSelectedLocation)

        val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.locationUpdated"))

        val okButton = MFXButton("OK")
        okButton.id = "customButton"

        val vbox = VBox(20.0, completedLabel, okButton)
        vbox.alignment = Pos.CENTER

        okButton.setOnAction { dialog.close() }

        try {
            locationsModel.updateLocation(locationID, location)
            showLocations()
            (dialog.content as MFXGenericDialog).content = vbox
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.location.correctlyUpdated"))

        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.updateLocationError"))
        }
    }


    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     */
    fun showDialogYesNoMessage(content: String) {

        val buttons = listOf(
            MessageBundle.getMess("label.yes") to { wantToEdit = true },
            MessageBundle.getMess("label.no") to { wantToEdit = false}
        )

        dialogMess = DialogUtils.showMessageDialogWithButtons(content, showLocationsButton.scene.window as Stage, buttons)
        if (dialogMess.owner.isShowing) dialogMess.showAndWait()
    }

    /**
     * Tworzy i wyświetla customowe okno dialogowe.
     *
     * @param vBox Kontener z zawartością okna dialogowego.
     */
    override fun createAndShowDialog(vBox: VBox) {
        vBox.alignment = Pos.CENTER


        dialog = DialogUtils.showCustomDialog(showLocationsButton.scene.window as Stage, vBox) {
            locationTableView.selectionModel.clearSelection()
            menu.hide()
            messageShown=false
            inEditMode=false
        }

        if (dialog.owner.isShowing) dialog.showAndWait()
    }


    /**
     * Tworzy i konfiguruje tabelę z lokalizacjami.
     */
    override fun setupTable() {
        val columns = mapOf<MFXTableColumn<Location>, KProperty1<Location, *>>(
            MFXTableColumn(MessageBundle.getMess("label.name"), false, Comparator.comparing(Location::locationName)) to Location::locationName,
            MFXTableColumn(MessageBundle.getMess("label.city"), false, Comparator.comparing(Location::city)) to Location::city,
            MFXTableColumn(MessageBundle.getMess("label.street"), false, Comparator.comparing(Location::street)) to Location::street,
            MFXTableColumn(MessageBundle.getMess("label.postcode"), false, Comparator.comparing(Location::postcode)) to Location::postcode
        )
        
        columns.forEach{ column ->
            column.key.rowCellFactory = Function<Location, MFXTableRowCell<Location?, *>>
            {
                val cell = MFXTableRowCell<Location?, Any?>(column.value)
                cell.styleClass.add("table-cell")
                cell
            }
        }

        locationTableView.filters.addAll(
            StringFilter(MessageBundle.getMess("label.name"), Location::locationName),
            StringFilter(MessageBundle.getMess("label.city"), Location::city),
            StringFilter(MessageBundle.getMess("label.street"), Location::street),
            StringFilter(MessageBundle.getMess("label.postcode"), Location::postcode)
        )

        locationTableView.tableColumns.addAll(columns.keys)

        for (i in 0 until locationTableView.tableColumns.size) {
            locationTableView.tableColumns[i].styleClass.add("table-header")

        }
        locationTableView.tableColumns[0].minWidth = 150.0
        locationTableView.tableColumns[1].minWidth = 150.0
        locationTableView.tableColumns[2].minWidth = 150.0
        locationTableView.tableColumns[3].minWidth = 80.0
        locationTableView.isFooterVisible = true
    }


    /**
     * Metoda wywoływana podczas zmiany zakładek przed użytkownika - czyści panel.
     */
    override fun onTabsChanged() {
        locationTableView.items.clear()
        locationTableView.selectionModel.clearSelection()
        locationTableView.update()
        CommonUtils.removeStepDependencies(stepper, locationNameTextField, cityTextField, streetTextField, postcodeTextField)
        createSteps()
        clearAllLocationForm()
        roomsToAdd.clear()
    }
}