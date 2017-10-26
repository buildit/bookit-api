package com.buildit.bookit.v1.location.bookable

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

/**
 * Booking Contoller spring mvc integration tests
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(BookableController::class)
class BookableControllerMockMvcTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper
) {
    /**
     * Get a booking
     */
    @Test
    fun getExistingBookableTest() {
        // arrange
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/nyc/bookable/The best bookable ever"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isOk)
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.name", Matchers.equalToIgnoringCase("The best bookable ever")))
    }

    /**
     * Fail on non-existent
     */
    @Test
    fun getNonexistentBookableTest() {
        // arrange
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/nyc/bookable/foo"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}
