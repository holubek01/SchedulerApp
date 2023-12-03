package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import javafx.collections.ObservableList

/**
 * Implementacja interfejsu `SubejctDAO` do obsługi operacji w bazie danych dla przedmiotów
 */
class SubjectDAOImpl: SubjectDAO {
    /**
     * Metoda pobierająca listę wszystkich przedmiotów.
     *
     * @return Lista wszystkich przedmiotów
     */
    override fun getSubjects(): ObservableList<String> {
        val query = "SELECT subjectName FROM subjects ORDER BY subjectName"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getString(1)}
    }

    /**
     * Metoda sprawdzająca czy przedmiot istnieje w bazie.
     * @param subject Przedmiot, którego istnienie należy sprawdzić
     *
     * @return True jeśli przedmiot istnieje, False w przeciwnym wypadku
     */
    override fun checkIfSubjectExists(subject: String): Boolean {
        val query = "SELECT COUNT(*) FROM subjects WHERE subjectName = ?"
        return DBQueryExecutor.executeQuery(query, subject){ resultSet -> resultSet.getBoolean(1)}.first()
    }
}