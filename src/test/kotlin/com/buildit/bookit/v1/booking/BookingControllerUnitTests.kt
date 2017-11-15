package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.location.bookable.InvalidBookable
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.Disposition
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class BookingControllerUnitTests {
    private lateinit var bookingController: BookingController
    private lateinit var locationRepo: LocationRepository
    private lateinit var bookableRepo: BookableRepository
    private val clock: Clock = Clock.systemUTC()
    private val today: LocalDate = LocalDate.now(ZoneId.of("America/New_York"))
    private val nycBookable1 = Bookable(1, 1, "NYC Bookable 1", Disposition())
    private val nycBookable2 = Bookable(2, 1, "NYC Bookable 2", Disposition())

    @BeforeEach
    fun setup() {
        locationRepo = mock {
            on { getLocations() }.doReturn(listOf(
                Location(1, "NYC", "America/New_York"),
                Location(2, "LON", "Europe/London")
            ))
        }

        bookableRepo = mock<BookableRepository> {
            on { getAllBookables() }.doReturn(listOf(nycBookable1, nycBookable2))
        }
    }

    @Nested
    inner class `|v1|booking` {
        @Nested
        inner class `GET` {
            private lateinit var bookingRepo: BookingRepository

            private val bookingToday = Booking(1, nycBookable1.id, "Booking today", today.atTime(9, 15), today.atTime(10, 15))
            private val bookingTomorrow = Booking(2, nycBookable1.id, "Booking tomorrow", today.plusDays(1).atTime(9, 15), today.plusDays(1).atTime(10, 15))
            private val bookingTodayDifferentBookable = Booking(3, nycBookable2.id, "Booking today, different bookable", today.atTime(11, 0), today.atTime(11, 30))
            private val bookingYesterday = Booking(4, nycBookable1.id, "Booking yesterday", today.minusDays(1).atTime(9, 15), today.minusDays(1).atTime(10, 15))

            @BeforeEach
            fun setup() {
                bookingRepo = mock {
                    on { getAllBookings() }.doReturn(
                        listOf(
                            bookingToday, bookingTomorrow, bookingTodayDifferentBookable, bookingYesterday
                        )
                    )
                }
                bookingController = BookingController(bookingRepo, bookableRepo, locationRepo, clock)
            }

            @Nested
            inner class `invoking getAllBookings()` {
                @Test
                fun `returns all existing bookings`() {
                    val bookings = bookingController.getAllBookings()
                    expect(bookings.size).to.be.equal(4)
                }

                @Test
                fun `returns all existing bookings filtered by start (inclusive) and end (exclusive)`() {
                    val bookings = bookingController.getAllBookings(today, today.plusDays(1))
                    expect(bookings).to.have.size(2)
                    expect(bookings).to.have.all.elements(bookingToday, bookingTodayDifferentBookable)
                }

                @Test
                fun `fails when end before start`() {
                    assertThat({
                        bookingController.getAllBookings(today, today.minusDays(1))
                    },
                        throws<EndBeforeStartException>())
                }

                @Test
                fun `fails when start == end`() {
                    assertThat({
                        bookingController.getAllBookings(today, today)
                    },
                        throws<EndBeforeStartException>())
                }

                @Test
                fun `start defaults to start of time`() {
                    val bookings = bookingController.getAllBookings(endDateExclusive = today.plusDays(1))
                    expect(bookings).to.have.size(3)
                    expect(bookings).to.have.all.elements(bookingToday, bookingTodayDifferentBookable, bookingYesterday)
                }

                @Test
                fun `end defaults to end of time`() {
                    val bookings = bookingController.getAllBookings(today)
                    expect(bookings).to.have.size(3)
                    expect(bookings).to.have.all.elements(bookingToday, bookingTodayDifferentBookable, bookingTomorrow)
                }

                @Test
                fun `no bookings on date`() {
                    val bookings = bookingController.getAllBookings(today.plusYears(1))
                    expect(bookings).to.have.size(0)
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
                    fun action() = BookingController(bookingRepo, mock {}, mock {}, clock).getBooking(99999)
                    assertThat({ action() }, throws<BookingNotFound>())
                }
            }
        }

        @Nested
        inner class `POST` {
            private val start = LocalDateTime.now(ZoneId.of("America/New_York")).plusHours(1).truncatedTo(ChronoUnit.MINUTES)
            private val end = start.plusHours(1)
            private val createdBooking = Booking(1, nycBookable1.id, "MyRequest", start, end)

            @BeforeEach
            fun setup() {
                val bookingRepository = mock<BookingRepository> {
                    on { insertBooking(nycBookable1.id, "MyRequest", start, end) }.doReturn(createdBooking)
                    on { getAllBookings() }.doReturn(listOf(
                        Booking(1, nycBookable1.id, "Before", start.minusHours(1), end.minusHours(1)),
                        Booking(2, nycBookable1.id, "After", start.plusHours(1), end.plusHours(1))
                    ))
                }
                bookingController = BookingController(bookingRepository, bookableRepo, locationRepo, clock)
            }

            @Test
            fun `should create a booking`() {
                val request = BookingRequest(nycBookable1.id, "MyRequest", start, end)

                val response = bookingController.createBooking(request)
                val booking = response.body

                expect(booking).to.equal(createdBooking)
            }

            @Test
            fun `should chop seconds`() {
                val request = BookingRequest(nycBookable1.id, "MyRequest", start.plusSeconds(59), end.plusSeconds(59))

                val response = bookingController.createBooking(request)
                val booking = response.body

                expect(booking).to.equal(createdBooking)
            }

            @Test
            fun `should validate bookable exists`() {
                val request = BookingRequest(999999, "MyRequest", start, end)
                fun action() = bookingController.createBooking(request)
                assertThat({ action() }, throws<InvalidBookable>())
            }

            @Test
            fun `should check that the bookable is available - overlap beginning`() {
                val request = BookingRequest(
                    nycBookable1.id,
                    "MyRequest",
                    start.minusMinutes(30),
                    end.minusMinutes(30))

                fun action() = bookingController.createBooking(request)
                assertThat({ action() }, throws<BookableNotAvailable>())
            }

            @Test
            fun `should check that the bookable is available - overlap end`() {
                val request = BookingRequest(
                    nycBookable1.id,
                    "MyRequest",
                    start.plusMinutes(30),
                    end.plusMinutes(30))

                fun action() = bookingController.createBooking(request)
                assertThat({ action() }, throws<BookableNotAvailable>())
            }
        }
    }
}
