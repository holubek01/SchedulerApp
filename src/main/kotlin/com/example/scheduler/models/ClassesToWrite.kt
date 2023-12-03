package com.example.scheduler.models

import com.example.scheduler.db.dao.ClassesDAOImpl
import com.example.scheduler.utils.MessageBundle
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException
import java.time.LocalDate

/**
 * Model reprezentujący pojedyńcze zajęcia do zapisu do bazy danych wraz z logiką biznesową.
 *
 * @property date    Data zajęć.
 * @property fieldOfStudy Kierunek kształcenia
 * @property group   Grupa, do której przypisane są zajęcia.
 * @property subject Przedmiot.
 * @property location Lokalizacja (budynek), w której odbywają się zajęcia.
 * @property hour    Zakres godzinowy, w jakim prowadzone są zajęcia.
 * @property room    Nazwa sali, w której odbywają się zajęcia.
 * @property teacher Nauczyciel prowadzący zajęcia.

 */
data class ClassesToWrite(
    var date: LocalDate? = null,
    var fieldOfStudy: String? = null,
    var group: String? = null,
    var subject: String? = null,
    var location: String? = null,
    var hour: String? = null,
    var room: String? = null,
    var teacher: String? = null

    //var date: ObjectProperty<LocalDate> = SimpleObjectProperty(),
    //var fieldOfStudy: StringProperty = SimpleStringProperty(),
)
{
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = ClassesDAOImpl()


    /**
     * @see ClassesDAOImpl.canTeacherMoveBetweenClasses
     */
    fun canTeacherMoveBetweenClasses(): Boolean{
        val firstname = teacher!!.split(" ")[1]
        val lastname = teacher!!.split(" ")[0]
        val locationn = if (this.location.equals(MessageBundle.getMess("label.platform"))) "Platform" else this.location

        return dao.canTeacherMoveBetweenClasses(firstname, lastname, locationn!!, hour!!, date.toString())
    }

    /**
     * @see ClassesDAOImpl.canGroupMoveBetweenClasses
     */
    fun canGroupMoveBetweenClasses(): Boolean{
        val locationn = if (this.location.equals(MessageBundle.getMess("label.platform"))) "Platform" else this.location
        return dao.canGroupMoveBetweenClasses(group!!,fieldOfStudy!!,locationn!!,hour!!,date.toString())
    }

    /**
     * @see ClassesDAOImpl.getGroups
     */
    fun getGroups(): ObservableList<String> {
        return dao.getGroups(fieldOfStudy!!)
    }

    /**
     * @see ClassesDAOImpl.getSubjects
     */
    fun getSubjects(): ObservableList<String>{
        return dao.getSubjects(group!!, fieldOfStudy!!)
    }


    /**
     * @see ClassesDAOImpl.getLocations
     */
    fun getLocations(): ObservableList<String> {
        return FXCollections.observableArrayList(
            dao.getLocations(date!!).map { (it.replace("Platform", MessageBundle.getMess("label.platform"))) })
    }

    /**
     * @see ClassesDAOImpl.getHours
     */
    fun getHours(): ObservableList<String> {
        return dao.getHours(date!!, group!!, fieldOfStudy!!)
    }


    /**
     * @see ClassesDAOImpl.getRooms
     */
    fun getRooms(): ObservableList<String> {
        return dao.getRooms(date!!, hour!!, location!!)
    }

    /**
     * @see ClassesDAOImpl.getTeachers
     */
    fun getTeachers(): ObservableList<String> {
        return dao.getTeachers(date!!, hour!!, subject!!)
    }

    /**
     * @see ClassesDAOImpl.addToPlan
     */
    @Throws(SQLException::class)
    fun addToPlan() {
        val classesToAdd = this.copy()
        classesToAdd.location = if (location == MessageBundle.getMess("label.platform")) "Platform" else location
        classesToAdd.room = if (location == MessageBundle.getMess("label.platform")) "Virtual" else room
        dao.addToPlan(classesToAdd)
    }

    /**
     * Tworzy podpowiedź dla użytkownika w przypadku braku nauczycieli do wyboru.
     * @param hours Lista godzin, w jakich grupa jest dostępna w danym dniu
     * @return Lista nauczycieli spełniających warunki i godzin, w których są dostępni
     */
    fun getHoursHint(hours: ObservableList<String>): List<String> {
        val teachersWithHours = dao.getTeacherWithHoursHint(subject!!,date!!)

        //Usuń te godziny, w których grupa jest zajęta
        teachersWithHours.removeIf { !hours.contains(it.second) }

        val locationn = if (location == MessageBundle.getMess("label.platform")) "Platform" else location

        //Usuń nauczycieli, którzy nie zdążą na zajęcia z innej lokalizacji
        teachersWithHours.removeIf { !canTeacherMoveBetweenClasses(it.first.split(" ")[1], it.first.split(" ")[0], locationn!!, it.second, date.toString())}
        return teachersWithHours.map { it.second }.distinct()
    }


    /**
     * Tworzy podpowiedź dla użytkownika w przypadku braku nauczycieli do wyboru oraz braku godzin możliwych do zamiany
     * @return Lista zajętych nauczycieli, którzy uczą wybranego przedmiotu i mogliby prowadzić zajęcia gdyby nie byli zajęci
     * a także zdążą zmienić lokalizację jeśli to konieczne.
     */
    fun getBusyTeachersHints(): List<String> {
        //Pobierz nauczycieli, którzy uczą tego przemdiotu ale są w tym czasie zajęci (uproszczona wersja getBusyTeachers())
        //Można wtedy zamienić tego zajętego nauczyciela na wolnego, zajęty nauczyciel stanie się wolnym i będzie można go przypisać do zajęć
        val locationn = if (location == MessageBundle.getMess("label.platform")) "Platform" else location
        val teachers = dao.getBusyTeachersHint(subject!!,date.toString(),hour!!)

        //Usuń jeśli nauczyciel nie zdążyłby zmienić lokalizacji
        teachers.removeIf { !canTeacherMoveBetweenClasses(it.split(" ")[1], it.split(" ")[0], locationn!!, hour!!, date.toString())}
        return teachers
    }

    /**
     * @see ClassesDAOImpl.getFields
     */
    fun getFields(): ObservableList<String> {
        return dao.getFields()
    }

    /**
     * @see ClassesDAOImpl.getHowManyHoursLeft
     */
    fun getHowManyHoursLeft(): Int {
        return dao.getHowManyHoursLeft(group!!,fieldOfStudy!!,subject!!)
    }

    fun canTeacherMoveBetweenClasses(firstname: String, lastname: String, location: String, hour: String, date: String): Boolean{
        return dao.canTeacherMoveBetweenClasses(firstname, lastname, location, hour, date)
    }


    /**
     * Sprawdza, czy przy próbie dodania nowych zajęć do planu użytkownik nie pominął danych.
     *
     * @return Komunikat o błędzie lub null, jeśli nie ma błędów.
     */
    fun getMissingFieldContentIfError(): String? {
        val messContent = when{
            date==null || date.toString().isBlank() -> MessageBundle.getMess("warning.noData")
            fieldOfStudy.isNullOrBlank() -> MessageBundle.getMess("warning.noField")
            group.isNullOrBlank() -> MessageBundle.getMess("warning.noGroup")
            subject.isNullOrBlank() ->MessageBundle.getMess("warning.noSubject")
            location.isNullOrBlank() ->MessageBundle.getMess("warning.noLocation")
            hour.isNullOrBlank() -> MessageBundle.getMess("warning.noHour")
            room.isNullOrBlank() -> MessageBundle.getMess("warning.noRoom")
            teacher.isNullOrBlank() -> MessageBundle.getMess("warning.noTeacher")
            else -> null
        }

        return messContent
    }
}