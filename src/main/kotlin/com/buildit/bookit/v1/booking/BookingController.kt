package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.bookable.dto.BookableNotFound
import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
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
import java.time.LocalDateTime

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
class StartDateTimeInPastException : RuntimeException("StartDateTime must be in the future")

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
class EndDateTimeBeforeStartTimeException : RuntimeException("EndDateTime must be after StartDateTime")

/**
 * Endpoint to manage bookings
 */
@RestController
@RequestMapping("/v1/booking")
@Transactional
class BookingController(private val bookingRepo: BookingRepository) {
    @Suppress("MagicNumber")
    val theBooking = Booking(1, 1000, "The Booking", LocalDateTime.now(), LocalDateTime.now())

    /**
     */
    @GetMapping
    fun getAllBookings(): ResponseEntity<Collection<Booking>> = ResponseEntity.ok(bookingRepo.getAllBookings())

    /**
     * Get a booking
     */
    @GetMapping(value = "/{id}")
    fun getBooking(@PathVariable("id") bookingId: Int): ResponseEntity<Booking> {
        if (bookingId == theBooking.bookingId) {
            return ResponseEntity.ok(theBooking)
        }

        throw BookableNotFound()
    }

    /**
     * Create a booking
     */
    @PostMapping()
    fun createBooking(@RequestBody bookingRequest: BookingRequest): ResponseEntity<Booking> {
        val now = LocalDateTime.now()
        if (!bookingRequest.startDateTime.isAfter(now)) {
            throw StartDateTimeInPastException()
        }

        if (!bookingRequest.endDateTime.isAfter(bookingRequest.startDateTime)) {
            throw EndDateTimeBeforeStartTimeException()
        }

        val booking = bookingRepo.insertBooking(bookingRequest.bookableId, bookingRequest.subject, bookingRequest.startDateTime, bookingRequest.endDateTime)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(booking)
    }
}
