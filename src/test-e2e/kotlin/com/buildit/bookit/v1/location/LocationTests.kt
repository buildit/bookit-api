package com.buildit.bookit.v1.location

import com.buildit.bookit.Global
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

/**
 * Test /v1/location like a black box
 */
class `Location E2E Tests` {
    @Test
    fun `get all locations`() {
        // act
        val response = Global.REST_TEMPLATE.getForEntity("/v1/location", String::class.java)

        // arrange
        val expectedResponse = """
                        [
                            {
                                "id": 1,
                                "name": "NYC",
                                "timeZone": "America/New_York"
                            },
                            {
                                "id": 2,
                                "name": "LON",
                                "timeZone": "Europe/London"
                            }
                        ]
                    """.trimIndent()
        JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
    }
}
