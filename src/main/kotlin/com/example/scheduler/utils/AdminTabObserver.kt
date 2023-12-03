package com.example.scheduler.utils

import com.example.scheduler.controller.observers.AdminTabsObserver


/**
 * Obiekt narzędziowy, umożliwiający rejestrowanie i powiadamianie obserwatorów o zmianach w zakładkach administratora.
 */
object AdminTabObserver {
    /**
     * Lista obserwatorów
     */
    private val observers = mutableListOf<AdminTabsObserver>()

    /**
     * Metoda dodająca obserwatora do listy obserwatorów.
     *
     * @param observer Obserwator, który ma zostać dodany do listy obserwatorów.
     */
    fun addObserver(observer: AdminTabsObserver) {
        observers.add(observer)
    }

    /**
     * Metodą służąca do powiadamiania wszystkich zarejestrowanych obserwatorów o zmianach.
     */
    fun notifyObservers() {
        observers.forEach { it.onTabsChanged() }
    }
}