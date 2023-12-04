package com.example.scheduler.controller

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.HoursModel
import com.example.scheduler.models.SubjectsModel
import com.example.scheduler.models.TeachersModel
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.effects.DepthLevel
import io.github.palexdev.materialfx.filter.StringFilter
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.function.Function
import kotlin.reflect.KProperty1

/**
 * Klasa kontrolera modułu TeacherModule do zarządzania danymi nauczycieli
 */
class TeacherModuleController: ITeacherModuleController, AdminTabsObserver, TabsObserver {
    /**
     * Przycisk służący do wyświetlania listy nauczycieli.
     */
    @FXML
    private lateinit var showTeachersButton: MFXButton

    /**
     * Tabela służąca do wyświetlania listy nauczycieli.
     */
    @FXML
    lateinit var teacherTableView: MFXTableView<Teacher>

    /**
     * Stepper odpowiedzialny za proces dodawania nauczyciela wraz z walidacją danych.
     */
    @FXML
    lateinit var stepper: MFXStepper

    /**
     * Stepper odpowiedzialny za proces edytowania nauczyciela wraz z walidacją danych.
     */
    var stepperEdit: MFXStepper = MFXStepper()

    /**
     * Obiekt reprezentujący ostatnio wybranego nauczyciela z tabeli
     */
    lateinit var lastSelectedTeacher: Teacher

    /**
     * Flaga informująca o edycji nauczyciela.
     */
    var wantToEdit = false

    /**
     * Flaga informująca o usunięcia nauczyciela.
     */
    var wantToDelete = false

    /**
     * Flaga informująca o tym czy nauczyciel jest w trybie edycji
     */
    var inEditMode = false

    /**
     * Flaga informująca o wyświetleniu okna dialogowego z komunikatem.
     */
    private var messageShown = false


    /**
     * Pomocnicze okno dialogowe do wiadomości.
     */
    lateinit var dialogMess: MFXStageDialog


    /**
     * Pomocnicze okno dialogowe do menu kontekstowego.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Pole tekstowe do wprowadzania imienia podczas dodawania nauczyciela
     */
    var firstNameTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania nazwiska podczas dodawania nauczyciela
     */
    var lastNameTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania adresu email podczas dodawania nauczyciela
     */
    var emailTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania numeru telefonu podczas dodawania nauczyciela
     */
    var phoneTextField: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania imienia podczas edycji nauczyciela
     */
    var firstNameTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania nazwiska podczas edycji nauczyciela
     */
    var lastNameTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania adresu email podczas edycji nauczyciela
     */
    var emailTextFieldEdit: MFXTextField = MFXTextField()

    /**
     * Pole tekstowe do wprowadzania numeru telefonu podczas edycji nauczyciela
     */
    var phoneTextFieldEdit: MFXTextField = MFXTextField()


    /**
     * Lista wybranych przedmiotów nauczyciela w trybie dodawania.
     */
    var subjectCheckList: MFXCheckListView<String> = MFXCheckListView()

    /**
     * Etykieta przedmiotów w trybie dodawania
     */
    var subjectLabel: Label = Label()

    /**
     * Lista wybranych przedmiotów nauczyciela w trybie edycji.
     */
    var subjectCheckListEdit: MFXCheckListView<String> = MFXCheckListView()

    /**
     * Etykieta przedmiotów w trybie edycji
     */
    private var subjectLabelEdit: Label = Label()

    /**
     * Lista dni do wybrania dla dyspozycyjności w trybie dodawania
     */
    var dayChoiceBox: MFXComboBox<String> = MFXComboBox()

    /**
     * Lista godzin dyspozycyjności do wybrania w trybie dodawania
     */
    var hourCheckList: MFXCheckListView<String> = MFXCheckListView()

    /**
     * Etykieta dyspozycyjności w trybie dodawania
     */
    var availabilityLabel: Label = Label()

    /**
     * Etykieta nauczyciela w trybie dodawania
     */
    var teacherLabel: Label = Label()

    /**
     * Przycisk typu toggle zaznaczania wszystkich godzin w trybie dodawania
     */
    var allHoursCheckBox: MFXToggleButton = MFXToggleButton()

    /**
     * Etykieta dyspozycyjności w trybie edycji
     */
    var availabilityLabelEdit: Label = Label()

    /**
     * Etykieta nauczyciela w trybie edycji
     */
    private var teacherLabelEdit: Label = Label()

    /**
     * Lista dni do wybrania dla dyspozycyjności w trybie dodawania
     */
    var dayChoiceBoxEdit: MFXComboBox<String> = MFXComboBox()

    /**
     * Lista godzin dyspozycyjności do wybrania w trybie edycji
     */
    var hourCheckListEdit: MFXCheckListView<String> = MFXCheckListView()

    /**
     * Przycisk typu toggle zaznaczania wszystkich godzin w trybie edycji
     */
    private var allHoursCheckBoxEdit: MFXToggleButton = MFXToggleButton()

    /**
     * Menu kontekstowe wyświetlające się po kliknięciu na wiersz tabeli z nauczycielami
     */
    lateinit var menu: MFXContextMenu

    private var messageShownSubjects = false

    /**
     * Przycisk do dodawania nauczycieli z pliku
     */
    @FXML
    private lateinit var addTeacherFromFileButton : MFXButton


    /**
     * Mapa przechowująca dyspozycyjność nauczyciela
     */
    var availabilityList = mutableMapOf<String, MutableList<String>>().apply {
        this["Friday"] = mutableListOf()
        this["Saturday"] = mutableListOf()
        this["Sunday"] = mutableListOf()
    }

