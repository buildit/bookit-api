package com.buildit.bookit.v1.booking

import com.buildit.bookit.Global
import com.buildit.bookit.toEntity
import com.winterbe.expekt.expect
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test /v1/booking like a black box
 */
class `Booking E2E Tests` {
    val now = LocalDateTime.now(ZoneId.of("America/New_York"))
    val tomorrow = now.plusDays(1)
    val yesterday = now.minusDays(1)
    var response: ResponseEntity<String>? = null

    @Nested
    inner class `POST a booking` {
        @Nested
        inner class `valid` {
            @Test
            fun `should return a created meeting`() {
                // arrange
                val goodRequest =
                    """
                {
                    "bookableId": 1,
                    "subject": "My new meeting",
                    "start": "$tomorrow",
                    "end": "${tomorrow.plusHours(1)}"
                }
                """

                // act
                response = Global.REST_TEMPLATE.postForEntity("/v1/booking", goodRequest.toEntity(), String::class.java)

                // assert
                val jsonResponse = JSONObject(response?.body)
                expect(jsonResponse.getInt("id")).to.be.above(0)
                expect(jsonResponse.get("bookableId")).to.be.equal(1)
                expect(jsonResponse.get("subject")).to.be.equal("My new meeting")
            }

            @AfterEach
            fun cleanup() {
                response?.headers?.location?.let { Global.REST_TEMPLATE.delete(it) }
            }
        }

        @Test
        fun `date in past should fail with 400`() {
            // arrange
            val badRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "start": "$yesterday",
                    "end": "${yesterday.plusHours(1)}"
                }
                """

            // act
            response = Global.REST_TEMPLATE.postForEntity("/v1/booking", badRequest.toEntity(), String::class.java)

            // assert
            val jsonResponse = JSONObject(response?.body)

            expect(response?.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
            expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.StartInPastException")
            expect(jsonResponse.get("message")).to.be.equal("Start must be in the future")
        }

        @Test
        fun `end date before start date fails with 400`() {
            // arrange
            val badRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "start": "$tomorrow",
                    "end": "${tomorrow.minusHours(1)}"
                }
                """

            // act
            response = Global.REST_TEMPLATE.postForEntity("/v1/booking", badRequest.toEntity(), String::class.java)

            // assert
            val jsonResponse = JSONObject(response?.body)

            expect(response?.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
            expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.EndBeforeStartException")
            expect(jsonResponse.get("message")).to.be.equal("End must be after Start")
        }

        @Test
        fun `bad formatted date fails with 400`() {
            // arrange
            val badRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "start": "foo",
                    "end": "bar"
                }
                """

            // act
            response = Global.REST_TEMPLATE.postForEntity("/v1/booking", badRequest.toEntity(), String::class.java)

            // assert
            val jsonResponse = JSONObject(response?.body)

            expect(response?.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
            expect(jsonResponse.getString("exception")).to.be.equal("org.springframework.http.converter.HttpMessageNotReadableException")
            expect(jsonResponse.getString("message")).to.contain("Cannot deserialize value of type `java.time.LocalDateTime` from String \"foo\"")
        }
    }
}

