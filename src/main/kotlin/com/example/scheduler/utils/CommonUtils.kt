package com.example.scheduler.utils

import io.github.palexdev.materialfx.controls.*
import javafx.collections.MapChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.text.TextAlignment
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Klasa narzędziowa dostarczająca różne metody pomocnicze, wspólne dla wielu klas.
 */
object CommonUtils{

    /**
     * Mapa cyfr rzymskich oraz odpowiadających im wartości całkowitych.
     */
    private val romanNumeralsMap = mapOf(
        1 to "I",
        2 to "II",
        3 to "III",
        4 to "IV",
        5 to "V",
        6 to "VI",
        7 to "VII",
        8 to "VIII",
        9 to "IX",
        10 to "X"
    )

    /**
     * Odwrócona mapa cyfr rzymskich oraz odpowiadających im wartości całkowitych.
     */
    private val reverseRomanNumeralsMap = romanNumeralsMap.entries.associateBy({ it.value }, { it.key })

    /**
     * Metoda, konwertująca liczbę całkowitą na cyfrę rzymską.
     *
     * @param num   Liczba całkowita do konwersji.
     * @return      Cyfra rzymska odpowiadająca zadanej liczbie całkowitej lub pusty ciąg znaków, jeśli liczba jest poza zakresem.
     */
    fun intToRoman(num: Int): String {
        return romanNumeralsMap[num] ?: ""
    }

    /**
     * Metoda, konwertująca cyfrę rzymską na liczbę całkowitą.
     *
     * @param roman     Cyfra rzymska.
     * @return          Liczba całkowita odpowiadająca cyfrze rzymskiej lub 0, jeśli konwersja nie jest możliwa (brak cyfry w mapie).
     */
    fun romanToInt(roman: String): Int {
        return reverseRomanNumeralsMap[roman] ?: 0
    }

    /**
     * Ustawia styl pól tekstowych.
     *
     * @param textFields    Pola tekstowe, dla których ma zostać ustawiony styl.
     */
    fun setTextFieldStyle(vararg textFields: MFXTextField) {
        textFields.forEach { textField ->
            textField.id = "comboWhite"
        }
    }

    /**
     * Czyści pola typu MFXComboBox
     */
    fun clearBox(box: MFXComboBox<*>)
    {
        box.valueProperty().set(null)
        box.clear()
        box.selectionModel.clearSelection()
    }

    /**
     * Czyści pola typu MFXDatePicker
     */
    fun clearDatePicker(box: MFXDatePicker)
    {
        box.valueProperty().set(null)
        box.clear()
    }


    /**
     * Metoda, zwracająca datę rozpoczęcia planu (data piątkowa potrzebna do stworzenia nazwy planu) na podstawie podanej daty.
     *
     * @param date      Wybrana data.
     * @return          Data rozpoczęcia planu.
     */
    fun getPlanStartDay(date: LocalDate): LocalDate {

        val day = when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.minusDays(1)
            DayOfWeek.SUNDAY -> date.minusDays(2)
            else -> date
        }

        return day
    }


    /**
     * Metoda ustawiająca nasłuchiwanie (listener) na wybór elementu w tabeli.
     *
     * @param tableView Tabela.
     * @param menu Menu kontekstowe do wyświetlenia po kliknięciu na tabelę.
     * @param callback Callback wywoływany po wyborze elementu.
     * @param <T> Typ elementu w tabeli.
     */
    fun <T> setOnItemSelected(
        tableView: MFXTableView<T>,
        menu: MFXContextMenu,
        callback: (Int) -> Unit
    ){
        tableView.selectionModel.selectionProperty()
            .addListener(MapChangeListener<Int?, T?>
            { change: MapChangeListener.Change<out Int?, out T?>? ->
                val selectedRowIndex = change!!.key ?: -1

                tableView.selectionModel.clearSelection()
                menu.hide()

                if (change.valueRemoved != change.valueAdded) {
                    callback(selectedRowIndex)
                }


            } as MapChangeListener<in Int?, in T?>?)
    }


    /**
     * Metoda wyświetlająca menu kontekstowe w odpowiednim miejscu na podstawie wybranego indeksu w tabeli.
     *
     * @param selectedRowIndex Wybrany indeks elementu w tabeli.
     * @param tableView Tabela.
     * @param menu Menu kontekstowe do wyświetlenia po kliknięciu na tabelę.
     * @param buttons Lista opcji (przycisków) do wyświetlenia w menu.
     * @param <T> Typ elementu w tabeli.
     */
    fun <T> showContextMenu(selectedRowIndex: Int, tableView: MFXTableView<T>, menu: MFXContextMenu, buttons: List<MFXContextMenuItem>){
        menu.items.clear()
        menu.hide()

        menu.items.addAll(buttons)

        val selectedRow = tableView.getCell(selectedRowIndex) as? MFXTableRow<*>
        if (selectedRow!=null)
        {
            val bounds = selectedRow.boundsInParent
            val boundsInWindow = tableView.localToScene(bounds.maxX, bounds.maxY)
            val menuX = boundsInWindow.x + tableView.scene.window.x + 10
            val menuY = boundsInWindow.y + tableView.scene.window.y + 40

            menu.show(tableView, menuX, menuY)
        }
    }

    /**
     * Metoda tworząca etykietę wskazującą na powodzenie wykonania zadania.
     *
     * @param text Tekst etykiety.
     * @return Odpowiednio wystylizowana etykieta z tekstem.
     */
    fun createCompletedLabel(text: String): Label {
        val completedLabel = Label(text)
        completedLabel.isWrapText = true
        completedLabel.alignment = Pos.CENTER
        completedLabel.textAlignment = TextAlignment.CENTER
        completedLabel.styleClass.add("completed-label")
        completedLabel.alignment = Pos.CENTER
        return completedLabel
    }

    /**
     * Metoda tworząca przycisk resetu
     * @return Odpowiednio wystylizowany przycisk resetu.
     */
    fun createResetButton(): MFXButton {
        val resetButton = MFXButton(MessageBundle.getMess("label.reset"))
        resetButton.id = "customButton"
        resetButton.prefWidth = 150.0
        return resetButton
    }

    /**
     * Metoda tworząca przycisk aktualizacji
     * @return Odpowiednio wystylizowany przycisk aktualizacji.
     */
    fun createUpdateButton(): MFXButton
    {
        val updateButton = MFXButton(MessageBundle.getMess("label.update"))
        updateButton.id = "customButton"
        updateButton.prefWidth = 200.0
        return updateButton
    }

    /**
     * Usuwa zależności dla pól pomiędzy krokami w stepper.
     *
     * @param stepper Komponent MFXStepper, z którego mają być usunięte zależności.
     * @param params  Lista pól tekstowych, dla których mają zostać usunięte zależności.
     */
    fun removeStepDependencies(stepper: MFXStepper, vararg params: MFXTextField)
    {
        params.forEach { stepper.stepperToggles[0].validator.removeDependency(it.validator) }
        stepper.stepperToggles.removeAll(stepper.stepperToggles)
    }
}