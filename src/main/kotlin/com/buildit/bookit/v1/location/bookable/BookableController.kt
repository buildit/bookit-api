package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.booking.dto.interval
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.BookableResource
import com.buildit.bookit.v1.location.dto.Location
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

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookableNotFound : RuntimeException("Bookable not found")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookable : RuntimeException("Bookable does not exist")

@RestController
@RequestMapping("/v1/location/{locationId}/bookable")
@Transactional
class BookableController(private val bookableRepository: BookableRepository, val bookingRepository: BookingRepository) {
    /**
     * Get a bookable
     */
    @GetMapping(value = "/{bookableId}")
    fun getBookable(@PathVariable("locationId") location: Location?, @PathVariable("bookableId") bookable: Bookable?): BookableResource {
        location ?: throw LocationNotFound()
        bookable ?: throw BookableNotFound()
        if (bookable.location != location) {
            throw BookableNotFound()
        }
        return BookableResource(bookable)
    }

    /**
     * Get all bookables
     */
    @GetMapping
    fun getAllBookables(
        @PathVariable("locationId") location: Location?,
        @RequestParam("start", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        startDate: LocalDate? = null,
        @RequestParam("end", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm[[:ss][.SSS]]]")
        endDate: LocalDate? = null,
        @RequestParam("expand", required = false)
        expand: List<String>? = emptyList()
    ): Collection<BookableResource> {
        location ?: throw LocationNotFound()
        val start = startDate ?: LocalDate.now(location.timeZone)
        val end = endDate ?: start

        if (end.isBefore(start)) {
            throw EndBeforeStartException()
        }

        val desiredInterval = Interval.of(
            start.atStartOfDay(location.timeZone).toInstant(),
            end.plusDays(1).atStartOfDay(location.timeZone).toInstant()
        )

        return bookableRepository.findByLocation(location)
            .map { bookable ->
                val bookings = when {
                    "bookings" in expand ?: emptyList() ->
                        bookingRepository.getAllBookings()
                            .takeWhile { it.bookableId == bookable.id }
                            .takeWhile { desiredInterval.overlaps(it.interval(location.timeZone)) }
                    else -> emptyList()
                }

                BookableResource(bookable, bookings)
            }
    }
}
