package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.bookable.dto.BookableNotFound
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.time.LocalDateTime

/**
 * Booking controller unit tests
 */
object BookingControllerTests : Spek({
        describe("get unknown booking") {
            on("GET") {
                it("should throw an exception") {
                    assertThat({ BookingController().getBooking(2) }, throws<BookableNotFound>())
                }
            }
        }

        describe("create a booking") {
            on("POST") {
                it("should create a booking") {
                    @Suppress("MagicNumber")
                    val request = BookingRequest(998, "MyRequest", LocalDateTime.now(), LocalDateTime.now())
                    val response = BookingController().createBooking(request)
                    val booking = response.body

                    expect(booking.subject).to.be.equal("MyRequest")
                    @Suppress("MagicNumber")
                    expect(booking.bookableId).to.be.equal(998)
                    expect(booking.bookingId).to.be.above(0)
                }
            }
        }
    })
