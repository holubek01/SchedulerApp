package com.example.scheduler.controller

import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.ActionType
import io.github.palexdev.materialfx.controls.*
import javafx.scene.control.Label
import javafx.scene.layout.VBox

interface ITeacherModuleController {
    fun showTeachers()
    fun setupTable()
    fun addTeacher(teacher: Teacher, subjects: List<String>)
    fun updateTeacher(
        teacher: Teacher,
        subjects: List<String>,
        deletedAvailability: MutableMap<String, MutableList<String>>
    )
    fun deleteTeacher(teacher: Teacher)
    fun createSteps(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, stepper: MFXStepper, labelTeacher: Label, labelSubjects: Label, labelAvailability: Label, allHoursToggle: MFXToggleButton)
    fun showContextMenu(selectedRowIndex: Int, selectedItem: Teacher)
    fun setOnTeacherSelected()
    fun setOnDaySelected(dayBox: MFXComboBox<String>, hourBox: MFXCheckListView<String>)
    fun setupTeacherForm(teacherLabel: Label, firstName: MFXTextField, lastName: MFXTextField, email: MFXTextField,phone: MFXTextField, subjects: MFXCheckListView<String>, subjectLabel: Label, availabilityLabel: Label, hours: MFXCheckListView<String>, days: MFXComboBox<String>, allHours: MFXToggleButton)
    fun setConstraints(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField)
    fun editTeacher(teacherToEdit: Teacher)
    fun showAvailability(teacher: Teacher)
    fun showSubjects(teacher: Teacher)
    fun createAndShowDialog(vBox: VBox)
    fun clearBoxes()
    fun performActionsBetweenSteps(subjects: MFXCheckListView<String>, firstname: MFXTextField, lastname: MFXTextField, phone: MFXTextField, email: MFXTextField, stepper: MFXStepper)
    fun setOnDialogClosing()
    fun showDialogYesNoMessage(content: String, type: ActionType)
    fun setUpUIComponents()
    fun createStep1(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, stepper: MFXStepper, labelTeacher: Label):MFXStepperToggle
    fun createStep2(labelSubjects: Label, subjects: MFXCheckListView<String>):MFXStepperToggle
    fun createStep3(labelAvailability: Label, days: MFXComboBox<String>, hours: MFXCheckListView<String>, allHoursToggle: MFXToggleButton):MFXStepperToggle
    fun setOnResetButton(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, labelTeacher: Label, labelSubjects: Label, labelAvailability: Label, allHoursToggle: MFXToggleButton, stepper: MFXStepper)
    fun createCompletedStep(firstName: MFXTextField, lastName: MFXTextField, phone: MFXTextField, email: MFXTextField, subjects: MFXCheckListView<String>, days: MFXComboBox<String>, hours: MFXCheckListView<String>, labelTeacher: Label, labelSubjects: Label, labelAvailability: Label, allHoursToggle: MFXToggleButton, step3: MFXStepperToggle, stepper: MFXStepper)
    fun handleActionsAfterFirstStep(stepper: MFXStepper, firstname: MFXTextField, lastname: MFXTextField, phone: MFXTextField, email: MFXTextField)
    fun checkDBWhileEditing(teacher: Teacher)
    fun checkDBWhileAdding(teacher: Teacher)
    fun handleActionsAfterSecondStep(stepper: MFXStepper, subjects: MFXCheckListView<String>)
    fun setUpFirstStepForm(teacherLabel: Label, firstName: MFXTextField, lastName: MFXTextField, email: MFXTextField, phone: MFXTextField)
    fun setUpSecondStepForm(subjects: MFXCheckListView<String>, subjectLabel: Label)
    fun setUpThirdStepForm(days: MFXComboBox<String>, availabilityLabel: Label, hours: MFXCheckListView<String>, allHours: MFXToggleButton)
    fun showAddTeacherFromFileDialog()
    fun uploadFileTeachers(filePath: String)
}