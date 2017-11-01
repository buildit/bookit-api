package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.LocationRepository
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

        @Nested
        inner class `get request` {
            private lateinit var bookingRepo: BookingRepository

            @BeforeEach
            fun setup() {
                val startDateTime = LocalDateTime.parse("2017-09-26T09:00")
                val endDateTime = LocalDateTime.parse("2017-09-26T10:00")
                bookingRepo = mock {
                    on { getAllBookings() }.doReturn(
                        listOf(
                            Booking(1, 10, "Booking", startDateTime, endDateTime),
                            Booking(2, 20, "Another Booking", startDateTime, endDateTime)
                        )
                    )
                }
            }

            @Nested
            inner class `invoking getAllBookings()` {

                @Test
                fun `returns all existing bookings`() {
                    val bookings = BookingController(bookingRepo, mock {}, mock {}, clock).getAllBookings().body
                    expect(bookings.size).to.be.equal(2)
                }
            }

            @Nested
            inner class `invoking getBooking()` {

                @Test
                fun `getBooking() for existing booking returns that booking`() {
                    val booking = BookingController(bookingRepo, mock {}, mock {}, clock).getBooking(1)
                    expect(booking.id).to.be.equal(1)
                }

                @Test
                fun `getBooking() for nonexistent booking throws exception`() {
                    fun action() = BookingController(bookingRepo, mock {}, mock {}, clock).getBooking(3)
                    assertThat({ action() }, throws<BookingNotFound>())
                }
            }
        }

        @Nested
        inner class `createBooking` {
            private lateinit var bookingController: BookingController
            private lateinit var locationRepo: LocationRepository

            private val start = LocalDateTime.now(ZoneId.of("America/New_York")).plusHours(1).truncatedTo(ChronoUnit.MINUTES)
            private val end = start.plusHours(1)
            private val createdBooking = Booking(1, 999999, "MyRequest", start, end)

            @BeforeEach
            fun setup() {
                locationRepo = mock {
                    on { getLocations() }.doReturn(listOf(
                        Location(1, "NYC", "America/New_York"),
                        Location(2, "LON", "Europe/London")
                    ))
                }

                val bookingRepository = mock<BookingRepository> {
                    on { insertBooking(999999, "MyRequest", start, end) }.doReturn(createdBooking)
                    on { getAllBookings() }.doReturn(listOf(
                        Booking(1, 999999, "Before", start.minusHours(1), end.minusHours(1)),
                        Booking(2, 999999, "After", start.plusHours(1), end.plusHours(1))
                    ))
                }
                val bookableRepo = mock<BookableRepository> {
                    on { getAllBookables() }.doReturn(listOf(Bookable(999999, 1, "Bookable", true)))
                }

                bookingController = BookingController(bookingRepository, bookableRepo, locationRepo, clock)
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