    val teachersModel = TeachersModel()
    private val hoursModel = HoursModel()
    private val subjectsModel = SubjectsModel()

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        TabObserver.addObserver(this)
        AdminTabObserver.addObserver(this)
        //I18N.setLanguage(Language.SIMPLIFIED_POLISH)
        setupTable()
        showTeachersButton.setOnAction { showTeachers() }
        addTeacherFromFileButton.setOnAction { showAddTeacherFromFileDialog()}
        menu = MFXContextMenu(teacherTableView)
        setOnTeacherSelected()
        setUpUIComponents()
    }

    /**
     * Konfiguruje komponenty GUI
     */
    override fun setUpUIComponents() {
        setOnDaySelected(dayChoiceBox, hourCheckList)
        setOnDaySelected(dayChoiceBoxEdit, hourCheckListEdit)

        setupTeacherForm(teacherLabel, firstNameTextField, lastNameTextField, emailTextField, phoneTextField, subjectCheckList, subjectLabel, availabilityLabel, hourCheckList, dayChoiceBox, allHoursCheckBox)
        setupTeacherForm(teacherLabelEdit, firstNameTextFieldEdit, lastNameTextFieldEdit, emailTextFieldEdit, phoneTextFieldEdit, subjectCheckListEdit, subjectLabelEdit, availabilityLabelEdit, hourCheckListEdit, dayChoiceBoxEdit, allHoursCheckBoxEdit)

        setConstraints(firstNameTextField, lastNameTextField, phoneTextField, emailTextField)
        setConstraints(firstNameTextFieldEdit, lastNameTextFieldEdit, phoneTextFieldEdit, emailTextFieldEdit)

        createSteps(firstNameTextField, lastNameTextField, phoneTextField, emailTextField, subjectCheckList, dayChoiceBox, hourCheckList, stepper, teacherLabel, subjectLabel, availabilityLabel, allHoursCheckBox)
        createSteps(firstNameTextFieldEdit, lastNameTextFieldEdit, phoneTextFieldEdit, emailTextFieldEdit, subjectCheckListEdit, dayChoiceBoxEdit, hourCheckListEdit, stepperEdit, teacherLabelEdit, subjectLabelEdit, availabilityLabelEdit, allHoursCheckBoxEdit)

        stepper.styleClass.add("stepper")
        showTeachersButton.text = MessageBundle.getMess("label.showTeachers")
    }

    /**
     * Wyświetla listę nauczycieli w tabeli
     */
    override fun showTeachers() {
        teacherTableView.items.clear()
        val teachers = teachersModel.getTeachers()
        if (teachers.isEmpty()) {
            MessageUtil.showInfoMessage(MessageBundle.getMess("warning.noTeachers"), MessageBundle.getMess("warning.teacherNotInDB"))
            teacherTableView.items.clear()
        } else teacherTableView.items = teachers
    }


    /**
     * Dodaje nowego nauczyciela wraz z przedmiotami oraz dyspozycyjnością do bazy danych.
     *
     * @param teacher   Nauczyciel do dodania.
     * @param subjects  Lista przedmiotów nauczanych przez nauczyciela.
     */
    override fun addTeacher(teacher: Teacher, subjects: List<String>) {
        try {
            teachersModel.addTeacher(teacher, subjects, availabilityList)
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.teacher.correctlyAdded"))
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.teacherAddingError"))
            teachersModel.deleteTeacher(teacher)
        }

        showTeachers()
        availabilityList.values.forEach { it.clear() }
    }

    /**
     * Aktualizuje nauczyciela w bazie danych.
     *
     * @param teacher     Nauczyciel po aktualizacji.
     * @param subjects    Lista przedmiotów nauczyciela do aktualizacji.
     * @param deletedAvailability Lista dyspozycyjności, której nie ma na nowej liście dyspozycyjności, a która znajdowała się na starej
     */
    override fun updateTeacher(
        teacher: Teacher,
        subjects: List<String>,
        deletedAvailability: MutableMap<String, MutableList<String>>
    ) {
        val teacherID = teachersModel.getTeacherID(lastSelectedTeacher)

        try {
            teachersModel.handleDeleteSubjects(teacherID, lastSelectedTeacher, subjects)
            teachersModel.handleDeleteAvailabulity(teacherID, deletedAvailability)
            teachersModel.updateTeacher(teacherID, teacher, subjects)
            try {
                teachersModel.updateTeacherAvailability(availabilityList, teacherID)
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.teacher.correctlyUpdated"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.updateTeacherAvailabilityError"))
            }
        }
        catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.updateTeacherError"))
        }

        availabilityList.values.forEach{it.clear()}
        showTeachers()
    }

    /**
     * Usuwa nauczyciela z bazy danych.
     *
     * @param teacher  Nauczyciel do usunięcia z bazy danych.
     */
    override fun deleteTeacher(teacher: Teacher) {
        teacherTableView.selectionModel.clearSelection()
        menu.hide()

        if (wantToDelete) {
            try {
                teachersModel.deleteTeacher(teacher)
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedTeacher"))
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.deleteTeacherError"))
            }
        }
        if (teacherTableView.items.size>1) showTeachers() else teacherTableView.items.clear()
    }


    /**
     * Tworzy pierwszy krok formularza dodawania lub edycji nauczyciela.
     *
     * @param firstName      Pole tekstowe do wprowadzania imienia.
     * @param lastName       Pole tekstowe do wprowadzania nazwiska.
     * @param phone          Pole tekstowe do wprowadzania numeru telefonu.
     * @param email          Pole tekstowe do wprowadzania adresu email.
     * @param stepper        Stepper dodawania lub edycji nauczyciela.
     * @param labelTeacher   Etykieta nauczyciela.
     * @return               Pierwszy krok formularza.
     */
    override fun createStep1(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, stepper: MFXStepper, labelTeacher: Label): MFXStepperToggle {
        val step = MFXStepperToggle(MessageBundle.getMess("label.data"), MFXFontIcon("fas-user", 16.0, Color.web("#40F6F6")))
        val step1Box = VBox(
            10.0,
            labelTeacher,
            ValidationWrapper.wrapNodeForValidationStepper(firstName, stepper),
            ValidationWrapper.wrapNodeForValidationStepper(lastName, stepper),
            ValidationWrapper.wrapNodeForValidationStepper(phone, stepper),
            ValidationWrapper.wrapNodeForValidationStepper(email, stepper),
        )

        step1Box.alignment = Pos.CENTER
        step.content = step1Box

        step.validator
            .dependsOn(firstName.validator)
            .dependsOn(lastName.validator)
            .dependsOn(phone.validator)
            .dependsOn(email.validator)
        return step
    }

    /**
     * Tworzy drugi krok formularza.
     *
     * @param labelSubjects  Etykieta przedmiotów.
     * @param subjects       Lista przedmiotów.
     * @return               Drugi krok formularza.
     */
    override fun createStep2(labelSubjects: Label, subjects: MFXCheckListView<String>): MFXStepperToggle {
        val step = MFXStepperToggle(MessageBundle.getMess("label.subjects"), MFXFontIcon("fas-pen", 16.0, Color.web("#40F6F6")))
        val step2Box = VBox(30.0, labelSubjects, subjects)

        step2Box.alignment = Pos.CENTER
        step.content = step2Box
        step2Box.padding = Insets(30.0)
        return step
    }

    /**
     * Tworzy trzeci krok formularza.
     *
     * @param labelAvailability  Etykieta dyspozycyjności.
     * @param days              Lista dni do wyboru.
     * @param hours             Lista godzin dyspozycyjności.
     * @param allHoursToggle     Przycisk do zaznaczania wszystkich godzin.
     * @return                  Trzeci krokformularza.
     */
    override fun createStep3(labelAvailability: Label, days: MFXComboBox<String>, hours: MFXCheckListView<String>, allHoursToggle: MFXToggleButton): MFXStepperToggle {
        val step = MFXStepperToggle(MessageBundle.getMess("label.availabilityShort"), MFXFontIcon("fas-clock", 16.0, Color.web("#40F6F6")))
        val step3Box = VBox(20.0, labelAvailability, days, allHoursToggle, hours)

        CommonUtils.clearBox(days)
        allHoursToggle.isSelected = false
        allHoursToggle.isDisable = true

        allHoursToggle.setOnAction {
            for (hour in hours.items) {
                if (allHoursToggle.isSelected) {
                    hours.selectionModel.selectItem(hour)
                }
            }

            if (!allHoursToggle.isSelected) {
                hours.selectionModel.clearSelection()
            }
        }

        step3Box.padding = Insets(20.0)
        step3Box.alignment = Pos.CENTER
        step.content = step3Box
        return step
    }


    override fun showAddTeacherFromFileDialog() {
        val vbox = createAddTeacherFromFileVBox()
        dialog = DialogUtils.showCustomDialog(showTeachersButton.scene.window as Stage, vbox, true) {}
        dialog.content.stylesheets.add(SchedulerApp::class.java.getResource("css/customStyles.css")?.toExternalForm()!!)
        if (dialog.owner.isShowing) dialog.showAndWait()
    }

    /**
     * Wczytuje nauczycieli z pliku Excel.
     *
     * @param filePath Ścieżka do pliku Excel zawierającego dane o nauczycielach.
     */
    override fun uploadFileTeachers(filePath: String)
    {
        if (filePath.isNotEmpty())
        {
            val teachersToAdd = mutableMapOf<Teacher, List<String>>()
            val dataFormatter = DataFormatter()
            val file = File(filePath)
            var myWorkBook:XSSFWorkbook?=null
            var fis:FileInputStream?=null

            try {
                fis = FileInputStream(file)
                myWorkBook = XSSFWorkbook(fis)
                val sheet = myWorkBook.getSheetAt(0)

                var startRowNr = 1
                val startCellNr = 1

                while (sheet.getRow(startRowNr)!=null)
                {
                    val firstname = dataFormatter.formatCellValue(sheet.getRow(startRowNr).getCell(startCellNr))
                    val lastname = dataFormatter.formatCellValue(sheet.getRow(startRowNr).getCell(startCellNr+1))
                    val phone = dataFormatter.formatCellValue(sheet.getRow(startRowNr).getCell(startCellNr+2))
                    val email = dataFormatter.formatCellValue(sheet.getRow(startRowNr).getCell(startCellNr+3))

                    var error = ""
                    val subjects = mutableListOf<String>()
                    var cellNum = startCellNr+4
                    while (sheet.getRow(startRowNr).getCell(cellNum)!=null)
                    {
                        val subject = sheet.getRow(startRowNr).getCell(cellNum).stringCellValue
                        //Sprawdź czy przedmiot istnieje
                        if (subjectsModel.checkIfSubjectExists(subject)) subjects.add(subject)
                        else
                        {
                            error = "${MessageBundle.getMess("warning.subjectNotExists")}: $subject"
                            break
                        }
                        cellNum++
                    }

                    val teacher = Teacher(firstname, lastname, email, phone)
                    if (error.isEmpty()) error = teachersModel.validateTeacher(teacher)
                    if (email.isEmpty()) throw java.lang.NullPointerException()

                    try {
                        teachersModel.checkDBwhileAdding(teacher)
                    }catch (e:DuplicatesException)
                    {
                        error = "${e.message!!}: (${teacher.firstname} ${teacher.lastname})"
                    }

                    if (error.isNotEmpty()){
                        MessageUtil.showErrorMessage(MessageBundle.getMess("warning.fixExcelFile"), error)
                        return
                    }

                    teachersToAdd[teacher] = subjects
                    startRowNr++
                }

                //Spróbuj dodać nauczycieli
                try {
                    teachersModel.checkDuplicatesAndAddTeachers(teachersToAdd)
                    MessageUtil.showInfoMessage(MessageBundle.getMess("label.fileLoaded"), MessageBundle.getMess("success.teachers.fileLoadedCorrectly"))
                    if (this::dialog.isInitialized) dialog.close()
                }catch (e: DuplicatesException)
                {
                    MessageUtil.showWarningMessage(MessageBundle.getMess("warning.fixExcelFile"), e.message!!)
                    return
                }

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
            catch (e: IOException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.readError"), MessageBundle.getMess("warning.fileAlreadyOpened"))
            }
            finally {
                myWorkBook?.close()
                fis?.close()
            }
        }
        else{
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.fileLoadingError"), MessageBundle.getMess("warning.shouldChooseFileFirst"))
        }
    }

    /**
     * Tworzy layout informujący o powodzeniu dodania lub edycji nauczyciela
     *
     * @param firstName        Pole tekstowe do wprowadzania imienia.
     * @param lastName         Pole tekstowe do wprowadzania nazwiska.
     * @param phone            Pole tekstowe do wprowadzania numeru telefonu.
     * @param email            Pole tekstowe do wprowadzania adresu email.
     * @param subjects         Lista przedmiotów.
     * @param days             Lista dni do wyboru.
     * @param hours            Lista godzin dyspozycyjności.
     * @param labelTeacher     Etykieta nauczyciela.
     * @param labelSubjects    Etykieta przedmiotów.
     * @param labelAvailability Etykieta dyspozycyjności.
     * @param allHoursToggle    Przycisk do zaznaczania wszystkich godzin.
     * @param step3             Trzeci krok formularza.
     * @param stepper           Stepper dodawania lub edycji nauczyciela.
     */
    override fun createCompletedStep(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, labelTeacher: Label, labelSubjects: Label, labelAvailability: Label, allHoursToggle: MFXToggleButton, step3: MFXStepperToggle, stepper: MFXStepper) {
        val completedLabel = CommonUtils.createCompletedLabel(MessageBundle.getMess("label.teacherAdded"))
        val resetButton = CommonUtils.createResetButton()

        resetButton.setOnAction {
            setOnResetButton(firstName, lastName, phone, email, subjects, days, hours, labelTeacher, labelSubjects, labelAvailability, allHoursToggle, stepper)
        }

        stepper.setOnLastNext {
            checkLastStep(firstName, lastName, phone, email, subjects, days, hours, completedLabel, step3, resetButton, stepper)
        }
    }


    /**
     * Dokonuje walidacji danych przy ostatnim kroku oraz sprawdza, które
     * terminy dyspozycyjności zostały usunięte z listy dyspozycyjności nauczyciela
     */
    fun checkLastStep(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, completedLabel: Label, step3: MFXStepperToggle, resetButton: MFXButton, stepper: MFXStepper)
    {
        //Jeśli dzień został wybrany to dodaj godziny
        if (!days.value.isNullOrEmpty()) {
            val dayValue = if (MessageBundle.bundle.locale.equals(Locale("pl", "PL"))) EnglishDayConverter.fromPolishName(days.value) else days.value
            availabilityList[dayValue]!!.clear()
            for (hour in hours.selectionModel.selectedValues) {
                availabilityList[dayValue]!!.add(hour)
            }
        }

        //Jeśli nie dodano dyspozycyjności
        if (availabilityList.values.all { it.isEmpty() }) {
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.availabilityError"), MessageBundle.getMess("warning.noAvailabilityChosen"))
        }

        if (availabilityList.values.any { it.isNotEmpty() }) {
            val teacher = Teacher(firstName.text,lastName.text, email.text, phone.text)

            if (inEditMode) {
                completedLabel.text = MessageBundle.getMess("label.teacherUpdated")
                val deletedAvailability = getDeletedAvailability()

                if (deletedAvailability.values.all { it.isEmpty() })
                {
                    updateTeacher(teacher, subjectCheckListEdit.selectionModel.selectedValues, deletedAvailability)
                    val vbox = step3.content as VBox
                    vbox.children.setAll(completedLabel, resetButton)
                }
                else
                {
                    val translatedKeysMap = deletedAvailability.mapKeys { (key, _) ->
                        when (key) {
                            "Friday" -> MessageBundle.getMess("label.friday")
                            "Saturday" -> MessageBundle.getMess("label.saturday")
                            "Sunday" -> MessageBundle.getMess("label.sunday")
                            else -> key
                        }
                    }.toMutableMap()

                    showDialogYesNoMessage("${MessageBundle.getMess("warning.askBeforeDeleteAvailability")} $translatedKeysMap", ActionType.EDIT)
                    if (!wantToEdit)
                    {
                        stepperEdit.previous()
                        hourCheckListEdit.selectionModel.clearSelection()
                        for (day in listOf("Friday", "Saturday", "Sunday")) {
                            availabilityList[day] = teachersModel.getAvailabilityByDay(lastSelectedTeacher, day.uppercase(Locale.getDefault())).toMutableList()
                        }

                        CommonUtils.clearBox(dayChoiceBoxEdit)
                    }
                    else
                    {
                        updateTeacher(teacher, subjectCheckListEdit.selectionModel.selectedValues, deletedAvailability)
                        val vbox = step3.content as VBox
                        vbox.children.setAll(completedLabel, resetButton)
                    }
                }
            } else {
                addTeacher(teacher, subjectCheckList.selectionModel.selectedValues)
            }

            val vbox = step3.content as VBox
            vbox.children.setAll(completedLabel, resetButton)
        } else {
            stepper.previous()
        }
    }


    /**
     * Pobiera terminy dyspozycyjności, które nie zostały wybrane przez użytkownika w
     * nowej liście dyspozycyjności, a które znajdowały się na starej liście.
     * @return Terminy dyspozycyjności nauczyciela do usunięcia
     */
    private fun getDeletedAvailability(): MutableMap<String, MutableList<String>>
    {
        //pobierz dyspozycyjność lastTeacherSelected i porównaj z availablity

        val availabilityOld = mutableMapOf<String, MutableList<String>>().apply {
            this["Friday"] = mutableListOf()
            this["Saturday"] = mutableListOf()
            this["Sunday"] = mutableListOf()
        }

        var availabilityDeleted = mutableMapOf<String, MutableList<String>>().apply {
            this["Friday"] = mutableListOf()
            this["Saturday"] = mutableListOf()
            this["Sunday"] = mutableListOf()
        }

        for (day in listOf("Friday", "Saturday", "Sunday")) {
            availabilityOld[day] = teachersModel.getAvailabilityByDay(lastSelectedTeacher, day.uppercase(Locale.getDefault())).toMutableList()
        }

        for (day in listOf("Friday", "Saturday", "Sunday")) {
            availabilityDeleted[day] = availabilityOld[day]!!.subtract(availabilityList[day]!!).toMutableList()
        }

        var wasAvailabilityDeleted = false
        if (availabilityDeleted.values.any { it.isEmpty() }) {
            wasAvailabilityDeleted = true
        }

        if (wasAvailabilityDeleted)
        {
            availabilityDeleted = availabilityDeleted.filterValues { it.isNotEmpty() }.toMutableMap()
        }
        return availabilityDeleted


    }

    /**
     * Obsługuje akcje wywoływane po naciśnięciu przycisku "Reset" na formularzu dodawania lub edycji nauczyciela.
     *
     * @param firstName         Pole tekstowe do wprowadzania imienia.
     * @param lastName          Pole tekstowe do wprowadzania nazwiska.
     * @param phone             Pole tekstowe do wprowadzania numeru telefonu.
     * @param email             Pole tekstowe do wprowadzania adresu email.
     * @param subjects          Lista przedmiotów.
     * @param days              Lista dni do wyboru.
     * @param hours             Lista godzin dyspozycyjności.
     * @param labelTeacher      Etykieta nauczyciela.
     * @param labelSubjects     Etykieta przedmiotów.
     * @param labelAvailability Etykieta dyspozycyjności.
     * @param allHoursToggle    Przycisk do zaznaczania wszystkich godzin.
     * @param stepper           Stepper dodawania lub edycji nauczyciela.
     */
    override fun setOnResetButton(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, labelTeacher: Label, labelSubjects: Label, labelAvailability: Label, allHoursToggle: MFXToggleButton, stepper: MFXStepper) {
        if (this::dialog.isInitialized) dialog.close()
        CommonUtils.removeStepDependencies(stepper, firstName, lastName, phone, email)
        createSteps(firstName, lastName, phone, email, subjects, days, hours, stepper, labelTeacher, labelSubjects, labelAvailability, allHoursToggle)
        if (!inEditMode) clearBoxes()
        inEditMode = false
    }

    /**
     * Tworzy kroki dla stepper procesu dodawania lub edycji nauczyciela.
     *
     * @param firstName         Pole tekstowe do wprowadzania imienia.
     * @param lastName          Pole tekstowe do wprowadzania nazwiska.
     * @param phone             Pole tekstowe do wprowadzania numeru telefonu.
     * @param email             Pole tekstowe do wprowadzania adresu email.
     * @param subjects          Lista przedmiotów.
     * @param days              Lista dni do wyboru.
     * @param hours             Lista godzin dyspozycyjności.
     * @param stepper           Stepper dodawania lub edycji.
     * @param labelTeacher      Etykieta nauczyciela.
     * @param labelSubjects     Etykieta przedmiotów.
     * @param labelAvailability Etykieta dyspozycyjności.
     * @param allHoursToggle    Przycisk do zaznaczania wszystkich godzin.
     */
    override fun createSteps(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, stepper: MFXStepper, labelTeacher: Label, labelSubjects: Label, labelAvailability: Label, allHoursToggle: MFXToggleButton) {

        val step1 = createStep1(firstName, lastName, phone, email, stepper, labelTeacher)
        val step2 = createStep2(labelSubjects, subjects)
        val step3 = createStep3(labelAvailability, days, hours, allHoursToggle)
        createCompletedStep(firstName, lastName, phone, email, subjects, days, hours, labelTeacher, labelSubjects, labelAvailability, allHoursToggle, step3, stepper)

        stepper.stepperToggles.addAll(listOf(step1, step2, step3))
        performActionsBetweenSteps(subjects, firstName, lastName, phone, email, stepper)
    }


    /**
     * Obsługuje akcje wywoływane po kliknięciu na nauczyciela w tabeli.
     */
    override fun setOnTeacherSelected() {
        CommonUtils.setOnItemSelected(teacherTableView, menu) { selectedRowIndex ->
            lastSelectedTeacher = teacherTableView.items[selectedRowIndex]
            showContextMenu(selectedRowIndex, lastSelectedTeacher)
        }
    }


    /**
     * Obsługuje akcje wywoływane po wyborze dnia w kroku 3 dodawania lub edycji nauczyciela.
     *
     * @param dayBox   Lista dni do wyboru.
     * @param hourBox  Lista godzin dyspozycyjności.
     */
    override fun setOnDaySelected(dayBox: MFXComboBox<String>, hourBox: MFXCheckListView<String>) {

        dayBox.valueProperty().addListener { _, oldItem, newItem ->
            allHoursCheckBox.isDisable = false
            allHoursCheckBoxEdit.isDisable = false

            allHoursCheckBox.isSelected = false
            allHoursCheckBoxEdit.isSelected = false

            //Dodaje do listy wszystkie godziny które były zaznaczone przed zmianą dnia
            if (oldItem!=null && newItem!=null) {
                val englishDay = if(MessageBundle.bundle.locale.equals(Locale("pl", "PL"))) EnglishDayConverter.fromPolishName(oldItem) else oldItem
                availabilityList[englishDay]!!.clear()
                for (hour in hourBox.selectionModel.selectedValues) {
                    availabilityList[englishDay]!!.add(hour)
                }
            }
            hourBox.selectionModel.clearSelection()

            if (newItem!=null) {
                val englishDay = if(MessageBundle.bundle.locale.equals(Locale("pl", "PL") )) EnglishDayConverter.fromPolishName(newItem) else newItem
                availabilityList[englishDay]?.forEach { item -> hourBox.selectionModel.selectItem(item) }
            }
        }
    }

    /**
     * Konfiguruje pierwszy krok formularza dodawania lub edycji nauczyciela.
     *
     * @param teacherLabel Etykieta nauczyciela.
     * @param firstName    Pole tekstowe do wprowadzania imienia.
     * @param lastName     Pole tekstowe do wprowadzania nazwiska.
     * @param email        Pole tekstowe do wprowadzania adresu email.
     * @param phone        Pole tekstowe do wprowadzania numeru telefonu.
     */
    override fun setUpFirstStepForm(teacherLabel: Label, firstName: MFXTextField, lastName: MFXTextField, email: MFXTextField, phone: MFXTextField)
    {
        teacherLabel.text = MessageBundle.getMess("label.fillData")
        teacherLabel.styleClass.add("header-label")
        firstName.floatingText = MessageBundle.getMess("label.firstname")
        lastName.floatingText = MessageBundle.getMess("label.lastname")
        email.floatingText = MessageBundle.getMess("label.email")
        phone.floatingText = MessageBundle.getMess("label.phone")
        phone.textLimit = 9
        teacherLabel.text = MessageBundle.getMess("label.fillData")
    }

    /**
     * Konfiguruje drugi krok formularza dodawania lub edycji nauczyciela.
     *
     * @param subjects      Lista przedmiotów.
     * @param subjectLabel  Etykieta przedmiotów.
     */
    override fun setUpSecondStepForm(subjects: MFXCheckListView<String>, subjectLabel: Label)
    {
        subjects.items = subjectsModel.getSubjectsNames()
        subjectLabel.text = MessageBundle.getMess("label.chooseSubjects")
        subjectLabel.styleClass.add("header-label")
        subjects.features().enableBounceEffect()
        subjects.features().enableSmoothScrolling(0.5)
        subjects.prefWidth = 450.0
        subjects.depthLevel = DepthLevel.LEVEL0
    }

    /**
     * Konfiguruje trzeci krok formularza dodawania lub edycji nauczyciela.
     *
     * @param days              Lista dni do wyboru.
     * @param availabilityLabel Etykieta dyspozycyjności nauczyciela.
     * @param hours             Lista godzin do wyboru.
     * @param allHours          Przycisk do wyboru wszystkich godzin.
     */
    override fun setUpThirdStepForm(days: MFXComboBox<String>, availabilityLabel: Label, hours: MFXCheckListView<String>, allHours: MFXToggleButton)
    {
        days.floatingText = MessageBundle.getMess("label.chooseDay")
        days.prefWidth = 150.0
        days.prefHeight = 60.0

        availabilityLabel.text = MessageBundle.getMess("label.addTeacherAvailability")
        availabilityLabel.styleClass.add("header-label")
        availabilityLabel.style = subjectLabelEdit.style

        days.items = FXCollections.observableArrayList(
            MessageBundle.getMess("label.friday"),
            MessageBundle.getMess("label.saturday"),
            MessageBundle.getMess("label.sunday")
        )
        hours.items = hoursModel.getHours()
        hours.features().enableBounceEffect()
        hours.features().enableSmoothScrolling(0.5)
        hours.depthLevel = DepthLevel.LEVEL0

        allHours.text = MessageBundle.getMess("label.allHours")
    }


    /**
     * Konfiguruje formularz dodawania lub edycji nauczyciela.
     *
     * @param teacherLabel      Etykieta nauczyciela.
     * @param firstName         Pole tekstowe do wprowadzania imienia.
     * @param lastName          Pole tekstowe do wprowadzania nazwiska.
     * @param email             Pole tekstowe do wprowadzania adresu email.
     * @param phone             Pole tekstowe do wprowadzania numeru telefonu.
     * @param subjects          Lista przedmiotów nauczyciela.
     * @param subjectLabel      Etykieta przedmiotów.
     * @param availabilityLabel Etykieta dyspozycyjności nauczyciela.
     * @param hours             Lista godzin do wyboru.
     * @param days              Lista dni do wyboru.
     * @param allHours          Przycisk do wyboru wszystkich godzin.
     */
    override fun setupTeacherForm(teacherLabel: Label, firstName: MFXTextField, lastName: MFXTextField, email: MFXTextField, phone: MFXTextField, subjects: MFXCheckListView<String>, subjectLabel: Label, availabilityLabel: Label, hours: MFXCheckListView<String>, days: MFXComboBox<String>, allHours: MFXToggleButton) {

        setUpFirstStepForm(teacherLabel, firstName, lastName, email, phone)
        setUpSecondStepForm(subjects,subjectLabel)
        setUpThirdStepForm(days, availabilityLabel, hours, allHours)
        CommonUtils.setTextFieldStyle(firstName, lastName, email, phone, days)
    }

    /**
     * Ustawia ograniczenia na pola tekstowe walidujące poprawność danych.
     *
     * @param firstName         Pole tekstowe do wprowadzania imienia.
     * @param lastName          Pole tekstowe do wprowadzania nazwiska.
     * @param email             Pole tekstowe do wprowadzania adresu email.
     * @param phone             Pole tekstowe do wprowadzania numeru telefonu.
     */
    override fun setConstraints(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField) {
        firstName.validator.constraint(
            ValidatorUtil.createMoreThanOneLetterConstraint(
                firstName.textProperty(),
                MessageBundle.getMess("firstname.validation.moreThanOneLetter")
            )
        )
        firstName.validator.constraint(
            ValidatorUtil.createLettersSmallExceptFirstConstraint(
                firstName.textProperty(),
                MessageBundle.getMess("firstname.validation.allLettersLowercaseExceptFirst")
            )
        )
        firstName.validator.constraint(
            ValidatorUtil.createFirstLetterBigConstraint(
                firstName.textProperty(),
                MessageBundle.getMess("firstname.validation.startWithUppercase")
            )
        )
        firstName.validator.constraint(
            ValidatorUtil.createNoSpecialCharsConstraint(
                firstName.textProperty(),
                MessageBundle.getMess("firstname.validation.noSpecialChars")
            )
        )

        lastName.validator.constraint(
            ValidatorUtil.createMoreThanOneLetterConstraint(
                lastName.textProperty(),
                MessageBundle.getMess("lastname.validation.moreThanOneLetter")
            )
        )
        lastName.validator.constraint(
            ValidatorUtil.createFirstLetterBigConstraint(
                lastName.textProperty(),
                MessageBundle.getMess("lastname.validation.startWithUppercase")
            )
        )
        lastName.validator.constraint(
            ValidatorUtil.createNoSpecialCharsConstraint(
                lastName.textProperty(),
                MessageBundle.getMess("lastname.validation.noSpecialChars")
            )
        )

        phone.validator.constraint(
            ValidatorUtil.createOnlyDigitsAllowedConstraint(
                phone.textProperty(),
                MessageBundle.getMess("phone.validation.onlyNumbers")
            )
        )
        phone.validator.constraint(
            ValidatorUtil.createExactlyNineDigitsConstraint(
                phone.textProperty(),
                MessageBundle.getMess("phone.validation.nineDigits")
            )
        )

        email.validator.constraint(
            ValidatorUtil.createIncorrectEmailConstraint(
                email.textProperty(),
                MessageBundle.getMess("email.validation.incorrectEmail")
            )
        )
        email.validator.constraint(
            ValidatorUtil.createNotEmptyConstraint(
                email.textProperty(),
                MessageBundle.getMess("email.validation.notEmpty")
            )
        )

    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przez użytkownika - czyści panel.
     */
    override fun onTabsChanged() {
        CommonUtils.removeStepDependencies(stepper, firstNameTextField, lastNameTextField, phoneTextField, emailTextField)
        createSteps(firstNameTextField, lastNameTextField, phoneTextField, emailTextField, subjectCheckList, dayChoiceBox, hourCheckList, stepper, teacherLabel, subjectLabel, availabilityLabel, allHoursCheckBox)
        clearBoxes()
        teacherTableView.update()
        teacherTableView.selectionModel.clearSelection()
        teacherTableView.items.clear()
    }

    /**
     * Czyści pola tekstowe formularza.
     */
    override fun clearBoxes() {
        firstNameTextField.clear()
        lastNameTextField.clear()
        phoneTextField.clear()
        emailTextField.clear()
        subjectCheckList.selectionModel.clearSelection()
        hourCheckList.selectionModel.clearSelection()
        CommonUtils.clearBox(dayChoiceBox)
    }

    /**
     * Obsługuje akcje wywoływane po drugim kroku podczas dodawania lub edycji nauczyciela.
     *
     * @param stepper  Stepper dodawania lub edycji nauczyciela.
     * @param subjects Lista przedmiotów nauczyciela.
     */
    override fun handleActionsAfterSecondStep(stepper: MFXStepper, subjects: MFXCheckListView<String>)
    {
        if (stepper.currentStepperNode == stepper.stepperToggles[2])
        {
            //Cofnij jeśli nie wybrano przedmiotów dla nauczyciela
            if (subjects.selectionModel.selectedValues.isEmpty())
            {
                stepper.previous()
                MessageUtil.showWarningMessage(MessageBundle.getMess("warning.teacherAddingError"), MessageBundle.getMess("warning.shouldChooseSubjects"))
            }

            else if (stepper == stepperEdit && !messageShownSubjects)
            {
                checkIfOldSubjectWasDeleted(subjects)
            }
            else if (stepper == this.stepper)
            {
                availabilityList.values.forEach { it.clear() }
            }
        }
    }

    /**
     * Sprawdza, które przedmioty z listy przedmiotów nauczyciela nie zostały wybrane na nowej liście
     * przedmiotów nauczyciela (trzeba będzie usunąć zajęcia z nimi związane) oraz informauje o tym użytkownika
     * @param subjects Nowa lista przedmiotów wybrana przez użytkownika
     */
    fun checkIfOldSubjectWasDeleted(subjects: MFXCheckListView<String>)
    {
        val newSubjects = subjects.selectionModel.selectedValues
        val oldSubjects = teachersModel.getTeacherSubjects(lastSelectedTeacher)
        val deletedSubjects = oldSubjects-newSubjects.toSet()
        if (deletedSubjects.isNotEmpty())
        {
            showDialogYesNoMessage("${MessageBundle.getMess("warning.askBeforeDeleteSubject")} $deletedSubjects", ActionType.EDIT)
            if (!wantToEdit)
            {
                stepperEdit.previous()
                subjectCheckListEdit.selectionModel.clearSelection()

                for (item in oldSubjects)
                {
                    subjectCheckListEdit.selectionModel.selectItem(item)
                }
            }
            else messageShownSubjects = true
        }
    }

    /**
     * Sprawdza czy w bazie danych nie ma już podobnych lub takich samych danych podczas dodawania nauczyciela.
     *
     * @param teacher Dodawany nauczyciel.
     */
    override fun checkDBWhileAdding(teacher: Teacher)
    {
        try {
            teachersModel.checkDBwhileAdding(teacher)
        }catch (e:DuplicatesException)
        {
            stepper.previous()
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.teacherAddingError"), e.message!!)
        }
    }

    /**
     * Sprawdza czy w bazie danych nie ma już podobnych lub takich samych danych podczas edycji nauczyciela.
     *
     * @param teacher Edytowany auczyciel.
     */
    override fun checkDBWhileEditing(teacher: Teacher)
    {
        try {
            teachersModel.checkDBwhileEditing(teacher, lastSelectedTeacher, messageShown)
        }catch (e:DuplicatesException)
        {
            stepperEdit.previous()
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.teacherEditingError"), e.message!!)
        }
        catch (e:IdenticalObjectExistsException)
        {
            showDialogYesNoMessage(e.message!!, ActionType.EDIT)
            if (!wantToEdit) stepperEdit.previous()
            else messageShown = true
        }
    }


    /**
     * Obsługuje akcje wywoływane po pierwszym kroku podczas dodawania lub edycji nauczyciela.
     *
     * @param stepper           Stepper dodawania lub edycji nauczyciela.
     * @param firstname         Pole tekstowe do wprowadzania imienia.
     * @param lastname          Pole tekstowe do wprowadzania nazwiska.
     * @param email             Pole tekstowe do wprowadzania adresu email.
     * @param phone             Pole tekstowe do wprowadzania numeru telefonu.
     */
    override fun handleActionsAfterFirstStep(stepper: MFXStepper, firstname: MFXTextField, lastname: MFXTextField, phone: MFXTextField, email: MFXTextField)
    {
        if (stepper.currentStepperNode == stepper.stepperToggles[1])
        {
            val teacher = Teacher(
                firstname = firstname.text,
                lastname = lastname.text,
                email = email.text,
                phone = phone.text
            )

            if (!inEditMode) checkDBWhileAdding(teacher)
            else checkDBWhileEditing(teacher)
        }
    }


    /**
     * Wykonuje akcje między krokami formularza.
     *
     * @param subjects          Lista przedmiotów.
     * @param firstname         Pole tekstowe do wprowadzania imienia.
     * @param lastname          Pole tekstowe do wprowadzania nazwiska.
     * @param email             Pole tekstowe do wprowadzania adresu email.
     * @param phone             Pole tekstowe do wprowadzania numeru telefonu.
     * @param stepper           Stepper dodawania lub edycji nauczyciela.
     */
    override fun performActionsBetweenSteps(subjects: MFXCheckListView<String>, firstname: MFXTextField, lastname: MFXTextField, phone: MFXTextField, email: MFXTextField, stepper: MFXStepper)
    {
        stepper.addEventHandler(MFXStepper.MFXStepperEvent.NEXT_EVENT) {
            handleActionsAfterFirstStep(stepper, firstname, lastname, phone, email)
            handleActionsAfterSecondStep(stepper, subjects)
        }

        stepperEdit.addEventHandler(MFXStepper.MFXStepperEvent.BEFORE_PREVIOUS_EVENT) {
            if (stepperEdit.currentStepperNode == stepperEdit.stepperToggles[2])
            {
                messageShownSubjects = false
            }
        }
    }

    /**
     * Tworzy i wyświetla menu kontekstowe dla wybranego nauczyciela z tabeli.
     *
     * @param selectedRowIndex Indeks zaznaczonego wiersza w tabeli.
     * @param selectedItem       Wybrany nauczyciel.
     */
    override fun showContextMenu(selectedRowIndex: Int, selectedItem: Teacher) {

        val deleteButton = MFXContextMenuItem(MessageBundle.getMess("label.delete"))
        val editButton = MFXContextMenuItem(MessageBundle.getMess("label.editTeacher"))
        val showSubjectsButton = MFXContextMenuItem(MessageBundle.getMess("label.showSubjects"))
        val showAvailabilityButton = MFXContextMenuItem(MessageBundle.getMess("label.showAvailability"))

        deleteButton.graphic =  MFXFontIcon("fas-trash-can", 16.0, Color.BLACK)
        editButton.graphic = MFXFontIcon("fas-wrench", 16.0, Color.BLACK)
        showAvailabilityButton.graphic = MFXFontIcon("fas-clock", 16.0, Color.BLACK)
        showSubjectsButton.graphic = MFXFontIcon("fas-file-pen", 16.0, Color.BLACK)

        deleteButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.teacher.askBeforeDelete"), ActionType.DELETE)
            deleteTeacher(selectedItem)
        }

        editButton.setOnAction {
            inEditMode = true
            editTeacher(selectedItem)
        }

        showAvailabilityButton.setOnAction {
            showAvailability(selectedItem)
        }

        showSubjectsButton.setOnAction {
            showSubjects(selectedItem)
        }

        CommonUtils.showContextMenu(selectedRowIndex, teacherTableView, menu, listOf(deleteButton, editButton, showAvailabilityButton, showSubjectsButton))

    }


    /**
     * Wyświetla listę przedmiotów nauczyciela.
     *
     * @param teacher Wybrany nauczyciel.
     */
    override fun showSubjects(teacher: Teacher) {
        val subjects = teachersModel.getTeacherSubjects(teacher)

        val labeltext = if (subjects.isEmpty())
        {
            MessageBundle.getMess("label.noSubjects")
        }
        else{
            MessageBundle.getMess("label.subjects")
        }

        val vbox = DialogUtils.createListView(labeltext, subjects)
        vbox.maxWidth = Double.MAX_VALUE

        createAndShowDialog(vbox)
    }

    /**
     * Wyświetla dyspozycyjność wybranego nauczyciela.
     *
     * @param teacher Wybrany nauczyciel.
     */
    override fun showAvailability(teacher: Teacher) {

        val availabilityList = teachersModel.getAvailability(teacher)
        val labelText = if (availabilityList.isEmpty()) {
            MessageBundle.getMess("label.noAvailability")
        } else {
            MessageBundle.getMess("label.availability")
        }

        val vbox = DialogUtils.createListView(labelText, availabilityList, 300.0)
        createAndShowDialog(vbox)
    }


    /**
     * Wyświetla formularz pozwalający na edycję nauczyciela.
     *
     * @param teacherToEdit Wybrany nauczyciel.
     */
    override fun editTeacher(teacherToEdit: Teacher) {
        val vbox = VBox()
        vbox.children.addAll(stepperEdit)
        stepperEdit.styleClass.add("stepper")

        firstNameTextFieldEdit.text = teacherToEdit.firstname
        lastNameTextFieldEdit.text = teacherToEdit.lastname
        phoneTextFieldEdit.text = teacherToEdit.phone
        emailTextFieldEdit.text = teacherToEdit.email

        subjectCheckListEdit.selectionModel.clearSelection()
        val subjectList = teachersModel.getTeacherSubjects(teacherToEdit)

        for (item in subjectList)
        {
            subjectCheckListEdit.selectionModel.selectItem(item)
        }

        //Jeśli nie wybrano żadnej dyspozycyjności
        for (day in listOf("Friday", "Saturday", "Sunday")) {
            availabilityList[day] = teachersModel.getAvailabilityByDay(lastSelectedTeacher, day.uppercase(Locale.getDefault())).toMutableList()
        }

        createAndShowDialog(vbox)
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
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

        dialogMess = DialogUtils.showMessageDialogWithButtons(content, showTeachersButton.scene.window as Stage, buttons)
        if (dialogMess.owner.isShowing) dialogMess.showAndWait()
    }

    /**
     * Tworzy layout dodawania nauczycieli z pliku
     */
    fun createAddTeacherFromFileVBox(): VBox {
        val uploadButton = MFXButton()
        val uploadTeachersLabel = Label()
        val uploadTeachersToggle = MFXRectangleToggleNode()
        uploadTeachersLabel.styleClass.add("header-label_white")
        uploadTeachersToggle.labelTrailingIcon = MFXFontIcon("fas-list-ul", 16.0, Color.BLACK)
        uploadTeachersToggle.text = MessageBundle.getMess("label.chooseFile")
        uploadButton.text = MessageBundle.getMess("label.upload")
        uploadTeachersLabel.text = MessageBundle.getMess("label.loadFileWithTeachers")
        uploadTeachersToggle.id = "comboWhite"
        uploadButton.id = "customButton"
        MFXTooltip.of(uploadTeachersToggle, MessageBundle.getMess("label.patternInfo")).install()
        uploadTeachersLabel.prefWidth = 700.0
        uploadTeachersLabel.alignment = Pos.CENTER
        uploadTeachersToggle.prefWidth = 380.0
        uploadTeachersToggle.setOnAction { ExcelUtils.searchFile(uploadTeachersToggle, showTeachersButton.scene.window as Stage) }
        uploadButton.setOnAction { uploadFileTeachers(uploadTeachersToggle.text) }

        val vbox = VBox(30.0, uploadTeachersLabel, uploadTeachersToggle, uploadButton)
        vbox.alignment = Pos.CENTER

        return vbox
    }

    /**
     * Tworzy i wyświetla customowe okno dialogowe.
     *
     * @param vBox Kontener z zawartością okna dialogowego.
     */
    override fun createAndShowDialog(vBox: VBox) {
        vBox.alignment = Pos.CENTER

        val shouldBeBigger = vBox.children.contains(stepperEdit)
        dialog = DialogUtils.showCustomDialog(showTeachersButton.scene.window as Stage, vBox, shouldBeBigger) {
            setOnDialogClosing()
        }

        if (dialog.owner.isShowing) dialog.showAndWait()
    }

    /**
     * Obsługuje zdarzenie zamknięcia okna dialogowego.
     */
    override fun setOnDialogClosing()
    {
        teacherTableView.selectionModel.clearSelection()
        menu.hide()
        messageShown=false
        messageShownSubjects=false
        inEditMode=false
        CommonUtils.removeStepDependencies(stepperEdit, firstNameTextFieldEdit, lastNameTextFieldEdit, phoneTextFieldEdit, emailTextFieldEdit)
        createSteps(firstNameTextFieldEdit, lastNameTextFieldEdit, phoneTextFieldEdit, emailTextFieldEdit, subjectCheckListEdit, dayChoiceBoxEdit, hourCheckListEdit, stepperEdit, teacherLabelEdit, subjectLabelEdit, availabilityLabelEdit, allHoursCheckBoxEdit)
    }


    /**
     * Tworzy i konfiguruje tabelę z nauczycielami.
     */
    override fun setupTable() {
        val columns = mapOf<MFXTableColumn<Teacher>, KProperty1<Teacher, *>>(
            MFXTableColumn(MessageBundle.getMess("label.firstname"), false, Comparator.comparing(Teacher::firstname)) to Teacher::firstname,
            MFXTableColumn(MessageBundle.getMess("label.lastname"), false, Comparator.comparing(Teacher::lastname)) to Teacher::lastname,
            MFXTableColumn(MessageBundle.getMess("label.email"), false, Comparator.comparing(Teacher::email)) to Teacher::email,
            MFXTableColumn(MessageBundle.getMess("label.phone"), false, Comparator.comparing(Teacher::phone)) to Teacher::phone
        )
        
        columns.forEach{ column ->
            column.key.rowCellFactory = Function<Teacher, MFXTableRowCell<Teacher?, *>>
            {
                val cell = MFXTableRowCell<Teacher?, Any?>(column.value)
                cell.styleClass.add("table-cell")
                cell
            }
        }

        //Ustawia filtry
        teacherTableView.filters.addAll(
            StringFilter(MessageBundle.getMess("label.firstname"), Teacher::firstname),
            StringFilter(MessageBundle.getMess("label.lastname"), Teacher::lastname),
            StringFilter(MessageBundle.getMess("label.email"), Teacher::email),
            StringFilter(MessageBundle.getMess("label.phone"), Teacher::phone)
        )

        teacherTableView.tableColumns.addAll(columns.keys)

        for (i in 0 until teacherTableView.tableColumns.size) {
            teacherTableView.tableColumns[i].styleClass.add("table-header")
        }

        teacherTableView.tableColumns[2].minWidth = 150.0
        teacherTableView.tableColumns[1].minWidth = 150.0
        teacherTableView.isFooterVisible = true
    }
}