package com.buildit.bookit.v1.location

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder

/**
 * Test /v1/location like a black box
 */
object LocationTests : Spek(
    {
        val uri: String = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
        val restTemplate = TestRestTemplate(RestTemplateBuilder().rootUri(uri).build())
        describe("/v1/location")
        {
            on("Get all locations")
            {
                val response = restTemplate.getForEntity("/v1/location", String::class.java)

                it("should return all locations")
                {
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
        }
    })
