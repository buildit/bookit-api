package com.buildit.bookit.v1.ping

import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Tests PingController spring integration
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(PingController::class)
class PingControllerMockMvcTests @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `ping success`() {
        mockMvc.perform(get("/v1/ping"))
            .andExpect(status().isOk)
            .andExpect(jsonPath<String>("$.status", equalToIgnoringCase("up")))
    }
}
