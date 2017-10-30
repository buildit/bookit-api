package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.BookableRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

/**
 * Booking controller spring mvc integration tests
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(BookingController::class)
class BookingControllerMockMvcTests @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper
) {
    private val NYC = "America/New_York"
    private val NYC_TZ = ZoneId.of(NYC)

    @MockBean
    lateinit var bookingRepository: BookingRepository

    @MockBean
    lateinit var bookableRepository: BookableRepository

    @MockBean
    lateinit var locationRepository: LocationRepository

    @BeforeEach
    fun setupMocks() {
        whenever(locationRepository.getLocations()).doReturn(listOf(Location(1, "NYC", NYC)))
    }

    @AfterEach
    fun resetMocks() {
        reset(bookingRepository)
        reset(bookableRepository)
        reset(locationRepository)
    }

    @Nested
    inner class GetBooking {
        private val startDateTime = now(NYC_TZ).plusHours(1)
        private val endDateTime = now(NYC_TZ).plusHours(2)

        @BeforeEach
        fun setupMock() {
            whenever(bookingRepository.getAllBookings())
                .doReturn(listOf(Booking(1, 1, "The Booking", startDateTime, endDateTime)))
        }

        @Test
        fun `existing booking is returned`() {
            mvc.perform(get("/v1/booking/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath<String>("$.subject", equalToIgnoringCase("The Booking")))
        }

        @Test
        fun `nonexistent booking is not found`() {
            mvc.perform(get("/v1/booking/999"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `booking id must be numeric`() {
            mvc.perform(get("/v1/booking/notanumber"))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class CreateBooking {
        private val startDateTime = now(NYC_TZ).plusHours(1)
        private val endDateTime = startDateTime.plusHours(1)
        private val subject = "New Meeting"

        @BeforeEach
        fun createMock() {
            whenever(bookingRepository.insertBooking(1, subject, startDateTime, endDateTime))
                .doReturn(Booking(1, 1, subject, startDateTime, endDateTime))
            whenever(bookableRepository.getAllBookables())
                .doReturn(listOf(Bookable(1, 1, "Foo", true)))
        }

        @Test
        fun `valid booking is created`() {
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isCreated)
                .andExpect(jsonPath<String>("$.subject", equalToIgnoringCase(subject)))
                .andExpect(jsonPath<String>("$.start", equalToIgnoringCase(startDateTime.format(ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath<String>("$.end", equalToIgnoringCase(endDateTime.format(ISO_LOCAL_DATE_TIME))))
        }

        @Test
        fun `booking must be in future`() {
            val startDateTime = now(NYC_TZ).minusHours(1)
            val endDateTime = startDateTime.plusHours(1)
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking start must precede end`() {
            val startDateTime = now(NYC_TZ).plusHours(1)
            val endDateTime = startDateTime.minusHours(1)
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking requires existing bookable`() {
            val request = BookingRequest(null, subject, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking must have non-null subject`() {
            val request = BookingRequest(1, null, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking must have non-blank subject`() {
            val request = BookingRequest(1, "  ", startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking must have non-empty subject`() {
            val request = BookingRequest(1, "", startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking must have start`() {
            val request = BookingRequest(1, subject, null, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking must have end`() {
            val request = BookingRequest(1, subject, startDateTime, null)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        private fun post(request: BookingRequest): MockHttpServletRequestBuilder? =
            post("/v1/booking")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
    }
}
