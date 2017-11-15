package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
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
import java.time.temporal.ChronoUnit

/**
 * Booking controller spring mvc integration tests
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(BookingController::class)
class BookingControllerMockMvcTests @Autowired constructor(
    private val mvc: MockMvc,
    private val mapper: ObjectMapper
) {
    private val NYC = ZoneId.of("America/New_York")
    private val location = Location("NYC", NYC, 1)
    private val bookable = Bookable(location, "Bookable")
    private val startDateTime = now(NYC).plusHours(1).truncatedTo(ChronoUnit.MINUTES)
    private val endDateTime = now(NYC).plusHours(2).truncatedTo(ChronoUnit.MINUTES)

    @MockBean
    lateinit var bookingRepo: BookingRepository

    @MockBean
    lateinit var bookableRepo: BookableRepository

    @AfterEach
    fun resetMocks() {
        reset(bookingRepo)
        reset(bookableRepo)
    }

    @Nested
    inner class GetBooking {
        @BeforeEach
        fun setupMock() {
            whenever(bookingRepo.findAll())
                .doReturn(listOf(Booking(bookable, "The Booking", startDateTime, endDateTime, 1)))
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
        private val subject = "New Meeting"
        private val booking = Booking(bookable, subject, startDateTime, endDateTime, 1)

        @BeforeEach
        fun createMock() {
            whenever(bookingRepo.save(booking))
                .doReturn(booking)
            whenever(bookableRepo.findOne(bookable.id))
                .doReturn(listOf(bookable))
        }

        @Test
        fun `valid booking is created`() {
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isCreated)
                .andExpect(jsonPath<String>("$.subject", equalToIgnoringCase(subject)))
                .andExpect(jsonPath<String>("$.start", equalToIgnoringCase(startDateTime.toString())))
                .andExpect(jsonPath<String>("$.end", equalToIgnoringCase(endDateTime.toString())))
        }

        @Test
        fun `booking must be in future`() {
            val startDateTime = now(NYC).minusHours(1)
            val endDateTime = startDateTime.plusHours(1)
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            mvc.perform(post(request))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `booking start must precede end`() {
            val startDateTime = now(NYC).plusHours(1)
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
