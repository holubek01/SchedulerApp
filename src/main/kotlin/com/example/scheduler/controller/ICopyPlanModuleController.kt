package com.example.scheduler.controller

import java.time.LocalDate

interface ICopyPlanModuleController {
    fun setupDatePickers()
    fun copyPlan(value: LocalDate)
    fun copyPlanMulti()
    fun setActions()
    fun setLabels()
    fun deleteChosenPlans(selectedValues: MutableList<String>)

}