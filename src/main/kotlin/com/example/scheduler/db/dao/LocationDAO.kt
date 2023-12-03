package com.example.scheduler.db.dao

import com.example.scheduler.objects.Location
import javafx.collections.ObservableList

interface LocationDAO {
    fun getLocations(): ObservableList<Location>?
    fun getLocationById(id:Int): Location
    fun checkIfLocationInDb(location: Location): Boolean
    fun checkIfLocationNameOrStreetInDb(locationName: String, city: String, street: String): Boolean
    fun checkIfLocationNameOrStreetInDbEdit(location: Location, selectedLocation: Location): Boolean
    fun getLocationID(lastSelectedLocation: Location): Int
    fun updateLocation(locationID: Int, location: Location)
    fun deleteLocation(location: Location)
    fun addLocation(location: Location)
    fun checkIfLocationInDbEdit(location: Location, lastSelectedLocation: Location): Boolean
    fun getLocationsNames(): ObservableList<String>
}