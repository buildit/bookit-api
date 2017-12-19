package com.buildit.bookit.v1.booking.dto

import com.buildit.bookit.v1.user.dto.User
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
    @NotNull(message = "bookableId is required")
    val bookableId: String?,
    @NotBlank(message = "subject is required and cannot be blank")
    val subject: String?,
    @NotNull(message = "start is required")
    val start: LocalDateTime?,
    @NotNull(message = "end is required")
    val end: LocalDateTime?
)

/**
 * Booking response
 */
data class Booking(
    val id: String,
    val bookableId: String,
    val subject: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val start: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val end: LocalDateTime,
    val user: User
)

fun Booking.interval(timeZone: ZoneId): Interval =
    Interval.of(
        this.start.atZone(timeZone).toInstant(),
        this.end.atZone(timeZone).toInstant())

