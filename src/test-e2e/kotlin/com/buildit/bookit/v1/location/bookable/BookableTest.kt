package com.buildit.bookit.v1.location.bookable

import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test /v1/location/<location>/bookable like a black box
 */
object BookableTest : Spek(
    {
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
            describe("Search for available bookables")
            {
                val now = LocalDateTime.now(ZoneId.of("America/New_York"))
                val inOneHour = now.plusHours(1)
                val inTwoHours = now.plusHours(2)

                it("should require end if start specified")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?start=$inOneHour", String::class.java)

                    expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
                }

                it("should require start if end specified")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?end=$inTwoHours", String::class.java)

                    expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
                }

                it("should require start before end")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?start=$inTwoHours&end=$inOneHour", String::class.java)

                    expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
                }

                it("should find available bookable")
                {
                    val response = restTemplate.getForEntity("/v1/location/1/bookable?start=$inOneHour&end=$inTwoHours", String::class.java)

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

                context("room unavailable") {
                    var location: URI? = null

                    beforeGroup {
                        val goodRequest =
                            """
                            {
                                "bookableId": 1,
                                "subject": "My new meeting",
                                "start": "$inOneHour",
                                "end": "$inTwoHours"
                            }
                            """.trimIndent()

                        val headers = HttpHeaders()
                        headers.contentType = MediaType.APPLICATION_JSON

                        val entity = HttpEntity<String>(goodRequest, headers)
                        val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)
                        location = response.headers.location
                    }

                    it("should find unavailable bookable")
                    {
                        val response = restTemplate.getForEntity("/v1/location/1/bookable?start=$inOneHour&end=$inTwoHours", String::class.java)

                        val expectedResponse = """
                        [
                            {
                                "id": 1,
                                "locationId": 1,
                                "name": "Red",
                                "available": false
                            }
                        ]
                    """.trimIndent()
                        JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
                    }

                    it("should find available bookable")
                    {
                        val response = restTemplate.getForEntity("/v1/location/1/bookable?start=$inTwoHours&end=${inTwoHours.plusHours(1)}", String::class.java)

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

                    afterGroup {
                        location?.let { restTemplate.delete(it) }
                    }
                }
            }
        }
    })
