package com.example.scheduler.objects

/**
 * Klasa reprezentująca pojedyńczy przedmiot podczas odczytu ze szkolnego
 * planu nauczania z pliku Excel
 *
 * @property subjectName         Nazwa przedmiotu
 * @property semester            Semestr
 * @property hoursInSemester     Liczba godzin na danym semestrze
 */
data class Subject(
    val subjectName: String,
    val semester: Int = -1,
    val hoursInSemester: Int = -1
)