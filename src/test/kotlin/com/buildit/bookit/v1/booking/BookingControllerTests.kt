package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Booking controller unit tests
 */
object BookingControllerTests : Spek({
    describe("/v1/booking") {
        val mockLocationRepository = mock<LocationRepository> {
            on { getLocations() }.doReturn(listOf(
                Location(1, "NYC", "America/New_York"),
                Location(2, "LON", "Europe/London")
            ))
        }

        val clock = Clock.systemUTC()
        on("invoking getAllBookings()") {
            it("should get all bookings") {
                val startDateTime = LocalDateTime.parse("2017-09-26T09:00:00")
                val endDateTime = LocalDateTime.parse("2017-09-26T10:00:00")

                val mockRepo = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 2, "Booking", startDateTime, endDateTime)))
                }
                val bookings = BookingController(mockRepo, mock {}, mockLocationRepository, clock).getAllBookings().body
                expect(bookings.size).to.be.equal(1)
            }
        }

        on("invoking getBooking()") {
            it("should throw an exception") {
                // arrange

                // act
                fun action() = BookingController(mock {}, mock {}, mockLocationRepository, clock).getBooking(2)

                // assert
                assertThat({ action() }, throws<BookingNotFound>())
            }
        }

        on("createBooking()") {
            val start = LocalDateTime.now(ZoneId.of("America/New_York")).plusHours(1)
            val end = start.plusHours(1)
            val createdBooking = Booking(1, 999999, "MyRequest", start, end)
            val mockRepo = mock<BookingRepository> {
                on { insertBooking(999999, "MyRequest", start, end) }.doReturn(createdBooking)
            }
            val mockBookableRepo = mock<BookableRepository> {
                on { getAllBookables() }.doReturn(listOf(Bookable(999999, 1, "Bookable", true)))
            }

            val bookingController = BookingController(mockRepo, mockBookableRepo, mockLocationRepository, clock)

            it("should create a booking") {
                // arrange
                val request = BookingRequest(999999, "MyRequest", start, end)

                // act
                val response = bookingController.createBooking(request)

                // assert
                val booking = response.body
                expect(booking).to.equal(createdBooking)
            }

            it("should validate bookable exists") {
                // arrange
                val request = BookingRequest(12345, "MyRequest", start, end)

                // act
                fun action() = bookingController.createBooking(request)

                // assert
                assertThat({ action() }, throws<InvalidBookable>())
            }

            it("should validate end after start") {
                // arrange
                val request = BookingRequest(999999, "MyRequest", end, start)

                // act
                fun action() = bookingController.createBooking(request)

                // assert
                assertThat({ action() }, throws<EndDateTimeBeforeStartTimeException>())
            }

            it("should validate start time in future") {
                // arrange
                val request = BookingRequest(999999, "MyRequest", start.minusDays(1), end)

                // act
                fun action() = bookingController.createBooking(request)

                // assert
                assertThat({ action() }, throws<StartDateTimeInPastException>())
            }
        }
    }
})
