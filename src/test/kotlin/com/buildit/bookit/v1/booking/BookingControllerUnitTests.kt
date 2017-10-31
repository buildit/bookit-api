package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Clock
import java.time.LocalDateTime

/**
 * Booking controller spring mvc integration tests
 */
@ExtendWith(SpringExtension::class)
class BookingControllerUnitTests {

    @BeforeEach
    fun setupMocks() {
    }

    @AfterEach
    fun resetMocks() {
    }

    @Nested
    inner class GetBookings {

        @BeforeEach
        fun setupMock() {
        }

        @Test
        fun `getAllBookings returns all existing bookings`() {
            val clock = Clock.systemUTC()

            val startDateTime = LocalDateTime.parse("2017-09-26T09:00:00")
            val endDateTime = LocalDateTime.parse("2017-09-26T10:00:00")

            val mockRepo = mock<BookingRepository> {
                on { getAllBookings() }.doReturn(
                    listOf(
                        Booking(1, 2, "Booking", startDateTime, endDateTime),
                        Booking(3, 4, "Another Booking", startDateTime, endDateTime)
                    )
                )
            }
            val bookings = BookingController(mockRepo, mock {}, mock {}, clock).getAllBookings().body
            expect(bookings.size).to.be.equal(2)
        }
    }
}
