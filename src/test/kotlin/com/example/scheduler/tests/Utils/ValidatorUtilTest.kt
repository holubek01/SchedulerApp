package com.example.scheduler.tests.Utils

import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.ValidatorUtil
import io.github.palexdev.materialfx.controls.MFXTextField
import io.github.palexdev.materialfx.validation.Constraint
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.util.*

/**
 * Klasa testująca tworzenie ograniczeń na pola
 */
internal class ValidatorUtilTest: ApplicationTest(){

    private lateinit var textField: MFXTextField
    private lateinit var textFieldRepeat: MFXTextField

    override fun start(stage: Stage) {
        textField = MFXTextField()
        textFieldRepeat = MFXTextField()
        MessageBundle.loadBundle(Locale("pl", "PL"))
    }

    @Test
    fun testCreateNotEmptyConstraint() {
        val errorMessage = MessageBundle.getMess("valueTest.notEmpty")
        val constraint:Constraint = ValidatorUtil.createNotEmptyConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = ""
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "Tomek"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun testCreateExactlyNineDigitConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.exactly9Digits")
        val constraint:Constraint = ValidatorUtil.createExactlyNineDigitsConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "12345"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "123456789"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createNoLongerThanSixLettersConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.noLongerThan6Chars")
        val constraint:Constraint = ValidatorUtil.createNoLongerThanSixLettersConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "123456789"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "123456"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createMoreThanOneLetterConstraint() {
        val errorMessage = MessageBundle.getMess("valueTest.moreThanOneLetter")
        val constraint:Constraint = ValidatorUtil.createMoreThanOneLetterConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = ""
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "ALALALA"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createFirstLetterBigConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.firstLetterBig")
        val constraint:Constraint = ValidatorUtil.createFirstLetterBigConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "tomek"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "Tomek"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createLettersSmallExceptFirstConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.allLettersLowercaseExceptFirst")
        val constraint:Constraint = ValidatorUtil.createLettersSmallExceptFirstConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "TomTom"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "Tomek"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createNoSpecialCharsConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.noSpecialChars")
        val constraint:Constraint = ValidatorUtil.createNoSpecialCharsConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "Tom:,!!ek"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "Tomek,"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "#Tom&"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "Kowalski-Nowak"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createOnlyDigitsAllowedConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.onlyDigits")
        val constraint:Constraint = ValidatorUtil.createOnlyDigitsAllowedConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "1234T123"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "Tomek"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "2893541829359329135912359"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }


    @Test
    fun createIncorrectEmailConstraint() {

        val errorMessage = MessageBundle.getMess("valueTest.incorrectEmail")
        val constraint:Constraint = ValidatorUtil.createIncorrectEmailConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)


        //Brak kropki
        textField.text = "tomek@pl"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        //Brak @
        textField.text = "Tomek.pl"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        //Pusta część emailu przed @
        textField.text = "@wp.pl"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        //Ostatnia . Przed małpą
        textField.text = "Jan.Kowalski.Kow@wppl"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        //dwie @
        textField.text = "Jan@kowalski@wp.pl"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "jan.kowalski@wp.pl"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }


    @Test
    fun createPostalCodeConstraint() {

        val errorMessage = MessageBundle.getMess("postcode.validation.correctForm")
        val constraint:Constraint = ValidatorUtil.createPostalCodeConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "1234T1-XX"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "222-22"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "22-22t"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "11-222"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }


    @Test
    fun testCreateBigLetterConstraint() {
        val errorMessage = MessageBundle.getMess("password.validation.containsBigLetter")
        val constraint:Constraint = ValidatorUtil.createContainsBigLetterConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "tomek123"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "tomeK123"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun testCreateContainsDigitConstraint() {
        val errorMessage = MessageBundle.getMess("password.validation.containsDigit")
        val constraint:Constraint = ValidatorUtil.createContainsDigitConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "tomekffasfer"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "tomeK123"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun testCreateEqualPasswordsConstraint() {
        val errorMessage = MessageBundle.getMess("password.validation.notEqualPasswords")
        val constraint:Constraint = ValidatorUtil.createEqualPasswordConstraint(textField.textProperty(), textFieldRepeat.textProperty(), errorMessage)
        textFieldRepeat.validator.constraint(constraint)

        textField.text = "Tomek123"
        textFieldRepeat.text = "Tomek123456"

        assertFalse(textFieldRepeat.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textFieldRepeat.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "tomeK123"
        textFieldRepeat.text = "tomeK123"
        assertTrue(textFieldRepeat.validator.validate().isEmpty(), "Test should pass")
    }


    @Test
    fun testCreateNoSpecialCharsExceptDigitsConstraint() {
        val errorMessage = MessageBundle.getMess("roomName.validation.noSpecialChars")
        val constraint:Constraint = ValidatorUtil.createNoSpecialCharsExceptDigitsConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "sala''@3"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "sala123"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun testCreateOnlyBigLettersConstraint() {
        val errorMessage = MessageBundle.getMess("shortcut.onlyBigLetters")
        val constraint:Constraint = ValidatorUtil.createOnlyBigLettersConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "SKRót"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )

        textField.text = "SKRÓT"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }

    @Test
    fun createNoLongerThan30LettersConstraint() {

        val errorMessage = MessageBundle.getMess("roomName.validation.noLongerThan30")
        val constraint:Constraint = ValidatorUtil.createNoLongerThanThirtyLettersConstraint(textField.textProperty(), errorMessage)
        textField.validator.constraint(constraint)

        textField.text = "123456789akfngkngferlwfkcnrpeonvji dskcnrepijv k;ascnrk"
        assertFalse(textField.validate().isEmpty(), "Test should fail ($errorMessage)")
        assertEquals(textField.validator.validate()[0].message, errorMessage, "Messages should be equals" )


        textField.text = "123456"
        assertTrue(textField.validator.validate().isEmpty(), "Test should pass")
    }
}