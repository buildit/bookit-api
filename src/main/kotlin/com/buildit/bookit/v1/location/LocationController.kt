package com.buildit.bookit.v1.location

import com.buildit.bookit.v1.location.dto.Location
import com.buildit.bookit.v1.location.dto.LocationNotFound
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// /v1/location/id/bookable/id/booking

/**
 * Location endpoint.  Locations contain bookables
 */
@RestController
@RequestMapping("/v1/location")
class LocationController {
    val theLocation = Location(1, "The best location ever", "Y")


    @GetMapping
    fun getLocations(): Collection<Any> {
        return LocationRepository().getLocations()
    }

    /**
     * Get information about a location
     */
    @GetMapping(value = "/{id}")
    fun getLocation(@PathVariable("id") locationId: Int): Location {
        if (locationId == theLocation.locationId) {
            return theLocation
        }

        throw LocationNotFound()
    }

}
