package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.auth.WithMockCustomUser
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
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@WebMvcTest(BookableController::class, includeFilters = [ComponentScan.Filter(EnableWebSecurity::class)])
@WithMockCustomUser
class BookableControllerMockMvcTests @Autowired constructor(
    private val context: WebApplicationContext
) {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    lateinit var bookableRepo: BookableRepository

    @MockBean
    lateinit var locationRepo: LocationRepository

    @MockBean
    lateinit var bookingRepo: BookingRepository

    @BeforeEach
    fun configureSecurityFilters() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @BeforeEach
    fun setupMocks() {
        whenever(locationRepo.getLocations())
            .doReturn(listOf(Location("location-guid", "NYC", "America/New_York")))
        whenever(bookableRepo.getAllBookables())
            .doReturn(listOf(Bookable("bookable-guid", "guid", "The best bookable ever", Disposition())))
    }

    @Test
    fun getExistingBookableTest() {
        mvc.perform(MockMvcRequestBuilders.get("/v1/location/location-guid/bookable/bookable-guid"))
            .andExpect(status().isOk)
            .andExpect(jsonPath<String>("$.name", equalToIgnoringCase("The best bookable ever")))
            .andExpect(jsonPath<Boolean>("$.disposition.closed", equalTo(false)))
    }
}
