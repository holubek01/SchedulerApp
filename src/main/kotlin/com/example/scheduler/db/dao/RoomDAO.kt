package com.example.scheduler.db.dao

import com.example.scheduler.objects.Room
import javafx.collections.ObservableList

interface RoomDAO {
    fun getRooms(location: String): ObservableList<Room>
    fun checkIfRoomInDb(roomName: String, locationName: String): Boolean
    fun addRoom(room: Room)
    fun deleteRoom(room: Room)
    fun getRoomID(room: Room): Int
    fun updateRoom(roomID: Int, room: Room)
    fun checkIfRoomInDbEdit(roomName: String, location: String, lastSelectedRoom: Room): Boolean
    fun getRoomsByLocation(location: String): ObservableList<String>
}