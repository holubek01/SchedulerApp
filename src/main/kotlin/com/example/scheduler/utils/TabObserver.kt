package com.example.scheduler.utils

import com.example.scheduler.controller.observers.TabsObserver

/**
 * Obiekt narzędziowy, umożliwiający rejestrowanie i powiadamianie obserwatorów o zmianach zakładek.
 */
object TabObserver {
    /**
     * Lista obserwatorów
     */
    private val observers = mutableListOf<TabsObserver>()

    /**
     * Metoda dodająca obserwatora do listy obserwatorów.
     *
     * @param observer Obserwator, który ma zostać dodany do listy obserwatorów.
     */
    fun addObserver(observer: TabsObserver) {
        observers.add(observer)
    }

    /**
     * Metodą służąca do powiadamiania wszystkich zarejestrowanych obserwatorów o zmianach.
     */
    fun notifyObservers() {
        observers.forEach { it.onTabsChanged() }
    }
}