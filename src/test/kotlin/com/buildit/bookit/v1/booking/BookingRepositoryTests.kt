package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.User
import com.buildit.bookit.v1.user.UserDatabaseRepository
import com.winterbe.expekt.expect
import org.junit.jupiter.api.BeforeEach
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
class BookingRepositoryTests @Autowired constructor(val jdbcTemplate: JdbcTemplate) {
    val start = LocalDateTime.parse("2017-04-21T10:00")
    val end = LocalDateTime.parse("2017-04-21T11:00")
    val bookingRepo = BookingDatabaseRepository(jdbcTemplate)
    val userRepo = UserDatabaseRepository(jdbcTemplate)

    lateinit var creatingUser: User

    @BeforeEach
    fun createUser() {
        creatingUser = userRepo.insertUser("external-userid-guid", "Test", "User")
    }

    @Test
    fun getAllBookingsNoBookings() {
        val bookings = bookingRepo.getAllBookings()

        expect(bookings.size).to.be.equal(0)
    }

    @Test
    fun insertBooking() {
        val booking = bookingRepo.insertBooking(
            "guid", "" +
            "My Inserted",
            start,
            end,
            creatingUser)

        expect(booking.id).not.to.be.`null`
        expect(booking.bookableId).to.be.equal("guid")
        expect(booking.subject).to.be.equal("My Inserted")
        expect(booking.start).to.be.equal(start)
        expect(booking.end).to.be.equal(end)
    }

    @Test
    fun `delete existing booking`() {
        val booking = bookingRepo.insertBooking("guid", "My Inserted", start, end, creatingUser)
        expect(bookingRepo.getAllBookings()).to.have.size(1)
        bookingRepo.delete(booking.id)
        expect(bookingRepo.getAllBookings()).to.be.empty
    }

    @Test
    fun `delete booking does not exist`() {
        bookingRepo.delete("12345")
        expect(bookingRepo.getAllBookings()).to.be.empty
    }

    @Test
    fun getAllBookings1Booking() {
        val booking = bookingRepo.insertBooking("guid", "My Inserted", start, end, creatingUser)

        val bookings = bookingRepo.getAllBookings()

        expect(bookings).to.contain(booking)
    }
}
