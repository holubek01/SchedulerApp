package com.example.scheduler.controller

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.FieldsModel
import com.example.scheduler.objects.*
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.filter.StringFilter
import io.github.palexdev.materialfx.filter.IntegerFilter
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import java.sql.SQLException
import java.util.function.Function
import kotlin.Comparator
import kotlin.reflect.KProperty1

/**
 * Klasa kontrolera modułu FieldModule do zarządzania danymi kierunków
 */
class FieldsModuleController: IFieldsModuleController, AdminTabsObserver, TabsObserver {

    /**
     * Przycisk do wyświetlania kierunków kształcenia w tabeli
     */
    @FXML
    private lateinit var showFieldsButton: MFXButton

    /**
     * Stepper odpowiedzialny za proces dodawania kierunku wraz z walidacją danych.
     */
    @FXML
    lateinit var stepper: MFXStepper

    /**
     * Tabela służąca do wyświetlania listy kierunków.
     */
    @FXML
    lateinit var fieldsTableView: MFXTableView<Field>

    /**
     * Obiekt reprezentujący ostatnio wybrany kierunek z tabeli
     */
    lateinit var lastSelectedField: Field

    /**
     * Menu kontekstowe wyświetlające się po kliknięciu na wiersz tabeli z kierunkami
     */
    lateinit var menu: MFXContextMenu

    /**
     * Flaga informująca o chęci edycji kierunku.
     */
    private var wantToEdit = false

    /**
     * Flaga informująca o chęci usunięcia kierunku.
     */
    private var wantToDelete = false

    /**
     * Pomocnicze okno dialogowe do menu kontekstowego.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Pomocnicze okno dialogowe do wyświetlania wiadomości.
     */
    lateinit var dialogMess: MFXStageDialog

    /**
     * Pole tekstowe do wprowadzania nazwy kierunku podczas dodawania kierunku
     */
    private var fieldNameTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania skrótu nazwy kierunku podczas dodawania kierunku
     */
    private var shortcutTextField: MFXTextField = MFXTextField()

    /**
     * Pole do wyboru liczby semestrów podczas dodawania kierunku
     */
    var semChoiceBox: MFXComboBox<Int> = MFXComboBox()

    /**
     * Pole tekstowe do wprowadzania nazwy kierunku podczas edycji kierunku
     */
    var fieldNameTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania skrótu nazwy kierunku podczas edycji kierunku
     */
    var shortcutTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Etykieta ładowania szkolnych planów nauczania
     */
    private var uploadSPNLabel: Label = Label()

    /**
     * Kontrolka służąca do ładowania planów nauczania
     */
    private var uploadSPNToggle: MFXRectangleToggleNode = MFXRectangleToggleNode()

    /**
     * Lista obiektów reprezentujących przedmioty do dodania do szkolnego planu
     * nauczania wraz z liczbą godzin na poszczególnych semestrach
     */
    var subjectsToAdd: MutableList<Subject> = mutableListOf()

    /**
     * Lista obiektów MFXComboBox<Int> służących do zaznaczania liczby godzin na poszczególnych semestrach
     */
    var groupNumberComboBoxArray: MutableList<MFXComboBox<Int>> = mutableListOf()

    /**
     * Flaga informująca o tym czy kierunek jest w trybie edycji
     */
    private var inEditMode = false

    val fieldsModel = FieldsModel()


    /**
     * Inicjalizuje kontroler
     */
    @FXML
    fun initialize()
    {
        setupTable()
        setupFieldForm()
        setActions()
        setConstraints(fieldNameTextField, shortcutTextField)
        setConstraints(fieldNameTextFieldEdit, shortcutTextFieldEdit)
        menu = MFXContextMenu(fieldsTableView)
        setOnFieldSelected()
        createSteps()
        showFieldsButton.text = MessageBundle.getMess("label.showFields")
        AdminTabObserver.addObserver(this)
        TabObserver.addObserver(this)
    }


