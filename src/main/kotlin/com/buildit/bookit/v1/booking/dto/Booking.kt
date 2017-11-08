package com.buildit.bookit.v1.booking.dto

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.fasterxml.jackson.annotation.JsonFormat
import org.hibernate.validator.constraints.NotBlank
import org.threeten.extra.Interval
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

/**
 * Booking request
 */
data class BookingRequest(
    @NotNull(message = "bookableId is required")
    val bookableId: Int?,
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
@Entity
data class Booking(
    @Id @GeneratedValue
    val id: Int? = null,
    @ManyToOne(optional = false)
    val bookable: Bookable,
    @Column(nullable = false)
    val subject: String,
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val start: LocalDateTime,
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val end: LocalDateTime
)

fun Booking.interval(timeZone: ZoneId): Interval =
    Interval.of(
        this.start.atZone(timeZone).toInstant(),
        this.end.atZone(timeZone).toInstant())

