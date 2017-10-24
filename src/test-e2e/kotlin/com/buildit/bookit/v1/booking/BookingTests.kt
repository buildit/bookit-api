package com.buildit.bookit.v1.booking

import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.json.JSONObject
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate

/**
 * Test /v1/ping like a black box
 */
object BookingTests : Spek(
{
    val uri : String = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
    val restTemplate = TestRestTemplate(RestTemplateBuilder().rootUri(uri).build())
    describe("/v1/booking")
    {
        on("POSTing a valid meeting")
        {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowISO = "${tomorrow.year}-${tomorrow.monthValue}-${tomorrow.dayOfMonth}"
            val goodRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My new meeting",
                    "startDateTime": "${tomorrowISO}T09:00:00",
                    "endDateTime": "${tomorrowISO}T10:00:00.000"
                }
                """

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity<String>(goodRequest, headers)
            val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

            it("should return a created meeting")
            {
                val jsonResponse = JSONObject(response.body)
                expect(jsonResponse.getInt("bookingId")).to.be.above(0)
                expect(jsonResponse.get("bookableId")).to.be.equal(1)
                expect(jsonResponse.get("subject")).to.be.equal("My new meeting")
            }
        }

        on("POSTing with a date in the past")
        {
            val yesterday = LocalDate.now().minusDays(1)
            val yesterdayISO = "${yesterday.year}-${yesterday.monthValue}-${yesterday.dayOfMonth}"
            val badRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "startDateTime": "${yesterdayISO}T09:00:00",
                    "endDateTime": "${yesterdayISO}T10:00:00.000"
                }
                """

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity<String>(badRequest, headers)
            val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

            it("should return fail with 400")
            {
                val jsonResponse = JSONObject(response.body)

                expect(response.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
                expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.StartDateTimeInPastException")
                expect(jsonResponse.get("message")).to.be.equal("StartDateTime must be in the future")
            }
        }

        on("POSTing with a end date before the start date")
        {
            val tomorrow = LocalDate.now().plusDays(1)
            val tomorrowISO = "${tomorrow.year}-${tomorrow.monthValue}-${tomorrow.dayOfMonth}"
            val badRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "startDateTime": "${tomorrowISO}T09:00:00",
                    "endDateTime": "${tomorrowISO}T08:00:00.000"
                }
                """

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity<String>(badRequest, headers)
            val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

            it("should fail with 400")
            {
                val jsonResponse = JSONObject(response.body)

                expect(response.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
                expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.EndDateTimeBeforeStartTimeException")
                expect(jsonResponse.get("message")).to.be.equal("EndDateTime must be after StartDateTime")
            }
        }

        on("POSTing with a bad formatted date")
        {
            val badRequest =
                """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "startDateTime": "foo",
                    "endDateTime": "bar"
                }
                """

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity<String>(badRequest, headers)
            val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

            it("should fail with 400")
            {
                val jsonResponse = JSONObject(response.body)

                expect(response.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
                expect(jsonResponse.getString("exception")).to.be.equal("org.springframework.http.converter.HttpMessageNotReadableException")
                expect(jsonResponse.getString("message")).to.contain("Can not deserialize value of type java.time.LocalDateTime from String \"foo\"")
            }
        }
    }
})
