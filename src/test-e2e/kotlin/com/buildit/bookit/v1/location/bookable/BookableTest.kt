package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.Global
import com.buildit.bookit.toEntity
import com.winterbe.expekt.expect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test /v1/location/<location>/bookable like a black box
 */
class `Bookable E2E Tests` {
    val now = LocalDateTime.now(ZoneId.of("America/New_York"))
    val today = LocalDate.now(ZoneId.of("America/New_York"))
    val inOneHour = now.plusHours(1)
    val inTwoHours = now.plusHours(2)

    @Test
    fun `get 1 bookable`() {
        // act
        val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable/1", String::class.java)

        // assert
        val expectedResponse = """
                        {
                            "id": 1,
                            "locationId": 1,
                            "name": "Red Room",
                            "disposition": {
                                "closed": false,
                                "reason": ""
                            },
                            bookings: []
                        }
                    """.trimIndent()
        JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
    }

    private val allBookables = """
                            [
                                {
                                    "id": 1,
                                    "locationId": 1,
                                    "name": "Red Room",
                                    "disposition": {
                                        "closed": false,
                                        "reason": ""
                                    },
                                    bookings: []
                                },
                                {
                                    "id": 2
                                },
                                {
                                    "id": 3
                                },
                                {
                                    "id": 4
                                },
                                {
                                    "id": 5
                                },
                                {
                                    "id": 6,
                                    "locationId": 1,
                                    "name": "Yellow Room",
                                    "disposition": {
                                        "closed": true,
                                        "reason": "Closed for remodeling"
                                    },
                                    bookings: []
                                }
                            ]
                        """.trimIndent()

    @Test
    fun `get all bookables`() {
        // act
        val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable", String::class.java)

        // assert
        JSONAssert.assertEquals(allBookables, response.body, JSONCompareMode.LENIENT)
    }

    @Nested
    inner class `Search for bookables` {
        @Test
        fun `should require start before end`() {
            val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$today&end=${today.minusDays(1)}", String::class.java)

            expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `should find available bookable`() {
            // act
            val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$today&end=$today&expand=bookings", String::class.java)

            // assert
            JSONAssert.assertEquals(allBookables, response.body, JSONCompareMode.LENIENT)
        }

        @Nested
        inner class `room unavailable` {
            var createResponse: ResponseEntity<String>? = null

            @BeforeEach
            fun `create booking`() {
                val goodRequest =
                    """
                            {
                                "bookableId": 1,
                                "subject": "My new meeting",
                                "start": "$inOneHour",
                                "end": "$inTwoHours"
                            }
                            """
                createResponse = Global.REST_TEMPLATE.postForEntity("/v1/booking", goodRequest.toEntity(), String::class.java)
            }

            @Test
            fun `should find bookable with bookings`() {
                // act
                val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?expand=bookings", String::class.java)

                // assert
                val expectedResponse = """
                        [
                            {
                                "id": 1,
                                "locationId": 1,
                                "name": "Red Room",
                                "disposition": {
                                    "closed": false,
                                    "reason": ""
                                },
                                bookings: [
                                    ${createResponse?.body}
                                ]
                            },
                            {
                                "id": 2
                            },
                            {
                                "id": 3
                            },
                            {
                                "id": 4
                            },
                            {
                                "id": 5
                            },
                            {
                                "id": 6
                            }
                        ]
                    """.trimIndent()
                JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.LENIENT)
            }

            @Test
            fun `should find bookable with no bookings on different day`() {
                // act
                val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=${today.plusDays(1)}&expand=bookings", String::class.java)

                // assert
                JSONAssert.assertEquals(allBookables, response.body, JSONCompareMode.LENIENT)
            }

            @AfterEach
            fun `delete booking`() {
                createResponse?.headers?.location?.let { Global.REST_TEMPLATE.delete(it) }
            }
        }
    }
}
