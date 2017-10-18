package com.buildit.bookit.v1.location

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
 * Tests the /location endpoint
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(LocationController::class)
class LocationControllerMockMvcTests @Autowired constructor(
    private val mockMvc: MockMvc
) {
    /**
     * Fail to get a location
     */
    @Test
    fun getValidLocationURITest() {
        // arrange
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/1"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isOk)
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.locationName", Matchers.equalToIgnoringCase("The best location ever")))
    }

    /**
     * Fail to get a location
     */
    @Test
    fun getMalformedURITest() {
        // arrange
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/notanumber"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    /**
     * Get location has a name
     */
    @Test
    fun getValidLocationTest() {
        // arrange
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/1"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isOk)
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.locationName",
                                                                Matchers.equalToIgnoringCase("The best location ever")))
    }
}
