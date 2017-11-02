package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.BookableResource
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
import java.time.LocalDate
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
    fun getBookable(@PathVariable("locationId") location: Int, @PathVariable("bookableId") bookable: Int): BookableResource {
        locationRepository.getLocations().find { it.id == location } ?: throw LocationNotFound()
        return BookableResource(bookableRepository.getAllBookables().find { it.id == bookable } ?: throw BookableNotFound())
    }

    /**
     * Get all bookables
     */
    @GetMapping
    fun getAllBookables(
        @PathVariable("locationId") locationId: Int,
        @RequestParam("start", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        startDate: LocalDate? = null,
        @RequestParam("end", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        endDate: LocalDate? = null,
        @RequestParam("expand", required = false)
        expand: List<String>? = emptyList()
    ): Collection<BookableResource> {
        val location = locationRepository.getLocations().find { it.id == locationId } ?: throw LocationNotFound()
        val timeZone = ZoneId.of(location.timeZone)
        val start = startDate ?: LocalDate.now(timeZone)
        val end = endDate ?: start

        if (end.isBefore(start)) {
            throw EndBeforeStartException()
        }

        val interval = Interval.of(
            start.atStartOfDay(timeZone).toInstant(),
            end.plusDays(1).atStartOfDay(timeZone).toInstant()
        )

        return bookableRepository.getAllBookables().takeWhile { it.locationId == locationId }.map { bookable ->
            val bookings = when {
                "bookings" in expand ?: emptyList() ->
                    bookingRepository.getAllBookings().takeWhile { it.bookableId == bookable.id }.takeWhile {
                        interval.overlaps(Interval.of(it.start.atZone(timeZone).toInstant(), it.end.atZone(timeZone).toInstant()))
                    }
                else -> emptyList()
            }

            BookableResource(bookable, bookings)
        }
    }
}
