package com.example.scheduler.utils

import com.example.scheduler.SchedulerApp
import com.example.scheduler.models.User
import io.github.palexdev.materialfx.controls.MFXButton
import io.github.palexdev.materialfx.controls.MFXListView
import io.github.palexdev.materialfx.controls.MFXPasswordField
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder
import io.github.palexdev.materialfx.dialogs.MFXStageDialog
import io.github.palexdev.materialfx.enums.ScrimPriority
import io.github.palexdev.mfxresources.fonts.MFXFontIcon
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * Klasa narzędziowa zawierająca metody do wyświetlania różnych rodzajów dialogów w aplikacji.
 * Umożliwia wyświetlanie niestandardowych dialogów oraz komunikatów z przyciskami.
 */
object DialogUtils {

    /**
     * Metoda wyświetlająca niestandardowe okno dialogowe z podanym komunikatem i przyciskami.
     *
     * @param message Treść wiadomości wyświetlanej w oknie dialogowym.
     * @param owner   Nadrzędne okno dialogu.
     * @param buttons Lista par zawierających tekst przycisku i akcję wykonywaną po jego kliknięciu.
     */
    fun showMessageDialogWithButtons(
        message: String,
        owner: Stage,
        buttons: List<Pair<String, () -> Unit>>): MFXStageDialog {

        val dialog = createCustomDialog(owner) {}

        val buttonNodes = buttons.map { (text, action) ->
            val button = MFXButton(text)
            button.onMouseClicked = EventHandler{dialog.close(); action.invoke()}
            button
        }

        val messageDialog = (dialog.content as MFXGenericDialog)
        messageDialog.addActions(*buttonNodes.toTypedArray())
        val messageLabel = Label(message)
        messageLabel.isWrapText = true
        messageLabel.maxWidth = 300.0
        messageDialog.isShowClose = false
        messageDialog.headerIcon = MFXFontIcon("fas-circle-question", 24.0)

        if (message.length > 100) messageLabel.styleClass.add("messageLong")
        else messageLabel.styleClass.add("message")

        val vbox = VBox(messageLabel)
        vbox.alignment = Pos.CENTER
        messageDialog.content = vbox
        return dialog
    }

    /**
     * Metoda wyświetlająca okno dialogowe z podaną zawartością.
     *
     * @param stage          Scena aplikacji.
     * @param content        Zawartość okna w postaci VBox.
     * @param onHiddenAction Akcja wykonywana po zamknięciu okna dialogowego.
     * @return Obiekt MFXStageDialog reprezentujący niestandardowe okno dialogowe.
     */
    fun showCustomDialog(
        stage: Stage,
        content: VBox,
        shouldBeBigger:Boolean = false,
        onHiddenAction: () -> Unit
    ) : MFXStageDialog {
        val dialog = createCustomDialog(stage,onHiddenAction)
        val messageDialog = (dialog.content as MFXGenericDialog)
        messageDialog.content = content
        messageDialog.styleClass.add("root")

        if (shouldBeBigger)
        {
            (dialog.content as MFXGenericDialog).minWidth = 700.0
            (dialog.content as MFXGenericDialog).minHeight = 600.0

        }
        return dialog
    }


    /**
     * Metoda tworząca niestandardowe okno dialogowe.
     *
     * @param owner         Nadrzędne okno dialogowe.
     * @param onHiddenAction Akcja wykonywana po zamknięciu okna.
     * @return Obiekt MFXStageDialog reprezentujący niestandardowe okno dialogowe.
     */
    private fun createCustomDialog(owner: Stage, onHiddenAction: () -> Unit): MFXStageDialog {
        val messageDialog = MFXGenericDialogBuilder.build()
            .makeScrollable(true)
            .setShowMinimize(true)
            .setShowClose(true)
            .setShowAlwaysOnTop(false)
            .get()
        val dialog = MFXGenericDialogBuilder.build(messageDialog)
            .toStageDialogBuilder()
            .initOwner(owner)
            .initModality(Modality.APPLICATION_MODAL)
            .setDraggable(true)
            .setOwnerNode(owner.scene.root as Pane?)
            .setScrimPriority(ScrimPriority.WINDOW)
            .setScrimOwner(true)
            .setOnHidden { onHiddenAction() }
            .get()

        messageDialog.stylesheets.add(SchedulerApp::class.java.getResource("css/customDialogStyles.css")?.toExternalForm())
        return dialog
    }


    /**
     * Tworzy customowy kontener z listą i etykietą.
     *
     * @param labelText Tekst etykiety.
     * @param items     Lista elementów.
     * @param maxWidth  Maksymalna szerokość listy.
     * @return Kontener z listą rozwijaną i etykietą.
     */
    fun createListView(labelText: String, items: List<String>, maxWidth: Double = Double.MAX_VALUE): VBox
    {
        val listView = MFXListView<String>()
        listView.features().enableBounceEffect()
        listView.features().enableSmoothScrolling(0.5)
        listView.styleClass.add("customList2")
        listView.maxWidth = maxWidth

        val label = Label(labelText)
        label.styleClass.add("header-label-big")
        listView.items = FXCollections.observableArrayList(items)

        return VBox(15.0, label, listView)
    }
}
