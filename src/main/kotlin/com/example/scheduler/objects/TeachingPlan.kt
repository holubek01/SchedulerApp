package com.example.scheduler.objects

import javafx.collections.ObservableList

/**
 * Klasa reprezentująca część szkolnego planu nauczania (przedmiot z liczbą godzin na poszczególnych semestrach)
 *
 * @property subjectName    Nazwa przedmiotu
 * @property semesters      Liczby godzin do zrealizowania na danych semestrach
 */
data class TeachingPlan(
    val subjectName: String,
    val semesters: ObservableList<Int>
)