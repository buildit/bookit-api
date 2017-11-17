package com.buildit.bookit.v1.booking

import com.buildit.bookit.Global
import com.buildit.bookit.toEntity
import com.winterbe.expekt.expect
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Test /v1/booking like a black box
 */
class `Booking E2E Tests` {
    private val now: LocalDateTime = LocalDateTime.now(ZoneId.of("America/New_York"))
    private val yesterday: LocalDateTime = now.minusDays(1)
    private val tomorrow: LocalDateTime = now.plusDays(1)
    private val tomorrowPlusAnHour = tomorrow.plusHours(1)
    private val dayAfterTomorrow: LocalDateTime = tomorrow.plusDays(2)

    private var response: ResponseEntity<String>? = null
    private val bookingForTomorrow =
        """
                {
                    "bookableId": "aab6d676-d3cb-4b9b-b285-6e63058aeda8",
                    "subject": "My new meeting",
                    "start": "$tomorrow",
                    "end": "$tomorrowPlusAnHour"
                }
                """
    @Nested
    inner class `Get bookings` {
        @Nested
        inner class `valid` {
            private val expectedBooking = """
                        [
                          {
                            "bookableId": "aab6d676-d3cb-4b9b-b285-6e63058aeda8",
                            "subject": "My new meeting"
                          }
                        ]
                    """.trimIndent()

            private val noBooking = """
                        [ ]
                    """.trimIndent()

            @BeforeEach
            fun `put booking in place`() {
                response = post(bookingForTomorrow, "/v1/booking")
                expect(response?.statusCode?.is2xxSuccessful).to.be.`true`
            }

            @Test
            fun `get with no params returns bookings`() {
                val response = get("/v1/booking")
                JSONAssert.assertEquals(expectedBooking, response.body, JSONCompareMode.LENIENT)
            }

            @Test
            fun `overlapping start date returns bookings`() {
                val start = DateTimeFormatter.ISO_LOCAL_DATE.format(now)
                val response = get("/v1/booking?start=$start")
                JSONAssert.assertEquals(expectedBooking, response.body, JSONCompareMode.LENIENT)
            }

            @Test
            fun `overlapping end date returns bookings`() {
                val end = DateTimeFormatter.ISO_LOCAL_DATE.format(dayAfterTomorrow)
                val response = get("/v1/booking?end=$end")
                JSONAssert.assertEquals(expectedBooking, response.body, JSONCompareMode.LENIENT)
            }

            @Test
            fun `overlapping start and end date returns bookings`() {
                val start = DateTimeFormatter.ISO_LOCAL_DATE.format(now)
                val end = DateTimeFormatter.ISO_LOCAL_DATE.format(dayAfterTomorrow)
                val response = get("/v1/booking?start=$start&end=$end")
                JSONAssert.assertEquals(expectedBooking, response.body, JSONCompareMode.LENIENT)
            }

            @Test
            fun `non-overlapping start date returns no bookings `() {
                val start = DateTimeFormatter.ISO_LOCAL_DATE.format(dayAfterTomorrow)
                val response = get("/v1/booking?start=$start")
                JSONAssert.assertEquals(noBooking, response.body, JSONCompareMode.LENIENT)
            }

            @Test
            fun `non-overlapping end date returns no bookings`() {
                val end = DateTimeFormatter.ISO_LOCAL_DATE.format(tomorrow)
                val response = get("/v1/booking?end=$end")
                JSONAssert.assertEquals(noBooking, response.body, JSONCompareMode.LENIENT)
            }

            @AfterEach
            fun `clean up`() {
                response?.headers?.location?.let { Global.REST_TEMPLATE.delete(it) }
            }
        }

        private fun get(url: String) = Global.REST_TEMPLATE.getForEntity(url, String::class.java)
    }

    @Nested
    inner class `POST a booking` {
        @Nested
        inner class `valid` {
            @Test
            fun `should return a created meeting`() {

                response = post(bookingForTomorrow, "/v1/booking")

                val jsonResponse = JSONObject(response?.body)
                expect(jsonResponse.getString("id")).not.to.be.`null`
                expect(jsonResponse.get("bookableId")).not.to.be.`null`
                expect(jsonResponse.get("subject")).to.be.equal("My new meeting")
            }

            @AfterEach
            fun cleanup() {
                response?.headers?.location?.let { Global.REST_TEMPLATE.delete(it) }
            }
        }

        @Test
        fun `date in past should fail with 400`() {
            val requestWithPastDate =
                """
                {
                    "bookableId": "aab6d676-d3cb-4b9b-b285-6e63058aeda8",
                    "subject": "My meeting in the past",
                    "start": "$yesterday",
                    "end": "${yesterday.plusHours(1)}"
                }
                """

            response = post(requestWithPastDate, "/v1/booking")

            val jsonResponse = JSONObject(response?.body)

            expect(response?.statusCode).to.be.equal(BAD_REQUEST)
            expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.StartInPastException")
            expect(jsonResponse.get("message")).to.be.equal("Start must be in the future")
        }

        @Test
        fun `end date before start date fails with 400`() {
            val requestWithEndBeforeStart =
                """
                {
                    "bookableId": "aab6d676-d3cb-4b9b-b285-6e63058aeda8",
                    "subject": "My meeting with bad date order",
                    "start": "$tomorrow",
                    "end": "${tomorrow.minusHours(1)}"
                }
                """

            response = post(requestWithEndBeforeStart, "/v1/booking")

            val jsonResponse = JSONObject(response?.body)

            expect(response?.statusCode).to.be.equal(BAD_REQUEST)
            expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.EndBeforeStartException")
            expect(jsonResponse.get("message")).to.be.equal("End must be after Start")
        }

        @Test
        fun `bad formatted date fails with 400`() {
            val requestWithNonDates =
                """
                {
                    "bookableId": "aab6d676-d3cb-4b9b-b285-6e63058aeda8",
                    "subject": "My meeting with bad dates",
                    "start": "foo",
                    "end": "bar"
                }
                """

            response = post(requestWithNonDates, "/v1/booking")

            val jsonResponse = JSONObject(response?.body)

            expect(response?.statusCode).to.be.equal(BAD_REQUEST)
            expect(jsonResponse.getString("exception")).to.be.equal("org.springframework.http.converter.HttpMessageNotReadableException")
            expect(jsonResponse.getString("message")).to.contain("Cannot deserialize value of type `java.time.LocalDateTime` from String \"foo\"")
        }
    }

    private fun post(request: String, url: String) =
        Global.REST_TEMPLATE.postForEntity(url, request.toEntity(), String::class.java)
}

