package com.example.scheduler.tests.Utils

import com.example.scheduler.utils.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest

/**
 * Klasa testująca metody do pobierania dni tygodnia w innym języku
 */
internal class EnglishDayConverterTest: ApplicationTest() {
    @Test
    fun testFromPolishName() {
        assertEquals("Monday", EnglishDayConverter.fromPolishName("Poniedziałek"))
        assertEquals("Tuesday", EnglishDayConverter.fromPolishName("Wtorek"))
        assertEquals("Wednesday", EnglishDayConverter.fromPolishName("Środa"))
        assertEquals("Thursday", EnglishDayConverter.fromPolishName("Czwartek"))
        assertEquals("Friday", EnglishDayConverter.fromPolishName("Piątek"))
        assertEquals("Saturday", EnglishDayConverter.fromPolishName("Sobota"))
        assertEquals("Sunday", EnglishDayConverter.fromPolishName("Niedziela"))
    }

    @Test
    fun testToPolishName() {
        assertEquals("Poniedziałek", EnglishDayConverter.toPolishName("Monday"))
        assertEquals("Wtorek", EnglishDayConverter.toPolishName("Tuesday"))
        assertEquals("Środa", EnglishDayConverter.toPolishName("Wednesday"))
        assertEquals("Czwartek", EnglishDayConverter.toPolishName("Thursday"))
        assertEquals("Piątek", EnglishDayConverter.toPolishName("Friday"))
        assertEquals("Sobota", EnglishDayConverter.toPolishName("Saturday"))
        assertEquals("Niedziela", EnglishDayConverter.toPolishName("Sunday"))
    }

    @Test
    fun testFromPolishNameNonExistingTranslation() {
        val result = EnglishDayConverter.fromPolishName("NieznanyDzien")
        assertEquals(null, result)
    }

    @Test
    fun testToPolishNameNonExistingTranslation() {
        val result = EnglishDayConverter.toPolishName("UnknownDay")
        assertEquals(null, result)
    }
}