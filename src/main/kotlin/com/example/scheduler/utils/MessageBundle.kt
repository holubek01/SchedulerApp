package com.example.scheduler.utils

import java.util.Locale
import java.util.ResourceBundle

/**
 * Klasa służąca do zarządzania wiadomościami w aplikacji na podstawie plików zasobów.
 * Umożliwia ładowanie wiadomości w zadanym języku.
 */
object MessageBundle {
    internal lateinit var bundle: ResourceBundle

    /**
     * Metoda wczytująca plik z wiadomościami w zadanym języku.
     *
     * @param locale Obiekt klasy Locale reprezentujący wybrany język.
     */
    fun loadBundle(locale: Locale)
    {
        //gdy locale to Locale("pl", "PL") to pobierze plik messages_pl_PL.properties
        bundle = ResourceBundle.getBundle("messages", locale)
    }

    /**
     * Metoda pobierająca wiadomość na podstawie klucza.
     *
     * @param key Klucz, który identyfikuje tłumaczenie wiadomości.
     * @return Wiadomość odpowiadająca kluczowi z pliku.
     */
    fun getMess(key: String): String{
        return bundle.getString(key)
    }
}