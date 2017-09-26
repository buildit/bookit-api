package com.buildit.bookit.booking.v1

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId
import java.time.ZonedDateTime

@RestController
@RequestMapping("/v1/booking")
class BookingController
{
    @PostMapping
    fun createBooking(): Booking
    {
        return Booking(1,
                       1,
                       "The best booking ever",
                       ZonedDateTime.of(2017, 9, 26, 9, 0, 0, 0, ZoneId.of("UTC")),
                       ZonedDateTime.of(2017, 9, 26, 10, 0, 0, 0, ZoneId.of("UTC")))
    }
}
