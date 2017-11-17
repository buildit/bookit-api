package com.buildit.bookit.v1.location.bookable.dto

import com.buildit.bookit.v1.booking.dto.Booking
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class Bookable(val id: String,
                    val locationId: String,
                    val name: String,
                    val disposition: Disposition = Disposition())

data class Disposition(
    val closed: Boolean = false,
    val reason: String = ""
)

data class BookableResource(
    @field:JsonUnwrapped
    val bookable: Bookable,
    val bookings: Collection<Booking> = emptyList()
)

