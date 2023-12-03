package com.example.scheduler.objects

import javafx.collections.ObservableList
import java.time.LocalDate


/**
 * Klasa reprezentująca wiersz w tabeli dla rozpiski sal (tabela w ShowPlanRoomsController).
 *
 * @property date       Data zajęć
 * @property hour       Zakres godzinowy, w jakim odbywają się zajęcia
 * @property rooms      Lista, której elementy to grupy, ale indeks listy odpowiada jednej sali (wskazuje jaka grupa ma zajęcia w jakiej sali)
 */
data class PlanForRooms(
    val date: LocalDate,
    val hour: String,
    val rooms: ObservableList<String>
)