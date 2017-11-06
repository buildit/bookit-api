package com.buildit.bookit.v1.location.bookable.dto

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.dto.Location
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.NaturalId
import javax.annotation.Generated
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

interface IBookable {
    val id: Int
    val location: Location
    val name: String
    val disposition: Disposition
}

@Entity
data class Bookable(
    @Id @Generated
    override val id: Int,
    @ManyToOne(optional = false)
    @NaturalId
    override val location: Location,
    @Column(nullable = false)
    @NaturalId
    override val name: String,
    @Embedded
    override val disposition: Disposition = Disposition()) : IBookable

@Embeddable
data class Disposition(
    @Column(nullable = false)
    val closed: Boolean = false,
    @Column(nullable = false)
    val reason: String = ""
)

data class BookableResource(
    @JsonIgnore
    val bookable: Bookable,
    val bookings: Collection<Booking> = emptyList()
) : IBookable by bookable {
    @JsonIgnore
    override val location = bookable.location
    val locationId = bookable.location.id
}

