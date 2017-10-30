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
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Booking Contoller spring mvc integration tests
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(BookingController::class)
class BookingControllerMockMvcTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper
) {
    @MockBean
    lateinit var mockBookingRepository: BookingRepository

    @MockBean
    lateinit var mockBookableRepository: BookableRepository

    @MockBean
    lateinit var mockLocationRepository: LocationRepository

    @BeforeEach
    fun setupMocks() {
        whenever(mockLocationRepository.getLocations()).doReturn(listOf(Location(1, "NYC", "America/New_York")))
    }

    @AfterEach
    fun resetMocks() {
        reset(mockBookingRepository)
        reset(mockBookableRepository)
        reset(mockLocationRepository)
    }

    @Nested
    inner class GetBooking {
        val startDateTime = LocalDateTime.now(ZoneId.of("America/New_York")).plusHours(1)
        val endDateTime = LocalDateTime.now(ZoneId.of("America/New_York")).plusHours(2)

        @BeforeEach
        fun setupMock() {
            whenever(mockBookingRepository.getAllBookings()).doReturn(listOf(Booking(1, 1, "The Booking", startDateTime, endDateTime)))
        }

        /**
         * Get a booking
         */
        @Test
        fun getExistingBookingTest() {
            // arrange
            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/booking/1"))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isOk)
            result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.subject", Matchers.equalToIgnoringCase("The Booking")))
        }

        /**
         * Fail to get a booking
         */
        @Test
        fun getNonexistentBookingTest() {
            // arrange
            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/booking/999"))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        /**
         * Fail to get a booking
         */
        @Test
        fun getMalformedURITest() {
            // arrange
            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/booking/notanumber"))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }

    @Nested
    inner class CreateBooking {
        private val startDateTime = LocalDateTime.now(ZoneId.of("America/New_York")).plusHours(1)
        private val endDateTime = startDateTime.plusHours(1)
        private val subject = "New Meeting"

        @BeforeEach
        fun createMock() {
            whenever(mockBookingRepository.insertBooking(1, subject, startDateTime, endDateTime)).doReturn(Booking(1, 1, subject, startDateTime, endDateTime))
            whenever(mockBookableRepository.getAllBookables()).doReturn(listOf(Bookable(1, 1, "Foo", true)))
        }

        /**
         * Create a booking
         */
        @Test
        fun createBookingTest() {
            // arrange
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isCreated)
            result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.subject", Matchers.equalToIgnoringCase(subject)))
            result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.start", Matchers.equalToIgnoringCase(startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
            result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.end", Matchers.equalToIgnoringCase(endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
        }

        @Test
        fun createBookingInThePastTest() {
            // arrange
            val startDateTime = LocalDateTime.now(ZoneId.of("America/New_York")).minusHours(1)
            val endDateTime = startDateTime.plusHours(1)
            val request = BookingRequest(1, subject, startDateTime, endDateTime)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun createBookingNoBookable() {
            // arrange
            val request = BookingRequest(null, subject, startDateTime, endDateTime)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun createBookingNoSubject() {
            // arrange
            val request = BookingRequest(1, null, startDateTime, endDateTime)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun createBookingBlankSubject() {
            // arrange
            val request = BookingRequest(1, "  ", startDateTime, endDateTime)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun createBookingNoStart() {
            // arrange
            val request = BookingRequest(1, subject, null, endDateTime)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }

        @Test
        fun createBookingNoEnd() {
            // arrange
            val request = BookingRequest(1, subject, startDateTime, null)

            // act
            val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))

            // assert
            result.andExpect(MockMvcResultMatchers.status().isBadRequest)
        }
    }
}

