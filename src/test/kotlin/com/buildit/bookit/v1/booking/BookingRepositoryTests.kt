package com.buildit.bookit.v1.booking

import com.winterbe.expekt.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

/**
 * Booking controller unit tests
 */
@ExtendWith(SpringExtension::class)
@JdbcTest
class BookingRepositoryTests @Autowired constructor(
    val jdbcTemplate: JdbcTemplate
) {
    @Test
    fun getAllBookingsNoBookings() {
        // arrange
        val bookingRepo = BookingDatabaseRepository(jdbcTemplate)

        // act
        val bookings = bookingRepo.getAllBookings()

        // assert
        expect(bookings.size).to.be.equal(0)
    }

    @Test
    fun insertBooking() {
        // arrange
        val start = LocalDateTime.parse("2017-04-21T10:00:00")
        val end = LocalDateTime.parse("2017-04-21T11:00:00")
        val bookingRepo = BookingDatabaseRepository(jdbcTemplate)

        // act
        val booking = bookingRepo.insertBooking(1, "My Inserted", start, end)

        // assert
        expect(booking.id).to.be.equal(1)
        expect(booking.bookableId).to.be.equal(1)
        expect(booking.subject).to.be.equal("My Inserted")
        expect(booking.start).to.be.equal(start)
        expect(booking.end).to.be.equal(end)
    }

    @Test
    fun getAllBookings1Booking() {
        // arrange
        val start = LocalDateTime.parse("2017-04-21T10:00:00")
        val end = LocalDateTime.parse("2017-04-21T11:00:00")
        val bookingRepo = BookingDatabaseRepository(jdbcTemplate)
        val booking = bookingRepo.insertBooking(1, "My Inserted", start, end)

        // act
        val bookings = bookingRepo.getAllBookings()

        // assert
        expect(bookings).to.contain(booking)
    }
}
