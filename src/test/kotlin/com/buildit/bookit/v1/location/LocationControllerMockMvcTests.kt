package com.buildit.bookit.v1.location

import com.buildit.bookit.auth.WithMockCustomUser
import com.buildit.bookit.v1.location.dto.Location
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.ZoneId

/**
 * Tests the /location endpoint
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(LocationController::class, includeFilters = [ComponentScan.Filter(Configuration::class)])
@WithMockCustomUser
class LocationControllerMockMvcTests @Autowired constructor(
    private val context: WebApplicationContext
) {
    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    lateinit var locationRepo: LocationRepository

    @BeforeEach
    fun configureSecurityFilters() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @BeforeEach
    fun setupMocks() {
        val location = Location("The best location ever", ZoneId.of("America/New_York"), "guid")
        whenever(locationRepo.findOne(location.id))
            .doReturn(location)
    }

    @Test
    fun `get known location`() {
        mvc.perform(MockMvcRequestBuilders.get("/v1/location/guid"))
            .andExpect(status().isOk)
            .andExpect(jsonPath<String>("$.name", equalToIgnoringCase("The best location ever")))
            .andExpect(jsonPath<String>("$.timeZone", equalToIgnoringCase("America/New_York")))
    }
}
