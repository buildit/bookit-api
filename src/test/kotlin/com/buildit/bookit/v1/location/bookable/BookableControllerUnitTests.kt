package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.booking.dto.Booking
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
    private val NYC = ZoneId.of("America/New_York")
    private val location = Location(1, "NYC", NYC)
    val availableBookable = Bookable(1, 1, "The best bookable ever", Disposition())
    private val londonBookable = Bookable(1, 2, "London Bookable", Disposition())

    val bookableRepo = mock<BookableRepository> {
        on { getAllBookables() }.doReturn(listOf(availableBookable, londonBookable))
    }
    val bookingRepo = mock<BookingRepository> {}

    val bookableController = BookableController(bookableRepo, bookingRepo)

    @Nested
    inner class `v1|bookable` {
        @Nested
        inner class `get single bookable` {
            @Nested inner class `that is known` {
                @Test
                fun `should return bookable1`() {
                    val bookable = bookableController.getBookable(location, 1)

                    expect(bookable).to.be.equal(BookableResource(availableBookable))
                }
            }

            @Nested
            inner class `that is unknown` {
                @Test
                fun `throws exception for invalid bookable`() {
                    assertThat({ bookableController.getBookable(location, 2) }, throws<BookableNotFound>())
                }

                @Test
                fun `throws exception for invalid location`() {
                    assertThat({ bookableController.getBookable(null, 1) }, throws<LocationNotFound>())
                }
            }
        }

        @Nested
        inner class `get multiple bookables` {
            @Nested
            inner class `for location` {
                @Test
                fun `returns all bookables`() {
                    val allBookables = bookableController.getAllBookables(location)
                    expect(allBookables).to.contain(BookableResource(availableBookable))
                }

                @Test
                fun `with invalid location throws exception`() {
                    assertThat({ bookableController.getAllBookables(null) }, throws<LocationNotFound>())
                }
            }

            @Nested
            inner class `expand bookings` {
                private val today: LocalDate = LocalDate.now(NYC)
                private val expandBookings = listOf("bookings")

                @Test
                fun `requires startDate before endDate`() {
                    assertThat({
                        bookableController.getAllBookables(location, today, today.minusDays(1), expandBookings)
                    },
                        throws<EndBeforeStartException>())
                }

                @Test
                fun `finds an available bookable - no bookings`() {
                    expect(
                        bookableController.getAllBookables(location, today, today, expandBookings))
                        .to.contain(BookableResource(availableBookable, emptyList()))
                }

                @Nested
                inner class `with bookings` {
                    private val booking = Booking(1, 1, "Booking", today.atTime(9, 15), today.atTime(10, 15))
                    private val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(booking))
                    }

                    private val controller = BookableController(bookableRepo, bookingRepo)

                    @Test
                    fun `finds bookable - with bookings`() {
                        expect(
                            controller.getAllBookables(location, today, today, expandBookings))
                            .to.contain(BookableResource(availableBookable, listOf(booking)))
                    }

                    @Test
                    fun `endDate defaults to startDate`() {
                        expect(
                            controller.getAllBookables(location, today, expand = expandBookings))
                            .to.contain(BookableResource(availableBookable, listOf(booking)))
                    }

                    @Test
                    fun `startDate defaults to today`() {
                        expect(
                            controller.getAllBookables(location, endDate = today, expand = expandBookings))
                            .to.contain(BookableResource(availableBookable, listOf(booking)))
                    }

                    @Test
                    fun `finds bookable - no bookings on date`() {
                        expect(
                            controller.getAllBookables(location, today.plusDays(1), today.plusDays(1), expandBookings))
                            .to.contain(BookableResource(availableBookable, emptyList()))
                    }

                    @Test
                    fun `ignores bookings for other bookables`() {
                        val bookingRepo = mock<BookingRepository> {
                            on { getAllBookings() }.doReturn(listOf(Booking(1, 2, "Booking", today.atTime(9, 15), today.atTime(10, 15))))
                        }
                        val controller = BookableController(bookableRepo, bookingRepo)

                        expect(
                            controller.getAllBookables(location, expand = expandBookings))
                            .to.contain(BookableResource(availableBookable, emptyList()))
                    }
                }
            }
        }
    }
}
