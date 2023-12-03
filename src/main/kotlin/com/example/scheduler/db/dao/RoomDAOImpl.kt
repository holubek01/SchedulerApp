package com.example.scheduler.db.dao

import com.example.scheduler.db.DBQueryExecutor
import com.example.scheduler.objects.Room
import javafx.collections.ObservableList
import java.sql.SQLException

/**
 * Implementacja interfejsu `RoomDAO` do obsługi operacji w bazie danych dla sal
 */
class RoomDAOImpl: RoomDAO {

    /**
     * Pobiera listę sal z danej lokalizacji z bazy danych.
     *
     * @param location Nazwa lokalizacji.
     * @return         Lista obiektów Room reprezentujących sale.
     */
    override fun getRooms(location: String): ObservableList<Room> {
        val query = "SELECT roomName, volume, floor, l.name FROM rooms " +
                "INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID " +
                "INNER JOIN location l on lr.locationID = l.locationId " +
                " WHERE lr.locationID = (SELECT locationID " +
                "FROM location WHERE name = ? )"


        return DBQueryExecutor.executeQuery(query, location){
                resultSet ->
            Room(
                roomName = resultSet.getString(1),
                volume = resultSet.getInt(2),
                floor = resultSet.getInt(3),
                location = resultSet.getString(4))
        }
    }

    /**
     * Metoda pobierająca id podanej sali.
     *
     * @param room Obiekt Room, którego id chcemy uzyskać
     * @return Id sali
     */
    override fun getRoomID(room: Room): Int {
        val query = "SELECT lr.roomID FROM rooms INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID " +
                "WHERE roomName = ? AND lr.locationID = (SELECT locationID FROM location AS l WHERE " +
                " l.name = ? ) AND volume = ? AND floor = ?"

        return DBQueryExecutor.executeQuery(query,
            room.roomName,
            room.location,
            room.volume,
            room.floor
        )
        {resultSet -> resultSet.getInt(1)}.first()
    }

    /**
     * Sprawdza, czy sala w danej lokalizacji, istnieje w bazie danych.
     *
     * @param roomName          Nazwa sali
     * @param locationName      Nazwa lokalizacji
     * @return                  `true`, jeśli sala istnieje w danej lokalizacji; `false` w przeciwnym razie.
     */
    override fun checkIfRoomInDb(roomName: String, locationName: String): Boolean {
        val query = "SELECT COUNT(*) FROM rooms INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID " +
                "WHERE roomName = ? AND locationID = (SELECT locationID FROM location WHERE location.name = ? ) "

        return DBQueryExecutor.executeQuery(
            query,
            roomName,
            locationName
        ){ resultSet -> resultSet.getBoolean(1) }.first()
    }

    /**
     * Metoda dodająca salę do bazy danych przy użyciu procedury z bazy danych.
     *
     * @param room Sala do dodania do bazy danych
     */
    @Throws(SQLException::class)
    override fun addRoom(room: Room) {
        val query = "{ CALL addRoom(?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            room.roomName,
            room.volume,
            room.floor,
            room.location
        )
    }

    /**
     * Metoda aktualizująca salę w bazie danych przy użyciu procedury z bazy danych.
     *
     * @param roomID Id sali do aktualizacji
     * @param room Sala ze zaktualizowanymi danymi
     */
    @Throws(SQLException::class)
    override fun updateRoom(roomID: Int, room: Room) {
        val query = "{ CALL updateRoom(?,?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            roomID,
            room.roomName,
            room.location,
            room.volume,
            room.floor
        )
    }

    /**
     * Metoda usuwająca salę z bazy danych.
     * Przy usuwaniu sali, zajęcia, które planowo miały się odbyć
     * w niej są zastępowane salą wirtualną
     *
     * @param room Sala do usunięcia
     */
    @Throws(SQLException::class)
    override fun deleteRoom(room: Room) {
        val query = "{ CALL deleteRoom(?,?,?,?) }"
        DBQueryExecutor.executePreparedStatement(
            query,
            room.roomName,
            room.volume,
            room.floor,
            room.location
        )
    }

    /**
     * Sprawdza, czy sala (inna niż 'lastSelectedRoom') w danej lokalizacji, istnieje w bazie danych.
     *
     * @param roomName              Nazwa sali
     * @param location              Nazwa lokalizacji
     * @param lastSelectedRoom Sala do pominięcia przy sprawdzaniu
     * @return                      `true`, jeśli sala istnieje w danej lokalizacji; `false` w przeciwnym razie.
     */
    override fun checkIfRoomInDbEdit(roomName: String, location: String, lastSelectedRoom: Room): Boolean {
        val query = "SELECT COUNT(*) FROM rooms INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID " +
                "WHERE roomName = ? AND locationID = (SELECT locationID FROM location WHERE location.name = ? ) " +
                "AND rooms.roomID != (SELECT r.roomID FROM rooms AS r INNER JOIN location_room AS lr ON lr.roomID = r.roomID WHERE r.roomName = ? AND r.volume = ? AND r.floor=? " +
                "AND lr.locationID = (SELECT locationID FROM location WHERE location.name = ? ))"

        return DBQueryExecutor.executeQuery(
            query,
            roomName,
            location,
            lastSelectedRoom.roomName,
            lastSelectedRoom.volume,
            lastSelectedRoom.floor,
            lastSelectedRoom.location
        ){ resultSet -> resultSet.getBoolean(1) }.first()
    }

    /**
     * Pobiera wszystkie sale dla określonej lokalizacji.
     *
     * @param location Nazwa lokalizacji.
     * @return Lista sal w danej lokalizacji.
     */
    override fun getRoomsByLocation(location: String): ObservableList<String> {
        val query = "SELECT roomName from rooms " +
                "INNER JOIN location_room AS lr ON lr.roomID = rooms.roomID " +
                "INNER JOIN location l on l.locationID = lr.locationID " +
                "WHERE l.name = ?"

        return DBQueryExecutor.executeQuery(query,location){
                resultSet -> resultSet.getString(1) }
    }
}