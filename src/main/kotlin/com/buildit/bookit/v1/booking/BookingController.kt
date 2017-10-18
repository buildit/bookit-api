package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.bookable.dto.BookableNotFound
import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDateTime

/**
 * Endpoint to manage bookings
 */
@RestController
@RequestMapping("/v1/booking")
class BookingController {
    val eastern = "america/new_york"

    @Suppress("MagicNumber")
    val theBooking = Booking(1, 1000, "The Booking", eastern, LocalDateTime.now(), LocalDateTime.now())

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
        @Suppress("MagicNumber")
        val bookingId = 1 + (Math.random() * 999999).toInt()

        val booking = Booking(
            bookingId,
            bookingRequest.bookableId,
            bookingRequest.subject,
            eastern,
            bookingRequest.startDateTime,
            bookingRequest.endDateTime)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(booking)
    }
}
