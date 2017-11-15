package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.winterbe.expekt.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

/**
 * Booking controller unit tests
 */
@ExtendWith(SpringExtension::class)
@DataJpaTest
class BookingRepositoryTests @Autowired constructor(
    val bookingRepo: BookingRepository,
    val bookableRepo: BookableRepository,
    val entityManager: TestEntityManager
) {
    private val bookable = bookableRepo.findOne(1)

    @Test
    fun getAllBookingsNoBookings() {
        // act
        val bookings = bookingRepo.findAll()?.toList()

        // assert
        expect(bookings).has.size(0)
    }

    @Test
    fun insertBooking() {
        // arrange
        val start = LocalDateTime.parse("2017-04-21T10:00")
        val end = LocalDateTime.parse("2017-04-21T11:00")

        // act
        val booking = bookingRepo.save(Booking(bookable, "My Inserted", start, end))

        // assert
        expect(booking.id).to.be.equal(1)
        expect(booking.bookable).to.be.equal(bookable)
        expect(booking.subject).to.be.equal("My Inserted")
        expect(booking.start).to.be.equal(start)
        expect(booking.end).to.be.equal(end)
    }

    @Test
    fun getAllBookings1Booking() {
        // arrange
        val start = LocalDateTime.parse("2017-04-21T10:00")
        val end = LocalDateTime.parse("2017-04-21T11:00")
        val booking = bookingRepo.save(Booking(bookable, "My Inserted", start, end))

        // act
        val bookings = bookingRepo.findAll()?.toList()

        // assert
        expect(bookings).to.contain(booking)
    }
}
