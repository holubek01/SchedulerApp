package com.example.scheduler.utils

import io.github.palexdev.materialfx.controls.MFXStepper
import io.github.palexdev.materialfx.controls.MFXTextField
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox

/**
 * Obiekt zawierający narzędzia do obsługi walidacji pól tekstowych oraz wyświetlania komunikatów błędów.
 * Jest używana w połączeniu z kontrolkami MFXTextField i MFXStepper.
 */
object ValidationWrapper {
    /**
     * Metoda opakowuje kontrolkę typu MFXTextField w kontener typu VBox i dodaje obsługę walidacji.
     *
     * @param node    Kontrolka typu MFXTextField do opakowania.
     * @param stepper Stepper, do którego jest przypisane pole do walidacji.
     * @return Kontener typu VBox zawierający kontrolkę typu MFXTextField i etykietę błędu.
     */
    fun wrapNodeForValidationStepper(node: MFXTextField, stepper: MFXStepper): VBox{
        val errorLabel = createErrorLabel()

        stepper.addEventHandler(MFXStepper.MFXStepperEvent.VALIDATION_FAILED_EVENT) {
            validationAction(node,errorLabel)
        }

        stepper.addEventHandler(MFXStepper.MFXStepperEvent.NEXT_EVENT) {
            errorLabel.text = ""
        }

        return createWrapper(node,errorLabel)
    }

    /**
     * Metoda wykonuje walidację kontrolki pola typu MFXTextField i aktualizuje etykietę błędu.
     *
     * @param node       Kontrolka typu MFXTextField do walidacji.
     * @param errorLabel Etykieta służąca do wyświetlania komunikatu błędu.
     */
    fun validationAction(node: MFXTextField, errorLabel: Label)
    {
        errorLabel.text = ""
        val validator = node.validator
        if (validator.validate().isNotEmpty()) errorLabel.text = validator.validate()[0].message
        if (validator.validate().isEmpty()) errorLabel.text = ""
    }

    /**
     * Tworzy etykietę przeznaczoną do wyświetlania komunikatu błędu.
     *
     * @return Etykieta błędu.
     */
    fun createErrorLabel(): Label
    {
        val errorLabel = Label()
        errorLabel.styleClass.add("error-label")
        errorLabel.isManaged = false
        return errorLabel
    }

    /**
     * Tworzy kontener typu VBox, który zawiera kontrolkę typu MFXTextField i etykietę błędu.
     *
     * @param node       Kontrolka typu MFXTextField do umieszczenia w kontenerze.
     * @param errorLabel Etykieta błędu.
     * @return Kontener typu VBox zawierający kontrolkę MFXTextField i etykietę błędu.
     */
    fun createWrapper(node: MFXTextField, errorLabel: Label): VBox
    {
        return object : VBox(node, errorLabel) {
            override fun layoutChildren() {
                super.layoutChildren()
                node.prefWidth = 300.0
                val x = node.boundsInParent.minX
                val y = node.boundsInParent.maxY + spacing
                val width = width
                val height = errorLabel.prefHeight(-1.0)
                errorLabel.resizeRelocate(x, y, width, height)
            }

            override fun computePrefHeight(width: Double): Double {
                return super.computePrefHeight(width) + errorLabel.height + spacing
            }
        }.apply { alignment = Pos.CENTER }
    }
}