package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

interface BookingRepository {
    fun getAllBookings(): Collection<Booking>
    /**
     * @param bookableId The id for the pre-exiting <code>Bookable</code> that is being booked.
     * @param subject The purpose of the booking.
     * @param startDateTime The start date of the booking.
     * @param endDateTime The end date of the booking.
     * @param creatingUser A pre-existing, fully-formed <code>User</code> object representing the person creating the booking.
     */
    fun insertBooking(bookableId: String, subject: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime, creatingUser: User): Booking

    fun delete(id: String): Boolean
}

@Repository
class BookingDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : BookingRepository {

    private val tableName = "BOOKING"

    override fun getAllBookings(): Collection<Booking> {
        val sql =
            """
            |SELECT b.BOOKING_ID,
            |       b.BOOKABLE_ID,
            |       b.SUBJECT,
            |       b.START_DATE,
            |       b.END_DATE,
            |       u.USER_ID,
            |       u.GIVEN_NAME,
            |       u.FAMILY_NAME
            |FROM $tableName b
            |LEFT JOIN USER u on b.USER_ID = u.USER_ID""".trimMargin()

        return jdbcTemplate.query(sql) { rs, _ ->
            Booking(
                rs.getString("BOOKING_ID"),
                rs.getString("BOOKABLE_ID"),
                rs.getString("SUBJECT"),
                rs.getObject("START_DATE", LocalDateTime::class.java),
                rs.getObject("END_DATE", LocalDateTime::class.java),
                makeUser(rs)
            )
        }
    }

    override fun insertBooking(bookableId: String, subject: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime, creatingUser: User): Booking {
        val bookingId = UUID.randomUUID().toString()

        SimpleJdbcInsert(jdbcTemplate).withTableName(tableName).apply {
            execute(
                mapOf("BOOKING_ID" to bookingId,
                    "BOOKABLE_ID" to bookableId,
                    "SUBJECT" to subject,
                    "START_DATE" to startDateTime,
                    "END_DATE" to endDateTime,
                    "USER_ID" to creatingUser.id
                )
            )
        }

        return Booking(bookingId, bookableId, subject, startDateTime, endDateTime, creatingUser)
    }

    override fun delete(id: String): Boolean = jdbcTemplate.update("DELETE FROM $tableName WHERE BOOKING_ID = ?", id) == 1

    private fun makeUser(rs: ResultSet): User =
        User(
            rs.getString("USER_ID"),
            "${rs.getString("GIVEN_NAME")} ${rs.getString("FAMILY_NAME")}"
        )
}
