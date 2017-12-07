package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.booking.dto.interval
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
class InvalidBookable : RuntimeException("Bookable does not exist")

@RestController
@RequestMapping("/v1/location/{locationId}/bookable")
@Transactional
class BookableController(private val bookableRepository: BookableRepository, private val locationRepository: LocationRepository, val bookingRepository: BookingRepository) {
    /**
     * Get a bookable
     */
    @GetMapping("/{bookableId}")
    fun getBookable(@PathVariable("locationId") location: String, @PathVariable("bookableId") bookable: String): BookableResource {
        locationRepository.getLocations().find { it.id == location } ?: throw LocationNotFound()
        return BookableResource(bookableRepository.getAllBookables().find { it.id == bookable } ?: throw BookableNotFound())
    }

    /**
     * Get all bookables
     */
    @GetMapping
    fun getAllBookables(
        @PathVariable("locationId") locationId: String,
        @RequestParam("start", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        startDateInclusive: LocalDate? = null,
        @RequestParam("end", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        endDateExclusive: LocalDate? = null,
        @RequestParam("expand", required = false)
        expand: List<String>? = emptyList()
    ): Collection<BookableResource> {
        val location = locationRepository.getLocations().find { it.id == locationId } ?: throw LocationNotFound()
        val timeZone = ZoneId.of(location.timeZone)
        val start = startDateInclusive ?: LocalDate.MIN
        val end = endDateExclusive ?: LocalDate.MAX

        if (end.isBefore(start)) {
            throw EndBeforeStartException()
        }

        val desiredInterval = Interval.of(
            start.atStartOfDay(timeZone).toInstant(),
            end.atStartOfDay(timeZone).toInstant()
        )

        return bookableRepository.getAllBookables()
            .filter { it.locationId == locationId }
            .map { bookable ->
                val bookings = when {
                    "bookings" in expand ?: emptyList() ->
                        bookingRepository.getAllBookings()
                            .filter { booking -> booking.bookableId == bookable.id }
                            .filter { desiredInterval.overlaps(it.interval(timeZone)) }
                    else -> emptyList()
                }

                BookableResource(bookable, bookings)
            }
    }
}
