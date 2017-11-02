package com.buildit.bookit.v1.location.bookable.dto

import com.buildit.bookit.v1.booking.dto.Booking
import com.fasterxml.jackson.annotation.JsonIgnore

interface IBookable {
    val id: Int
    val locationId: Int
    val name: String
    val available: Boolean
}

/**
 * Bookable resource response
 */
data class Bookable(override val id: Int,
                    override val locationId: Int,
                    override val name: String,
                    override val available: Boolean = true) : IBookable

data class BookableResource(
    @field:JsonIgnore
    val bookable: Bookable,
    val bookings: Collection<Booking>? = null
) : IBookable by bookable

