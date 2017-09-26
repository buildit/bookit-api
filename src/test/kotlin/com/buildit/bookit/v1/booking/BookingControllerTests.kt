package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.BookingController
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object BookingControllerTests : Spek(
    {
        describe("create a booking")
        {
            on("POST")
            {
                it("should create a booking")
                {
                    val booking = BookingController().createBooking()
                    expect(booking.subject).to.be.equal("The best booking ever")
                    expect(booking.bookableId).to.be.equal(1)
                    expect(booking.bookingId).to.be.equal(1)
                }
            }
        }
    })
