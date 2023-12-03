package com.example.scheduler.db.dao

import com.example.scheduler.objects.Teacher
import javafx.collections.ObservableList

interface TeacherDAO {
    fun getTeacherSubjectsByName(teacher: String): ObservableList<String>
    fun getTeacherSubjects(teacher: Teacher): ObservableList<String>
    fun getTeacherID(teacher: Teacher): Int
    fun updateTeacher(teacherID: Int, teacher: Teacher, subjectsJSON: String)
    fun updateTeacherAvailability(teacherID: Int, day: String, hoursJSON: String)
    fun addAvailabilityToTeacher(day: String, hoursJSON: String)
    fun deleteTeacher(teacher: Teacher)
    fun checkIfTeacherInDbEdit(teacher: Teacher, selectedTeacher:Teacher): Boolean
    fun checkIfTeacherInDb(teacher: Teacher): Boolean
    fun getTeachers(): ObservableList<Teacher>
    fun addTeacher(teacher: Teacher, subjectsJSON: String)
    fun checkIfNameAndLastnameInDb(firstname: String, lastname:String): Boolean
    fun getAvailability(teacher: Teacher): ObservableList<String>?
    fun getAvailabilityByDay(teacher: Teacher, day: String): ObservableList<String>
    fun checkIfEmailInDb(email: String): Boolean
    fun checkIfPhoneInDbEdit(phone: String, selectedTeacher: Teacher): Boolean
    fun checkIfEmailInDbEdit(email: String, selectedTeacher: Teacher): Boolean
    fun checkIfNameAndLastnameInDbEdit(firstname: String, lastname:String, selectedTeacher: Teacher): Boolean
    fun checkIfPhoneInDb(phone: String): Boolean
    fun deleteClassesAssociatedToDeletedSubjectAndTeacher(subject: String, teacherID: Int)
    fun deleteClassesAssociatedToDeletedTeacherAndAvailability(day: String, hour: String, teacherID: Int)
}