package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.location.bookable.InvalidBookable
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class BookingControllerUnitTests {

    @Nested
    inner class `|v1|booking` {
        private val clock: Clock = Clock.systemUTC()
        private val NYC = ZoneId.of("America/New_York")
        private val location = Location("NYC", NYC, 1)
        private val bookable1 = Bookable(location, "Bookable1")
        private val bookable2 = Bookable(location, "Bookable2")

        @Nested
        inner class `get request` {
            private lateinit var bookingRepo: BookingRepository
            private val startDateTime = LocalDateTime.parse("2017-09-26T09:00")
            private val endDateTime = LocalDateTime.parse("2017-09-26T10:00")
            private val booking = Booking(bookable1, "Booking", startDateTime, endDateTime, 1)

            @BeforeEach
            fun setup() {
                bookingRepo = mock {
                    on { findAll() }.doReturn(
                        listOf(
                            booking,
                            Booking(bookable2, "Another Booking", startDateTime, endDateTime, 2)
                        )
                    )
                }
            }

            @Nested
            inner class `invoking getAllBookings()` {

                @Test
                fun `returns all existing bookings`() {
                    val bookings = BookingController(bookingRepo, mock {}, clock).getAllBookings().body
                    expect(bookings.size).to.be.equal(2)
                }
            }

            @Nested
            inner class `invoking getBooking()` {

                @Test
                fun `getBooking() for existing booking returns that booking`() {
                    val booking = BookingController(bookingRepo, mock {}, clock).getBooking(booking)
                    expect(booking.id).to.be.equal(1)
                }

                @Test
                fun `getBooking() for nonexistent booking throws exception`() {
                    fun action() = BookingController(bookingRepo, mock {}, clock).getBooking(null)
                    assertThat({ action() }, throws<BookingNotFound>())
                }
            }
        }


        @Nested
        inner class `createBooking` {
            private lateinit var bookingController: BookingController

            private val start = LocalDateTime.now(NYC).plusHours(1).truncatedTo(ChronoUnit.MINUTES)
            private val end = start.plusHours(1)
            private val createdBooking = Booking(bookable1, "MyRequest", start, end, 1)

            @BeforeEach
            fun setup() {

                val bookingRepository = mock<BookingRepository> {
                    on { save(createdBooking) }.doReturn(createdBooking)
                    on { findAll() }.doReturn(listOf(
                        Booking(bookable1, "Before", start.minusHours(1), end.minusHours(1), 1),
                        Booking(bookable1, "After", start.plusHours(1), end.plusHours(1), 2)
                    ))
                }
                val bookableRepo = mock<BookableRepository> {
                    on { findOne(bookable1.id) }.doReturn(listOf(bookable1))
                }

                bookingController = BookingController(bookingRepository, bookableRepo, clock)
            }

            @Test
            fun `should create a booking`() {
                val request = BookingRequest(999999, "MyRequest", start, end)

                val response = bookingController.createBooking(request)
                val booking = response.body

                expect(booking).to.equal(createdBooking)
            }

            @Test
            fun `should chop seconds`() {
                val request = BookingRequest(999999, "MyRequest", start.plusSeconds(59), end.plusSeconds(59))

                val response = bookingController.createBooking(request)
                val booking = response.body

                expect(booking).to.equal(createdBooking)
            }

            @Test
            fun `should validate bookable exists`() {
                val request = BookingRequest(12345, "MyRequest", start, end)
                fun action() = bookingController.createBooking(request)
                assertThat({ action() }, throws<InvalidBookable>())
            }

            @Test
            fun `should check that the bookable is available - overlap beginning`() {
                val request = BookingRequest(
                    999999,
                    "MyRequest",
                    start.minusMinutes(30),
                    end.minusMinutes(30))

                fun action() = bookingController.createBooking(request)
                assertThat({ action() }, throws<BookableNotAvailable>())
            }

            @Test
            fun `should check that the bookable is available - overlap end`() {
                val request = BookingRequest(
                    999999,
                    "MyRequest",
                    start.plusMinutes(30),
                    end.plusMinutes(30))

                fun action() = bookingController.createBooking(request)
                assertThat({ action() }, throws<BookableNotAvailable>())
            }
        }
    }
}
