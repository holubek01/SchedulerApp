package com.example.scheduler.models

import com.example.scheduler.db.dao.ClassesDAOImpl
import com.example.scheduler.utils.MessageBundle
import javafx.collections.ObservableList
import java.sql.SQLException
import java.time.LocalDate

/**
 * Model reprezentujący pojedyńcze zajęcia do odczytu w tabeli wraz z logiką biznesową.
 *
 * @property date    Data zajęć.
 * @property hour    Zakres godzinowy, w jakim prowadzone są zajęcia.
 * @property subject Przedmiot.
 * @property room    Nazwa sali, w której odbywają się zajęcia.
 * @property teacher Nauczyciel prowadzący zajęcia.
 * @property group   Grupa, do której przypisane są zajęcia.
 */
data class ClassesToRead(
    val date: LocalDate,
    val hour: String,
    val subject: String,
    var room: String,
    var teacher: String,
    val group: String
)
{
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = ClassesDAOImpl()

    /**
     * Metoda pobierająca tylko godziny, w których wybrana grupa nie ma innych zajęć, nauczyciel zgłosił
     * dyspozycyjność, nie prowadzi w tym czasie innych zajęć oraz
     * nauczyciel i grupa zdążą zmienić lokalizację, jeśli to konieczne
     * @return lista wolnych godzin
     */
    fun getFreeHours(): ObservableList<String> {
        val hours = dao.getFreeHours(this.copy())
        val location = if (room.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else room.split(", ")[1]

        hours.removeIf { !canTeacherMoveBetweenClasses(teacher.split(" ")[1], teacher.split(" ")[0], location, it, date.toString()) }
        hours.removeIf { !canGroupMoveBetweenClasses(location, it) }
        hours.removeIf { it == hour}
        return hours
    }


    /**
     * @see ClassesDAOImpl.canGroupMoveBetweenClasses
     */
    private fun canGroupMoveBetweenClasses(location: String, h: String): Boolean{
        return dao.canGroupMoveBetweenClasses(group.split(" ")[0], group.split(" ")[1],location,h,date.toString())
    }

    /**
     * @see ClassesDAOImpl.changeHours
     */
    @Throws(SQLException::class)
    fun changeHours(selectedItem: String, value: String) {
        dao.changeHours(selectedItem,value,this.copy())
    }


    /**
     * @see ClassesDAOImpl.changeHoursPlatform
     */
    @Throws(SQLException::class)
    fun changeHoursPlatform(newHour: String) {
        var copyClasses = this.copy()
        if (room.contains(MessageBundle.getMess("label.virtual"))) copyClasses.room = "Virtual, Platform"
        dao.changeHoursPlatform(newHour,copyClasses)
    }

    /**
     * @see ClassesDAOImpl.getFreeRoomsByHour
     */
    fun getFreeRoomsByHour(hour: String): ObservableList<String> {
        return dao.getFreeRoomsByHour(date, hour, room.split(", ")[1])
    }

    /**
     * @see ClassesDAOImpl.setAnotherRoom
     */
    @Throws(SQLException::class)
    fun setAnotherRoom(selectedItem: String) {
        dao.setAnotherRoom(selectedItem,this.copy())
    }

    /**
     * @see ClassesDAOImpl.changeRooms
     */
    @Throws(SQLException::class)
    fun changeRooms(selectedItem: String) {
        dao.changeRooms(selectedItem,this.copy())
    }

    /**
     * Pobiera sale, które są wolne w terminie zajęć oraz, które znajdują się w lokalizacji, do której
     * zdąży się przemieścić grupa oraz nauczyciel
     *
     * @return lista wolnych sal
     */
    fun getFreeRooms(): ObservableList<String> {
        //Jeśli nie platforma to dodaj platformę (zawsze można zmienić na platformę)
        val locations = dao.getLocationsNames()

        //Wyeliminuj wszystkie lokalizacje, na które nie można zmienić platformy
        locations.removeIf { !canGroupMoveBetweenClasses(group.split(", ")[0], group.split(", ")[1], it, hour, date.toString())}
        locations.removeIf { !canTeacherMoveBetweenClasses( teacher.split(" ")[1], teacher.split(" ")[0], it, hour, date.toString())}
        val rooms = dao.getFreeRooms(this.copy(),locations)

        if (!room.contains(MessageBundle.getMess("label.virtual")))
        {
            rooms.add("${MessageBundle.getMess("label.virtual")}, ${MessageBundle.getMess("label.platform")}")
        }

        return rooms
    }

    private fun canGroupMoveBetweenClasses(group: String, location: String, locationToCheck: String, hour: String, date: String): Boolean {
        return dao.canGroupMoveBetweenClasses(group,location,locationToCheck, hour, date)
    }

    /**
     * @see ClassesDAOImpl.getBusyRooms
     */
    fun getBusyRooms(): ObservableList<String> {
        val rooms = dao.getBusyRooms(this.copy())
        rooms.removeIf { it == room }
        return rooms
    }

    /**
     * @see ClassesDAOImpl.setAnotherTeacher
     */
    @Throws(SQLException::class)
    fun setAnotherTeacher(teacher: String, selectedItem: String) {
        return dao.setAnotherTeacher(teacher, selectedItem, this.copy())
    }

    /**
     * @see ClassesDAOImpl.changeTeachers
     */
    @Throws(SQLException::class)
    fun changeTeachers(firstTeacher: String, selectedItem: String) {
        dao.changeTeachers(firstTeacher, selectedItem, this.copy())
    }

    /**
     * @see ClassesDAOImpl.getBusyTeachers
     */
    fun getBusyTeachers(subjects: ObservableList<String>): ObservableList<String> {
        val teachers = dao.getBusyTeachers(subject, date, hour, subjects, room.split(", ")[1])

        //Proponuje nauczycieli z tej samej lokalizacji, więc nie trzeba sprawdzać czy zdążą dojechać
        teachers.removeIf { it == teacher }
        return teachers

    }

    /**
     * Metoda zwracająca nauczycieli, którzy w terminie zajęć nie mają żadnych zajęć, zgłosili dyspo
     * oraz zdążą zmienić lokalizację jeśli to konieczne
     * @return Lista wolnych nauczycieli, spełniających warunki
     */
    fun getFreeTeachers(): ObservableList<String> {
        val free = dao.getFreeTeachers(subject, date, hour)

        //Usuń z freeTeachers, tych nauczycieli, którzy nie zdążą dotrzeć na zajęcia
        val location = if (room.split(", ")[1] == MessageBundle.getMess("label.platform")) "Platform" else room.split(", ")[1]
        free.removeIf {
            !canTeacherMoveBetweenClasses(it.split(" ")[1], it.split(" ")[0], location, hour, date.toString())
        }

        return free
    }

    /*
    fun getFreeHoursPlatform(): ObservableList<String> {
        val hours = dao.getFreeHours(this.copy())
        hours.removeIf { it == hour}
        return hours
    }

 */


    fun canTeacherMoveBetweenClasses(firstname: String, lastname:String, location: String, hour: String, date: String): Boolean{
        return dao.canTeacherMoveBetweenClasses(firstname, lastname, location, hour, date)
    }

    /**
     * @see ClassesDAOImpl.deleteClasses
     */
    @Throws(SQLException::class)
    fun deleteClasses() {
        dao.deleteClasses(this.copy())
    }
}