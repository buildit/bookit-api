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
import java.time.ZonedDateTime

/**
 * Endpoint to manage bookings
 */
@RestController
@RequestMapping("/v1/booking")
class BookingController
{
    val theBooking = Booking(2, 1, "The Booking", ZonedDateTime.now(), ZonedDateTime.now())

    /**
     * Get a booking
     */
    @GetMapping(value = "/{id}")
    fun getBookable(@PathVariable("id") bookableId: Int): ResponseEntity<Booking>
    {
        if (bookableId == 1)
        {
            return ResponseEntity.ok(theBooking)
        }

        throw BookableNotFound()
    }

    /**
     * Create a booking
     */
    @PostMapping()
    fun createBooking(@RequestBody bookingRequest: BookingRequest): ResponseEntity<Booking>
    {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Booking(1, 1, bookingRequest.subject, bookingRequest.startDateTime, bookingRequest.endDateTime))
    }
}
