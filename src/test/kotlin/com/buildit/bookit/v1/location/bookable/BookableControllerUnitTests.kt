package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.auth.UserPrincipal
import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.User
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
    val nyc = Location("NYC", ZoneId.of("America/New_York"), "guid1")
    val nycBookable1 = Bookable(nyc, "NYC Bookable 1", Disposition(), "guid1")
    val nycBookable2 = Bookable(nyc, "NYC Bookable 2", Disposition(), "guid2")

    val bookableRepo = mock<BookableRepository> {
        on { findByLocation(nyc) }.doReturn(listOf(nycBookable1, nycBookable2))
    }
    private val userPrincipal = UserPrincipal("foo", "bar", "baz")
    private val bookingUser = User("111", "bar baz", "foo")
    private val anotherBookingUser = User("222", "user principal", "another")

    private val bookingRepo = mock<BookingRepository> {}

    val bookableController = BookableController(bookableRepo, bookingRepo)

    @Nested
    inner class `v1|location|bookable` {
        @Nested
        inner class `get single bookable` {
            @Nested inner class `that is known` {
                @Test
                fun `should return bookable1`() {
                    val bookable = bookableController.getBookable(nyc, nycBookable1)

                    expect(bookable).to.be.equal(BookableResource(nycBookable1))
                }
            }

            @Nested
            inner class `that is unknown` {
                @Test
                fun `throws exception for invalid bookable`() {
                    assertThat({ bookableController.getBookable(nyc, null) }, throws<BookableNotFound>())
                }

                @Test
                fun `throws exception for invalid location`() {
                    assertThat({ bookableController.getBookable(null, nycBookable1) }, throws<LocationNotFound>())
                }
            }
        }

        @Nested
        inner class `get multiple bookables` {
            @Nested
            inner class `for location` {
                @Test
                fun `returns all bookables`() {
                    val allBookables = bookableController.getAllBookables(nyc, userPrincipal)
                    expect(allBookables).to.contain(BookableResource(nycBookable1))
                    expect(allBookables).to.contain(BookableResource(nycBookable2))
                }

                @Test
                fun `with invalid location throws exception`() {
                    assertThat({ bookableController.getAllBookables(null, userPrincipal) }, throws<LocationNotFound>())
                }
            }

            @Nested
            inner class `expand bookings` {
                private val today: LocalDate = LocalDate.now(ZoneId.of("America/New_York"))
                private val expandBookings = listOf("bookings")

                @Test
                fun `requires startDate before endDate`() {
                    assertThat({
                        bookableController.getAllBookables(nyc, userPrincipal, today, today.minusDays(1), expandBookings)
                    },
                        throws<EndBeforeStartException>())
                }

                @Test
                fun `finds an available bookable - no bookings`() {
                    expect(
                        bookableController.getAllBookables(nyc, userPrincipal, today, today, expandBookings))
                        .to.contain(BookableResource(nycBookable1, emptyList()))
                }

                @Nested
                inner class `with bookings` {

                    private val bookingToday =
                        Booking("guid1",
                            nycBookable1.id!!,
                            "Booking 1",
                            today.atTime(9, 15),
                            today.atTime(10, 15),
                            bookingUser
                        )
                    private val anotherUsersBookingToday =
                        Booking("guidAnother",
                            nycBookable1.id!!,
                            "Booking Another",
                            today.atTime(12, 0),
                            today.atTime(12, 30),
                            anotherBookingUser
                        )
                    private val anotherUsersBookingTodayMasked = anotherUsersBookingToday.copy(subject = "**********")
                    private val bookingToday2 =
                        Booking("guid2",
                            nycBookable1.id!!,
                            "Booking 2",
                            today.atTime(11, 0),
                            today.atTime(11, 30),
                            bookingUser
                        )
                    private val bookingTodayDifferentBookable =
                        Booking("guid3",
                            nycBookable2.id!!,
                            "Booking 3, different bookable",
                            today.atTime(11, 0),
                            today.atTime(11, 30),
                            bookingUser
                        )
                    private val bookingYesterday =
                        Booking("guid4",
                            nycBookable1.id!!,
                            "Booking 4, yesterday",
                            today.minusDays(1).atTime(11, 0),
                            today.minusDays(1).atTime(11, 30),
                            bookingUser
                        )
                    private val bookingTomorrow =
                        Booking("guid5",
                            nycBookable1.id!!,
                            "Booking 5, tomorrow",
                            today.plusDays(1).atTime(11, 0),
                            today.plusDays(1).atTime(11, 30),
                            bookingUser
                        )

                    private val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(bookingToday, bookingToday2, anotherUsersBookingToday, bookingTodayDifferentBookable, bookingYesterday, bookingTomorrow))
                    }

                    private val controller = BookableController(bookableRepo, bookingRepo)

                    @Test
                    fun `finds bookable - with bookings from all users`() {
                        val bookables = controller.getAllBookables(nyc, userPrincipal, today, today.plusDays(1), expandBookings)
                        expect(bookables).to.contain(BookableResource(nycBookable1, listOf(bookingToday, bookingToday2, anotherUsersBookingTodayMasked)))
                        expect(bookables).to.contain(BookableResource(nycBookable2, listOf(bookingTodayDifferentBookable)))
                    }

                    @Test
                    fun `endDate defaults to end of time`() {
                        expect(
                            controller.getAllBookables(nyc, userPrincipal, today, expand = expandBookings))
                            .to.contain(BookableResource(nycBookable1, listOf(bookingToday, bookingToday2, anotherUsersBookingTodayMasked, bookingTomorrow)))
                    }

                    @Test
                    fun `startDate defaults to beginning of time`() {
                        expect(
                            controller.getAllBookables(nyc, userPrincipal, endDateExclusive = today, expand = expandBookings))
                            .to.contain(BookableResource(nycBookable1, listOf(bookingYesterday)))
                    }

                    @Test
                    fun `finds bookable - no bookings on date`() {
                        expect(
                            controller.getAllBookables(nyc, userPrincipal, today.plusDays(2), today.plusDays(3), expandBookings))
                            .to.contain(BookableResource(nycBookable1, emptyList()))
                    }

                    @Test
                    fun `ignores bookings for other bookables`() {
                        val bookingRepo = mock<BookingRepository> {
                            on { getAllBookings() }.doReturn(
                                listOf(
                                    Booking("guid1", "guid2", "Booking", today.atTime(9, 15), today.atTime(10, 15), bookingUser)
                                )
                            )
                        }
                        val controller = BookableController(bookableRepo, bookingRepo)

                        expect(
                            controller.getAllBookables(nyc, userPrincipal, expand = expandBookings))
                            .to.contain(BookableResource(nycBookable1, emptyList()))
                    }
                }
            }
        }
    }
}
