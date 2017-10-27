package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.EndDateTimeBeforeStartTimeException
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.LocationNotFound
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookableNotFound : RuntimeException("Bookable not found")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookableSearchStartDateRequired : RuntimeException("startDateTime is required if endDateTime is specified")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookableSearchEndDateRequired : RuntimeException("endDateTime is required if startDateTime is specified")

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
        startDateTime: LocalDateTime? = null,
        @RequestParam("endDateTime", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDateTime: LocalDateTime? = null
    ): Collection<Bookable> {
        locationRepository.getLocations().find { it.id == location } ?: throw LocationNotFound()

        if (startDateTime != null && endDateTime == null) {
            throw InvalidBookableSearchEndDateRequired()
        }

        if (startDateTime == null && endDateTime != null) {
            throw InvalidBookableSearchStartDateRequired()
        }

        if (startDateTime != null && endDateTime != null && !endDateTime.isAfter(startDateTime)) {
            throw EndDateTimeBeforeStartTimeException()
        }
        
        return bookableRepository.getAllBookables().takeWhile { it.locationId == location }
    }
}
