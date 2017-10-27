package com.buildit.bookit.v1.location.bookable

import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test /v1/location/<location>/bookable like a black box
 */
object BookableTest : Spek(
    {
        val clock = Clock.system(ZoneId.of("America/New_York"))
        val uri: String = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
        val restTemplate = TestRestTemplate(RestTemplateBuilder().rootUri(uri).build())
        describe("/v1/location/<location>/bookable/<bookable>")
        {
            on("Get 1 bookable")
            {
                val response = restTemplate.getForEntity("/v1/location/1/bookable/1", String::class.java)

                it("should return 1 bookable")
                {
                    val expectedResponse = """
                        {
                            "id": 1,
                            "locationId": 1,
                            "name": "Red",
                            "available": true
                        }
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
                }
            }
        }

        describe("/v1/location/<location>/bookable")
        {
            on("Get all bookables")
            {
                val response = restTemplate.getForEntity("/v1/location/1/bookable", String::class.java)

                it("should return all bookables")
                {
                    val expectedResponse = """
                        [
                            {
                                "id": 1,
                                "locationId": 1,
                                "name": "Red",
                                "available": true
                            }
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
                }
            }
            on("Search for available bookables")
            {
                val now = LocalDateTime.now(clock)
                val inOneHour = now.plusHours(1).toString()
                val inTwoHours = now.plusHours(2).toString()

                it("should require endDateTime if startDateTime specified")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?startDateTime=$inOneHour", String::class.java)

                    expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
                }

                it("should require startDateTime if endDateTime specified")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?endDateTime=$inTwoHours", String::class.java)

                    expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
                }

                it("should find available bookable")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?startDateTime=$inOneHour&endDateTime=$inTwoHours", String::class.java)

                    val expectedResponse = """
                        [
                            {
                                "id": 1,
                                "locationId": 1,
                                "name": "Red",
                                "available": true
                            }
                        ]
                    """.trimIndent()
                    JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
                }
            }
        }
    })
