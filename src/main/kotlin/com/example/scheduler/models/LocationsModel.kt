package com.example.scheduler.models;

import com.example.scheduler.controller.exceptions.DuplicatesException
import com.example.scheduler.controller.exceptions.IdenticalObjectExistsException
import com.example.scheduler.controller.observers.LocationObserver
import com.example.scheduler.db.dao.LocationDAOImpl
import com.example.scheduler.objects.Field
import com.example.scheduler.objects.Location
import com.example.scheduler.objects.Room
import com.example.scheduler.objects.Subject
import com.example.scheduler.utils.MessageBundle
import com.example.scheduler.utils.MessageUtil
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.Stage
import java.sql.SQLException

/**
 * Model zawierający logikę biznesową związaną z Location
 */
class LocationsModel {
    /**
     * Obiekt zawierajacy zapytania do bazy danych
     */
    private val dao = LocationDAOImpl()
    private var locations = FXCollections.observableArrayList<Location>()

    /**
     * Pobiera nazwy wszystkich lokalizacji oprócz Platformy
     */
    fun getLocationsNamesExceptPlatform(): ObservableList<String> {
        val locations = dao.getLocationsNames()
        locations.removeIf { it == "Platform" }
        return locations
    }


    /**
     * Metoda sprawdza czy w bazie nie istnieją już lokalizacje o podobnych danych
     * @param location  Lokalizacja do dodania
     * @throws DuplicatesException jeśli istnieje lokalizacja o podobnych danych
     */
    @Throws(SQLException::class)
    fun checkDBwhileAdding(location: Location) {
        if(dao.checkIfLocationInDb(location))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.locationExists"))
        }
        else if(dao.checkIfLocationNameOrStreetInDb(location.locationName, location.city, location.street)) {
            throw DuplicatesException(MessageBundle.getMess("warning.similarLocationExists"))
        }
    }

    /**
     * @see LocationDAOImpl.addLocation
     */
    @Throws(SQLException::class)
    fun addLocation(location: Location) {
        dao.addLocation(location)
        notifyObservers()
    }

    /**
     * @see LocationDAOImpl.deleteLocation
     */
    @Throws(SQLException::class)
    fun deleteLocation(location: Location) {
        dao.deleteLocation(location)
        notifyObservers()
    }

    /**
     * Pobiera nazwy wszystkich lokalizacji
     */
    fun getLocationsNames(): ObservableList<String> {
        val locations = dao.getLocationsNames()
        locations.replaceAll { if (it == "Platform") MessageBundle.getMess("label.platform") else it }
        return locations
    }

    /**
     * Metoda sprawdza czy w bazie nie istnieją już lokalizacje o podobnych danych podczas edycji
     * @param location  Lokalizacja do edycji
     * @param lastSelectedLocation Ostatnio zaznaczona lokalizacja (pomijana przy sprawdzaniu)
     */
    @Throws(SQLException::class)
    fun checkDBWhileEditing(location: Location, lastSelectedLocation: Location, messageShown: Boolean) {
        if(dao.checkIfLocationInDbEdit(location, lastSelectedLocation))
        {
            throw DuplicatesException(MessageBundle.getMess("warning.locationAlreadyInDB"))
        }
        //Czy nazwa lokalizacji lub miasto i ulica są już zajęte
        else if (dao.checkIfLocationNameOrStreetInDbEdit(location, lastSelectedLocation)) {
            throw DuplicatesException(MessageBundle.getMess("warning.similarLocationExists"))
        }

        else if (!messageShown && dao.checkIfLocationInDb(location)) {
            throw IdenticalObjectExistsException(MessageBundle.getMess("warning.locationExistsInEdit"))

        }
    }

    /**
     * @see LocationDAOImpl.getLocationID
     */
    fun getLocationID(lastSelectedLocation: Location): Int {
        return dao.getLocationID(lastSelectedLocation)
    }

    /**
     * @see LocationDAOImpl.updateLocation
     */
    @Throws(SQLException::class)
    fun updateLocation(locationID: Int, location: Location) {
        dao.updateLocation(locationID, location)
        notifyObservers()
    }

    /**
     * Pobiera wszystkie lokalizacje oprócz Platformy
     */
    fun getLocationsExceptPlatform(): ObservableList<Location> {
        return dao.getLocations()
    }

    /**
     * Obiekt, umożliwiający rejestrowanie i powiadamianie obserwatorów o zmianach w
     * lokalizacjach (dodanie, usunięcie lub edycja lokalizacji).
     */
    companion object{
        /**
         * Lista obserwatorów
         */
        private val observers = mutableListOf<LocationObserver>()

        /**
         * Metoda dodająca obserwatora do listy obserwatorów.
         *
         * @param observer Obserwator, który ma zostać dodany do listy obserwatorów.
         */
        fun addObserver(observer: LocationObserver) {
            observers.add(observer)
        }

        /**
         * Metodą służąca do powiadamiania wszystkich zarejestrowanych obserwatorów o zmianach.
         */
        fun notifyObservers() {
            observers.forEach { it.onLocationsChanged() }
        }
    }


}
