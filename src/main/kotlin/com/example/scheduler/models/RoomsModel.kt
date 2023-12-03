package com.example.scheduler.models;

import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.db.dao.RoomDAOImpl
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Room
import com.example.scheduler.objects.Subject
import com.example.scheduler.utils.MessageBundle
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException

/**
 * Model zawierający logikę biznesową związaną z Room
 */
class RoomsModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = RoomDAOImpl()
    private var rooms = FXCollections.observableArrayList<Room>()

    /**
     * @see RoomDAOImpl.getRooms
     */
    fun getRooms(location: String): ObservableList<Room> {
        val loc = if (location == MessageBundle.getMess("label.platform")) "Platform" else location
        val rooms = dao.getRooms(loc)
        val modifiedRooms = rooms.map { room -> if (room.roomName == "Virtual") { room.copy(roomName = MessageBundle.getMess("label.virtual")) } else { room } }
        return FXCollections.observableArrayList(modifiedRooms)
    }

    /**
     * @see RoomDAOImpl.checkIfRoomInDb
     */
    private fun checkIfRoomInDb(roomName: String, location:String): Boolean {
        return dao.checkIfRoomInDb(roomName, location)
    }

    /**
     * @see RoomDAOImpl.checkIfRoomInDbEdit
     */
    private fun checkIfRoomInDbEdit(roomName: String, location: String, lastSelectedRoom: Room): Boolean {
        return dao.checkIfRoomInDbEdit(roomName, location, lastSelectedRoom)
    }

    /**
     * @see RoomDAOImpl.addRoom
     */
    @Throws(SQLException::class)
    fun addRoom(room: Room) {
        dao.addRoom(room)
    }

    /**
     * @see RoomDAOImpl.updateRoom
     */
    @Throws(SQLException::class)
    fun updateRoom(roomID: Int, updatedRoom: Room) {
        dao.updateRoom(roomID, updatedRoom)
    }

    /**
     * @see RoomDAOImpl.deleteRoom
     */
    @Throws(SQLException::class)
    fun deleteRoom(room: Room) {
        dao.deleteRoom(room)
    }

    /**
     * @see RoomDAOImpl.getRoomID
     */
    fun getRoomID(lastSelectedRoom: Room): Int {
        return dao.getRoomID(lastSelectedRoom)
    }

    /**
     * Metoda dodająca sale do bazy danych. Najpierw usuwa duplikaty
     * @return True jeśli istnieją duplikaty, False w przeciwnym przypadku
     */
    @Throws(SQLException::class)
    fun removeDuplicatesAndAddRooms(roomsToAdd: MutableList<Room>): Boolean {
        val duplicatesExists = roomsToAdd.groupBy { it.roomName }.filter { it.value.size > 1 }.isNotEmpty()
        for (room in roomsToAdd.distinctBy { it.roomName }) dao.addRoom(room)
        return duplicatesExists
    }

    /**
     * Metoda sprawdzająca istnienie podobnych danych podczas dodawania lub edytowania sali
     * @param location Lokalizacja sali
     * @param roomName Nazwa sali
     * @param lastSelectedRoom Zaznaczona sala (podczas edycji)
     * @param inEditMode Flaga informująca o trybie edycji
     */
    @Throws(SQLException::class)
    fun checkDB(location: String, roomName: String, inEditMode: Boolean, messageShown: Boolean, lastSelectedRoom: Room? = null) {
        if (location.isBlank())
        {
            throw DuplicatesException(MessageBundle.getMess("warning.noLocation"))
        }
        else if (!inEditMode)
        {
            if(checkIfRoomInDb(roomName, location))
            {
                throw DuplicatesException(MessageBundle.getMess("warning.roomAlreadyInDB"))
            }
        }
        else
        {
            if(checkIfRoomInDbEdit(roomName, location, lastSelectedRoom!!))
            {
                throw DuplicatesException(MessageBundle.getMess("warning.roomAlreadyInDB"))
            }
            else if (!messageShown && checkIfRoomInDb(roomName, location))
            {
                throw IdenticalObjectExistsException(MessageBundle.getMess("warning.roomExistsInEdit"))
            }
        }
    }
}
