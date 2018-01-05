package com.buildit.bookit.v1.booking.dto

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.user.dto.User
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.GenericGenerator
import org.hibernate.validator.constraints.NotBlank
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.threeten.extra.Interval
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull

/**
 * Booking request
 */
data class BookingRequest(
    @field:NotNull
    val bookableId: String?,
    @field:NotBlank
    val subject: String?,
    @ApiModelProperty(example = "2017-12-29T14:00", required = true)
    @field:NotNull
    val start: LocalDateTime?,
    @ApiModelProperty(example = "2017-12-29T15:00", required = true)
    @field:NotNull
    val end: LocalDateTime?
)

@Suppress("UnnecessaryAbstractClass")
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class Auditable {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var created: Instant? = null
    @LastModifiedDate
    @Column
    var lastModified: Instant? = null
}

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
    @ApiModelProperty(example = "2017-12-29T14:00")
    val start: LocalDateTime,
    @Column(nullable = false) @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @ApiModelProperty(example = "2017-12-29T15:00")
    val end: LocalDateTime,
    @ManyToOne(optional = false)
    val user: User,
    @Id @GeneratedValue(generator = "uuid2") @GenericGenerator(name = "uuid2", strategy = "uuid2") @Column(length = 36)
    val id: String? = null
) : Auditable() {
    val startTimezoneAbbreviation get() = this.start.atZone(bookable.location.timeZone).format(DateTimeFormatter.ofPattern("zzz"))
    val endTimezoneAbbreviation get() = this.end.atZone(bookable.location.timeZone).format(DateTimeFormatter.ofPattern("zzz"))
}

fun Booking.interval(timeZone: ZoneId): Interval =
    Interval.of(
        this.start.atZone(timeZone).toInstant(),
        this.end.atZone(timeZone).toInstant())

