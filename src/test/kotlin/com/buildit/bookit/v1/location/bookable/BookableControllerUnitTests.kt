package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.booking.EndBeforeStartException
import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import com.buildit.bookit.v1.location.dto.LocationNotFound
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

typealias Describe = Nested
typealias On = Nested
typealias It = Test

class BookableControllerUnitTests {
    val locationRepo = mock<LocationRepository> {
        on { getLocations() }.doReturn(listOf(
            Location(1, "NYC", "America/New_York"),
            Location(2, "LON", "Europe/London")
        ))
    }
    private val londonBookable = Bookable(1, 2, "London Bookable", true)
    val availableBookable = Bookable(1, 1, "The best bookable ever", true)
    val unavailableBookable = Bookable(1, 1, "The best bookable ever", false)

    val bookableRepo = mock<BookableRepository> {
        on { getAllBookables() }.doReturn(listOf(availableBookable, londonBookable))
    }
    val bookingRepo = mock<BookingRepository> {}

    val bookableController = BookableController(bookableRepo, locationRepo, bookingRepo)

    @Describe inner class `v1|bookable` {
        @Describe inner class `get single bookable` {
            @On inner class `that is known` {
                @It
                fun `should return bookable1`() {
                    val bookable = bookableController.getBookable(1, 1)

                    expect(bookable.name).to.be.equal("The best bookable ever")
                    expect(bookable.available).to.be.`true`
                }
            }

            @On inner class `that is unknown` {
                @It
                fun `throws exception for invalid bookable`() {
                    assertThat({ bookableController.getBookable(1, 2) }, throws<BookableNotFound>())
                }

                @It
                fun `throws exception for invalid location`() {
                    assertThat({ bookableController.getBookable(-1, 1) }, throws<LocationNotFound>())
                }
            }
        }

        @Describe inner class `get multiple bookables` {
            @On inner class `for location` {
                @It
                fun `returns all bookables`() {
                    val allBookables = bookableController.getAllBookables(1)
                    expect(allBookables).to.contain(availableBookable)
                }

                @It
                fun `with invalid location throws exception`() {
                    assertThat({ bookableController.getAllBookables(-1) }, throws<LocationNotFound>())
                }
            }

            @On inner class `with availability` {
                private val now: LocalDateTime = LocalDateTime.now(ZoneId.of("America/New_York")).truncatedTo(ChronoUnit.MINUTES)

                @It
                fun `requires endDate if startDate specified`() {
                    assertThat({
                        bookableController.getAllBookables(1, now.plusHours(1))
                    },
                        throws<InvalidBookableSearchEndDateRequired>())
                }

                @It
                fun `requires startDate if endDate specified`() {
                    assertThat({
                        bookableController.getAllBookables(1, null, now.plusHours(1))
                    },
                        throws<InvalidBookableSearchStartDateRequired>())
                }

                @It
                fun `requires startDate before endDate, equal`() {
                    assertThat({
                        bookableController.getAllBookables(1, now.plusHours(1), now.plusHours(1))
                    },
                        throws<EndBeforeStartException>())
                }

                @It
                fun `requires startDate before endDate, not equal`() {
                    assertThat({
                        bookableController.getAllBookables(1, now.plusHours(2), now.plusHours(1))
                    },
                        throws<EndBeforeStartException>())
                }

                @It
                fun `finds an available bookable`() {
                    expect(
                        bookableController.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(availableBookable)
                }

                @It
                fun `honors bookable available setting`() {
                    val bookableRepo = mock<BookableRepository> {
                        on { getAllBookables() }.doReturn(listOf(unavailableBookable))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(unavailableBookable)
                }

                @It
                fun `checks that there are no bookings for that time slot, same`() {
                    val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now.plusHours(1), now.plusHours(2))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(unavailableBookable)
                }

                @It
                fun `checks that there are no bookings for that time slot, same - chop seconds`() {
                    // arrange
                    val mockRepository = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now.plusHours(1).plusSeconds(59), now.plusHours(2).plusSeconds(59))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, mockRepository)

                    // act
                    val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                    // assert
                    expect(bookables).to.contain(unavailableBookable)
                }

                @It
                fun `ignores bookings for other bookables`() {
                    val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 2, "Booking", now.plusHours(1), now.plusHours(2))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(availableBookable)
                }

                @It
                fun `checks that there are no bookings for that time slot, overlap start`() {
                    val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now, now.plusMinutes(90))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(unavailableBookable)
                }

                @It
                fun `checks that there are no bookings for that time slot, overlap end`() {
                    val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now.plusMinutes(90), now.plusMinutes(180))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(unavailableBookable)
                }

                @It
                fun `checks that there are no bookings for that time slot, overlap both`() {
                    val bookingRepo = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now, now.plusHours(3))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, bookingRepo)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(unavailableBookable)
                }

                @It
                fun `checks that there are no bookings for that time slot, abut`() {
                    val mockRepository = mock<BookingRepository> {
                        on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now, now.plusHours(1))))
                    }
                    val controller = BookableController(bookableRepo, locationRepo, mockRepository)

                    expect(
                        controller.getAllBookables(1, now.plusHours(1), now.plusHours(2)))
                        .to.contain(availableBookable)
                }
            }
        }
    }
}
