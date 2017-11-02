package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.booking.BookingRepository
import com.buildit.bookit.v1.location.LocationRepository
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.Disposition
import com.buildit.bookit.v1.location.dto.Location
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(BookableController::class)
class BookableControllerMockMvcTests @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockBean
    lateinit var bookableRepo: BookableRepository

    @MockBean
    lateinit var locationRepo: LocationRepository

    @MockBean
    lateinit var bookingRepo: BookingRepository

    @BeforeEach
    fun setupMocks() {
        whenever(locationRepo.findOne(1))
            .doReturn(Location(1, "NYC", "America/New_York"))
        whenever(bookableRepo.getAllBookables())
            .doReturn(listOf(Bookable(1, 1, "The best bookable ever", Disposition())))
    }

    @Test
    fun getExistingBookableTest() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/1/bookable/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath<String>("$.name", equalToIgnoringCase("The best bookable ever")))
            .andExpect(jsonPath<Boolean>("$.disposition.closed", equalTo(false)))
    }

    @Test
    fun getBadLocation() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/nyc/bookable/1"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getBadBooking() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/location/1/bookable/foo"))
            .andExpect(status().isBadRequest)
    }
}
