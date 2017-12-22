package com.buildit.bookit.v1.booking.dto

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.user.dto.User
import com.fasterxml.jackson.annotation.JsonFormat
import org.hibernate.annotations.GenericGenerator
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
    @field:NotNull(message = "bookableId is required")
    val bookableId: String?,
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
@Entity
data class Booking(
    @ManyToOne(optional = false)
    val bookable: Bookable,
    @Column(nullable = false)
    val subject: String,
    @Column(nullable = false) @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val start: LocalDateTime,
    @Column(nullable = false) @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val end: LocalDateTime,
    @ManyToOne(optional = false)
    val user: User,
    @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(length = 36)
    val id: String? = null
)

fun Booking.interval(timeZone: ZoneId): Interval =
    Interval.of(
        this.start.atZone(timeZone).toInstant(),
        this.end.atZone(timeZone).toInstant())

