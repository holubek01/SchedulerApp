package com.example.scheduler.tests.controllersAdmin

import com.example.scheduler.SchedulerApp
import com.example.scheduler.controller.CopyPlanModuleController
import com.example.scheduler.db.dao.*
import com.example.scheduler.models.ClassesToWrite
import com.example.scheduler.models.PlansModel
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Location
import com.example.scheduler.objects.Room
import com.example.scheduler.objects.Teacher
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.time.LocalDate
import java.util.*

internal class CopyPlanModuleControllerTest: ApplicationTest()
{
    private lateinit var controller: CopyPlanModuleController

    override fun start(stage: Stage) {
        MessageBundle.loadBundle(Locale("PL", "pl"))
        val loader = FXMLLoader(SchedulerApp::class.java.getResource("view/copyPlanModule.fxml"))
        val root: Parent = loader.load()
        controller = loader.getController()
        stage.scene = Scene(root)
    }

    //Test sprawdzający czy funkcja createArrayOfDays zwraca poprawną listę terminów
    @Test
    fun checkCreateArrayOfDay()
    {
        val daysList = arrayListOf<LocalDate>(LocalDate.parse("2030-09-06"), LocalDate.parse("2030-09-13"),LocalDate.parse("2030-09-20"), LocalDate.parse("2030-09-27"))
        val from = LocalDate.from(LocalDate.parse("2030-09-06"))
        val to = LocalDate.from(LocalDate.parse("2030-09-27"))
        val days = controller.plansModel.createArrayOfDays(from,to)
        assertEquals(days, daysList, "Both days arrays should have same items")
    }

    //Test sprawdzający usuwanie wybranych planów
    @Test
    fun deleteSelectedPlans()
    {
        Platform.runLater {
            controller.wantToDelete = true
            //Najpierw upewnij się, że nie ma takiego planu w bazie
            var list = controller.plansModel.getAllPlans()
            assertFalse(list.contains("plan_2020-01-01"))

            //Dodajmy plan
            controller.plansModel.createTable("plan_2020-01-01", "plan")
            val fromTable = "group_subject_hours_left"
            controller.plansModel.createTable("${fromTable}_2020-01-01", "group_subject_hours_left")

            //Upewnij się, że plan został dodany
            list = controller.plansModel.getAllPlans()
            assertTrue(list.contains("plan_2020-01-01"))

            //Usuń plan
            controller.deleteChosenPlans(mutableListOf("plan_2020-01-01"))

            //Upewnij się, że plan się poprawnie usunął
            list = controller.plansModel.getAllPlans()
            assertFalse(list.contains("plan_2020-01-01"))
        }
    }


    //Test kopiowania planów na różne terminy
    @Test
    fun copyPlanTest()
    {
        Platform.runLater {
            runBlocking {
                val planModel = PlansModel()
                planModel.refillHours()

                controller.wantToDelete = true
                var list = controller.plansModel.getAllPlans()
                assertFalse(list.contains("plan_2020-01-01"))
                controller.plansModel.createTable("plan_2020-01-01", "plan")
                val fromTable = "group_subject_hours_left"
                controller.plansModel.createTable("${fromTable}_2020-01-01", "group_subject_hours_left")

                //Nie wybrano końca zakresu
                controller.datepickerFrom.value = LocalDate.parse("2020-01-08")
                controller.datepickerTo.value = null
                controller.copyPlanMulti()
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.noRangeSelected"))

                //Nie wybrano początku zakresu
                controller.datepickerFrom.value = LocalDate.parse("2020-01-15")
                controller.datepickerTo.value = LocalDate.parse("2020-01-08")
                controller.copyPlanMulti()
                assertEquals(MessageUtil.content, MessageBundle.getMess("warning.copyPlansErrorContent"))

                //error - pusty aktualny plan
                controller.datepicker.value = LocalDate.parse("2020-01-15")
                controller.copyPlan(controller.datepicker.value)
                assertEquals(MessageUtil.content, "${MessageBundle.getMess("warning.currentPlanEmpty")}: plan_2020-01-15")

                //Musimy dodać zajęcia do planu
                val classes1 = ClassesToWrite(
                    LocalDate.parse("2020-01-01"),
                    "Tymczasowy kierunek",
                    "IA",
                    "Język polski",
                    "LocTemp",
                    "08.00-08.45",
                    "roomTest1",
                    "Kowalski2 Jan2"
                )

                val field = Field("Tymczasowy kierunek", 2, "TEMP2")
                val teacherToAdd = Teacher("Jan2", "Kowalski2", "ssrqwetg@s.pl", "774577777")
                val locationToAdd = Location("LocTemp", "Wro", "Ryn10", "22-222")
                val room1 = Room("roomTest1", locationToAdd.locationName, 1, 1)

                val teacherDAO = TeacherDAOImpl()
                val fieldDAO = FieldDAOImpl()
                val locationDAO = LocationDAOImpl()
                val roomDAO = RoomDAOImpl()
                val classesDAO = ClassesDAOImpl()
                if (teacherDAO.getTeachers().contains(teacherToAdd)) teacherDAO.deleteTeacher(teacherToAdd)
                val subjects = mutableListOf("Język polski")
                val objectMapper = ObjectMapper()
                var subjectsJSON = objectMapper.writeValueAsString(subjects)
                val hoursJSON = objectMapper.writeValueAsString(listOf("08.00-08.45", "08.45-09.30", "09.35-10.20"))
                teacherDAO.addTeacher(teacherToAdd, subjectsJSON)
                teacherDAO.addAvailabilityToTeacher("FRIDAY", hoursJSON)
                fieldDAO.deleteSPN(field.fieldName)
                if (fieldDAO.getFields().contains(field)) fieldDAO.deleteField(field)
                fieldDAO.addField(field)
                fieldDAO.addGroups(field, 3, 1, "I")
                fieldDAO.addSubjectToSem("Język polski", field.fieldName, 1, 1)
                if (locationDAO.getLocations().contains(locationToAdd)) locationDAO.deleteLocation(locationToAdd)
                locationDAO.addLocation(locationToAdd)
                roomDAO.addRoom(room1)

                //Plan juz nie jest pusty - mozna kopiować
                classesDAO.addToPlan(classes1)

                //Teraz powinno poprawnie skopiować plany
                controller.datepickerFrom.value = LocalDate.parse("2020-01-08")
                controller.datepickerTo.value = LocalDate.parse("2020-01-15")
                controller.copyPlanMulti()

                list = controller.plansModel.getAllPlans()
                assertTrue(list.contains("plan_2020-01-01"))
                assertTrue(list.contains("plan_2020-01-08"))
                assertTrue(list.contains("plan_2020-01-15"))

                planModel.createNewPlan()
                planModel.refillHours()

                //Przywróć nowy plan
                planModel.refillFromOldPlan("plan_2020-01-08","group_subject_hours_left_2020-01-08")

                delay(3000)

                //Pobierz jeden ze skopiowanych planów i zobacz czy daty się zgadzają
                val plan = planModel.getPlanGroup(field.fieldName, "IA", MessageBundle.getMess("label.wholePlan"))
                assertEquals(plan.size,1)
                assertEquals(plan[0].date,  LocalDate.parse("2020-01-08"))

                controller.deleteChosenPlans(mutableListOf("plan_2020-01-01", "plan_2020-01-08", "plan_2020-01-15"))

                locationDAO.deleteLocation(locationToAdd)
                fieldDAO.deleteSPN(field.fieldName)
                fieldDAO.deleteField(field)
                teacherDAO.deleteTeacher(teacherToAdd)
                planModel.refillHours()
            }

        }
    }
}