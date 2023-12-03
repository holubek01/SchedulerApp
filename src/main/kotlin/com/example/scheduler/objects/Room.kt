package com.example.scheduler.objects

/**
 * Klasa reprezentująca salę lekcyjną
 *
 * @property roomName       Nazwa sali lekcyjnej
 * @property location       Nazwa lokalizacji (budynku), w której znajduje się sala
 * @property volume         Pojemność sali (liczba grup, która sala pomieści)
 * @property floor          Piętro, na którym znajduje się sala
 */
data class Room(
    var roomName: String,
    val location: String,
    val volume: Int,
    val floor: Int,
)