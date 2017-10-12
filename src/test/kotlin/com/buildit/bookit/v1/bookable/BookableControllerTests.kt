package com.buildit.bookit.v1.bookable

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
object BookableControllerTests : Spek(
    {
        describe("get known location")
        {
            on("GET")
            {
                it("should return UP")
                {
                    val bookableController = BookableController()
                    expect(bookableController.getBookable(1).bookableName).to.be.equal("The best bookable ever")
                }
            }
        }

        describe("get unknown location")
        {
            on("GET")
            {
                it("should throw an exception")
                {
                    assertThat({ BookableController().getBookable(2) }, throws<BookableNotFound>())
                }
            }
        }
    })
