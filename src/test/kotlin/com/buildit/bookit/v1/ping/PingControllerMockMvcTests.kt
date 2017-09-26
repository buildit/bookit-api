package com.buildit.bookit.v1.ping

import org.hamcrest.Matchers.equalToIgnoringCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@RunWith(SpringRunner::class)
@WebMvcTest(PingController::class)
class PingControllerMockMvcTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun ping() {
        // arrange
        // act
        val result = mockMvc.perform(get("/v1/ping"))

        // assert
        result.andExpect(status().isOk)
        result.andExpect(jsonPath<String>("$.status", equalToIgnoringCase("up")))
    }
}
