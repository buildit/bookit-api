package com.buildit.bookit.v1.location

import com.buildit.bookit.v1.location.dto.Location
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
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

    @MockBean
    lateinit var mockLocationRepo: LocationRepository

    @BeforeEach
    fun setupMocks() {
        whenever(mockLocationRepo.getLocations()).doReturn(listOf(Location(1, "The best location ever", "America/New_York")))
    }

    @AfterEach
    fun resetMocks() {
        reset(mockLocationRepo)
    }

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
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.name", Matchers.equalToIgnoringCase("The best location ever")))
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.timeZone", Matchers.equalToIgnoringCase("America/New_York")))
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
        result.andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
