package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.EndDateTimeBeforeStartTimeException
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
        on { getLocations() }.doReturn(listOf(Location(1, "NYC", "America/New_York")))
    }
    val mockBookableRepository = mock<BookableRepository> {
        on { getAllBookables() }.doReturn(listOf(Bookable(1, 1, "The best bookable ever", true)))
    }
    val bookableController = BookableController(mockBookableRepository, mockLocationRepository)

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
                assertThat({ bookableController.getBookable(2, 1) }, throws<LocationNotFound>())
            }
        }
    }

    describe("get all bookables") {
        on("Get all bookables") {
            it("returns all bookables") {
                // act
                val allBookables = bookableController.getAllBookables(1)

                // assert
                expect(allBookables).to.contain(Bookable(1, 1, "The best bookable ever", true))
            }
            it("should throw an exception - invalid locaiton") {
                assertThat({ bookableController.getAllBookables(2) }, throws<LocationNotFound>())
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

            it("should require startDate before endDate") {
                assertThat({ bookableController.getAllBookables(1, now.plusHours(1), now.plusHours(1)) }, throws<EndDateTimeBeforeStartTimeException>())
            }
        }
    }
})
