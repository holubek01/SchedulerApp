package com.example.scheduler.utils

/**
 * Klasa narzędziowa umożliwiająca konwersję nazwy dnia tygodnia między językiem polskim a angielskim.
 */
object EnglishDayConverter {
    private val dayNameMap = mapOf(
        "Poniedziałek" to "Monday",
        "Wtorek" to "Tuesday",
        "Środa" to "Wednesday",
        "Czwartek" to "Thursday",
        "Piątek" to "Friday",
        "Sobota" to "Saturday",
        "Niedziela" to "Sunday"
    )

    private val reverseDayNameMap = dayNameMap.entries.associateBy({ it.value }) { it.key }

    /**
     * Konwertuje nazwę podanego dnia tygodnia z języka polskiego na angielski.
     *
     * @param polishName Nazwa dnia tygodnia po polsku.
     * @return Nazwa dnia tygodnia po angielsku lub null, jeśli nie ma odpowiadającego tłumaczenia.
     */
    fun fromPolishName(polishName: String): String? = dayNameMap[polishName]

    /**
     * Konwertuje nazwę podanego dnia tygodnia z języka angielskiego na polski.
     *
     * @param englishName Nazwa dnia tygodnia po angielsku.
     * @return Nazwa dnia tygodnia po polsku lub null, jeśli nie ma odpowiadającego tłumaczenia.
     */
    fun toPolishName(englishName: String): String? = reverseDayNameMap[englishName]
}
