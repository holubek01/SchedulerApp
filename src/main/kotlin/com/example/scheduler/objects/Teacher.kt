package com.example.scheduler.objects

/**
 * Klasa reprezentująca nauczyciela
 *
 * @property firstname      Imię nauczyciela
 * @property lastname       Nazwisko nauczyciela
 * @property email          Adres e-mail nauczyciela
 * @property phone          Numer telefonu nauczyciela
 */
data class Teacher(
    var firstname: String,
    var lastname: String,
    var email: String,
    var phone: String
)