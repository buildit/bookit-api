package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger

val local: ZoneId = ZoneId.systemDefault()
val utc: ZoneId = ZoneId.of("UTC") // this should be replaced with that of the bookable

interface BookingRepository {
    fun getAllBookings(): Collection<Booking>
    fun insertBooking(bookableId: Int, subject: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Booking
}

@Repository
class BookingDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : BookingRepository {
    private val tableName = "BOOKING"

    // replace this with a s
    private var bookingIdGenerator = AtomicInteger(0)

    override fun getAllBookings(): Collection<Booking> = jdbcTemplate.query(
        "SELECT BOOKING_ID, BOOKABLE_ID, SUBJECT, START_DATE, END_DATE FROM $tableName") { rs, _ ->
        fun toLocalDateTime(ts: Timestamp) = ZonedDateTime.ofInstant(ts.toInstant(), utc).withZoneSameInstant(local).toLocalDateTime()

        Booking(
            rs.getInt("BOOKING_ID"),
            rs.getInt("BOOKABLE_ID"),
            rs.getString("SUBJECT"),
            toLocalDateTime(rs.getTimestamp("START_DATE")),
            toLocalDateTime(rs.getTimestamp("END_DATE"))
        )
    }

    override fun insertBooking(bookableId: Int,
                               subject: String,
                               startDateTime: LocalDateTime,
                               endDateTime: LocalDateTime): Booking {
        val bookingId = bookingIdGenerator.incrementAndGet()

        SimpleJdbcInsert(jdbcTemplate).withTableName(tableName).apply {
            execute(
                mapOf("BOOKING_ID" to bookingId,
                    "BOOKABLE_ID" to bookableId,
                    "SUBJECT" to subject,
                    "START_DATE" to startDateTime,
                    "END_DATE" to endDateTime
                )
            )
        }

        return Booking(bookingId, bookableId, subject, startDateTime, endDateTime)
    }
}
