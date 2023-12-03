package com.example.scheduler.db.dao

import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Group
import com.example.scheduler.objects.Subject
import com.example.scheduler.objects.TeachingPlan
import javafx.collections.ObservableList

interface FieldDAO {
    fun addField(field: Field)
    fun checkIfFieldInDb(fieldName: String, shortcut: String): Boolean
    fun checkIfFieldNameInDb(fieldName: String): Boolean
    fun checkIfFieldNameInDbEdit(fieldName: String, selectedField: Field): Boolean
    fun checkIfFieldShortInDbEdit(fieldShort: String, selectedField: Field): Boolean
    fun checkIfFieldShortcutInDb(shortcut: String): Boolean
    fun checkIfFieldInDbEdit(fieldName: String, shortcut: String, fieldNameOld: String, shortcutOld: String): Boolean
    fun addGroups(field: Field, groupsNum: Int, sem: Int, shortcutRoman: String)
    fun deleteField(field: Field)
    fun showSPN(field: Field): ObservableList<TeachingPlan>
    fun getFields(): ObservableList<Field>
    fun getSemesters(field: String): Int
    fun getSubjectsByFieldAndSem(field: String, semester: String): ObservableList<Subject>
    fun getGroups(fieldName: String, sem: Int): ObservableList<String>
    fun deleteGroup(group: Group)
    fun addGroup(fieldName: String, sem: Int, romanNumber: String)
    fun getFieldID(lastSelectedField: Field): Int
    fun updateField(fieldID: Int, fieldName: String, shortcut: String)
    fun addSubjectToSem(subject: String, field:String, sem:Int, weeklyHours: Int)
    fun getGroupsByField(field: String): ObservableList<String>
    fun deleteSPN(fieldName: String)
    fun checkIfFieldExists(fieldName: String):Boolean
}