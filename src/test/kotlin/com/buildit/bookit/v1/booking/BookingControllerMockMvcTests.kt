package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.booking.dto.BookingRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime
import org.springframework.boot.test.mock.mockito.MockBean



/*
 data class BookingRequest(
 val bookableId: Int,
 val subject: String,
 val startDateTime: ZonedDateTime,
 val endDateTime: ZonedDateTime
 )
  */

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
    lateinit var mockRepository: BookingRepository

    @Before
    fun setupMock() {
        mockRepository = mock {}
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
        result.andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    /**
     * Create a booking
     */
    @Test
    fun createBookingTest() {
        // arrange
        val startDateTime = LocalDateTime.now().plusHours(1)
        val endDateTime = startDateTime.plusHours(1)
        val request = BookingRequest(1, "New Meeting", startDateTime, endDateTime)

        Mockito.`when`(mockRepository.insertBooking(1, "New Meeting", startDateTime, endDateTime)).doReturn(Booking(1, 1, "New Meeting", startDateTime, endDateTime))
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))

        // assert
        result.andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.CREATED.value()))
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.subject", Matchers.equalToIgnoringCase("New Meeting")))
    }

    @Test
    fun createBookingInThePastTest() {
        // arrange
        val startDateTime = LocalDateTime.now().minusHours(1)
        val endDateTime = startDateTime.plusHours(1)
        val request = BookingRequest(1, "New Meeting", startDateTime, endDateTime)

        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))

        // assert
        result.andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
    }

    @Test
    fun createBookingWithMisOrderedDatesTest() {
        // arrange
        val startDateTime = LocalDateTime.now().plusHours(1)
        val endDateTime = startDateTime.minusHours(1)
        val request = BookingRequest(1, "New Meeting", startDateTime, endDateTime)

        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))

        // assert
        result.andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
    }

}
