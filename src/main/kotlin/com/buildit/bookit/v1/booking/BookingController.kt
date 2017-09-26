package com.buildit.bookit.v1.booking

import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.time.ZonedDateTime

@RestController
@RequestMapping("/v1/booking")
class BookingController
{
    @PostMapping
    fun createBooking(@RequestBody bookingRequest: BookingRequest): Booking
    {
        print(bookingRequest)
        return Booking(1,
                       1,
                       "The best booking ever",
                       ZonedDateTime.of(2017, 9, 26, 9, 0, 0, 0, ZoneId.of("UTC")),
                       ZonedDateTime.of(2017, 9, 26, 10, 0, 0, 0, ZoneId.of("UTC")))
    }
}
