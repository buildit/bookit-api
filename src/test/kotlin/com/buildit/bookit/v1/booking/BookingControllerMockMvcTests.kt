package com.buildit.bookit.v1.booking

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZonedDateTime
import java.util.*

/*
 data class BookingRequest(
 val bookableId: Int,
 val subject: String,
 val startDateTime: ZonedDateTime,
 val endDateTime: ZonedDateTime
 )
  */


@ExtendWith(SpringExtension::class)
@WebMvcTest(BookingController::class)
class BookingControllerMockMvcTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Test
    fun getExistingBookingTest() {
        // arrange
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/booking/1"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isOk)
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.subject", Matchers.equalToIgnoringCase("The Booking")))
    }


    @Test
    fun createBookingTest() {
        // arrange
        val startDateTime = ZonedDateTime.parse("2017-09-26T09:00:00.000-04:00")
        val endDateTime = ZonedDateTime.parse("2017-09-26T10:00:00.000-04:00")
        val request = BookingRequest(1, "New Meeting", startDateTime, endDateTime)

        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/booking")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))

        // assert
        result.andExpect(MockMvcResultMatchers.status().`is`(201))
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.subject", Matchers.equalToIgnoringCase("New Meeting")))
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.startDateTime", Matchers.equalToIgnoringCase("New Meeting")))
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.endDateTime", Matchers.equalToIgnoringCase("New Meeting")))
    }
}
