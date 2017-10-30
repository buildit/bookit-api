package com.buildit.bookit.v1.booking.dto

import org.hibernate.validator.constraints.NotBlank
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

/**
 * Booking request
 */
data class BookingRequest(
    @field:NotNull(message = "bookableId is required")
    val bookableId: Int?,
    @field:NotBlank(message = "subject is required and cannot be blank")
    val subject: String?,
    @field:NotNull(message = "start is required")
    val start: LocalDateTime?,
    @field:NotNull(message = "end is required")
    val end: LocalDateTime?
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

