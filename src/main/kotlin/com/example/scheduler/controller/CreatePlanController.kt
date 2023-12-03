package com.example.scheduler.controller

import com.example.scheduler.controller.exceptions.EmptyPlanException
import com.example.scheduler.controller.exceptions.MissingValueException
import com.example.scheduler.controller.observers.PlansObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.PlansModel
import com.example.scheduler.models.ClassesToWrite
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.*
import io.github.palexdev.materialfx.controls.cell.MFXDateCell
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.stage.Stage
import java.sql.SQLException
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.function.Function

/**
 * Klasa kontrolera do tworzenia planu zajęć.
 */
class CreatePlanController: ICreatePlanController, PlansObserver, TabsObserver {

    /**
     * Etykieta aktywnego planu.
     */
    lateinit var activePlanLabel: Label

    /**
     * Przycisk do dodawania zajęć.
     */
    @FXML
    internal lateinit var addClassesButton: MFXButton

    /**
     * Przycisk do zapisywania planu.
     */
    @FXML
    internal lateinit var savePlanButton: MFXButton

    /**
     * Przycisk do zamykania planu.
     */
    @FXML
    internal lateinit var closePlanButton: MFXButton

    /**
     * Przycisk do usuwania aktualnego planu.
     */
    @FXML
    internal lateinit var deletePlanButton: MFXButton

    /**
     * Kontrolka służąca do wyboru daty.
     */
    @FXML
    internal lateinit var datepicker:MFXDatePicker

    /**
     * Kontrolka wyboru kierunku kształcenia.
     */
    @FXML
    internal lateinit var fieldOfStudyChoiceBox: MFXComboBox<String>

    /**
     * Pomocnicze okno dialogowe.
     */
    lateinit var dialog: MFXStageDialog

    /**
     * Kontrolka wyboru planu, który użytkownik chce przywrócić.
     */
    @FXML
    internal lateinit var getPlanChoiceBox: MFXComboBox<String>

    /**
     * Przycisk przywracania wybranego planu.
     */
    @FXML
    internal lateinit var getPlanButton: MFXButton

    /**
     * Kontrolka wyboru grupy.
     */
    @FXML
    internal lateinit var  groupChoiceBox: MFXComboBox<String>

    /**
     * Kontrolka wyboru godziny.
     */
    @FXML
    internal lateinit var hourChoiceBox: MFXComboBox<String>

    /**
     * Kontrolka wyboru lokalizacji.
     */
    @FXML
    internal lateinit var locationChoiceBox: MFXComboBox<String>

    /**
     * Kontrolka wyboru sali.
     */
    @FXML
    internal lateinit var roomChoicebox: MFXFilterComboBox<String>

    /**
     * Kontrolka wyboru przedmiotu.
     */
    @FXML
    internal lateinit var subjectChoiceBox: MFXComboBox<String>

    /**
     * Kontrolka wyboru nauczyciela.
     */
    @FXML
    internal lateinit var teacherChoiceBox: MFXFilterComboBox<String>

    /**
     * Flaga informująca o edycji planu.
     */
    var wantToEdit = false

    /**
     * Flaga informująca o usunięcia planu.
     */
    private var wantToDelete = false

    /**
     * Flaga informująca o istnieniu planu (tabeli w bazie).
     */
    private var tableExists = false

    /**
     * Scena aplikacji
     */
    lateinit var stage: Stage

    /**
     * Flaga informująca o tym czy użytkownik aktualnie modyfikuje istniejący plan
     */
    private var inEditWeekMode = false

    /**
     * Flaga informująca o tym, że użytkownik jest w trakcie tworzenia nowego planu
     */
    private var activePlanExists = false

    /**
     * Lista proponowanych godzin w przypadku braku nauczycieli
     */
    lateinit var suggestedHours:List<String>

    /**
     * Lista zajętych nauczycieli, spełniających warunki w przypadku braku nauczycieli
     */
    lateinit var suggestedBusyTeachers:List<String>

