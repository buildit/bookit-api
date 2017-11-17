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
                                "id": "b1177996-75e2-41da-a3e9-fcdd75d1ab31",
                                "name": "NYC",
                                "timeZone": "America/New_York"
                            },
                            {
                                "id": "43ec3f7d-348d-427f-8c13-102ca0362a62",
                                "name": "LON",
                                "timeZone": "Europe/London"
                            }
                        ]
                    """.trimIndent()
        JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
    }
}
