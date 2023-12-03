package com.example.scheduler.utils

import io.github.palexdev.materialfx.validation.Constraint
import javafx.beans.binding.Bindings
import javafx.beans.property.StringProperty

/**
 * Klasa zawierająca narzędzia do tworzenia różnych rodzajów walidatorów, używanych do walidacji pól tekstowych.
 * Walidatory są wykorzystywane do sprawdzania i zapewniania poprawności danych wprowadzanych przez użytkownika
 * w formularzach dodawania i edytowania obiektów.
 */
object ValidatorUtil {
    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość rozpoczyna się z dużą literą.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createFirstLetterBigConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.let {it.isNotEmpty() && it[0].isUpperCase()}},
                property
            ))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy wszystkie litery właściwości poza pierwszą są małe.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createLettersSmallExceptFirstConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.let {it.isNotEmpty() && it.substring(1).lowercase() == it.substring(1) } },
                property
            ))
    }

    /**
     * Tworzy ograniczenie sprawdzające, czy hasło i jego powtórzenie są identyczne.
     *
     * @param propertyPassword       Właściwość typu StringProperty reprezentująca hasło.
     * @param propertyPasswordRepeat Właściwość typu StringProperty reprezentująca powtórzenie hasła.
     * @param errorMessage           Komunikat błędu wyświetlany, gdy hasła nie są identyczne.
     * @return Ograniczenie Constraint sprawdzające, czy hasło i jego powtórzenie są identyczne.
     */
    fun createEqualPasswordConstraint(propertyPassword: StringProperty,propertyPasswordRepeat: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                {
                    propertyPassword.value == propertyPasswordRepeat.value },
                propertyPasswordRepeat
            ))
    }

    /**
     * Tworzy ograniczenie sprawdzające, czy dany ciąg znaków zawiera co najmniej jedną cyfrę.
     *
     * @param property     Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie Constraint sprawdzające, czy dany ciąg znaków zawiera co najmniej cyfrę.
     */
    fun createContainsDigitConstraint(property: StringProperty, errorMessage: String): Constraint {
        val regex = Regex("[0-9]")

        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { regex.containsMatchIn(property.value) },
                property
            ))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość składa się jedynie z wielkich liter.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createOnlyBigLettersConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.matches("[A-ZÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ]+".toRegex()) },
                property
            ))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość zawiera jedynie cyfry.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createOnlyDigitsAllowedConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.matches(Regex("[0-9]+")) },
                property
            ))
    }


    /**
     * Tworzy ograniczenie, które sprawdza, czy właściwość jest poprawnym adresem e-mail.
     * Adres e-mail musi spełniać następujące kryteria:
     * - Zawiera dokładnie jeden znak "@"
     * - Nie zawiera spacji
     * - Zawiera kropkę po znaku "@"
     * - Znak "@" nie jest pierwszym znakiem
     * - Po znaku "@" znajdują się znaki inne niż spacja
     * - Zawiera kropkę przed końcem adresu e-mail
     * - Kropka nie jest ostatnim znakiem adresu e-mail
     *
     * @param property    Właściwość typu StringProperty do walidacji.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createIncorrectEmailConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                {
                    val email = property.value
                    val atIndex = email.indexOf("@")
                    val dotIndex = email.lastIndexOf(".")
                    val hasExactlyOneAt = email.count{it == '@'} == 1
                    val hasNoSpace = !email.contains(" ")
                    val containsDot = email.contains(".")
                    val atIsNotFirstChar = email.indexOf("@") >1
                    val containsDotAfterAt = dotIndex > atIndex
                    val containsCharBetweenDotAndAt = if (containsDotAfterAt) email.substring(atIndex+1,dotIndex).isNotEmpty() else false
                    val dotIsNotLastCharacter = dotIndex != email.length-1

                    property.value.let {
                    hasExactlyOneAt &&
                    hasNoSpace &&
                    containsDot &&
                    atIsNotFirstChar &&
                    containsCharBetweenDotAndAt &&
                    containsDotAfterAt &&
                    dotIsNotLastCharacter
                     }},
                property
            ))
    }


    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość nie jest pusta.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createNotEmptyConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage, property.isNotEmpty)
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość ma dokładnie 9 znaków.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createExactlyNineDigitsConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,property.length().isEqualTo(9))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość jest nie dłuższa niź 6 znaków.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createNoLongerThanSixLettersConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage, property.length().lessThanOrEqualTo(6))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość jest nie dłuższa niź 30 znaków.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createNoLongerThanThirtyLettersConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage, property.length().lessThanOrEqualTo(30))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość ma co najmniej 1 znak.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createMoreThanOneLetterConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage, property.length().greaterThan(1))
    }



    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość pasuje do podanego wzorca (Nie zawiera innych znaków).
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createNoSpecialCharsConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.matches("[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùżźśúûüųūÿýñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ∂ð'-]+".toRegex()) },
                property
            ))
    }

    fun createNoSpecialCharsConstraintSpceAllowed(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.matches("[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùżźśúûüųūÿýñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ∂ð '-]+".toRegex()) },
                property
            ))
    }

    /**
     * Tworzy ograniczenie sprawdzające, czy dany ciąg znaków nie zawiera żadnych znaków specjalnych, z wyjątkiem cyfr.
     *
     * @param property     Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie Constraint sprawdzające, czy dany ciąg znaków nie zawiera żadnych znaków specjalnych, z wyjątkiem cyfr.
     */
    fun createNoSpecialCharsExceptDigitsConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { property.value.matches("[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùżźśúûüųūÿýñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ∂ð'0-9]+".toRegex()) },
                property
            ))
    }


    /**
     * Tworzy ograniczenie sprawdzające, czy dany ciąg znaków zawiera co najmniej jedną wielką literę.
     *
     * @param property     Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie Constraint sprawdzające, czy dany ciąg znaków zawiera co najmniej jedną wielką literę.
     */
    fun createContainsBigLetterConstraint(property: StringProperty, errorMessage: String): Constraint {
        val regex = Regex("[A-ZÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ∂ð]")

        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding(
                { regex.containsMatchIn(property.value) },
                property
            ))
    }

    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość ma co najmniej 5 znaków.
     *
     * @param property    Właściwość typu StringProperty do zwalidowania.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createMoreThanFiveLetterConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage, property.length().greaterThan(5))
    }


    /**
     * Metoda tworząca walidator, który sprawdza, czy właściwość pasuje do wzorca kodu pocztowego XX-XXX gdzie X-cyfra.
     *
     * @param property    Właściwość typu StringProperty do walidacji.
     * @param errorMessage Komunikat błędu wyświetlany, gdy walidacja nie powiedzie się.
     * @return Ograniczenie typu Constraint.
     */
    fun createPostalCodeConstraint(property: StringProperty, errorMessage: String): Constraint {
        return Constraint.of(errorMessage,
            Bindings.createBooleanBinding({ property.value.matches(Regex("\\d{2}-\\d{3}")) },
                property
            )
        )
    }
}