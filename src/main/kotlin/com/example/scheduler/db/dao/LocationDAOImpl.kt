package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.objects.Location
import javafx.collections.ObservableList
import java.sql.SQLException

/**
 * Implementacja interfejsu `LocationDAO` do obsługi operacji w bazie danych dla lokalizacji
 */
class LocationDAOImpl: LocationDAO {

    /**
     * Metoda pobierająca listę wszystkich lokalizacji oprócz Platformy.
     *
     * @return Lista wszystkich lokalizacji
     */
    override fun getLocations(): ObservableList<Location> {
        val query = "SELECT name, city, street, postcode FROM location AS l WHERE l.name != 'Platform' ORDER BY name"

        return DBQueryExecutor.executeQuery(query){
                resultSet ->
            Location(
                locationName = resultSet.getString(1),
                city = resultSet.getString(2),
                street = resultSet.getString(3),
                postcode = resultSet.getString(4)
            )
        }
    }


    /**
     * Metoda dodająca lokalizację do bazy danych przy użyciu procedury z bazy danych.
     *
     * @param location Lokalizacja do dodania do bazy danych
     */
    @Throws(SQLException::class)
    override fun addLocation(location: Location) {
        val query = "{ CALL addLocation(?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            location.locationName,
            location.city,
            location.street,
            location.postcode
        )
    }


    /**
     * Metoda aktualizująca lokalizację w bazie danych przy użyciu procedury z bazy danych.
     *
     * @param locationID Id lokalizacji do aktualizacji
     * @param location Lokalizacja do aktualizacji
     */
    @Throws(SQLException::class)
    override fun updateLocation(locationID: Int, location: Location) {
        val query = "{ CALL updateLocation(?,?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            locationID,
            location.locationName,
            location.city,
            location.street,
            location.postcode
        )
    }

    /**
     * Metoda usuwająca lokalizację z bazy danych.
     * Przy usuwaniu lokalizacji usuwane są także wszystkie sale z nią związane
     * a zajęcia, które planowo miały się odbyć w jednej z takich sal są zastępowane salą wirtualną
     *
     * @param location Lokalizacja do usunięcia
     */
    @Throws(SQLException::class)
    override fun deleteLocation(location: Location) {
        val query = "{ CALL deleteLocation(?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            location.locationName,
            location.city,
            location.street,
            location.postcode
        )
    }

    /**
     * Pobiera nazwy wszystkich lokalizacji z bazy danych, uporządkowane alfabetycznie.
     *
     * @return Lista nazw lokalizacji.
     */
    override fun getLocationsNames(): ObservableList<String> {
        val query = "SELECT name FROM location ORDER BY name"
        return DBQueryExecutor.executeQuery(query){ resultSet -> resultSet.getString(1) }
    }


    /**
     * Sprawdza, czy lokalizacja, która nie jest lokalizacją 'selectedLocation' istnieje w bazie danych.
     *
     * @param location          Nazwa lokalizacji do sprawdzenia.
     * @param lastSelectedLocation   Wybrana lokalizacja do edycji (pomijana).
     * @return                  `true`, jeśli lokalizacja istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfLocationInDbEdit(location: Location, lastSelectedLocation: Location): Boolean {
        val query = "SELECT COUNT(*) FROM location AS l\n" +
                "WHERE (l.name = ? AND l.city =\n" +
                "? AND l.street = ? AND l.postcode = ? )\n" +
                "AND l.locationId != (SELECT locationId FROM location AS l WHERE l.name =? AND l.city =? \n" +
                "AND l.street =? AND l.postcode = ?)"

        return DBQueryExecutor.executeQuery(query,
            location.locationName,
            location.city,
            location.street,
            location.postcode,
            lastSelectedLocation.locationName,
            lastSelectedLocation.city,
            lastSelectedLocation.street,
            lastSelectedLocation.postcode)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }


    /**
     * Sprawdza, czy lokalizacja, istnieje w bazie danych.
     *
     * @param location          Nazwa lokalizacji
     * @return                 `true`, jeśli lokalizacja istnieje; `false` w przeciwnym razie.
     */
    override fun checkIfLocationInDb(location: Location): Boolean {
        val query = "SELECT COUNT(*) FROM location AS l " +
                "WHERE l.name = ? AND l.city = ? AND l.street = ? AND l.postcode = ?"

        return DBQueryExecutor.executeQuery(query,
            location.locationName,
            location.city,
            location.street,
            location.postcode
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy nazwa lokalizacji lub miasto i ulica, które nie są przypisane do wybranej lokalizacji, istnieje w bazie danych
     * i jest różne od 'selectedLocation'.
     *
     * @param location          Lokalizacja do sprawdzenia
     * @param selectedLocation   Wybrana lokalizacja do edycji (pomijana).
     * @return                  `true`, jeśli dane istnieją, `false` w przeciwnym razie.
     */
    override fun checkIfLocationNameOrStreetInDbEdit(location: Location, selectedLocation: Location): Boolean {
        val query = "SELECT COUNT(*) FROM location AS l\n" +
                "WHERE (l.name = ? OR (l.city = ? AND l.street = ? ))\n" +
                "AND l.locationId != (SELECT locationId FROM location WHERE \n" +
                "location.name = ? AND location.city = ? AND location.street = ? AND location.postcode = ? )"

        return DBQueryExecutor.executeQuery(query,
            location.locationName,
            location.city,
            location.street,
            selectedLocation.locationName,
            selectedLocation.city,
            selectedLocation.street,
            selectedLocation.postcode
        )
        {resultSet -> resultSet.getBoolean(1)}.first()
    }

    /**
     * Sprawdza, czy nazwa lokalizacji lub miasto i ulica, istnieje w bazie danych.
     *
     * @param locationName      Nazwa lokalizacji
     * @param city              Nazwa miasta
     * @param street            Nazwa ulicy
     * @return                  `true`, jeśli dane istnieją; `false` w przeciwnym razie.
     */
    override fun checkIfLocationNameOrStreetInDb(locationName: String, city: String, street: String): Boolean {
        val query = "SELECT COUNT(*) FROM location AS l " +
                "WHERE l.name = ? OR ( l.city = ? AND l.street = ? ) "

        return DBQueryExecutor.executeQuery(query, locationName, city, street)
        {resultSet -> resultSet.getBoolean(1)}.first()
    }


    /**
     * Metoda pobierająca id podanej lokalizacji.
     *
     * @param lastSelectedLocation Obiekt Location, którego id chcemy uzyskać
     * @return Id lokalizacji
     */
    override fun getLocationID(lastSelectedLocation: Location): Int {
        val query = "SELECT locationID FROM location AS l " +
                "WHERE l.name = ? AND l.city = ? AND l.street = ? AND l.postcode = ?"

        return DBQueryExecutor.executeQuery(query,
            lastSelectedLocation.locationName,
            lastSelectedLocation.city,
            lastSelectedLocation.street,
            lastSelectedLocation.postcode
        )
        {resultSet -> resultSet.getInt(1)}.first()
    }

    /**
     * Metoda pobierająca lokalizację o podanym id.
     *
     * @param id    Id lokalizacji
     * @return      Obiekt Location reprezentujący lokalizację
     */
    override fun getLocationById(id: Int): Location {
        val query = "SELECT name, city, street, postcode FROM location AS l WHERE l.locationID = ?"

        return DBQueryExecutor.executeQuery(query, id){
                resultSet ->
            Location(
                locationName = resultSet.getString(1),
                city = resultSet.getString(2),
                street = resultSet.getString(3),
                postcode = resultSet.getString(4)
            )
        }.first()
    }

}