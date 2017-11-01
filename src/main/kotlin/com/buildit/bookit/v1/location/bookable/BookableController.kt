package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
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
import org.threeten.extra.Interval
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookableNotFound : RuntimeException("Bookable not found")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookableSearchStartDateRequired : RuntimeException("start is required if end is specified")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookableSearchEndDateRequired : RuntimeException("end is required if start is specified")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookable : RuntimeException("Bookable does not exist")

@RestController
@RequestMapping("/v1/location/{locationId}/bookable")
@Transactional
class BookableController(private val bookableRepository: BookableRepository, private val locationRepository: LocationRepository, val bookingRepository: BookingRepository) {
    /**
     * Get a bookable
     */
    @GetMapping(value = "/{bookableId}")
    fun getBookable(@PathVariable("locationId") location: Int, @PathVariable("bookableId") bookable: Int): Bookable {
        locationRepository.getLocations().find { it.id == location } ?: throw LocationNotFound()
        return bookableRepository.getAllBookables().find { it.id == bookable } ?: throw BookableNotFound()
    }

    /**
     * Get all bookables
     */
    @GetMapping
    fun getAllBookables(
        @PathVariable("locationId") locationId: Int,
        @RequestParam("start", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'H:mm[[:ss][.SSS]]")
        startDateTime: LocalDateTime? = null,
        @RequestParam("end", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm[[:ss][.SSS]]")
        endDateTime: LocalDateTime? = null
    ): Collection<Bookable> {
        val location = locationRepository.getLocations().find { it.id == locationId } ?: throw LocationNotFound()

        if (startDateTime != null && endDateTime != null && !endDateTime.isAfter(startDateTime)) {
            throw EndBeforeStartException()
        }

        val interval = Interval.of(
            startDateTime?.atZone(ZoneId.of(location.timeZone))?.toInstant() ?: Instant.MIN,
            endDateTime?.atZone(ZoneId.of(location.timeZone))?.toInstant() ?: Instant.MAX
        )

        if (interval == Interval.ALL) {
            return bookableRepository.getAllBookables().takeWhile { it.locationId == locationId }
        }

        if (interval.isUnboundedStart) {
            throw InvalidBookableSearchStartDateRequired()
        }

        if (interval.isUnboundedEnd) {
            throw InvalidBookableSearchEndDateRequired()
        }

        val unavailableBookables = bookingRepository.getAllBookings().takeWhile {
            interval.overlaps(Interval.of(it.start.atZone(ZoneId.of(location.timeZone)).toInstant(), it.end.atZone(ZoneId.of(location.timeZone)).toInstant()))
        }.distinctBy { it.bookableId }

        return bookableRepository.getAllBookables().takeWhile { it.locationId == locationId }.map { bookable ->
            bookable.copy(available = bookable.available && unavailableBookables.none { it.bookableId == bookable.id })
        }
    }
}
