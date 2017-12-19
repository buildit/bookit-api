package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.user.UserRepository
import com.buildit.bookit.v1.user.dto.User
import com.winterbe.expekt.expect
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

/**
 * Booking controller unit tests
 */
@ExtendWith(SpringExtension::class)
@DataJpaTest
class BookingRepositoryTests @Autowired constructor(
    private val bookingRepo: BookingRepository,
    bookableRepo: BookableRepository,
    private val userRepo: UserRepository
) {
    private val start = LocalDateTime.parse("2017-04-21T10:00")
    private val end = LocalDateTime.parse("2017-04-21T11:00")
    private val bookable = bookableRepo.findAll().first()

    lateinit var creatingUser: User

    @BeforeEach
    fun createUser() {
        creatingUser = userRepo.save(User("external-userid-guid", "Test", "User"))
    }

    @Test
    fun getAllBookingsNoBookings() {
        val bookings = bookingRepo.findAll()?.toList()

        expect(bookings).has.size(0)
    }

    @Test
    fun insertBooking() {
        val booking = bookingRepo.save(
            Booking(
                bookable,
                "My Inserted",
                start,
                end,
                creatingUser
            )
        )

        expect(booking.id).not.to.be.`null`
        expect(booking.bookable).to.be.equal(bookable)
        expect(booking.subject).to.be.equal("My Inserted")
        expect(booking.start).to.be.equal(start)
        expect(booking.end).to.be.equal(end)
    }

    @Test
    fun `delete existing booking`() {
        val booking = bookingRepo.save(
            Booking(
                bookable,
                "My Inserted",
                start,
                end,
                creatingUser
            )
        )
        expect(bookingRepo.findOne(booking.id)).to.not.be.`null`
        bookingRepo.delete(booking)
        expect(bookingRepo.findOne(booking.id)).to.be.`null`
    }

    @Test
    fun getAllBookings1Booking() {
        val booking = bookingRepo.save(
            Booking(
                bookable,
                "My Inserted",
                start,
                end,
                creatingUser
            )
        )

        val bookings = bookingRepo.findAll()?.toList()

        expect(bookings).to.contain(booking)
    }
}
