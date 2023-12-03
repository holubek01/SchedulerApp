package com.example.scheduler.utils

import io.github.palexdev.materialfx.controls.MFXTableRow
import io.github.palexdev.materialfx.controls.MFXTableView
import javafx.css.PseudoClass
import java.util.function.Function

/**
 * Obiekt odpowiedzialny za stylizację wierszy tabeli w aplikacji.
 * Nadaje nieparzystym i parzystym wierszom różne style, aby dane w tabeli prezentowały się w przejrzysty sposób.
 */
object TableRowStyler {
    private val EVEN_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("even")
    private val ODD_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("odd")

    /**
     * Metoda nadająca styl nieparzystym i parzystym wierszom na podstawie ich indeksu.
     *
     * @param row   Wiersz tabeli do stylizacji.
     * @param index Indeks wiersza w tabeli.
     */
    private fun applyRowStyles(row: MFXTableRow<*>, index: Int) {
        if (index % 2 != 0) {
            row.pseudoClassStateChanged(ODD_PSEUDO_CLASS, true)
            row.pseudoClassStateChanged(EVEN_PSEUDO_CLASS, false)
        } else {
            row.pseudoClassStateChanged(ODD_PSEUDO_CLASS, false)
            row.pseudoClassStateChanged(EVEN_PSEUDO_CLASS, true)
        }
    }

    /**
     * Metoda konfigurująca styl tabeli, aby zróżnicować wygląd nieparzystych i parzystych wierszy.
     *
     * @param tableView Tabela, której wiersze mają być stylizowane.
     * @param <T>       Typ elementów w tabeli.
     */
    fun <T> setTableStyle(tableView: MFXTableView<T>){
        tableView.tableRowFactory = Function { t ->
        object : MFXTableRow<T>(tableView, t) {
            override fun updateIndex(index: Int) {
                super.updateIndex(index)
                applyRowStyles(this, index)
            }
        }
    }
    }
}