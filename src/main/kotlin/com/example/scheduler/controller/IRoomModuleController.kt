package com.example.scheduler.controller

import com.example.scheduler.objects.Room
import io.github.palexdev.materialfx.controls.MFXComboBox
import io.github.palexdev.materialfx.controls.MFXStepper
import io.github.palexdev.materialfx.controls.MFXStepperToggle
import io.github.palexdev.materialfx.controls.MFXTextField

interface IRoomModuleController {
    fun showRooms(location: String)
    fun setupTable()
    fun addRoom(room: Room)
    fun deleteRoom(room: Room)
    fun createSteps(roomName: MFXTextField, locationBox: MFXComboBox<String>, volumeBox: MFXComboBox<Int>, floorBox: MFXComboBox<Int>, stepperr: MFXStepper)
    fun performActionsBetweenSteps(stepper: MFXStepper, roomName: MFXTextField, location: MFXComboBox<String>)
    fun showDialogYesNoMessage(content: String)
    fun showContextMenu(selectedRowIndex: Int, selectedItem: Room)
    fun setupRoomForm(roomName: MFXTextField, locationBox: MFXComboBox<String>, floorBox: MFXComboBox<Int>, volumeBox: MFXComboBox<Int>)
    fun setConstraints(roomName: MFXTextField)
    fun setOnRoomSelected()
    fun editRoom(room: Room)
    fun updateRoom(roomID: Int, room: Room)
    fun setActions()
    fun handleActionAfterFirstStep(roomName: MFXTextField, location: MFXComboBox<String>, stepper: MFXStepper)
    fun setUpUI()
    fun createStep1(roomName: MFXTextField, stepperr: MFXStepper, locationBox: MFXComboBox<String>): MFXStepperToggle
    fun createStep2(volumeBox: MFXComboBox<Int>, floorBox: MFXComboBox<Int>): MFXStepperToggle
    fun createCompletedStep(roomName: MFXTextField, locationBox: MFXComboBox<String>, volumeBox: MFXComboBox<Int>, floorBox: MFXComboBox<Int>, stepperr: MFXStepper, step2: MFXStepperToggle)
    fun clearBoxes()
    fun setUpFirstStepForm(roomName: MFXTextField, locationBox: MFXComboBox<String>)
    fun setUpSecondStepForm(volumeBox: MFXComboBox<Int>, floorBox: MFXComboBox<Int>)
}