    /**
     * Pomocniczy dymek informacyjny tooltip
     */
    lateinit var tooltip: MFXTooltip
    var classesModel = ClassesToWrite()
    val plansModel = PlansModel()

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        setupDatePicker()
        setListeners()
        setActions()
        setTexts()
        getPlanChoiceBox.items = plansModel.getAllPlansExceptCurrent()
        TabObserver.addObserver(this)
        PlansModel.addObserver(this)
        /*
        fieldOfStudyChoiceBox.itemsProperty().bind(
            Bindings.createObjectBinding(
                { FXCollections.observableArrayList(classesModel.getFields()) },
                classesModel.date
            )
        )
        groupChoiceBox.itemsProperty().bind(
            Bindings.createObjectBinding(
                { FXCollections.observableArrayList(classesModel.getGroups()) },
                classesModel.fieldOfStudy
            )
        )

        fieldOfStudyChoiceBox.valueProperty().bindBidirectional(classesModel.fieldOfStudy)
         */
    }

    /**
     * Ustawia etykiety dla komponentów
     */
    override fun setTexts(){
        getPlanButton.text = MessageBundle.getMess("label.restorePlan")
        savePlanButton.text = MessageBundle.getMess("label.savePlan")
        closePlanButton.text = MessageBundle.getMess("label.closePlan")
        deletePlanButton.text = MessageBundle.getMess("label.deletePlan")
        datepicker.promptText = MessageBundle.getMess("label.chooseDate")
        fieldOfStudyChoiceBox.floatingText = MessageBundle.getMess("label.field")
        groupChoiceBox.floatingText = MessageBundle.getMess("label.group")
        subjectChoiceBox.floatingText = MessageBundle.getMess("label.subject")
        locationChoiceBox.floatingText = MessageBundle.getMess("label.location")
        hourChoiceBox.floatingText = MessageBundle.getMess("label.hour")
        roomChoicebox.floatingText = MessageBundle.getMess("label.room")
        teacherChoiceBox.floatingText = MessageBundle.getMess("label.teacher")
        addClassesButton.text = MessageBundle.getMess("label.add")
        datepicker.locale = MessageBundle.bundle.locale
    }

    /**
     * Ustawia akcje dla przycisków
     */
    override fun setActions()
    {
        getPlanButton.setOnAction { getPlanFromBox() }
        deletePlanButton.setOnAction { deletePlan() }

        addClassesButton.setOnAction {
            try {
                onAddClassesPressed()
            }
            catch (e: MissingValueException) {
                MessageUtil.showWarningMessage(MessageBundle.getMess("warning.planAddingError"), e.message!!)
            }
        }
        savePlanButton.setOnAction { savePlan() }
        closePlanButton.setOnAction { saveAndClosePlan() }
    }

    /**
     * Usuwa aktualny plan zajęć po potwierdzeniu przez użytkownika.
     * Jeśli użytkownik wyrazi chęć usunięcia, tabela z planem w bazie danych zostanie usunięta
     * a aktualny plan zresetowany
     */
    override fun deletePlan() {
        showDialogYesNoMessage(MessageBundle.getMess("question.plan.askBeforeDelete"), ActionType.DELETE)
        if (wantToDelete)
        {
            try {
                plansModel.deleteCurrentPlan()
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlyDeletedPlan"))
                setDefaultValues()
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.savePlanError"))
            }
            catch (e: EmptyPlanException) {
                MessageUtil.showErrorMessage(e.message!!, MessageBundle.getMess("warning.emptyPlan"))
            }
            CommonUtils.clearDatePicker(datepicker)
            classesModel.date = null
        }
    }

    /**
     * Pobiera plan zajęć z bazy danych na podstawie wybranej daty i ustawia etykietę aktywnego planu.
     *
     * @param startPlanDay Data rozpoczęcia planu (nazwa planu).
     */
    override fun getPlanFromDatepicker(startPlanDay: LocalDate) {
        val tableHoursLeft = "group_subject_hours_left_$startPlanDay"
        val tablePlan = "plan_$startPlanDay"

        try {
            plansModel.refillFromOldPlan(tablePlan,tableHoursLeft)
            setActivePlan(tablePlan)
            getPlanChoiceBox.items = plansModel.getAllPlansExceptCurrent()
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.getPlanError"))
        }
    }

    /**
     * Pobiera wybrany plan zajęć na podstawie wyboru użytkownika z listy planów z bazy danych.
     * Przed pobraniem wybranego planu wykonywane jest zapisanie i zamknięcie aktualnego planu jeśli taki istnieje.
     * Zapobiega to utracie danych
     */
    override fun getPlanFromBox() {
        val plan = getPlanChoiceBox.value
        if (plan == null)
        {
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.noPlan"), MessageBundle.getMess("warning.shouldChoosePlanFirst"))
        }
        else
        {
            val date = plan.split("_")[1]
            val tableHoursLeft = "group_subject_hours_left_$date"

            val shouldSaveOldPlan = plansModel.shouldSaveOldPlan()

            //najpierw trzeba zapisać stary plan
            if (shouldSaveOldPlan) {
                showDialogYesNoMessage(MessageBundle.getMess("question.askBeforeGetAnotherPlan"), ActionType.EDIT)

                //Jeśli użytkownik chce zapisać stary plan i przywrócić wybrany
                if (wantToEdit) saveAndClosePlan() else return
            }

            plansModel.refillFromOldPlan(plan,tableHoursLeft)
            setActivePlan(plan)
            CommonUtils.clearBox(getPlanChoiceBox)
            clearAllBoxes()
            setupDatePickerActivePlan(LocalDate.parse(plan.split("_")[1]))
            PlansModel.notifyObservers()
        }
    }

    /**
     * Ustawia wartości gdy istnieje aktywny plan i wyświetla wiadomość czy aktywkny plan jest w pełni ułożony
     */
    private fun setActivePlan(plan: String)
    {
        inEditWeekMode = true
        activePlanExists = true
        if (this::activePlanLabel.isInitialized) activePlanLabel.text = "${MessageBundle.getMess("label.currentPlan")} $plan"
        if (!plansModel.isPlanFull()) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.warning"), MessageBundle.getMess("warning.planIsNotFull"))
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     * @param type Typ wykonywanej akcji
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

        dialog = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if (dialog.owner.isShowing) dialog.showAndWait()
    }


    /**
     *  Ustawia nasłuchiwanie kontrolki, aby monitorować ich zmiany
     *  i wywoływać odpowiednie akcje w zależności od tych zmian.
     */
    override fun setListeners() {
        datepicker.valueProperty().addListener { _, _, date ->
            fieldOfStudyChoiceBox.items.clear()
            classesModel.fieldOfStudy = null
            if (date!==null) onDatePickerChanged(date)
        }


        fieldOfStudyChoiceBox.valueProperty().addListener { _, _, field ->
            groupChoiceBox.items.clear()
            classesModel.group = null
            if (!field.isNullOrBlank())
            {
                classesModel.fieldOfStudy = field
                groupChoiceBox.items = classesModel.getGroups()
            }
        }

        groupChoiceBox.valueProperty().addListener { _, _, group ->
            subjectChoiceBox.items.clear()
            classesModel.subject=null
            if (!group.isNullOrBlank())
            {
                classesModel.group = group
                subjectChoiceBox.items = classesModel.getSubjects()
            }
        }

        subjectChoiceBox.valueProperty().addListener { _, _, subject ->
            locationChoiceBox.items.clear()
            classesModel.location = null
            if (!subject.isNullOrBlank())
            {
                classesModel.subject = subject
                installHoursLeftTooltip()
                locationChoiceBox.items = classesModel.getLocations()
            }
        }

        locationChoiceBox.valueProperty().addListener { _, _, location ->
            hourChoiceBox.items.clear()
            classesModel.hour=null
            if (!location.isNullOrBlank())
            {
                classesModel.location = location
                hourChoiceBox.items = classesModel.getHours()
            }
        }


        hourChoiceBox.valueProperty().addListener { _, _, hour ->
            roomChoicebox.items.clear()
            classesModel.room = null
            if (!hour.isNullOrBlank())
            {
                classesModel.hour = hour
                roomChoicebox.items = if (locationChoiceBox.value.equals(MessageBundle.getMess("label.platform")))
                    FXCollections.observableArrayList(MessageBundle.getMess("label.virtual"))
                    else classesModel.getRooms()


                val canGroupMoveBetweenClasses = classesModel.canGroupMoveBetweenClasses()
                if (!canGroupMoveBetweenClasses)
                {
                    CommonUtils.clearBox(roomChoicebox)
                    classesModel.room = null

                    showDialogOkMessage(MessageBundle.getMess("warning.noBreakBetweenChangeLocationForGroup"))
                }

            }
        }

        roomChoicebox.valueProperty().addListener { _, _, room ->
            teacherChoiceBox.items.clear()
            classesModel.teacher = null
            if (!room.isNullOrBlank())
            {
                classesModel.room = room
                teacherChoiceBox.items = classesModel.getTeachers()
                if (teacherChoiceBox.items.isEmpty())
                {
                    showHint()
                }
            }
        }

        teacherChoiceBox.valueProperty().addListener { _, _, teacher ->

            if (!teacher.isNullOrBlank())
            {
                classesModel.teacher = teacher
                val canTeacherMoveBetweenClasses = classesModel.canTeacherMoveBetweenClasses()
                if (!canTeacherMoveBetweenClasses)
                {
                    CommonUtils.clearBox(teacherChoiceBox)
                    classesModel.teacher = null
                    showDialogOkMessage(MessageBundle.getMess("warning.noBreakBetweenChangeLocationForTeacher"))
                }
            }
        }
    }

    /**
     * Tworzy dymek informujący o liczbie godzin danego przedmiotu, która została do zrealizowania dla wybranej grupy
     */
    private fun installHoursLeftTooltip()
    {
        val hoursLeft = classesModel.getHowManyHoursLeft()
        if (this::tooltip.isInitialized) tooltip.uninstall()
        tooltip = MFXTooltip.of(subjectChoiceBox, "${MessageBundle.getMess("label.leftHours")} $hoursLeft")
        tooltip.install()
    }


    /**
     * Zapisuje plan zajęć
     */
    override fun savePlan() {
        try {
            createPlanTable()
            clearAllBoxes()
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlySaved"))

        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.createTableError"))
        }
        catch (e: EmptyPlanException) {
            MessageUtil.showErrorMessage(e.message!!, MessageBundle.getMess("warning.emptyPlan"))
        }

    }

    /**
     * Metoda generująca podpowiedź co można zrobić w przypadku braku nauczycieli do wyboru
     */
    private fun showHint()
    {
        //Pobierz listę nauczycieli, którzy uczą tego przedmiotu oraz godziny, w których mogliby poprowadzić zajęcia
        suggestedHours = classesModel.getHoursHint(hourChoiceBox.items)

        //Pobierz listę zajętych nauczycieli, którzy uczą przemdiotu
        suggestedBusyTeachers = classesModel.getBusyTeachersHints()

        if (suggestedHours.isNotEmpty()) showDialogOkMessage(MessageBundle.getMess("warning.noTeachersHint") +  suggestedHours)
        else if (suggestedBusyTeachers.isNotEmpty()) showDialogOkMessage(MessageBundle.getMess("warning.noTeachersNoHoursHint") + suggestedBusyTeachers)
        else  showDialogOkMessage(MessageBundle.getMess("warning.noTeachers.tryChangeDay"))
    }

    /**
     * Zapisuje i zamyka aktualny plan zajęć oraz resetuje plan
     */
    override fun saveAndClosePlan() {
        try {
            createPlanTable()
            resetPlan()
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), MessageBundle.getMess("success.plan.correctlySavedAndClose"))

        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.createTableError"))
        }
        catch (e: EmptyPlanException) {
            MessageUtil.showErrorMessage(e.message!!, MessageBundle.getMess("warning.emptyPlan"))
        }
    }


    /**
     * Usuwa wszystkie zajęcia z aktualnego planu i czyści kontrolki.
     */
    override fun resetPlan()
    {
        try {
            plansModel.refillHours()
            setDefaultValues()
            clearAllBoxes()
            PlansModel.notifyObservers()
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.resetPlanError"))
        }
    }


    /**
     * Tworzy nową tabelę planu zajęć w bazie danych o nazwie opartej na piątkowej dacie.
     * Tworzy również tabelę przechowującą liczbę godzin, która została do stworzenia pełnego planu i wypełnia ją.
     */
    @Throws(SQLException::class, EmptyPlanException::class)
    override fun createPlanTable() {
        plansModel.createNewPlan()
        CommonUtils.clearDatePicker(datepicker)
    }

    /**
     * Konfiguruje datepicker, tak aby umożliwiać użytkownikowi wybór tylko dat weekendowych oraz piątku oraz blokować
     * daty wcześniejsze niż obecna data.
     */
    override fun setupDatePicker() {

        this.datepicker.cellFactory = Function { t ->
            object : MFXDateCell(datepicker, t) {
                override fun updateItem(item: LocalDate) {
                    super.updateItem(item)

                    isDisable = item.isBefore(LocalDate.now()) || item.dayOfWeek !in setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

                    styleClass.setAll(
                        if (isDisable) {
                            "disabled-date"
                        } else if (!plansModel.checkIfPlanExists(item)) {
                            "enabled-date"
                        } else {
                            "plan-exists-date"
                        }
                    )
                }
            }
        }
    }

    /**
     * Wyświetla okno dialogowe z przyciskiem "OK".
     *
     * @param content Wiadomość do wyświetlenia.
     */
    override fun showDialogOkMessage(content: String) {

        val buttons = listOf("OK" to {})
        dialog = DialogUtils.showMessageDialogWithButtons(content, stage, buttons)
        if (dialog.owner.isShowing) dialog.showAndWait()
    }


    /**
     * Obsługuje zmianę daty w datepicker. W zależności od trybu (edycja lub nowy plan) oraz tego czy dla wybranej daty istnieje już plan
     * wyświetla odpowiednie komunikaty i podejmuje odpowiednie akcje.
     * @param new Wybrana data w kalendarzu
     */
    override fun onDatePickerChanged(new: LocalDate) {
        fieldOfStudyChoiceBox.items.clear()
        classesModel.fieldOfStudy = null
        tableExists = plansModel.checkIfPlanExists(new)

        //Jeśli plan istnieje
        if(tableExists && !inEditWeekMode)
        {
            showPlanExistsDialog()
            if (wantToEdit)
            {
                inEditWeekMode = true
                classesModel.date = new
                val startPlanDay = CommonUtils.getPlanStartDay(datepicker.value)
                setupDatePickerActivePlan(startPlanDay)
                getPlanFromDatepicker(startPlanDay)
                fieldOfStudyChoiceBox.items = classesModel.getFields()

            }
            else
            {
                CommonUtils.clearDatePicker(datepicker)
                classesModel.date = null
                inEditWeekMode=false
            }
        }
        //Jeśli w trybie edit to nie sprawdzaj czy tabela istnieje
        else if (inEditWeekMode)
        {
            classesModel.date = new
            fieldOfStudyChoiceBox.items = classesModel.getFields()
        }
        //Tworzenie nowego planu
        else
        {
            stage = getPlanButton.scene.window as Stage
            MessageUtil.showInfoMessage(MessageBundle.getMess("label.newPlan"),MessageBundle.getMess("label.startNewPlan"))
            classesModel.date = new
            fieldOfStudyChoiceBox.items = classesModel.getFields()
        }
    }


    /**
     * Wyświetla okno dialogowe informujące o istnieniu planu dla wybranej daty
     * i pyta, czy użytkownik chce go edytować
     */
    override fun showPlanExistsDialog() {
        if (!inEditWeekMode && this::activePlanLabel.isInitialized && !activePlanLabel.text.contains("plan_")) {
            showDialogYesNoMessage( MessageBundle.getMess("question.plan.exists.askIfWantCreate"), ActionType.EDIT)
        }
    }

    /**
     * Ustawia domyślne wartości
     */
    private fun setDefaultValues()
    {
        if (this::activePlanLabel.isInitialized) activePlanLabel.text = MessageBundle.getMess("label.currentPlan")
        activePlanExists = false
        inEditWeekMode=false
        setupDatePicker()
    }

    /**
     * Konfiguruje datepicker, tak aby umożliwiać użytkownikowi wybór dat tylko z aktualnego planu
     * @param startPlanDay początkowa data planu
     */
    override fun setupDatePickerActivePlan(startPlanDay: LocalDate) {
        datepicker.cellFactory = Function { t ->
            object : MFXDateCell(datepicker, t) {
                override fun updateItem(item: LocalDate) {
                    super.updateItem(item)
                    isDisable = (item != startPlanDay && item != startPlanDay.plusDays(1) && item != startPlanDay.plusDays(2))
                    styleClass.setAll(if (isDisable) "disabled-date" else "enabled-date")
                }
            }
        }
    }


    /**
     * Obsługuje akcję dodawania zajęć do planu w bazie danych. Sprawdza, czy dane są kompletnie wprowadzone,
     * a następnie dodaje zajęcia do planu i aktualizuje interfejs użytkownika.
     *
     * @throws MissingValueException Jeśli brakuje jakiejś wartości.
     */
    override fun onAddClassesPressed() {
        val messContent = classesModel.getMissingFieldContentIfError()
        if (messContent != null)
        {
            throw MissingValueException("$messContent")
        }

        try {
            //Tutaj bez powiadomienia o poprawnie dodanych zajęciach, żeby nie było to irytujące, gdy niepowodzenie dodania planu to wtedy powiadom
            classesModel.addToPlan()
            val startPlanDay = CommonUtils.getPlanStartDay(classesModel.date!!)
            setupDatePickerActivePlan(startPlanDay)
            activePlanExists = true
            inEditWeekMode = true
            if (this::activePlanLabel.isInitialized) activePlanLabel.text = "${MessageBundle.getMess("label.currentPlan")} plan_$startPlanDay"
            clearAllBoxes()
            if (this::tooltip.isInitialized) tooltip.uninstall()
        }catch (e: SQLException) {
            MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), MessageBundle.getMess("warning.addToPlanError"))
        }
    }


    /**
     * Czyści kontrolki z ich zawartości w interfejsie użytkownika.
     */
    override fun clearAllBoxes() {
        fieldOfStudyChoiceBox.items.clear()
        groupChoiceBox.items.clear()
        subjectChoiceBox.items.clear()
        locationChoiceBox.items.clear()
        hourChoiceBox.items.clear()
        roomChoicebox.items.clear()
        teacherChoiceBox.items.clear()

        CommonUtils.clearDatePicker(datepicker)
        CommonUtils.clearBox(fieldOfStudyChoiceBox)
        CommonUtils.clearBox(groupChoiceBox)
        CommonUtils.clearBox(subjectChoiceBox)
        CommonUtils.clearBox(locationChoiceBox)
        CommonUtils.clearBox(hourChoiceBox)
        CommonUtils.clearBox(roomChoicebox)
        CommonUtils.clearBox(teacherChoiceBox)

        classesModel.date = null
        //classesModel.fieldOfStudy = null
    }

    /**
     * Aktualizuje listę dostępnych planów po zmianie w bazie danych i ustawia kalendarz
     */
    override fun onPlansChanged() {
        getPlanChoiceBox.items = plansModel.getAllPlansExceptCurrent()
        if (!activePlanExists) setupDatePicker()
    }

    /**
     * Obsługuje zmianę zakładek w interfejsie użytkownika - czyści panel
     */
    override fun onTabsChanged() {
        CommonUtils.clearBox(getPlanChoiceBox)
        clearAllBoxes()
        if (this::tooltip.isInitialized) tooltip.uninstall()
    }

}


