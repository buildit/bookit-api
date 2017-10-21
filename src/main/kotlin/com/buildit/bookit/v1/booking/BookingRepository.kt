package com.buildit.bookit.v1.booking

import com.buildit.bookit.database.DataAccess
import com.buildit.bookit.v1.booking.dto.Booking
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger

val local: ZoneId = ZoneId.systemDefault()
val utc: ZoneId = ZoneId.of("UTC") // this should be replaced with that of the bookable

fun mapFromResultSet(rs: ResultSet): Booking
{
    fun toLocalDateTime(ts: Timestamp) = ZonedDateTime.ofInstant(ts.toInstant(), utc).withZoneSameInstant(local).toLocalDateTime()

    return Booking(
        rs.getInt("BOOKING_ID"),
        rs.getInt("BOOKABLE_ID"),
        rs.getString("SUBJECT"),
        toLocalDateTime(rs.getTimestamp("START_DATE")),
        toLocalDateTime(rs.getTimestamp("END_DATE")))
}

@Suppress("MagicNumber")
fun applyParameters(ps: PreparedStatement,
                    bookingId: Int,
                    bookableId: Int,
                    subject: String,
                    startDateTime: LocalDateTime,
                    endDateTime: LocalDateTime) {
    ps.setInt(1, bookingId)
    ps.setInt(2, bookableId)
    ps.setString(3, subject)
    ps.setTimestamp(4, Timestamp.from(startDateTime.toInstant(utc.rules.getOffset(startDateTime))))
    ps.setTimestamp(5, Timestamp.from(endDateTime.toInstant(utc.rules.getOffset(endDateTime))))
}

interface BookingRepository {
    fun getAllBookings(): Collection<Booking>
    fun insertBooking(bookableId: Int, subject: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Booking
}

class BookingDatabaseRepository(private val dataAccess: DataAccess) : BookingRepository {
    private val tableName = "BOOKING"

    private val fields = arrayOf("BOOKING_ID", "BOOKABLE_ID", "SUBJECT", "START_DATE", "END_DATE")

    private val projection = fields.joinToString()

    private val parameters = (1..fields.size).map { "?" }.joinToString()

    private val baseProjection = "SELECT $projection FROM $tableName"
    private val insertStatement = "INSERT INTO $tableName VALUES ($parameters)"

    // replace this with a s
    private var bookingId = AtomicInteger(0)

    override fun getAllBookings(): Collection<Booking> {
        return dataAccess.fetch(baseProjection, ::mapFromResultSet)
    }

    override fun insertBooking(bookableId: Int,
                               subject: String,
                               startDateTime: LocalDateTime,
                               endDateTime: LocalDateTime): Booking {
        val booking = bookingId.incrementAndGet()

        dataAccess.insert(insertStatement, { ps -> applyParameters(ps, booking, bookableId, subject, startDateTime, endDateTime) })

        return Booking(booking, bookableId, subject, startDateTime, endDateTime)
    }
}
