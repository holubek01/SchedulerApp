package com.example.scheduler.controller

import com.example.scheduler.objects.Location
import io.github.palexdev.materialfx.controls.*
import javafx.scene.control.Label
import javafx.scene.layout.VBox

interface ILocationsModuleController {
    fun showLocations()
    fun setupTable()
    fun createSteps()
    fun performActionsBetweenSteps()
    fun showContextMenu(selectedRowIndex: Int, selectedItem: Location)
    fun setOnLocationSelected()
    fun setupLocationForm()
    fun setConstraints(locationName: MFXTextField, city: MFXTextField, street: MFXTextField, postcode: MFXTextField)
    fun createAndShowDialog(vBox: VBox)
    fun editLocation(locationToEdit: Location)
    fun updateLocation(location: Location)
    fun deleteLocation(locationToDelete: Location)
    fun uploadFileRooms(filePath:String, location: String)
    fun clearAllLocationForm()
    fun setUpFirstStepForm()
    fun setUpSecondStepForm()
    fun createFirstStep(): MFXStepperToggle
    fun createSecondStep(): MFXStepperToggle
    fun handleActionsAfterFirstStep()
    fun handleActionsAfterLastStep()
    fun createCompletedStep()
    fun setOnResetButton()
    fun createUpdateButton(errorLocation: Label, errorCity: Label, errorStreet: Label, errorPostcode: Label):MFXButton
}