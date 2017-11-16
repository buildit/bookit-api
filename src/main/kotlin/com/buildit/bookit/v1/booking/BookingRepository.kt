package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

interface BookingRepository {
    fun getAllBookings(): Collection<Booking>
    fun insertBooking(bookableId: Int, subject: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Booking
    fun delete(id: Int)
}

@Repository
class BookingDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : BookingRepository {
    private val tableName = "BOOKING"

    // replace this with a s
    private var bookingIdGenerator = AtomicInteger(10000)

    override fun getAllBookings(): Collection<Booking> = jdbcTemplate.query(
        "SELECT BOOKING_ID, BOOKABLE_ID, SUBJECT, START_DATE, END_DATE FROM $tableName") { rs, _ ->

        Booking(
            rs.getInt("BOOKING_ID"),
            rs.getInt("BOOKABLE_ID"),
            rs.getString("SUBJECT"),
            rs.getObject("START_DATE", LocalDateTime::class.java),
            rs.getObject("END_DATE", LocalDateTime::class.java)
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

    override fun delete(id: Int) {
        jdbcTemplate.update("DELETE FROM $tableName WHERE BOOKING_ID = ?", id)
    }
}
