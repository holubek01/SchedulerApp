package com.example.scheduler.db.dao

import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.objects.PlanForRooms
import javafx.collections.ObservableList
import java.time.LocalDate

interface PlanDAO {
    fun deletePlan(planName: String, hoursLeftName: String)
    fun getAllPlans(): ObservableList<String>
    fun getMaxDateFromTable(): LocalDate
    fun getPlan(location: String, rooms: ObservableList<String>): ObservableList<PlanForRooms>
    fun getPlanForPlatform(): ObservableList<PlanForRooms>
    fun getPlanTeacher(teacher: String, day:String): ObservableList<ClassesToRead>
    fun getPlanGroup(field: String, group: String, day: String): ObservableList<ClassesToRead>
    fun createTable(tableName: String, fromTable: String)
    fun shouldSaveOldPlan(): Boolean
    fun refillHours()
    fun checkIfPlanExists(day: LocalDate): Boolean
    fun isPlanFull(): Boolean
    fun refillFromOldPlan(tablePlan: String, tableHoursLeft: String)
    fun copyTable(tableTo: String, tableFrom: String, oldFriday: String, oldSaturday: String, oldSunday: String, newFriday: String, newSaturday: String, newSunday: String, )

}