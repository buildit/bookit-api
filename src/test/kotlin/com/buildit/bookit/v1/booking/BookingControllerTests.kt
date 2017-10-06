package com.buildit.bookit.v1.booking

import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.time.ZonedDateTime

object BookingControllerTests : Spek(
    {
        describe("create a booking")
        {
            on("POST")
            {
                it("should create a booking")
                {
                    val request = BookingRequest(1, "MyRequest", ZonedDateTime.now(), ZonedDateTime.now())
                    val response = BookingController().createBooking(request)
                    val booking = response.body

                    expect(booking.subject).to.be.equal("MyRequest")
                    expect(booking.bookableId).to.be.equal(1)
                    expect(booking.bookingId).to.be.equal(1)
                }
            }
        }
    })
