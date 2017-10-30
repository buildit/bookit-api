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
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Bookable controller unit tests
 */
object BookableControllerTests : Spek({
    val mockLocationRepository = mock<LocationRepository> {
        on { getLocations() }.doReturn(listOf(
            Location(1, "NYC", "America/New_York"),
            Location(2, "LON", "Europe/London")
        ))
    }
    val availableBookable = Bookable(1, 1, "The best bookable ever", true)
    val unavailableBookable = Bookable(1, 1, "The best bookable ever", false)
    val londonBookable = Bookable(1, 2, "London Bookable", true)
    val mockBookableRepository = mock<BookableRepository> {
        on { getAllBookables() }.doReturn(listOf(availableBookable, londonBookable))
    }
    val mockBookingRepository = mock<BookingRepository> {}
    val bookableController = BookableController(mockBookableRepository, mockLocationRepository, mockBookingRepository)

    describe("get 1 bookable") {
        on("GET known bookable") {
            it("should return bookable") {
                // act
                val bookable = bookableController.getBookable(1, 1)

                // assert
                expect(bookable.name).to.be.equal("The best bookable ever")
                expect(bookable.available).to.be.`true`
            }
        }

        on("GET unknown bookable") {
            it("should throw an exception - invalid bookable") {
                assertThat({ bookableController.getBookable(1, 2) }, throws<BookableNotFound>())
            }

            it("should throw an exception - invalid locaiton") {
                assertThat({ bookableController.getBookable(-1, 1) }, throws<LocationNotFound>())
            }
        }
    }

    describe("get all bookables") {
        on("Get all bookables") {
            it("returns all bookables") {
                // act
                val allBookables = bookableController.getAllBookables(1)

                // assert
                expect(allBookables).to.contain(availableBookable)
            }

            it("should throw an exception - invalid locaiton") {
                assertThat({ bookableController.getAllBookables(-1) }, throws<LocationNotFound>())
            }
        }

        on("Find available bookables") {
            val now = LocalDateTime.now(ZoneId.of("America/New_York"))
            it("should require endDate if startDate specified") {
                assertThat({ bookableController.getAllBookables(1, now.plusHours(1)) }, throws<InvalidBookableSearchEndDateRequired>())
            }

            it("should require startDate if endDate specified") {
                assertThat({ bookableController.getAllBookables(1, null, now.plusHours(1)) }, throws<InvalidBookableSearchStartDateRequired>())
            }

            it("should require startDate before endDate, equal") {
                assertThat({ bookableController.getAllBookables(1, now.plusHours(1), now.plusHours(1)) }, throws<EndBeforeStartException>())
            }

            it("should require startDate before endDate, not equal") {
                assertThat({ bookableController.getAllBookables(1, now.plusHours(2), now.plusHours(1)) }, throws<EndBeforeStartException>())
            }

            it("should find an available bookable") {
                // act
                val bookables = bookableController.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(availableBookable)
            }

            it("should find an available bookable") {
                // act
                val bookables = bookableController.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(availableBookable)
            }

            it("should honor bookable available setting") {
                // arrange
                val mockRepo = mock<BookableRepository> {
                    on { getAllBookables() }.doReturn(listOf(unavailableBookable))
                }
                val controller = BookableController(mockRepo, mockLocationRepository, mockBookingRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(unavailableBookable)
            }

            it("should check that there are no bookings for that time slot, same") {
                // arrange
                val mockRepository = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now.plusHours(1), now.plusHours(2))))
                }
                val controller = BookableController(mockBookableRepository, mockLocationRepository, mockRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(unavailableBookable)
            }

            it("should ignore bookings for other bookables") {
                // arrange
                val mockRepository = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 2, "Booking", now.plusHours(1), now.plusHours(2))))
                }
                val controller = BookableController(mockBookableRepository, mockLocationRepository, mockRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(availableBookable)
            }

            it("should check that there are no bookings for that time slot, overlap start") {
                // arrange
                val mockRepository = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now, now.plusMinutes(90))))
                }
                val controller = BookableController(mockBookableRepository, mockLocationRepository, mockRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(unavailableBookable)
            }

            it("should check that there are no bookings for that time slot, overlap end") {
                // arrange
                val mockRepository = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now.plusMinutes(90), now.plusMinutes(180))))
                }
                val controller = BookableController(mockBookableRepository, mockLocationRepository, mockRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(unavailableBookable)
            }

            it("should check that there are no bookings for that time slot, overlap both") {
                // arrange
                val mockRepository = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now, now.plusHours(3))))
                }
                val controller = BookableController(mockBookableRepository, mockLocationRepository, mockRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(unavailableBookable)
            }

            it("should check that there are no bookings for that time slot, abut") {
                // arrange
                val mockRepository = mock<BookingRepository> {
                    on { getAllBookings() }.doReturn(listOf(Booking(1, 1, "Booking", now, now.plusHours(1))))
                }
                val controller = BookableController(mockBookableRepository, mockLocationRepository, mockRepository)

                // act
                val bookables = controller.getAllBookables(1, now.plusHours(1), now.plusHours(2))

                // assert
                expect(bookables).to.contain(availableBookable)
            }
        }
    }
})
