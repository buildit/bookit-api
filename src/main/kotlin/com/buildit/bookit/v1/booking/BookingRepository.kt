package com.buildit.bookit.v1.booking

import com.buildit.bookit.database.BookItDBConnectionProvider
import com.buildit.bookit.v1.booking.dto.Booking
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId


fun mapFromResultSet(rs: ResultSet): Booking
{
    return Booking(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getTimestamp(4).toLocalDateTime(), rs.getTimestamp(5).toLocalDateTime())
}

class BookingRepository {
    private val fields = "BOOKING_ID, BOOKABLE_ID, SUBJECT, START_DATE, END_DATE"
    private val baseProjection = "SELECT $fields FROM BOOKING"

    // Not thread safe and should be fetched from a sequence
    private var bookingId = 0

    fun getAllBookings(): Collection<Booking> {
        return BookItDBConnectionProvider.fetch(baseProjection, ::mapFromResultSet)
    }

    fun insertBooking(bookableId: Int,
                      subject: String,
                      startDateTime: LocalDateTime,
                      endDateTime: LocalDateTime): Booking {
        bookingId++
        BookItDBConnectionProvider.insert("", { ps ->
            val tz = ZoneId.systemDefault() // this should be replaced with that of the bookable

            ps.setInt(1, bookingId)
            ps.setInt(2, bookableId)
            ps.setString(3, subject)
            ps.setTimestamp(4, Timestamp.from(startDateTime.toInstant(tz.rules.getOffset(startDateTime))))
            ps.setTimestamp(5, Timestamp.from(endDateTime.toInstant(tz.rules.getOffset(endDateTime))))
        })

        return Booking(bookingId, bookableId, subject, startDateTime, endDateTime)
    }
}
