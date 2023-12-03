package com.example.scheduler.db.dao

import javafx.collections.ObservableList

interface HourDAO {
    fun getHours(): ObservableList<String>

}