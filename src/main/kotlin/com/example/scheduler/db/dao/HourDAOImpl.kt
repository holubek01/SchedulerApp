package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import javafx.collections.ObservableList

/**
 * Implementacja interfejsu `HourDAO` do obsługi operacji w bazie danych dla godzin
 */
class HourDAOImpl: HourDAO {
    /**
     * Metoda pobierająca listę wszystkich godzin.
     *
     * @return Lista wszystkich godzin
     */
    override fun getHours(): ObservableList<String> {
        val query = "SELECT hourRange FROM hours"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getString(1)}
    }
}