package com.example.scheduler.models

import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.observers.FieldsObserver
import com.example.scheduler.db.dao.*
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Group
import com.example.scheduler.objects.Subject
import com.example.scheduler.objects.TeachingPlan
import com.example.scheduler.utils.CommonUtils
import com.example.scheduler.utils.MessageBundle
import io.github.palexdev.materialfx.controls.MFXComboBox
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException


/**
 * Model zawierający logikę biznesową związaną z Field
 */
class FieldsModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = FieldDAOImpl()
    private var fields = FXCollections.observableArrayList<Field>()

    /**
     * Dodaje kierunek kształcenia do bazy wraz z przypisanym szkolnym planem nauczania
     * @param   field   Kierunek do dodania
     * @param groupNumberComboBoxArray Lista obiektów MFXComboBox, które zawierają liczbę grup na poszczególnych semestrach
     * @param subjectsToAdd  Lista przedmiotów do dodania
     */
    @Throws(SQLException::class)
    fun addField(field: Field, groupNumberComboBoxArray: MutableList<MFXComboBox<Int>>, subjectsToAdd: MutableList<Subject>) {
        dao.addField(field)

        for (i in 1 until groupNumberComboBoxArray.size+1)
        {
            val romanNumber = CommonUtils.intToRoman(i)
            dao.addGroups(field, groupNumberComboBoxArray[i-1].value, i, romanNumber)
        }

        //Dodawanie przedmiotów z godzinami
        for (subject in subjectsToAdd)
        {
            dao.addSubjectToSem(subject.subjectName, field.fieldName, subject.semester, subject.hoursInSemester )
        }

        notifyObservers()
    }

    /**
     * @see FieldDAOImpl.deleteField
     */
    @Throws(SQLException::class)
    fun deleteField(field: Field) {
        dao.deleteField(field)
        notifyObservers()
    }

    /**
     * Metoda sprawdza czy w bazie nie istnieją już kierunki o podobnych danych podczas dodawania
     * @param fieldName Nazwa kierunku
     * @param shortcut Skrót kierunku
     * @param sem MFXComboBox zawierający liczbę semestrów
     * @throws DuplicatesException jeśli istnieje kierunek o podobnych danych
     *
     */
    @Throws(SQLException::class)
    fun checkDBWhileAdding(fieldName: String, shortcut: String, sem: MFXComboBox<Int>) {
        if (sem.value==null)
        {
            throw DuplicatesException(MessageBundle.getMess("warning.noSemCountSelected"))
        }
        else if(dao.checkIfFieldNameInDb(fieldName))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.fieldNameExistsInEdit"))
        }
        else if(dao.checkIfFieldShortcutInDb(shortcut))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.fieldShortcutExistsInEdit"))
        }

    }

    /**
     * @see FieldDAOImpl.showSPN
     */
    fun showSPN(field: Field): ObservableList<TeachingPlan> {
        return dao.showSPN(field)
    }

    /**
     * @see FieldDAOImpl.getFields
     */
    fun getFields(): ObservableList<Field> {
        return dao.getFields()
    }

    /**
     * Metoda sprawdza czy w bazie nie istnieją już kierunki o podobnych danych podczas edycji
     * @param fieldName Nazwa kierunku
     * @param shortcut Skrót kierunku
     * @param lastSelectedField Wybrany kierunek z listy (edytowany)
     */
    @Throws(SQLException::class)
    fun checkDBWhileEditing(fieldName: String, shortcut: String, lastSelectedField: Field) {
        //sprwdza czy w bazie jest już kierunek o tych danych ale różny od obecnego kierunku
        if (dao.checkIfFieldNameInDbEdit(fieldName, lastSelectedField)) {
            throw DuplicatesException(MessageBundle.getMess("warning.fieldNameExistsInEdit"))
        }
        else if (dao.checkIfFieldShortInDbEdit(shortcut, lastSelectedField)) {
            throw DuplicatesException(MessageBundle.getMess("warning.fieldShortcutExistsInEdit"))
        }
        else if(dao.checkIfFieldInDb(fieldName, shortcut))
        {
            throw IdenticalObjectExistsException(MessageBundle.getMess("warning.fieldExistsInEdit"))
        }
    }

    /**
     * @see FieldDAOImpl.getFieldID
     */
    fun getFieldID(lastSelectedField: Field): Int {
        return dao.getFieldID(lastSelectedField)
    }

    /**
     * @see FieldDAOImpl.updateField
     */
    @Throws(SQLException::class)
    fun updateField(fieldID: Int, fieldName: String, fieldShort: String) {
        dao.updateField(fieldID, fieldName, fieldShort)
        notifyObservers()
    }

    /**
     * @see FieldDAOImpl.getSemesters
     */
    fun getSemesters(field: String): Int {
        return dao.getSemesters(field)
    }

    /**
     * @see FieldDAOImpl.addGroup
     */
    @Throws(SQLException::class)
    fun addGroup(fieldName: String, sem: Int, romanNumber: String) {
        dao.addGroup(fieldName, sem, romanNumber)
    }

    /**
     * @see FieldDAOImpl.deleteGroup
     */
    @Throws(SQLException::class)
    fun deleteGroup(groupToDelete: Group) {
        dao.deleteGroup(groupToDelete)
    }

    /**
     * @see FieldDAOImpl.getGroups
     */
    fun getGroupsForGivenSem(field: String, semester: Int): ObservableList<String> {
        return dao.getGroups(field, semester)
    }

    /**
     * @see FieldDAOImpl.getGroupsByField
     */
    fun getGroups(field: String): ObservableList<String> {
        return dao.getGroupsByField(field)
    }

    /**
     * @see FieldDAOImpl.checkIfFieldExists
     */
    fun checkIfFieldExists(fieldName: String): Boolean {
        return dao.checkIfFieldExists(fieldName)
    }

    /**
     * @see FieldDAOImpl.addSubjectToSem
     */
    @Throws(SQLException::class)
    fun addSubjectToSem(subjectName: String, fieldName: String, semester: Int, hoursInSemester: Int) {
        dao.addSubjectToSem(subjectName, fieldName, semester, hoursInSemester)
    }

    /**
     * @see FieldDAOImpl.deleteSPN
     */
    @Throws(SQLException::class)
    fun deleteSPN(fieldName: String) {
        dao.deleteSPN(fieldName)
    }

    /**
     * Obiekt, umożliwiający rejestrowanie i powiadamianie obserwatorów o zmianach w
     * kierunkach kształcenia (dodanie, usunięcie lub edycja kierunku).
     */
    companion object{
        /**
         * Lista obserwatorów
         */
        private val observers = mutableListOf<FieldsObserver>()

        /**
         * Metoda dodająca obserwatora do listy obserwatorów.
         *
         * @param observer Obserwator, który ma zostać dodany do listy obserwatorów.
         */
        fun addObserver(observer: FieldsObserver) {
            observers.add(observer)
        }

        /**
         * Metodą służąca do powiadamiania wszystkich zarejestrowanych obserwatorów o zmianach.
         */
        fun notifyObservers() {
            observers.forEach { it.onFieldsChanged() }
        }
    }
}
