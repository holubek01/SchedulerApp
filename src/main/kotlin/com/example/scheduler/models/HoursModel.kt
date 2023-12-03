package com.example.scheduler.models;

import com.example.scheduler.db.dao.HourDAOImpl
import com.example.scheduler.objects.Field
import javafx.collections.ObservableList

/**
 * Model zawierający logikę biznesową związaną z godzinami
 */
class HoursModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val hourDAO = HourDAOImpl()

    /**
     * @see HourDAOImpl.getHours
     */
    fun getHours(): ObservableList<String> {
        return hourDAO.getHours()
    }



}
