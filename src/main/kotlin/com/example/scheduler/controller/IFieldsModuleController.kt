package com.example.scheduler.controller

import com.example.scheduler.objects.Field
import com.example.scheduler.objects.TeachingPlan
import com.example.scheduler.utils.ActionType
import io.github.palexdev.materialfx.controls.*
import javafx.scene.control.Label
import javafx.scene.layout.VBox

interface IFieldsModuleController {
    fun showFields()
    fun setOnFieldSelected()
    fun setupTable()
    fun showContextMenu(selectedRowIndex: Int, selectedItem: Field)
    fun editField(field: Field)
    fun deleteField(field: Field)
    fun setupFieldForm()
    fun setConstraints(fieldName: MFXTextField, fieldShort: MFXTextField)
    fun createSteps()
    fun addField(field: Field)
    fun updateField()
    fun setActions()
    fun createFirstsStep(): MFXStepperToggle
    fun createSecondStep(): MFXStepperToggle
    fun createThirdStep(): MFXStepperToggle
    fun handleActionAfterFirstStep()
    fun handleActionAfterSecondStep()
    fun performActionsBetweenSteps()
    fun showSPN(field: Field)
    fun createAndShowDialog(vBox: VBox)
    fun showDialogYesNoMessage(content: String, type: ActionType)
    fun clearBoxes()
    fun checkWhileEditing(fieldName: String, shotcut: String)
    fun createSPNtable(field: Field): MFXTableView<TeachingPlan>
    fun onUpdateButtonPressed(errorFieldName: Label, errorShortcut: Label)
    fun checkIfSemComboBoxEmpty()
    fun checkDbWhileAdding(fieldName: String, shotcut: String, sem: MFXComboBox<Int>)
}