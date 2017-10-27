package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import com.fasterxml.jackson.databind.ObjectMapper
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
 * Booking Contoller spring mvc integration tests
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(BookableController::class)
class BookableControllerMockMvcTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper
) {
    @MockBean
    lateinit var mockBookableRepository: BookableRepository

    @MockBean
    lateinit var mockLocationRepository: LocationRepository

    @BeforeEach
    fun setupMocks() {
        whenever(mockLocationRepository.getLocations()).doReturn(listOf(Location(1, "NYC", "America/New_York")))
        whenever(mockBookableRepository.getAllBookables()).doReturn(listOf(Bookable(1, 1, "The best bookable ever", true)))
    }

    @AfterEach
    fun resetMocks() {
        reset(mockBookableRepository)
        reset(mockLocationRepository)
    }

    @Test
    fun getExistingBookableTest() {
        // arrange
        // act
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/1/bookable/1"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isOk)
        result.andExpect(MockMvcResultMatchers.jsonPath<String>("$.name", Matchers.equalToIgnoringCase("The best bookable ever")))
        result.andExpect(MockMvcResultMatchers.jsonPath<Boolean>("$.available", Matchers.equalTo(true)))
    }

    @Test
    fun getBadLocation() {
        // arrange
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/nyc/bookable/1"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun getBadBooking() {
        // arrange
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/1/bookable/foo"))

        // assert
        result.andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
