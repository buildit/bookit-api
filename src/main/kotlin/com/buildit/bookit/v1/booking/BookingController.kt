package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.location.bookable.InvalidBookable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import javax.validation.Valid

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
open class InvalidBooking(message: String) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class StartInPastException : InvalidBooking("Start must be in the future")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class EndBeforeStartException : InvalidBooking("End must be after Start")

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookingNotFound : RuntimeException("Booking not found")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class BookableNotAvailable : RuntimeException("Bookable is not available.  Please select another time")

/**
 * Endpoint to manage bookings
 */
@RestController
@RequestMapping("/v1/booking")
@Transactional
class BookingController(private val bookingRepository: BookingRepository, private val bookableRepository: BookableRepository, private val locationRepoistory: LocationRepository, private val clock: Clock) {
    /**
     */
    @GetMapping
    fun getAllBookings(): ResponseEntity<Collection<Booking>> = ResponseEntity.ok(bookingRepository.getAllBookings())

    /**
     * Get a booking
     */
    @GetMapping(value = "/{id}")
    fun getBooking(@PathVariable("id") bookingId: Int): Booking =
        bookingRepository.getAllBookings().find { it.id == bookingId } ?: throw BookingNotFound()

    /**
     * Create a booking
     */
    @Suppress("UnsafeCallOnNullableType")
    @PostMapping()
    fun createBooking(@Valid @RequestBody bookingRequest: BookingRequest, errors: Errors? = null): ResponseEntity<Booking> {
        val bookable = bookableRepository.getAllBookables().find { it.id == bookingRequest.bookableId } ?: throw InvalidBookable()
        val location = locationRepoistory.getLocations().single { it.id == bookable.locationId }

        if (errors?.hasErrors() == true) {
            val errorMessage = errors.allErrors.joinToString(",", transform = { it.defaultMessage })

            throw InvalidBooking(errorMessage)
        }

        val now = LocalDateTime.now(clock.withZone(ZoneId.of(location.timeZone)))
        if (!bookingRequest.start!!.isAfter(now)) {
            throw StartInPastException()
        }

        if (!bookingRequest.end!!.isAfter(bookingRequest.start)) {
            throw EndBeforeStartException()
        }

        if (bookingRepository.getAllBookings().filter { it.bookableId == bookable.id }.any {
            false
        }) {
            throw BookableNotAvailable()
        }

        val booking = bookingRepository.insertBooking(
            bookingRequest.bookableId!!,
            bookingRequest.subject!!,
            bookingRequest.start,
            bookingRequest.end
        )

        return ResponseEntity
            .created(URI("/v1/booking/${booking.id}"))
            .body(booking)
    }
}
