package com.example.scheduler.db

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

/**
 * Obiekt służący do wykonywania zapytań do bazy danych.
 */
object DBQueryExecutor {
    /**
     * Metoda służąca do wykonywania przygotowanego wcześniej zapytania PreparedStatement do bazy danych.
     *
     * @param query   Zapytanie SQL w postaci tekstowej.
     * @param params  Lista parametrów przekazywanych do zapytania.
     */
    fun executePreparedStatement(query: String, vararg params: Any){
        val connection = DBConnection.getConnection()
        val statement = connection.prepareStatement(query)
        try {
            params.forEachIndexed { index, param ->
                statement.setObject(index + 1, param)
            }

            statement.execute()
        }catch (e: SQLException) {
            throw e
        } finally {
            statement.close()
            DBConnection.closeConnection()
        }
    }

    /**
     * Metoda służąca do wykonywania funkcji w bazie danych i zwrócenia wartości logicznej.
     *
     * @param query   Zapytanie SQL w postaci tekstowej wywołujące funkcję.
     * @param params  Lista parametrów przekazywanych do funkcji.
     * @return        Wartość logiczna zwracana przez funkcję.
     */
    fun executeFunction(query: String, vararg params: Any): Boolean{
        val connection = DBConnection.getConnection()
        val statement = connection.prepareCall(query)

        try {
            statement.registerOutParameter(1, Types.BOOLEAN);

            params.forEachIndexed { index, param ->
                statement.setObject(index + 2, param)
            }

            statement.execute()
            return statement.getInt(1) == 1
        } catch (e: SQLException) {
            throw e
        } finally {
            statement.close()
            DBConnection.closeConnection()
        }


    }

    /**
     * Metoda, służąca do wykonywania zapytania do bazy danych i mapowania
     * wyniku na obiekty używając podanego mapper-a.
     *
     * @param query   Zapytanie SQL w postaci tekstowej.
     * @param params  Lista parametrów przekazywanych do zapytania.
     * @param mapper  Funkcja mapująca wyniki zapytania na obiekty.
     * @return        Lista obiektów uzyskanych z wyników zapytania.
     */
    fun <T> executeQuery(query: String, vararg params: Any, mapper: (ResultSet)->T): ObservableList<T>
    {
        val connection = DBConnection.getConnection()
        val statement = connection.prepareStatement(query)

        try {
            params.forEachIndexed { index, param ->
                statement.setObject(index + 1, param)
            }

            val resultSet = statement.executeQuery()
            val resultList = FXCollections.observableArrayList<T>()

            while(resultSet.next()) {
                resultList.add(mapper(resultSet))
            }

            return resultList

        }catch (e: SQLException) {
            throw e
        } finally {
            statement.close()
            DBConnection.closeConnection()
        }
    }
}