package com.buildit.bookit.v1.booking.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.hibernate.validator.constraints.NotBlank
import org.threeten.extra.Interval
import java.time.LocalDateTime
import java.time.ZoneId
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
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val start: LocalDateTime,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val end: LocalDateTime
)

fun Booking.interval(timeZone: ZoneId): Interval =
    Interval.of(
        this.start.atZone(timeZone).toInstant(),
        this.end.atZone(timeZone).toInstant())

