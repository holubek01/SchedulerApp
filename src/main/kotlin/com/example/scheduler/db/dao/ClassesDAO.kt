package com.example.scheduler.db.dao

import com.example.scheduler.models.ClassesToRead
import com.example.scheduler.models.ClassesToWrite
import javafx.collections.ObservableList
import java.time.LocalDate

interface ClassesDAO {
    fun getFields(): ObservableList<String>
    fun deleteClasses(classes: ClassesToRead)
    fun getFreeTeachers(subject: String, date: LocalDate, hour: String): ObservableList<String>
    fun getBusyTeachers(subject: String, date: LocalDate, hour: String, subjects: ObservableList<String>, location:String): ObservableList<String>?
    fun changeTeachers(firstTeacher: String, secondTeacher: String, classes: ClassesToRead)
    fun setAnotherTeacher(firstTeacher: String, secondTeacher: String, classes: ClassesToRead)
    fun getFreeRooms(classes: ClassesToRead, locations: ObservableList<String>): ObservableList<String>
    fun getBusyRooms(classes: ClassesToRead): ObservableList<String>
    fun setAnotherRoom(selectedItem: String, classes: ClassesToRead)
    fun changeRooms(selectedItem: String, classes: ClassesToRead)
    fun getFreeRoomsByHour(date: LocalDate, hour: String, location: String): ObservableList<String>
    fun changeHours(selectedItem: String, hour: String, classes: ClassesToRead)
    fun getFreeHours(classesToEdit: ClassesToRead): ObservableList<String>?
    fun getGroups(field: String): ObservableList<String>
    fun getSubjects(group:String, field: String): ObservableList<String>
    fun getHours(day:LocalDate, group:String, field: String): ObservableList<String>
    fun getRooms(day: LocalDate, hour:String, location:String): ObservableList<String>
    fun getTeachers(day: LocalDate, hour:String, subject: String): ObservableList<String>
    fun addToPlan(classes: ClassesToWrite)
    fun canTeacherMoveBetweenClasses(firstname: String, lastname: String, location: String, hour: String, date: String): Boolean
    fun canGroupMoveBetweenClasses(group: String, field: String, location: String, hour: String, date: String): Boolean
    fun getLocations(day: LocalDate): ObservableList<String>
    fun getTeacherWithHoursHint(subject: String, date: LocalDate):ObservableList<Pair<String, String>>
    fun getBusyTeachersHint(subject: String, date:String, hour:String): ObservableList<String>
    fun changeHoursPlatform(hour: String, classes: ClassesToRead)
    fun getHowManyHoursLeft(group: String, field: String, subject: String): Int

}