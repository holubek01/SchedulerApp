package com.example.scheduler.controller


import com.example.scheduler.controller.observers.AdminTabsObserver
import com.example.scheduler.controller.observers.PlansObserver
import com.example.scheduler.controller.observers.TabsObserver
import com.example.scheduler.models.PlansModel
import com.example.scheduler.utils.*
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXCheckListView
import io.github.palexdev.materialfx.controls.MFXDatePicker
import io.github.palexdev.materialfx.controls.cell.MFXDateCell
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.stage.Stage
import java.sql.SQLException
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.function.Function

/**
 * Klasa kontrolera modułu CopyPlanModule do zarządzania planami (kopiowanie i usuwanie)
 */
class CopyPlanModuleController: ICopyPlanModuleController, PlansObserver, AdminTabsObserver, TabsObserver{

    /**
     * Przycisk służący do kopiowania aktualnego planu na wybraną datę.
     */
    @FXML
    internal lateinit var copyCurrentPlan: MFXButton

    /**
     * Etykieta informująca o kopiowaniu planu na wybraną datę.
     */
    @FXML
    internal lateinit var copySinglePlanLabel: Label

    /**
     * Kontrolka służąca do wyboru daty, na którą ma zostać skopiowany plan.
     */
    @FXML
    internal lateinit var datepicker: MFXDatePicker

    /**
     * Przycisk do usuwania wybranych na liście planów.
     */
    @FXML
    internal lateinit var deleteChosenPlansButton: MFXButton

    /**
     * Pomocnicze okno dialogowe.
     */
    private lateinit var dialog: MFXStageDialog

    /**
     * Lista służąca do wyboru planów do usunięcia.
     */
    @FXML
    internal lateinit var allPlansListView: MFXCheckListView<String>

    /**
     * Przycisk do kopiowania aktualnego planu na wybrany zakres dni.
     */
    @FXML
    internal lateinit var copyCurrentPlanMulti: MFXButton


    /**
     * Etykieta informująca o kopiowaniu wielu planów.
     */
    @FXML
    internal lateinit var copyMultiplePlanLabel: Label

    /**
     * Kontrolka do wyboru początkowej daty zakresu do kopiowania planów.
     */
    @FXML
    internal lateinit var datepickerFrom: MFXDatePicker

    /**
     * Flaga informująca o chęci usunięcia planów.
     */
    var wantToDelete = false

    /**
     * Kontrolka do wyboru końcowej daty zakresu do kopiowania planów.
     */
    @FXML
    internal lateinit var datepickerTo: MFXDatePicker
    val plansModel = PlansModel()

    /**
     * Inicjalizacja kontrolera
     */
    @FXML
    fun initialize() {
        AdminTabObserver.addObserver(this)
        PlansModel.addObserver(this)
        TabObserver.addObserver(this)
        setActions()
        setLabels()
        allPlansListView.items= plansModel.getAllPlansExceptCurrent()
        datepicker.locale = MessageBundle.bundle.locale
        datepickerFrom.locale = MessageBundle.bundle.locale
        datepickerTo.locale = MessageBundle.bundle.locale
    }

    /**
     * Ustawia teksty na przyciskach i kontrolkach.
     */
    override fun setLabels() {
        deleteChosenPlansButton.text = MessageBundle.getMess("label.deletePlans")
        copySinglePlanLabel.text = MessageBundle.getMess("label.copyPlan")
        copyMultiplePlanLabel.text = MessageBundle.getMess("label.copyPlans")
        copyCurrentPlan.text = MessageBundle.getMess("label.copyPlanButton")
        copyCurrentPlanMulti.text = MessageBundle.getMess("label.copyPlansButton")
        datepicker.promptText = MessageBundle.getMess("label.chooseDay")
        datepickerFrom.promptText=MessageBundle.getMess("label.chooseStartRange")
        datepickerTo.promptText=MessageBundle.getMess("label.chooseEndRange")
    }

    /**
     * Ustawia akcje komponentów.
     */
    override fun setActions() {
        datepicker.setOnMouseClicked { setupDatePickers() }
        copyCurrentPlanMulti.setOnAction { copyPlanMulti() }
        datepickerFrom.setOnMouseClicked { setupDatePickers() }
        datepickerTo.setOnMouseClicked { setupDatePickers() }
        deleteChosenPlansButton.setOnAction {
            showDialogYesNoMessage(MessageBundle.getMess("question.plans.askBeforeDelete"))
            deleteChosenPlans(allPlansListView.selectionModel.selectedValues) }
        copyCurrentPlan.setOnAction {
            if (datepicker.value == null) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.copyPlansError"), MessageBundle.getMess("warning.noData"))
            else copyPlan(datepicker.value)
        }
    }

