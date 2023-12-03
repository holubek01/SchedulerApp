package com.example.scheduler.tests.Utils

import com.example.scheduler.utils.CommonUtils
import com.example.scheduler.utils.CommonUtils.getPlanStartDay
import com.example.scheduler.utils.CommonUtils.setOnItemSelected
import com.example.scheduler.utils.CommonUtils.setTextFieldStyle
import com.example.scheduler.utils.MessageBundle
import io.github.palexdev.materialfx.controls.*
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Pos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.time.LocalDate
import java.util.*

/**
 * Klasa testująca metody z CommonUtils
 */
class CommonUtilTest: ApplicationTest() {

    //Testowanie konwersji z liczby całkowitej na rzymską
    @Test
    fun testIntToRoman() {
        assertEquals("I", CommonUtils.intToRoman(1))
        assertEquals("II", CommonUtils.intToRoman(2))
        assertEquals("III", CommonUtils.intToRoman(3))
        assertEquals("IV", CommonUtils.intToRoman(4))
        assertEquals("V", CommonUtils.intToRoman(5))
        assertEquals("VI", CommonUtils.intToRoman(6))
        assertEquals("VII", CommonUtils.intToRoman(7))
        assertEquals("VIII", CommonUtils.intToRoman(8))
        assertEquals("IX", CommonUtils.intToRoman(9))
        assertEquals("X", CommonUtils.intToRoman(10))
        assertEquals("", CommonUtils.intToRoman(0))
        assertEquals("", CommonUtils.intToRoman(11))
    }

    @Test
    fun testRomanToInt() {
        assertEquals(1, CommonUtils.romanToInt("I"))
        assertEquals(2, CommonUtils.romanToInt("II"))
        assertEquals(3, CommonUtils.romanToInt("III"))
        assertEquals(4, CommonUtils.romanToInt("IV"))
        assertEquals(5, CommonUtils.romanToInt("V"))
        assertEquals(6, CommonUtils.romanToInt("VI"))
        assertEquals(7, CommonUtils.romanToInt("VII"))
        assertEquals(8, CommonUtils.romanToInt("VIII"))
        assertEquals(9, CommonUtils.romanToInt("IX"))
        assertEquals(10, CommonUtils.romanToInt("X"))
        assertEquals(0, CommonUtils.romanToInt(""))
        assertEquals(0, CommonUtils.romanToInt("InvalidRoman"))
    }

    //Testtowanie tworzenia etykiety informującej o powodzeniu
    @Test
    fun testCreateCompletedLabel() {
        val labelText = "Ukończono zadanie"
        val completedLabel = CommonUtils.createCompletedLabel(labelText)

        assertEquals(labelText, completedLabel.text)
        assertTrue(completedLabel.isWrapText)
        assertEquals(Pos.CENTER, completedLabel.alignment)
        assertTrue(completedLabel.styleClass.contains("completed-label"))
    }

    //Testowanie tworzenia przycisku reset
    @Test
    fun testCreateResetButton() {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val resetButton = CommonUtils.createResetButton()
        assertEquals("Reset", resetButton.text)
        assertEquals("customButton", resetButton.id)
        assertEquals(150.0, resetButton.prefWidth)
    }

    //Testowanie tworzenia przycisku aktualizacji
    @Test
    fun testCreateUpdateButton() {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val resetButton = CommonUtils.createUpdateButton()
        assertEquals(MessageBundle.getMess("label.update"), resetButton.text)
        assertEquals("customButton", resetButton.id)
        assertEquals(200.0, resetButton.prefWidth)
    }

    //Testowanie zaznaczania elementów w tabeli
    @Test
    fun testSetOnItemSelected() {
        Platform.runLater {
            val tableView = MFXTableView<String>()
            val menu = MFXContextMenu(tableView)
            val testList = FXCollections.observableArrayList("Item1", "Item2", "Item3")
            tableView.items = testList

            var selectedRowIndex = -1

            val callback: (Int) -> Unit = { index -> selectedRowIndex = index }

            setOnItemSelected(tableView, menu, callback)

            tableView.selectionModel.selectItem("Item1")

            assert(selectedRowIndex == 0)

            tableView.selectionModel.clearSelection()
            tableView.selectionModel.selectItem("Item3")


            // Sprawdź, czy indeks wybranego elementu został zaktualizowany
            assert(selectedRowIndex == 2)
        }
    }

    //Testowanie pobierania daty początku planu w zależności od wybranego dnia
    @Test
    fun testGetPlanStartDay() {
        val testDateFriday = LocalDate.of(2023, 10, 13)
        val testDateSaturday = LocalDate.of(2023, 10, 14)
        val testDateSunday = LocalDate.of(2023, 10, 15)

        val resultSaturday = getPlanStartDay(testDateSaturday)
        val resultSunday = getPlanStartDay(testDateSunday)
        val resultFriday = getPlanStartDay(testDateFriday)

        assertEquals(LocalDate.of(2023, 10, 13), resultSaturday)
        assertEquals(LocalDate.of(2023, 10, 13), resultSunday)
        assertEquals(LocalDate.of(2023, 10, 13), resultFriday)
    }

    //Testowanie nadawania stylu obiektom MFXTextField
    @Test
    fun testSetTextFieldStyle() {
        val textField1 = MFXTextField()
        val textField2 = MFXTextField()
        val textField3 = MFXTextField()

        setTextFieldStyle(textField1, textField2, textField3)

        assertEquals("comboWhite", textField1.id)
        assertEquals("comboWhite", textField2.id)
        assertEquals("comboWhite", textField3.id)
    }



    /*
@Test
fun testRemoveStepDependencies() {
    val stepper = MFXStepper()
    val textField1 = MFXTextField()
    val textField2 = MFXTextField()
    val step1 = MFXStepperToggle("Dane", MFXFontIcon("fas-user", 16.0, Color.web("#40F6F6")))
    val step2 = MFXStepperToggle("Dane2", MFXFontIcon("fas-user", 16.0, Color.web("#40F6F6")))

    stepper.setOnNext {
        val errorLabel = Label()
        errorLabel.text = ""
        val validator = textField1.validator
        if (validator.validate().isNotEmpty()) errorLabel.text = validator.validate()[0].message
        if (validator.validate().isEmpty()) errorLabel.text = ""
    }


    textField1.validator.constraint(ValidatorUtil.createNotEmptyConstraint(textField1.textProperty(), "Error"))
    step1.validator.dependsOn(textField1.validator)

    step1.content = VBox(ValidationWrapper.wrapNodeForValidationStepper(textField1, stepper))

    stepper.stepperToggles.addAll(step1, step2)

    textField1.text = ""
    stepper.next()
    stepper.next()
}

     */
}