package com.buildit.bookit.v1.location

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/location")
class LocationController
{
    val theLocation = Location(1, "The best location ever")

    @GetMapping(value = "/{id}")
    fun getLocation(@PathVariable("id") locationId: Int): Location
    {
        if (locationId == 1)
        {
            return theLocation
        }

        throw LocationNotFound()
    }

}
