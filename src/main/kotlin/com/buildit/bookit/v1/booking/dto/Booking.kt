package com.buildit.bookit.v1.booking.dto

import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * Booking request
 */
data class BookingRequest(
    @NotNull val bookableId: Int?,
    @NotNull @Min(1) val subject: String?,
    @NotNull val start: LocalDateTime?,
    @NotNull val end: LocalDateTime?
)

/**
 * Booking response
 */
data class Booking(
    val id: Int,
    val bookableId: Int,
    val subject: String,
    val start: LocalDateTime,
    val end: LocalDateTime
)

