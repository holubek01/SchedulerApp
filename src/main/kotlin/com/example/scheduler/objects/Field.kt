package com.example.scheduler.objects

/**
 * Klasa reprezentująca kierunek kształcenia.
 *
 * @property fieldName      Nazwa kierunku kształcenia
 * @property semsNumber     Liczba semestrów na kierunku
 * @property shortcut       Skrót identyfikujący kierunek

 */
data class Field(
    var fieldName: String,
    val semsNumber: Int,
    val shortcut: String
)