package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.BookableNotFound
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Bookable controller unit tests
 */
object BookableControllerTests : Spek({
    describe("get known location") {
        on("GET") {
            it("should return UP") {
                // arrange
                val bookableController = BookableController()

                // act
                val controller = bookableController.getBookable("NYC", "The best bookable ever")

                // assert
                expect(controller.name).to.be.equal("The best bookable ever")
                expect(controller.location).to.be.equal("NYC")
            }
        }
    }

    describe("get unknown bookable") {
        on("GET") {
            it("should throw an exception - invalid bookable") {
                assertThat({ BookableController().getBookable("NYC", "foo") }, throws<BookableNotFound>())
            }
            it("should throw an exception - invalid locaiton") {
                assertThat({ BookableController().getBookable("foo", "The best bookable ever") }, throws<BookableNotFound>())
            }
        }
    }
})
