package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.BookableResource
import com.buildit.bookit.v1.location.bookable.dto.Disposition
import com.buildit.bookit.v1.location.dto.Location
import com.buildit.bookit.v1.location.dto.LocationNotFound
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class BookableControllerUnitTests {
    val locationRepo = mock<LocationRepository> {
        on { getLocations() }.doReturn(listOf(
            Location("guid1", "NYC", "America/New_York"),
            Location("guid2", "LON", "Europe/London")
        ))
    }
    val nycBookable1 = Bookable("guid1", "guid1", "NYC Bookable 1", Disposition())
    val nycBookable2 = Bookable("guid2", "guid1", "NYC Bookable 2", Disposition())
    val londonBookable = Bookable("guid3", "guid2", "London Bookable 1", Disposition())

    val bookableRepo = mock<BookableRepository> {
        on { getAllBookables() }.doReturn(listOf(nycBookable1, nycBookable2, londonBookable))
    }
    val bookingRepo = mock<BookingRepository> {}

    val bookableController = BookableController(bookableRepo, locationRepo, bookingRepo)

    @Nested
    inner class `v1|location|bookable` {
        @Nested
        inner class `get single bookable` {
            @Nested inner class `that is known` {
                @Test
                fun `should return bookable1`() {
                    val bookable = bookableController.getBookable("guid1", "guid1")

                    expect(bookable).to.be.equal(BookableResource(nycBookable1))
                }
            }

            @Nested
            inner class `that is unknown` {
                @Test
                fun `throws exception for invalid bookable`() {
                    assertThat({ bookableController.getBookable("guid1", "guid-not-there") }, throws<BookableNotFound>())
                }

                @Test
                fun `throws exception for invalid location`() {
                    assertThat({ bookableController.getBookable("guid-not-there", "guid1") }, throws<LocationNotFound>())
                }
            }
        }

        @Nested
        inner class `get multiple bookables` {
            @Nested
            inner class `for location` {
                @Test
                fun `returns all bookables`() {
                    val allBookables = bookableController.getAllBookables("guid1")
                    expect(allBookables).to.contain(BookableResource(nycBookable1))
                    expect(allBookables).to.contain(BookableResource(nycBookable2))
                }

                @Test
                fun `with invalid location throws exception`() {
                    assertThat({ bookableController.getAllBookables("guid-not-there") }, throws<LocationNotFound>())
                }
            }

            @Nested
            inner class `expand bookings` {
                private val today: LocalDate = LocalDate.now(ZoneId.of("America/New_York"))
                private val expandBookings = listOf("bookings")

                @Test
                fun `requires startDate before endDate`() {
                    assertThat({
                        bookableController.getAllBookables("guid1", today, today.minusDays(1), expandBookings)
                    },
                        throws<EndBeforeStartException>())
                }

                @Test
                fun `finds an available bookable - no bookings`() {
                    expect(
                        bookableController.getAllBookables("guid1", today, today, expandBookings))
                        .to.contain(BookableResource(nycBookable1, emptyList()))
                }

                @Nested
                inner class `with bookings` {
                    private val bookingToday = Booking("guid1", nycBookable1.id, "Booking 1", today.atTime(9, 15), today.atTime(10, 15))
                    private val bookingToday2 = Booking("guid2", nycBookable1.id, "Booking 2", today.atTime(11, 0), today.atTime(11, 30))
                    private val bookingTodayDifferentBookable = Booking("guid3", nycBookable2.id, "Booking 3, different bookable", today.atTime(11, 0), today.atTime(11, 30))
                    private val bookingYesterday = Booking("guid4", nycBookable1.id, "Booking 4, yesterday", today.minusDays(1).atTime(11, 0), today.minusDays(1).atTime(11, 30))
                    private val bookingTomorrow = Booking("guid5", nycBookable1.id, "Booking 5, tomorrow", today.plusDays(1).atTime(11, 0), today.plusDays(1).atTime(11, 30))

                    private val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(bookingToday, bookingToday2, bookingTodayDifferentBookable, bookingYesterday, bookingTomorrow))
                    }

                    private val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    @Test
                    fun `finds bookable - with bookings`() {
                        val bookables = controller.getAllBookables("guid1", today, today.plusDays(1), expandBookings)
                        expect(bookables).to.contain(BookableResource(nycBookable1, listOf(bookingToday, bookingToday2)))
                        expect(bookables).to.contain(BookableResource(nycBookable2, listOf(bookingTodayDifferentBookable)))
                    }

                    @Test
                    fun `endDate defaults to end of time`() {
                        expect(
                            controller.getAllBookables("guid1", today, expand = expandBookings))
                            .to.contain(BookableResource(nycBookable1, listOf(bookingToday, bookingToday2, bookingTomorrow)))
                    }

                    @Test
                    fun `startDate defaults to beginning of time`() {
                        expect(
                            controller.getAllBookables("guid1", endDateExclusive = today, expand = expandBookings))
                            .to.contain(BookableResource(nycBookable1, listOf(bookingYesterday)))
                    }

                    @Test
                    fun `finds bookable - no bookings on date`() {
                        expect(
                            controller.getAllBookables("guid1", today.plusDays(2), today.plusDays(3), expandBookings))
                            .to.contain(BookableResource(nycBookable1, emptyList()))
                    }

                    @Test
                    fun `ignores bookings for other bookables`() {
                        val bookingRepo = mock<BookingRepository> {
                            on { getAllBookings() }.doReturn(
                                listOf(
                                    Booking("guid1", "guid2", "Booking", today.atTime(9, 15), today.atTime(10, 15))
                                )
                            )
                        }
                        val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                        expect(
                            controller.getAllBookables("guid1", expand = expandBookings))
                            .to.contain(BookableResource(nycBookable1, emptyList()))
                    }
                }
            }
        }
    }
}