    /**
     * Usuwa wybrane plany.
     * @param selectedValues Lista planów do usunięcia.
     */
    override fun deleteChosenPlans(selectedValues: MutableList<String>)
    {
        if (wantToDelete)
        {
            for (plan in selectedValues)
            {
                val hoursLeftTable = "group_subject_hours_left_${plan.split("_").last()}"
                try {
                    plansModel.deletePlan(plan, hoursLeftTable)
                    MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.plan.correctlyDeletedPlan")}: $plan")
                }catch (e: SQLException) {
                    MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), "${MessageBundle.getMess("warning.deletePlanError")}: $plan")
                }
            }
        }
    }


    /**
     * Kopiuje plan na podany zakres dat.
     */
    override fun copyPlanMulti() {
        val from = datepickerFrom.value
        val to = datepickerTo.value

        if (from==null || to == null) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.copyPlansError"), MessageBundle.getMess("warning.noRangeSelected"))
        else if (!to.isBefore(from))
        {
            val daysArray = plansModel.createArrayOfDays(from, to)
            for (day in daysArray) copyPlan(day)
        }
        else{
            MessageUtil.showWarningMessage(MessageBundle.getMess("warning.copyPlansError"), MessageBundle.getMess("warning.copyPlansErrorContent"))
        }

        CommonUtils.clearDatePicker(datepickerTo)
        CommonUtils.clearDatePicker(datepickerFrom)
    }


    /**
     * Kopiuje aktualny plan na podaną datę.
     *
     * @param value      Początek tygodnia, na który ma zostać skopiowany plan.
     */
    override fun copyPlan(value: LocalDate) {
        //Najpierw upewnij się, że aktualny plan nie jest pusty
        val currentPlanIsEmpty = !plansModel.shouldSaveOldPlan()

        if (currentPlanIsEmpty) MessageUtil.showWarningMessage(MessageBundle.getMess("warning.copyPlanError"), "${MessageBundle.getMess("warning.currentPlanEmpty")}: plan_$value")
        else {
            //Plan istnieje i nie jest pusty
            val day = CommonUtils.getPlanStartDay(plansModel.getMaxDateFromTable())
            try {
                plansModel.copyTable("plan_$value", "plan", day.toString(), day.plusDays(1).toString(), day.plusDays(2).toString(), value.toString(), value.plusDays(1).toString(), value.plusDays(2).toString())
                plansModel.createTable("group_subject_hours_left_$value", "group_subject_hours_left")
                MessageUtil.showInfoMessage(MessageBundle.getMess("label.operationSucceed"), "${MessageBundle.getMess("success.plan.correctlyCopiedPlan")}: plan_$value")
            }catch (e: SQLException) {
                MessageUtil.showErrorMessage(MessageBundle.getMess("warning.operationFailed"), "${MessageBundle.getMess("warning.copyPlanError")}: plan_$value")
            }
        }
        CommonUtils.clearDatePicker(datepicker)
    }

    /**
     * Konfiguruje kontrolki wyboru dat w zależności od tego, które plany już istnieją.
     * Blokuje wybór dat, dla których plany już istnieją oraz daty przeszłe
     */
    override fun setupDatePickers() {
        val allDatepickers = listOf(datepicker,datepickerFrom,datepickerTo)

        for (picker in allDatepickers)
        {
            picker.cellFactory = Function { t ->
                object : MFXDateCell(picker, t) {
                    override fun updateItem(item: LocalDate) {
                        super.updateItem(item)
                        isDisable = item.dayOfWeek != DayOfWeek.FRIDAY || plansModel.checkIfPlanExists(item) || item.isBefore(LocalDate.now())
                        styleClass.setAll(if (isDisable) "disabled-date" else "enabled-date")
                    }
                }
            }
        }
    }

    /**
     * Metoda wywoływana po zmianie (dodanie lub usunięcie) planów zajęć.
     */
    override fun onPlansChanged() {
        allPlansListView.selectionModel.clearSelection()
        allPlansListView.items= plansModel.getAllPlansExceptCurrent()
    }

    /**
     * Metoda wywoływana podczas zmiany zakładek przed użytkownika - czyści planel.
     */
    override fun onTabsChanged() {
        allPlansListView.selectionModel.clearSelection()
        CommonUtils.clearDatePicker(datepicker)
        CommonUtils.clearDatePicker(datepickerTo)
        CommonUtils.clearDatePicker(datepickerFrom)
    }

    /**
     * Wyświetla okno dialogowe z przyciskami "Tak" lub "Nie".
     *
     * @param content Wiadomość do wyświetlenia.
     */
    fun showDialogYesNoMessage(content: String) {
        val buttons = listOf(
            MessageBundle.getMess("label.yes") to { wantToDelete = true },
            MessageBundle.getMess("label.no") to { wantToDelete = false}
        )

        dialog = DialogUtils.showMessageDialogWithButtons(content, deleteChosenPlansButton.scene.window as Stage, buttons)
        if (dialog.owner.isShowing) dialog.showAndWait()
    }

}
