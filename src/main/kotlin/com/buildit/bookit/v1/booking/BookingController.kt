package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.bookable.BookableRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
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

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class StartDateTimeInPastException : RuntimeException("StartDateTime must be in the future")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class EndDateTimeBeforeStartTimeException : RuntimeException("EndDateTime must be after StartDateTime")

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookingNotFound : RuntimeException("Booking not found")

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookable : RuntimeException("Bookable does not exist")

/**
 * Endpoint to manage bookings
 */
@RestController
@RequestMapping("/v1/booking")
@Transactional
class BookingController(private val bookingRepository: BookingRepository, private val bookableRepository: BookableRepository, private val clock: Clock) {
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
    @PostMapping()
    fun createBooking(@RequestBody bookingRequest: BookingRequest): ResponseEntity<Booking> {
        bookableRepository.getAllBookables().find { it.id == bookingRequest.bookableId } ?: throw InvalidBookable()

        val now = LocalDateTime.now(clock.withZone(ZoneId.of("America/New_York")))
        if (!bookingRequest.startDateTime.isAfter(now)) {
            throw StartDateTimeInPastException()
        }

        if (!bookingRequest.endDateTime.isAfter(bookingRequest.startDateTime)) {
            throw EndDateTimeBeforeStartTimeException()
        }

        val booking = bookingRepository.insertBooking(
            bookingRequest.bookableId,
            bookingRequest.subject,
            bookingRequest.startDateTime,
            bookingRequest.endDateTime
        )

        return ResponseEntity
            .created(URI("/v1/booking/${booking.id}"))
            .body(booking)
    }
}