    /**
     * Dodaje kierunek kształcenia do bazy wraz z przypisanym szkolnym planem nauczania
     * @param   field   Kierunek do dodania
     */
    override fun addField(field: Field) {
        try {
            fieldsModel.addField(field, groupNumberComboBoxArray, subjectsToAdd)
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.fieldAdded"), MessageBundle.getMess("success.field.correctlyAdded"))

        } catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.fieldAddingError"))
            fieldsModel.deleteField(field)
        }
        showFields()
    }

    /**
     * Tworzy pierwszy krok formularza dodawania kierunku.
     */
    override fun createFirstsStep(): MFXStepperToggle
    {
        val step1 = MFXStepperToggle(MessageBundle.getMess("label.data"), MFXFontIcon("fas-paperclip", 16.0, Color.web("#40F6F6")))
        semChoiceBox.prefWidth = 300.0

        val step1Box = VBox(20.0,
            ValidationWrapper.wrapNodeForValidationStepper(fieldNameTextField,stepper),
            ValidationWrapper.wrapNodeForValidationStepper(shortcutTextField,stepper),
            semChoiceBox)

        step1Box.alignment = Pos.CENTER
        step1.content = step1Box
        step1.validator
            .dependsOn(fieldNameTextField.validator)
            .dependsOn(shortcutTextField.validator)

        return step1
    }

    /**
     * Tworzy drugi krok formularza dodawania kierunku.
     */
    override fun createSecondStep(): MFXStepperToggle
    {
        val step2 = MFXStepperToggle(MessageBundle.getMess("label.groups"), MFXFontIcon("fas-people-group", 16.0, Color.web("#40F6F6")))
        val step2Box = VBox(30.0)
        step2Box.alignment = Pos.CENTER
        step2.content = step2Box
        step2Box.padding = Insets(30.0)

        return step2
    }

    /**
     * Tworzy trzeci krok formularza dodawania kierunku.
     */
    override fun createThirdStep(): MFXStepperToggle
    {
        val step3 = MFXStepperToggle(MessageBundle.getMess("label.subjects"), MFXFontIcon("fas-file-circle-plus", 16.0, Color.web("#40F6F6")))

        MFXTooltip.of(uploadSPNToggle, MessageBundle.getMess("label.SPN.patternInfo")).install()
        val step3Box = VBox(20.0, uploadSPNLabel, uploadSPNToggle)
        step3Box.alignment = Pos.CENTER
        step3.content = step3Box
        step3Box.padding = Insets(30.0)

        return step3
    }

    /**
     * Tworzy groupNumberComboBoxArray i dodaje ją do kroku 2 dodawania kierunku
     */
    fun createGroupNumberArray()
    {
        for (i in 1 .. semChoiceBox.value)
        {
            val box = MFXComboBox<Int>()
            box.floatingText = "${MessageBundle.getMess("label.enterHoursNum")} $i"
            box.id = "comboWhite"
            box.items = FXCollections.observableArrayList((1..15).toList())
            groupNumberComboBoxArray.add(box)
        }

        val comboBoxContainer = VBox(20.0)
        comboBoxContainer.children.addAll(groupNumberComboBoxArray)
        comboBoxContainer.alignment = Pos.CENTER
        stepper.stepperToggles[1].content = comboBoxContainer    }

    /**
     * Tworzy kroki dla stepper procesu dodawania kierunku.
     */
    override fun createSteps() {
        val step1 = createFirstsStep()
        val step2 = createSecondStep()
        val step3 = createThirdStep()

        val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.fieldAdded"))
        val resetButton = CommonUtils.createResetButton()

        stepper.setOnBeforePrevious {
            if (stepper.currentStepperNode == stepper.stepperToggles[1])
            {
                groupNumberComboBoxArray.clear()
            }
        }

        stepper.setOnBeforeNext{

            if (stepper.currentStepperNode == stepper.stepperToggles[0] && semChoiceBox.value!=null)
            {
                groupNumberComboBoxArray.clear()
                createGroupNumberArray()
            }

        }

        resetButton.setOnAction {
            CommonUtils.removeStepDependencies(stepper, fieldNameTextField, shortcutTextField)
            createSteps()
            clearBoxes()
            inEditMode=false
        }

        stepper.setOnLastNext {
            if (uploadSPNToggle.text.isNotEmpty() && uploadSPNToggle.isSelected)
            {
                subjectsToAdd.clear()
                ExcelUtils.uploadSingleSPN(uploadSPNToggle.text, subjectsToAdd)

                val field = Field(
                    fieldName = fieldNameTextField.text,
                    shortcut = shortcutTextField.text,
                    semsNumber = semChoiceBox.value
                )

                addField(field)

                val vbox = step3.content as VBox
                vbox.children.setAll(completedLabel, resetButton)
            }
            else
            {
                stepper.previous()
                MessageUtil.showWarningMessage(MessageBundle.getMess("warning.noSPN"), MessageBundle.getMess("warning.noSPNSelected"))
            }
            uploadSPNToggle.isSelected = false
        }

        stepper.stepperToggles.addAll(listOf(step1, step2, step3))
        performActionsBetweenSteps()
    }

    /**
     * Obsługuje akcje wywoływane po pierwszym kroku podczas dodawania kierunku.
     */
    override fun handleActionAfterFirstStep()
    {
        if (stepper.currentStepperNode == stepper.stepperToggles[1])
        {
            checkDbWhileAdding(fieldNameTextField.text, shortcutTextField.text, semChoiceBox)
        }
    }

    /**
     * Metoda sprawdza istnienie w bazie kierunku o podobnych danych podczas dodawania
     * Jeśli walidacja przebiegła pomyślnie to kierunek jest dodawany
     * @param fieldName     nazwa kierunku do dodania
     * @param shotcut       skrót kierunku do dodania
     * @param sem           Liczba semestrów na kierunku do dodania
     */
    override fun checkDbWhileAdding(fieldName: String, shotcut: String, sem: MFXComboBox<Int>)
    {
        try {
            fieldsModel.checkDBWhileAdding(fieldName, shotcut, sem)
        }catch (e: DuplicatesException)
        {
            stepper.previous()
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.fieldAddingError"), e.message!!)
        }
    }

    /**
     * Obsługuje akcje wywoływane po drugim kroku podczas dodawania kierunku.
     */
    override fun handleActionAfterSecondStep()
    {
        if (stepper.currentStepperNode == stepper.stepperToggles[2])
        {
            checkIfSemComboBoxEmpty()
        }

    }

    /**
     * Metoda sprawdzająca, czy użytkownik wybrał liczbę grup dla każdego semestru
     * Jeśli nie - cofa do poprzedniego kroku
     */
    override fun checkIfSemComboBoxEmpty()
    {
        val anyComboBoxEmpty = groupNumberComboBoxArray.any { it.value == null }
        if (anyComboBoxEmpty)
        {
            stepper.previous()
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.fieldAddingError"), MessageBundle.getMess("warning.noGroupsNumber"))
        }
    }

    /**
     * Wykonuje akcje między krokami formularza.
     */
    override fun performActionsBetweenSteps()
    {
        stepper.addEventHandler(MFXStepper.MFXStepperEvent.NEXT_EVENT) {
            handleActionAfterFirstStep()
            handleActionAfterSecondStep()
        }
    }


    /**
     * Ustawia ograniczenia na pola tekstowe walidujące poprawność danych.
     *
     * @param fieldName         Pole tekstowe do wprowadzania nazwy kierunku.
     * @param fieldShort        Pole tekstowe do wprowadzania skrótu kierunku.
     */
    override fun setConstraints(fieldName: MFXTextField, fieldShort: MFXTextField) {
        fieldName.validator.constraint(ValidatorUtil.createNotEmptyConstraint(fieldName.textProperty(), MessageBundle.getMess("fieldName.validation.notEmpty")))
        fieldName.validator.constraint(ValidatorUtil.createNoSpecialCharsConstraintSpceAllowed(fieldName.textProperty(),MessageBundle.getMess("fieldName.validation.noSpecialChars") ))
        fieldShort.validator.constraint(ValidatorUtil.createNotEmptyConstraint(fieldShort.textProperty(),MessageBundle.getMess("shortcut.validation.notEmpty") ))
        fieldShort.validator.constraint(ValidatorUtil.createNoLongerThanSixLettersConstraint(fieldShort.textProperty(),MessageBundle.getMess("shortcut.noLongerThan6Chars") ))
        fieldShort.validator.constraint(ValidatorUtil.createOnlyBigLettersConstraint(fieldShort.textProperty(), MessageBundle.getMess("shortcut.onlyBigLetters")))
    }


    /**
     * Konfiguruje formularz dodawania kierunku.
     */
    override fun setupFieldForm() {
        stepper.styleClass.add("stepper")
        fieldNameTextField.floatingText = MessageBundle.getMess("label.enterFieldName")
        shortcutTextField.floatingText = MessageBundle.getMess("label.enterFieldShortcut")
        semChoiceBox.floatingText = MessageBundle.getMess("label.enterSemNum")

        semChoiceBox.id = "biggerIconCombo"
        semChoiceBox.items = FXCollections.observableArrayList((1..6).toList())

        CommonUtils.setTextFieldStyle(fieldNameTextField, shortcutTextField, semChoiceBox)

        uploadSPNLabel.text = MessageBundle.getMess("label.enterSPN")
        uploadSPNLabel.textAlignment = TextAlignment.CENTER
        uploadSPNLabel.styleClass.add("header-label-big")
        uploadSPNToggle.labelTrailingIcon = MFXFontIcon("fas-list-ul", 16.0, Color.BLACK)
        uploadSPNToggle.text = MessageBundle.getMess("label.chooseFile")

        uploadSPNLabel.prefWidth = 380.0
        uploadSPNToggle.prefWidth = 380.0

    }

    /**
     * Tworzy i konfiguruje tabelę z kierunkami.
     */
    override fun setupTable() {
        val columns = mapOf<MFXTableColumn<Field>, KProperty1<Field, *>>(
            MFXTableColumn(MessageBundle.getMess("label.fieldName"), false, Comparator.comparing(Field::fieldName)) to Field::fieldName,
            MFXTableColumn(MessageBundle.getMess("label.semCount"), false, Comparator.comparing(Field::semsNumber)) to Field::semsNumber,
            MFXTableColumn(MessageBundle.getMess("label.shortcut"), false, Comparator.comparing(Field::shortcut)) to Field::shortcut)


        columns.forEach{ column ->
            column.key.rowCellFactory = Function<Field, MFXTableRowCell<Field?, *>>
            {
                val cell = MFXTableRowCell<Field?, Any?>(column.value)
                cell.styleClass.add("table-cell")
                cell
            }
        }

        addFilters()

        fieldsTableView.tableColumns.addAll(columns.keys)

        for (i in 0 until fieldsTableView.tableColumns.size) {
            fieldsTableView.tableColumns[i].styleClass.add("table-header")
        }

        fieldsTableView.tableColumns[0].minWidth = 220.0
        fieldsTableView.tableColumns[1].minWidth = 150.0
        fieldsTableView.tableColumns[2].minWidth = 70.0
        fieldsTableView.isFooterVisible = true

    }

    /**
     * Tworzy filtry dla tabeli
     */
    private fun addFilters()
    {
        fieldsTableView.filters.addAll(
            StringFilter(MessageBundle.getMess("label.fieldName"), Field::fieldName),
            IntegerFilter(MessageBundle.getMess("label.semCount"), Field::semsNumber),
            StringFilter(MessageBundle.getMess("label.shortcut"), Field::shortcut),
        )
    }

    /**
     * Ustawia akcje dla kontrolek
     */
    override fun setActions()
    {
        showFieldsButton.setOnAction { showFields() }
        uploadSPNToggle.setOnAction {
            val stage = showFieldsButton.scene.window as Stage
            ExcelUtils.searchFile(uploadSPNToggle, stage ) }

    }

    /**
     * Obsługuje akcje wywoływane po kliknięciu na kierunek w tabeli.
     */
    override fun setOnFieldSelected() {
        CommonUtils.setOnItemSelected(fieldsTableView, menu) { selectedRowIndex ->
            lastSelectedField = fieldsTableView.items[selectedRowIndex]
            showContextMenu(selectedRowIndex, lastSelectedField)
        }
    }


    /**
     * Wyświetla menu kontekstowe dla wybranego kierunku z tabeli.
     *
     * @param selectedRowIndex Indeks zaznaczonego wiersza w tabeli.
     * @param selectedItem       Wybrany kierunek.
     */
    override fun showContextMenu(selectedRowIndex: Int, selectedItem: Field) {

        val deleteButton = MFXContextMenuItem(MessageBundle.getMess("label.deleteField"))
        val editButton = MFXContextMenuItem(MessageBundle.getMess("label.editField"))
        val showSPNButton = MFXContextMenuItem(MessageBundle.getMess("label.showSPN"))

        deleteButton.graphic =  MFXFontIcon("fas-trash-can", 16.0, Color.BLACK)
        editButton.graphic = MFXFontIcon("fas-wrench", 16.0, Color.BLACK)
        showSPNButton.graphic = MFXFontIcon("fas-paperclip", 16.0, Color.BLACK)

        deleteButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.field.askBeforeDelete"), ActionType.DELETE)
            deleteField(selectedItem)
        }

        editButton.setOnAction {
            inEditMode = true
            editField(selectedItem)
        }

        showSPNButton.setOnAction {
            showSPN(selectedItem)
        }

        CommonUtils.showContextMenu(selectedRowIndex, fieldsTableView, menu, listOf(deleteButton,editButton,showSPNButton))
    }


    /**
     * Tworzy tabelę do wyświetlania szkolnego planu nauczania dla wybranego kierunku.
     * @param field Kierunek, dla którego ma zostać utworzona tabela.
     * @return Tabela do wyświetlania szkolnego planu nauczania dla wybranego kierunku
     */
    override fun createSPNtable(field: Field): MFXTableView<TeachingPlan>
    {
        val spnTableView = MFXTableView<TeachingPlan>()
        spnTableView.isFooterVisible = false
        spnTableView.stylesheets.add(SchedulerApp::class.java.getResource("css/customStyles.css")?.toExternalForm()!!)

        spnTableView.maxWidth = Double.MAX_VALUE
        spnTableView.maxHeight = Double.MAX_VALUE
        VBox.setVgrow(spnTableView, Priority.ALWAYS)

        val columns: ObservableList<MFXTableColumn<TeachingPlan>> = FXCollections.observableArrayList()
        val subjectNameColumn: MFXTableColumn<TeachingPlan> = MFXTableColumn(MessageBundle.getMess("label.subject"), true, java.util.Comparator.comparing(TeachingPlan::subjectName))

        for(sem in 0 until field.semsNumber)
        {
            columns.add(MFXTableColumn("sem ${sem+1}", true))
        }

        subjectNameColumn.rowCellFactory = Function<TeachingPlan, MFXTableRowCell<TeachingPlan?, *>>
        {
            MFXTableRowCell<TeachingPlan?, Any?>(TeachingPlan::subjectName)
        }

        for ((counter, column) in columns.withIndex()) {
            column.rowCellFactory = Function {
                val cell = MFXTableRowCell<TeachingPlan, Any?> { item ->
                    item?.semesters?.getOrNull(counter)
                }
                cell
            }
        }

        spnTableView.tableColumns.addAll(subjectNameColumn)
        spnTableView.tableColumns.addAll(columns)

        for (i in 0 until spnTableView.tableColumns.size) {
            spnTableView.tableColumns[i].styleClass.add("table-header")
            spnTableView.tableColumns[i].minWidth = 350.0 / (spnTableView.tableColumns.size-1)

        }
        spnTableView.tableColumns[0].minWidth = 350.0

        return spnTableView
    }


    /**
     * Wyświetla szkolny plan nauczania dla danego kierunku w tabeli.
     * @param field Kierunek, dla którego wyświetlony zostanie szkolny plan nauczania.
     */
    override fun showSPN(field: Field)
    {
        val spnTableView = createSPNtable(field)
        val vbox = VBox(10.0, spnTableView)
        vbox.alignment = Pos.CENTER

        spnTableView.items = fieldsModel.showSPN(field)
        createAndShowDialog(vbox)
    }


    /**
     * Tworzy i wyświetla customowe okno dialogowe.
     *
     * @param vBox Kontener z zawartością okna dialogowego.
     */
    override fun createAndShowDialog(vBox: VBox) {
        vBox.alignment = Pos.CENTER

        val stage: Stage = showFieldsButton.scene.window as Stage
        dialog = DialogUtils.showCustomDialog(stage, vBox) {
            fieldsTableView.selectionModel.clearSelection()
            menu.hide()
            inEditMode = false
        }

        if (dialog.owner.isShowing) dialog.showAndWait()
    }


    /**
     * Wyświetla listę kierunków kształcenia w tabeli
     */
    override fun showFields() {
        val fields = fieldsModel.getFields()
        if (fields.isEmpty()) MessageUtil.showInfoMessage(MessageBundle.getMess("label.noFields"), MessageBundle.getMess("warning.fieldNotInDB"))
        else fieldsTableView.items = fields
    }

    /**
     * Metoda sprawdza istnienie w bazie kierunku o podobnych danych
     * Jeśli walidacja przebiegła pomyślnie to kierunek jest aktualizowany
     * @param fieldName Nazwa kierunku do sprawdzenia
     * @param shotcut Skrót kierunku do sprawdzenia
     */
    override fun checkWhileEditing(fieldName: String, shotcut: String)
    {
        try {
            fieldsModel.checkDBWhileEditing(fieldName, shotcut, lastSelectedField)
            updateField()

        }catch (e: DuplicatesException)
        {
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.fieldEditingError"), e.message!!)
        }
        catch (e: IdenticalObjectExistsException)
        {
            //Jest to edytowany kierunek
            showDialogYesNoMessage(e.message!!, ActionType.EDIT)
            if (wantToEdit) dialog.close()
        }
    }

    /**
     * Metoda tworząca i wyświetlająca okno dialogowe służące do zmiany danych kierunku
     * Pozwala na zmianę nazwy kierunku oraz skrótu nazwy kierunku
     * @param field edytowany kierunek
     */
    override fun editField(field: Field) {
        val labelEdit = Label(MessageBundle.getMess("label.editFieldData"))
        labelEdit.styleClass.add("header-label-big")

        val updateButton = CommonUtils.createUpdateButton()
        val errorFieldName = ValidationWrapper.createErrorLabel()
        val errorShortcut = ValidationWrapper.createErrorLabel()

        val vbox = VBox(
            20.0,
            labelEdit,
            ValidationWrapper.createWrapper(fieldNameTextFieldEdit, errorFieldName),
            ValidationWrapper.createWrapper(shortcutTextFieldEdit, errorShortcut),
            updateButton)

        updateButton.setOnAction { onUpdateButtonPressed(errorFieldName, errorShortcut) }

        fieldNameTextFieldEdit.floatingText = MessageBundle.getMess("label.enterFieldName")
        shortcutTextFieldEdit.floatingText = MessageBundle.getMess("label.enterFieldShortcut")
        CommonUtils.setTextFieldStyle(fieldNameTextFieldEdit, shortcutTextFieldEdit)

        fieldNameTextFieldEdit.prefWidth = 300.0
        shortcutTextFieldEdit.prefWidth = 300.0

        fieldNameTextFieldEdit.text = field.fieldName
        shortcutTextFieldEdit.text =  field.shortcut

        vbox.styleClass.add("stepper")

        createAndShowDialog(vbox)
    }

    /**
     * Metoda sprawdzająca poprawność wprowadzonych danych podczas edytowania
     * @param errorFieldName etykieta błędu związana z nazwą kierunku
     * @param errorShortcut etykieta błędu związana ze skrótem kierunku
     */
    override fun onUpdateButtonPressed(errorFieldName: Label, errorShortcut: Label)
    {
        ValidationWrapper.validationAction(fieldNameTextFieldEdit, errorFieldName)
        ValidationWrapper.validationAction(shortcutTextFieldEdit, errorShortcut)

        if (errorFieldName.text.isEmpty() && errorShortcut.text.isEmpty())
        {
            checkWhileEditing(fieldNameTextFieldEdit.text, shortcutTextFieldEdit.text)
        }
    }

    /**
     * Metoda służąca do aktualizacji kierunku kształcenia
     */
    override fun updateField() {
        val fieldID = fieldsModel.getFieldID(lastSelectedField)
        val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.fieldUpdated"))

        val okButton = MFXButton("OK")
        okButton.id = "customButton"
        okButton.setOnAction { dialog.close() }

        val vbox = VBox(20.0, completedLabel, okButton)
        vbox.alignment = Pos.CENTER

        try {
            fieldsModel.updateField(fieldID, fieldNameTextFieldEdit.text, shortcutTextFieldEdit.text)
            if (this::dialog.isInitialized) (dialog.content as MFXGenericDialog).content = vbox
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.fieldUpdated"), MessageBundle.getMess("success.field.correctlyUpdated"))
            showFields()

        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.updateFieldError"))
        }
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia
     * @param type Rodzaj akcji do wykonania
     */
    override fun showDialogYesNoMessage(content: String, type: ActionType) {
        val buttons = listOf(
            MessageBundle.getMess("label.yes") to {
                when (type) {
                    ActionType.EDIT -> wantToEdit = true
                    ActionType.DELETE -> wantToDelete = true
                }
            },
            MessageBundle.getMess("label.no") to {
                when (type) {
                    ActionType.EDIT -> wantToEdit = false
                    ActionType.DELETE -> wantToDelete = false
                }
            }
        )

        dialogMess = DialogUtils.showMessageDialogWithButtons(content, showFieldsButton.scene.window as Stage, buttons)
        if (dialogMess.owner.isShowing) dialogMess.showAndWait()
    }

    /**
     * Metoda usuwająca kierunek kształcenia
     * @param field Kierunek do usunięcia
     */
    override fun deleteField(field: Field) {
        if (wantToDelete)
        {
            try {
                fieldsModel.deleteField(field)
                if (fieldsTableView.items.size>1) showFields() else fieldsTableView.items.clear()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedField"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteFieldError"))
            }
        }
    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przed użytkownika.
     */
    override fun onTabsChanged() {
        fieldsTableView.items.clear()
        CommonUtils.removeStepDependencies(stepper, fieldNameTextField, shortcutTextField)
        createSteps()
        clearBoxes()
    }

    /**
     * Metoda czyszcząca kontrolki
     */
    override fun clearBoxes()
    {
        fieldNameTextField.clear()
        shortcutTextField.clear()
        groupNumberComboBoxArray.clear()
        CommonUtils.clearBox(semChoiceBox)
        uploadSPNToggle.text=""
    }
}