package com.example.scheduler.objects

/**
 * Klasa reprezentująca lokalizację (budynek).
 *
 * @property locationName      Nazwa lokalizacji
 * @property city              Miasto, w którym znajduje się lokalizacja
 * @property street            Ulica, na której znajduje się lokalizacja
 * @property postcode          Kod pocztowy
 */
data class Location(
    var locationName: String,
    var city: String,
    var street: String,
    val postcode: String
)
