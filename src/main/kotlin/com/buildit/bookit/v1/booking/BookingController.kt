package com.buildit.bookit.v1.booking

import com.buildit.bookit.database.DefaultDataAccess
import com.buildit.bookit.database.DerbyConnectionProvider
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
class BookingController(private val bookingRepo: BookingRepository = BookingDatabaseRepository(DefaultDataAccess(DerbyConnectionProvider()))) {
    @Suppress("MagicNumber")
    val theBooking = Booking(1, 1000, "The Booking", LocalDateTime.now(), LocalDateTime.now())

    /**
     */
    @GetMapping
    fun getAllBookings(): ResponseEntity<Collection<Booking>> {
        return ResponseEntity.ok(bookingRepo.getAllBookings())
    }

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
        val booking = bookingRepo.insertBooking(bookingRequest.bookableId, bookingRequest.subject, bookingRequest.startDateTime, bookingRequest.endDateTime)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(booking)
    }
}
