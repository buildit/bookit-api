package com.buildit.bookit.v1.location.bookable

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder

/**
 * Test /v1/location/<location>/bookable like a black box
 */
object BookableTest : Spek(
    {
        val uri: String = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
        val restTemplate = TestRestTemplate(RestTemplateBuilder().rootUri(uri).build())
        describe("/v1/location/<location>/bookable")
        {
            on("Get 1 bookable")
            {
                val response = restTemplate.getForEntity("/v1/location/nyc/bookable/The best bookable ever", String::class.java)

                it("should return 1 bookable")
                {
                    val expectedResponse = """
                        {
                            "name": "The best bookable ever",
                            "location": "NYC"
                        }
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
                }
            }
        }
    })
