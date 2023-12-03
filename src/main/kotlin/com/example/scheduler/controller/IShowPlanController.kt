package com.example.scheduler.controller

import com.example.scheduler.models.ClassesToRead
import io.github.palexdev.materialfx.controls.MFXComboBox
import io.github.palexdev.materialfx.controls.MFXListView
import javafx.collections.ObservableList
import javafx.scene.layout.VBox
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet

interface IShowPlanController {
    fun setupTable()
    fun showGroupPlan()
    fun showTeacherPlan()
    fun showEmptyPlanMessage()
    fun exportTeacherPlan()
    fun exportGroupPlan()
    fun fillTable(sheet: XSSFSheet, cellStyle: XSSFCellStyle, startNr: Int)
    fun fillTableAllGroups(sheet: XSSFSheet, cellStyle: XSSFCellStyle, plan: ObservableList<ClassesToRead>)
    fun exportAllGroupsFromField()
    fun exportAllTeachers()
    fun deleteFromPlan(classesToDelete: ClassesToRead)
    fun changeTeacher(classesToEdit: ClassesToRead)
    fun createAndShowDialog(vbox: VBox)
    fun changeRoom(classesToEdit: ClassesToRead)
    fun changeHour(classesToEdit: ClassesToRead)
    fun showContextMenu(selectedRowIndex: Int, selectedItem: ClassesToRead)
    fun setUpUI()
    fun setActions()
    fun createCells(row: XSSFRow, classes: ClassesToRead, cellStyle: XSSFCellStyle)
    fun setListeners()
    fun setTexts()
    fun setOnClassesSelected()
    fun clearAllBoxes()
    fun createChangeTeacherVbox(freeTeachers: MFXListView<String>, busyTeachers: MFXListView<String>): VBox
    fun setOnTeacherToChangeSelected(freeTeachers: MFXListView<String>, busyTeachers: MFXListView<String>, classesToEdit: ClassesToRead)
    fun setOnRoomToChangeSelected(freeRooms: MFXListView<String>, busyRooms: MFXListView<String>, classesToEdit: ClassesToRead)
    fun createChangeRoomVbox(freeRooms: MFXListView<String>, busyRooms: MFXListView<String>): VBox
    fun createChangeHourVbox(freeRooms: MFXListView<String>, hourChoicebox: MFXComboBox<String>, classesToEdit: ClassesToRead): VBox
}