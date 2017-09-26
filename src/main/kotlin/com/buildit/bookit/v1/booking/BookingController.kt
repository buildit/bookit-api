package com.buildit.bookit.v1.booking

import org.springframework.web.bind.annotation.*

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
                       bookingRequest.subject,
                       bookingRequest.startDateTime,
                       bookingRequest.endDateTime)
    }
}
