package com.example.scheduler.controller

import javafx.collections.ObservableList
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDate

interface IShowPlanRoomsController {
    fun setupTable(rooms: ObservableList<String>)
    fun showPlan()
    fun exportPlan()
    fun fillTable(sheet: XSSFSheet, cellStyle: XSSFCellStyle)
    fun setTexts()
}