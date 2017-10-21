package com.buildit.bookit.v1.booking

import com.buildit.bookit.database.ConnectionProvider
import com.buildit.bookit.v1.booking.dto.Booking
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Booking controller unit tests
 */
object BookingRepositoryTests : Spek({
    describe("map result set") {
        on("getting a result set") {
            it("should map it to a booking") {
                val default = ZoneId.systemDefault()
                val start = LocalDateTime.parse("2017-01-21T10:00:00")
                val end = LocalDateTime.parse("2017-01-21T11:00:00")

                val mockRS = mock<ResultSet> {
                    on { getInt("BOOKING_ID") }.doReturn(1)
                    on { getInt("BOOKABLE_ID") }.doReturn(2)
                    on { getString("SUBJECT") }.doReturn("My Meeting")
                    on { getTimestamp("START_DATE") }.doReturn(Timestamp.from(start.toInstant(default.rules.getOffset(start))))
                    on { getTimestamp("END_DATE") }.doReturn(Timestamp.from(end.toInstant(default.rules.getOffset(end))))
                }

                val booking = mapFromResultSet(mockRS)

                expect(booking.bookingId).to.be.equal(1)
                expect(booking.bookableId).to.be.equal(2)
                expect(booking.subject).to.be.equal("My Meeting")
                expect(booking.startDateTime).to.be.equal(start)
                expect(booking.endDateTime).to.be.equal(end)
            }
        }
    }

    describe("apply parameters result set") {
        on("mapping parameters to an prepared statement") {
            it("should apply them to the proper position") {
                val start = LocalDateTime.parse("2017-02-21T10:00:00")
                val end = LocalDateTime.parse("2017-02-21T11:00:00")

                val mockRS = mock<PreparedStatement> {}

                applyParameters(mockRS, 1, 1, "My Meeting To Insert", start, end)

            }
        }
    }

    describe("get all bookings") {
        on("invoking getAllBookings()") {
            it("should get all bookings") {
                val connProvider = mock<ConnectionProvider> {
                    val start = LocalDateTime.parse("2017-03-21T10:00:00")
                    val end = LocalDateTime.parse("2017-03-21T11:00:00")

                    on {
                        fetch("SELECT BOOKING_ID, BOOKABLE_ID, SUBJECT, START_DATE, END_DATE FROM BOOKING", ::mapFromResultSet)
                    }.doReturn(listOf(Booking(1, 1, "My Booking", start, end)))
                }

                val bookingRepo = BookingRepository(connProvider)
                val bookings = bookingRepo.getAllBookings()
                expect(bookings.size).to.be.equal(1)

            }
        }
    }

    describe("inserting a booking") {
        on("invoking insertBookings()") {
            it("should insert a booking") {
                val start = LocalDateTime.parse("2017-04-21T10:00:00")
                val end = LocalDateTime.parse("2017-04-21T11:00:00")

                @Suppress("MagicNumber")
                val connProvider = mock<ConnectionProvider> {}

                val bookingRepo = BookingRepository(connProvider)
                val booking = bookingRepo.insertBooking(1, "My Inserted", start, end)
                expect(booking.bookingId).to.be.equal(1)
                expect(booking.bookableId).to.be.equal(1)
                expect(booking.subject).to.be.equal("My Inserted")
            }
        }
    }

})
