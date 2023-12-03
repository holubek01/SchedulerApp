package com.example.scheduler.models;

import com.example.scheduler.db.dao.SubjectDAOImpl
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Subject
import com.example.scheduler.objects.Teacher
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * Model zawierający logikę biznesową związaną z Subject
 */
class SubjectsModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = SubjectDAOImpl()
    private var subjects = FXCollections.observableArrayList<Subject>()

    /**
     * @see SubjectDAOImpl.getSubjects
     */
    fun getSubjectsNames(): ObservableList<String> {
        return dao.getSubjects()
    }

    /**
     * Metoda sprawdzająca czy przedmiot istnieje w bazie danych
     * @param subject Przedmiot do sprawdzenia
     */
    fun checkIfSubjectExists(subject: String): Boolean
    {
        return dao.checkIfSubjectExists(subject)
    }
}
