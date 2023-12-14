package com.example.scheduler.models;

import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.observers.TeacherObserver
import com.example.scheduler.db.dao.TeacherDAOImpl
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.MessageBundle
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException

/**
 * Model zawierający logikę biznesową związaną z Teacher
 */
class TeachersModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = TeacherDAOImpl()
    private var teachers = FXCollections.observableArrayList<Teacher>()

    /**
     * @see TeacherDAOImpl.getTeachers
     */
    fun getTeachers(): ObservableList<Teacher> {
        teachers = dao.getTeachers()
        return teachers
    }

    /**
     * @see TeacherDAOImpl.deleteTeacher
     */
    @Throws(SQLException::class)
    fun deleteTeacher(teacher: Teacher) {
        dao.deleteTeacher(teacher)
        notifyObservers()
    }

    /**
     * Dodaje nowego nauczyciela wraz z przedmiotami oraz dyspozycyjnością do bazy danych.
     *
     * @param teacher   Model nauczyciela do dodania.
     * @param subjects  Lista przedmiotów nauczanych przez nauczyciela.
     * @param availabilityList Lista dyspozycyjności do dodania
     */
    @Throws(SQLException::class)
    fun addTeacher(teacher: Teacher, subjects: List<String>, availabilityList: MutableMap<String, MutableList<String>>) {
        val objectMapper = ObjectMapper()
        val subjectsJSON = objectMapper.writeValueAsString(subjects)

        dao.addTeacher(teacher, subjectsJSON)

        availabilityList.filterNot { it.value.isEmpty() }.forEach { (key, value) ->
            val hoursJSON = objectMapper.writeValueAsString(value)
            dao.addAvailabilityToTeacher(key.uppercase(), hoursJSON)
        }

        notifyObservers()
    }

    /**
     * Aktualizuje nauczyciela oraz jego przedmioty w bazie danych.
     *
     * @param teacherID   Id edytowanego nauczyciela.
     * @param teacher     Model nauczyciela z nowymi danymi.
     * @param subjects    Nowa lista przedmiotów nauczyciela.
     */
    @Throws(SQLException::class)
    fun updateTeacher(teacherID: Int, teacher: Teacher, subjects: List<String>) {
        val objectMapper = ObjectMapper()
        val subjectsJSON = objectMapper.writeValueAsString(subjects)

        dao.updateTeacher(teacherID, teacher, subjectsJSON)
        notifyObservers()
    }

    /**
     * Aktualizuje dyspozycyjność nauczyciela w bazie danych
     * @param availabilityList  Nowa lista dyspozycyjności
     * @param teacherID id nauczyciela do aktualizacji
     */
    @Throws(SQLException::class)
    fun updateTeacherAvailability(availabilityList: MutableMap<String, MutableList<String>>, teacherID: Int) {

        val objectMapper = ObjectMapper()
        availabilityList.forEach { (key, value) ->
            val hoursJSON = objectMapper.writeValueAsString(value)
            dao.updateTeacherAvailability(teacherID, key.uppercase(), hoursJSON)
        }
    }

    /**
     * Dodaje nowego nauczyciela wraz z przedmiotami (ale bez dyspozycyjności) do bazy danych.
     *
     * @param teacher   Model nauczyciela do dodania.
     * @param subjects  Lista przedmiotów do przypisania nauczycielowi.
     */
    @Throws(SQLException::class)
    fun addTeacher(teacher: Teacher, subjects: List<String>) {
        val objectMapper = ObjectMapper()
        val subjectsJSON = objectMapper.writeValueAsString(subjects)

        dao.addTeacher(teacher, subjectsJSON)
        notifyObservers()
    }


    /**
     * @see TeacherDAOImpl.getTeacherID
     */
    fun getTeacherID(lastSelectedTeacher: Teacher): Int {
        return dao.getTeacherID(lastSelectedTeacher)
    }

    /**
     * Sprawdza, czy w bazie danych nie ma już podobnych lub takich samych danych podczas dodawania nauczyciela.
     *
     * @param teacher Dodawany nauczyciel.
     * @throws DuplicatesException jeśli istnieją duplikaty
     */
    @Throws(DuplicatesException::class)
    fun checkDBwhileAdding(teacher: Teacher) {
        //sprawdź czy w bazie nie istnieje już nauczyciel z tymi danymi
        if(dao.checkIfTeacherInDb(teacher))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.teacherAlreadyInDB"))
        }
        //Nie mogą być powtarzalne (Imię i nazwisko), nr telefonu, mail (ale samo nazwisko może być powtarzalne)
        else if(dao.checkIfNameAndLastnameInDb(teacher.firstname,teacher.lastname))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.teacherExists"))
        }
        else if(dao.checkIfPhoneInDb(teacher.phone))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.phoneOccupied"))
        }
        else if(dao.checkIfEmailInDb(teacher.email))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.emailOccupied"))
        }
    }

    /**
     * Sprawdza czy w bazie danych nie ma już podobnych lub takich samych danych podczas edycji nauczyciela.
     *
     * @param teacher Edytowany nauczyciel.
     * @param lastSelectedTeacher Ostatni wybrany nauczyciel z tabeli nauczycieli
     * @throws DuplicatesException jeśli istnieją duplikaty
     */
    @Throws(DuplicatesException::class)
    fun checkDBwhileEditing(teacher: Teacher, lastSelectedTeacher: Teacher, messageShown: Boolean) {
        //Sprawdź czy nauczyciel o podanych danych istnieje w bazie (oprócz samego siebie)
        if(dao.checkIfTeacherInDbEdit(teacher, lastSelectedTeacher))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.teacherAlreadyInDB"))
        }

        //Sprawdź czy nauczyciel o podanym imieniu i nazwisku istnieje w bazie (oprócz samego siebie)
        else if(dao.checkIfNameAndLastnameInDbEdit(teacher.firstname,teacher.lastname, lastSelectedTeacher))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.teacherExists"))
        }

        //Sprawdź czy nauczyciel o podanym numerze telefonu istnieje w bazie (oprócz samego siebie)
        else if(dao.checkIfPhoneInDbEdit(teacher.phone, lastSelectedTeacher))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.phoneOccupied"))
        }
        else if(dao.checkIfEmailInDbEdit(teacher.email, lastSelectedTeacher))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.emailOccupied"))
        }
        else if(!messageShown && dao.checkIfTeacherInDb(teacher))
        {
            throw IdenticalObjectExistsException(MessageBundle.getMess("warning.teacherExistsInEdit"))
        }
    }


    /**
     * @see TeacherDAOImpl.getTeacherSubjects
     */
    fun getTeacherSubjects(teacher: Teacher): ObservableList<String> {
        return dao.getTeacherSubjects(teacher)
    }

    /**
     * @see TeacherDAOImpl.getTeacherSubjectsByName
     */
    fun getTeacherSubjectsByName(teacher: String): ObservableList<String> {
        return dao.getTeacherSubjectsByName(teacher)
    }

    /**
     * @see TeacherDAOImpl.getAvailability
     */
    fun getAvailability(teacher: Teacher): ObservableList<String> {
        val availabilityList = FXCollections.observableArrayList(dao.getAvailability(teacher).map { item ->
            item.replace("SATURDAY", MessageBundle.getMess("label.saturday"))
                .replace("SUNDAY", MessageBundle.getMess("label.sunday"))
                .replace("FRIDAY", MessageBundle.getMess("label.friday"))
        })
        return availabilityList
    }

    /**
     * @see TeacherDAOImpl.getAvailabilityByDay
     */
    fun getAvailabilityByDay(lastSelectedTeacher: Teacher, day: String): ObservableList<String> {
        return dao.getAvailabilityByDay(lastSelectedTeacher, day)
    }

    /**
     * Metoda sprawdzająca czy w pliku Excel nie ma duplikatów. Jeśli nie istnieją duplikaty to nauczyciele są dodawani
     * @param teachersToAdd Nauczyciele z przedmiotami do sprawdzenia i dodania
     * @throws DuplicatesException jeśli istnieją duplikaty
     */
    @Throws(DuplicatesException::class)
    fun checkDuplicatesAndAddTeachers(teachersToAdd: MutableMap<Teacher, List<String>>) {

        val distinctTeachers = teachersToAdd.keys.distinctBy { it }
        var duplicatesExist = teachersToAdd.keys.size != distinctTeachers.size
        if (duplicatesExist) throw DuplicatesException(MessageBundle.getMess("warning.teacher.excel.duplicated"))

        val distinctNames = teachersToAdd.keys.distinctBy { "${it.firstname} ${it.lastname}" }
        duplicatesExist = teachersToAdd.keys.size != distinctNames.size
        if (duplicatesExist) throw DuplicatesException(MessageBundle.getMess("warning.teacher.excel.duplicatedNames"))

        val distinctPhones = teachersToAdd.keys.distinctBy { it.phone }
        duplicatesExist = teachersToAdd.keys.size != distinctPhones.size
        if (duplicatesExist) throw DuplicatesException(MessageBundle.getMess("warning.teacher.excel.duplicatedPhones"))

        val distinctEmails = teachersToAdd.keys.distinctBy { it.email }
        duplicatesExist = teachersToAdd.keys.size != distinctEmails.size
        if (duplicatesExist) throw DuplicatesException(MessageBundle.getMess("warning.teacher.excel.duplicatedEmails"))

        teachersToAdd.forEach { (teacher, subjects) ->
            addTeacher(teacher, subjects)
        }
    }

    /**
     * Funkcja walidująca nauczyciela podczas dodawania z pliku Excel
     * @param teacher   Nauczyciel do walidacji
     * @return error Błąd wynikający z walidacji
     */
    fun validateTeacher(teacher: Teacher): String {
        return when{
            teacher.firstname.length<2 -> "${MessageBundle.getMess("firstname.validation.moreThanOneLetter")}: ${teacher.firstname}"
            !teacher.firstname[0].isUpperCase() ->"${MessageBundle.getMess("firstname.validation.startWithUppercase")}: ${teacher.firstname}"
            teacher.firstname.substring(1).lowercase() != teacher.firstname.substring(1) -> "${MessageBundle.getMess("firstname.validation.allLettersLowercaseExceptFirst")}: ${teacher.firstname}"
            !teacher.firstname.matches("[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùżźśúûüųūÿýñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ∂ð'-]+".toRegex()) -> "${MessageBundle.getMess("firstname.validation.noSpecialChars")}: ${teacher.firstname}"
            teacher.lastname.length<2 ->"${MessageBundle.getMess("lastname.validation.moreThanOneLetter")}: ${teacher.lastname}"
            !teacher.lastname[0].isUpperCase() -> "${MessageBundle.getMess("lastname.validation.startWithUppercase")}: ${teacher.lastname}"
            !teacher.lastname.matches("[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùżźśúûüųūÿýñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕŻŹŚØÙÚÛÜŲŪŸÝÑßÇŒÆŠŽ∂ð'-]+".toRegex()) -> "${MessageBundle.getMess("lastname.validation.noSpecialChars")}: ${teacher.lastname}"
            !teacher.phone.matches(Regex("[0-9]+")) ->"${MessageBundle.getMess("phone.validation.onlyNumbers")}: ${teacher.phone}"
            teacher.phone.length!=9 ->"${MessageBundle.getMess("phone.validation.nineDigits")}: ${teacher.phone}"
            teacher.email.count{it == '@'}!=1 || !teacher.email.contains(".") || teacher.email.contains(" ") || (teacher.email.lastIndexOf(".") <= teacher.email.indexOf("@")) -> "${MessageBundle.getMess("email.validation.incorrectEmail")}: ${teacher.email}"
            else -> ""
        }
    }

    /**
     * Metoda obsługująca zdarzenia, gdy nowa lista przedmiotów nauczyciela nie obejmuje wszystkich starych przedmiotów
     * @param teacherID ID edytowanego nauczyciela
     * @param lastSelectedTeacher Ostatni wybrany nauczyciel z tabeli nauczycieli
     * @param subjects Nowa lista przedmiotów
     */
    fun handleDeleteSubjects(teacherID: Int, lastSelectedTeacher: Teacher, subjects: List<String>) {
        val oldSubjects = getTeacherSubjects(lastSelectedTeacher)
        val deletedSubjects = oldSubjects-subjects.toSet()
        for (subject in deletedSubjects)
        {
            dao.deleteClassesAssociatedToDeletedSubjectAndTeacher(subject, teacherID)
        }
    }

    /**
     * Metoda obsługująca zdarzenia, gdy nowa lista dyspozycyjności nauczyciela nie całej starej dyspozycyjności
     * @param teacherID ID edytowanego nauczyciela
     * @param deletedAvailability Lista dyspozycyjności, która ma zostać usunięta
     */
    fun handleDeleteAvailabulity(teacherID: Int, deletedAvailability: MutableMap<String, MutableList<String>>) {
        for ((day, hours) in deletedAvailability) {
            for (hour in hours) {
                dao.deleteClassesAssociatedToDeletedTeacherAndAvailability(day,hour,teacherID)
            }
        }
    }


    /**
     * Obiekt, umożliwiający rejestrowanie i powiadamianie obserwatorów o zmianach
     * nauczycieli w bazie danych (dodanie, usunięcie lub edycja nauczyciela).
     */
    companion object{
        /**
         * Lista obserwatorów
         */
        private val observers = mutableListOf<TeacherObserver>()

        /**
         * Metoda dodająca obserwatora do listy obserwatorów.
         *
         * @param observer Obserwator, który ma zostać dodany do listy obserwatorów.
         */
        fun addObserver(observer: TeacherObserver) {
            observers.add(observer)
        }

        /**
         * Metodą służąca do powiadamiania wszystkich zarejestrowanych obserwatorów o zmianach.
         */
        fun notifyObservers() {
            observers.forEach { it.onTeachersChanged() }
        }
    }
}
