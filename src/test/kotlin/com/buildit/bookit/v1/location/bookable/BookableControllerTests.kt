package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.BookableNotFound
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
    describe("get known bookable") {
        on("GET") {
            it("should return bookable") {
                // arrange
                val bookableController = BookableController(mockBookableRepository, mockLocationRepository)

                // act
                val controller = bookableController.getBookable(1, 1)

                // assert
                expect(controller.name).to.be.equal("The best bookable ever")
                expect(controller.available).to.be.`true`
            }
        }
    }

    describe("get unknown bookable") {
        on("GET") {
            it("should throw an exception - invalid bookable") {
                assertThat({ BookableController(mockBookableRepository, mockLocationRepository).getBookable(1, 2) }, throws<BookableNotFound>())
            }
            it("should throw an exception - invalid locaiton") {
                assertThat({ BookableController(mockBookableRepository, mockLocationRepository).getBookable(2, 1) }, throws<LocationNotFound>())
            }
        }
    }
})
