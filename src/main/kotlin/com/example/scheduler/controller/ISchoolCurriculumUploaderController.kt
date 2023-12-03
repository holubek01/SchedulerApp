package com.example.scheduler.controller

interface ISchoolCurriculumUploaderController {
    fun chooseFiles()
    fun showDialogYesNoMessage(content: String)
    fun uploadSPNs()
    fun deleteExistingSPN(fieldName: String)
}