package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.BookableNotFound
import com.buildit.bookit.v1.location.bookable.dto.InvalidBookableSearch
import com.buildit.bookit.v1.location.dto.LocationNotFound
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Endpoint to manage bookables (aka rooms and resources)
 */
@RestController
@RequestMapping("/v1/location/{location}/bookable")
@Transactional
class BookableController(private val bookableRepository: BookableRepository, private val locationRepository: LocationRepository) {
    /**
     * Get a bookable
     */
    @GetMapping(value = "/{name}")
    fun getBookable(@PathVariable("location") location: Int, @PathVariable("name") bookable: Int): Bookable {
        locationRepository.getLocations().find { it.id == location } ?: throw LocationNotFound()
        return bookableRepository.getAllBookables().find { it.id == bookable } ?: throw BookableNotFound()
    }

    /**
     * Get all bookables
     */
    @GetMapping
    fun getAllBookables(
        @PathVariable("location") location: Int,
        @RequestParam("startDateTime", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDateTime: LocalDateTime?,
        @RequestParam("endDateTime", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDateTime: LocalDateTime?
    ): Collection<Bookable> {
        if (startDateTime != null && endDateTime == null) {
            throw InvalidBookableSearch()
        }

        if (startDateTime == null && endDateTime != null) {
            throw InvalidBookableSearch()
        }

        return bookableRepository.getAllBookables().takeWhile { it.locationId == location }
    }
}
