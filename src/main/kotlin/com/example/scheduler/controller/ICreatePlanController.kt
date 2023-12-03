package com.example.scheduler.controller

import com.example.scheduler.utils.ActionType
import java.time.LocalDate
import javax.swing.Action

interface ICreatePlanController {
    fun onAddClassesPressed()
    fun clearAllBoxes()
    fun showPlanExistsDialog()
    fun onDatePickerChanged(new: LocalDate)
    fun setupDatePicker()
    fun setListeners()
    fun savePlan()
    fun showDialogOkMessage(content: String)
    fun setupDatePickerActivePlan(startPlanDay: LocalDate)
    fun createPlanTable()
    fun saveAndClosePlan()
    fun resetPlan()
    fun getPlanFromBox()
    fun getPlanFromDatepicker(startPlanDay: LocalDate)
    fun deletePlan()
    fun showDialogYesNoMessage(content: String, type: ActionType)
    fun setTexts()
    fun setActions()
}