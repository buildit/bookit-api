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
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test /v1/location/<location>/bookable like a black box
 */
class `Bookable E2E Tests` {
    val now = LocalDateTime.now(ZoneId.of("America/New_York"))
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
                            "name": "Red",
                            "available": true
                        }
                    """.trimIndent()
        JSONAssert.assertEquals(expectedResponse, response.body, JSONCompareMode.STRICT)
    }

    @Test
    fun `get all bookables`() {
        // act
        val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable", String::class.java)

        // assert
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

    @Nested
    inner class `Search for bookables` {
        @Test
        fun `should require end if start specified`() {
            val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$inOneHour", String::class.java)

            expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `should require start if end specified`() {
            val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?end=$inTwoHours", String::class.java)

            expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `should require start before end`() {
            val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$inTwoHours&end=$inOneHour", String::class.java)

            expect(response.statusCode).to.equal(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `should find available bookable`() {
            // act
            val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$inOneHour&end=$inTwoHours", String::class.java)

            // assert
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

        @Nested
        inner class `room unavailable` {
            var location: URI? = null

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
                location = Global.REST_TEMPLATE.postForLocation("/v1/booking", goodRequest.toEntity())
            }

            @Test
            fun `should find unavailable bookable`() {
                // act
                val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$inOneHour&end=$inTwoHours", String::class.java)

                // assert
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

            @Test
            fun `should find available bookable`() {
                // act
                val response = Global.REST_TEMPLATE.getForEntity("/v1/location/1/bookable?start=$inTwoHours&end=${inTwoHours.plusHours(1)}", String::class.java)

                // assert
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

            @AfterEach
            fun `delete booking`() {
                location?.let { Global.REST_TEMPLATE.delete(it) }
            }
        }
    }
}
