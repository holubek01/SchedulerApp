package com.example.scheduler.models;

import com.example.scheduler.controller.exceptions.EmptyPlanException
import com.example.scheduler.controller.observers.PlansObserver
import com.example.scheduler.db.dao.*
import com.example.scheduler.objects.PlanForRooms
import com.example.scheduler.utils.CommonUtils
import com.example.scheduler.utils.MessageBundle
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.SQLException
import java.time.LocalDate
import java.util.ArrayList

/**
 * Model zawierający logikę biznesową związaną z planami
 */
class PlansModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val planDao = PlanDAOImpl()

    /**
     * @see PlanDAOImpl.deletePlan
     */
    @Throws(SQLException::class)
    fun deletePlan(plan: String, hoursLeftTable: String) {
        planDao.deletePlan(plan, hoursLeftTable)
        notifyObservers()
    }

    /**
     * @see PlanDAOImpl.getAllPlans
     */
    fun getAllPlans(): ObservableList<String> {
        return planDao.getAllPlans()
    }

    /**
     * @see PlanDAOImpl.getMaxDateFromTable
     */
    fun getMaxDateFromTable(): LocalDate {
        return planDao.getMaxDateFromTable()
    }

    /**
     * @see PlanDAOImpl.getPlanForPlatform
     */
    fun getPlanForPlatform(): ObservableList<PlanForRooms> {
        return planDao.getPlanForPlatform()
    }

    /**
     * @see PlanDAOImpl.deletePlan
     */
    fun getPlan(location: String, rooms: ObservableList<String>): ObservableList<PlanForRooms> {
        return planDao.getPlan(location, rooms)
    }

    /**
     * @see PlanDAOImpl.getPlanGroup
     */
    fun getPlanGroup(field: String, group: String,day: String): ObservableList<ClassesToRead>{
        val plan = FXCollections.observableArrayList(planDao.getPlanGroup(field, group, day).map { classes ->
            if (classes.room == "Virtual, Platform") {
                classes.copy(room = "${MessageBundle.getMess("label.virtual")}, ${MessageBundle.getMess("label.platform")}")
            } else {
                classes
            } })

        return plan
    }

    /**
     * @see PlanDAOImpl.getPlanTeacher
     */
    fun getPlanTeacher(teacher: String, day: String): ObservableList<ClassesToRead>{
        val plan = FXCollections.observableArrayList(planDao.getPlanTeacher(teacher,day).map { classes ->
            if (classes.room == "Virtual, Platform") {
                classes.copy(room = "${MessageBundle.getMess("label.virtual")}, ${MessageBundle.getMess("label.platform")}")
            } else {
                classes
            } })

        return plan
    }

    /**
     * @see PlanDAOImpl.shouldSaveOldPlan
     */
    fun shouldSaveOldPlan(): Boolean {
        return planDao.shouldSaveOldPlan()
    }

    /**
     * @see PlanDAOImpl.refillHours
     */
    @Throws(SQLException::class)
    fun refillHours() {
        planDao.refillHours()
    }

    /**
     * @see PlanDAOImpl.createTable
     */
    @Throws(SQLException::class)
    fun createTable(newTable: String, fromTable: String) {
        planDao.createTable(newTable,fromTable)
    }

    /**
     * @see PlanDAOImpl.refillFromOldPlan
     */
    @Throws(SQLException::class)
    fun refillFromOldPlan(tablePlan: String, tableHoursLeft: String) {
        planDao.refillFromOldPlan(tablePlan,tableHoursLeft)
    }

    /**
     * @see PlanDAOImpl.isPlanFull
     */
    fun isPlanFull(): Boolean {
        return planDao.isPlanFull()
    }

    /**
     * @see PlanDAOImpl.checkIfPlanExists
     */
    fun checkIfPlanExists(item: LocalDate): Boolean {
        return planDao.checkIfPlanExists(item)
    }

    /**
     * @see PlanDAOImpl.copyTable
     */
    @Throws(SQLException::class)
    fun copyTable(tableTo: String, tableFrom: String, oldFriday: String, oldSaturday: String, oldSunday: String, newFriday: String, newSaturday: String, newSunday: String) {
        planDao.copyTable(tableTo, tableFrom, oldFriday, oldSaturday, oldSunday, newFriday, newSaturday, newSunday)
        notifyObservers()
    }

    /**
     * Metoda zwracająca wszystkie plany z bazy oprócz aktualnego
     */
    fun getAllPlansExceptCurrent(): ObservableList<String> {
        val plans = getAllPlans()
        val day = getMaxDateFromTable()
        if (day!= LocalDate.MIN) {
            val startDay = CommonUtils.getPlanStartDay(day)
            val currentPlan = "plan_$startDay"
            plans.removeIf { it == currentPlan }
        }

        return plans
    }

    /**
     * Metoda tworząca w bazie tabelę z nowym planem
     * @throws EmptyPlanException Jeśli aktualny plan jest pusty
     */
    @Throws(SQLException::class)
    fun createNewPlan() {
        val maxDay = getMaxDateFromTable()
        if(maxDay!= LocalDate.MIN) {
            val planName = CommonUtils.getPlanStartDay(maxDay)

            //Mamy nazwę, teraz trzeba utworzyć tabele
            createTable("plan_$planName","plan")
            val fromTable = "group_subject_hours_left"
            createTable("${fromTable}_$planName","group_subject_hours_left")
            notifyObservers()
        }
        else throw EmptyPlanException(MessageBundle.getMess("warning.savePlanError"))
    }

    /**
     * Metoda usuwająca aktualny plan zajęć z bazy
     * @throws EmptyPlanException Jeśli aktualny plan jest pusty
     */
    @Throws(SQLException::class)
    fun deleteCurrentPlan() {
        val maxDay = getMaxDateFromTable()
        if(maxDay!= LocalDate.MIN) {
            val planStart = CommonUtils.getPlanStartDay(maxDay)
            val planName = "plan_$planStart"
            val hoursLeftName = "group_subject_hours_left_$planStart"
            deletePlan(planName, hoursLeftName)
            refillHours()
        }
        else throw EmptyPlanException(MessageBundle.getMess("warning.deletePlansError"))
    }


    /**
     * Tworzy listę początków planów (piątki) między podanymi datami.
     *
     * @param from Początkowa data.
     * @param to   Końcowa data.
     * @return Lista początków planów między podanymi datami.
     */
    fun createArrayOfDays(from: LocalDate, to: LocalDate): List<LocalDate> {
        val daysList = ArrayList<LocalDate>()
        var currentDay = from

        while (!currentDay.isAfter(to)) {
            if (!checkIfPlanExists(currentDay)) daysList.add(currentDay)
            currentDay = currentDay.plusDays(7)
        }

        return daysList
    }


    /**
     * Obiekt narzędziowy, umożliwiający rejestrowanie i powiadamianie obserwatorów o zmianach w
     * planach zajęciowych (dodanie lub usunięcie planu).
     */
    companion object {
        /**
         * Lista obserwatorów
         */
        private val observers = mutableListOf<PlansObserver>()

        /**
         * Metoda dodająca obserwatora do listy obserwatorów.
         *
         * @param observer Obserwator, który ma zostać dodany do listy obserwatorów.
         */
        fun addObserver(observer: PlansObserver) {
            observers.add(observer)
        }

        /**
         * Metodą służąca do powiadamiania wszystkich zarejestrowanych obserwatorów o zmianach.
         */
        fun notifyObservers() {
            observers.forEach { it.onPlansChanged() }
        }
    }



}
