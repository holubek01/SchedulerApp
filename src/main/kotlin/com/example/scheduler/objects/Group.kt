package com.example.scheduler.objects

/**
 * Klasa reprezentująca grupę zajęciową.
 *
 * @property fieldName      Nazwa kierunku kształcenia, do jakiego przypisana jest grupa
 * @property groupName      Nazwa grupy
 * @property sem            Semestr, na którym jest grupa
 */
data class Group(
    val groupName: String,
    val fieldName: String,
    val sem: Int
)