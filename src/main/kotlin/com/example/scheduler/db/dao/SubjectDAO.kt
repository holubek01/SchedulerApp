package com.example.scheduler.db.dao

import javafx.collections.ObservableList

interface SubjectDAO {
    fun getSubjects(): ObservableList<String>
    fun checkIfSubjectExists(subject: String): Boolean
}