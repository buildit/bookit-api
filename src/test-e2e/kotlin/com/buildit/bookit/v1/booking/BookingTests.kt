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
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test /v1/booking like a black box
 */
object BookingTests : Spek(
    {
        val uri: String = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
        val restTemplate = TestRestTemplate(RestTemplateBuilder().rootUri(uri).build())
        describe("/v1/booking")
        {
            describe("POSTing a valid meeting")
            {
                val tomorrow = LocalDateTime.now(ZoneId.of("America/New_York")).plusDays(1)
                val goodRequest =
                    """
                {
                    "bookableId": 1,
                    "subject": "My new meeting",
                    "start": "$tomorrow",
                    "end": "${tomorrow.plusHours(1)}"
                }
                """.trimIndent()

                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON

                val entity = HttpEntity<String>(goodRequest, headers)
                val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

                it("should return a created meeting")
                {
                    val jsonResponse = JSONObject(response.body)
                    expect(jsonResponse.getInt("id")).to.be.above(0)
                    expect(jsonResponse.get("bookableId")).to.be.equal(1)
                    expect(jsonResponse.get("subject")).to.be.equal("My new meeting")
                }

                afterEachTest {
                    response.headers.location?.let { restTemplate.delete(it) }
                }
            }

            on("POSTing with a date in the past")
            {
                val yesterday = LocalDateTime.now(ZoneId.of("America/New_York")).minusDays(1)
                val badRequest =
                    """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "start": "$yesterday",
                    "end": "${yesterday.plusHours(1)}"
                }
                """.trimIndent()

                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON

                val entity = HttpEntity<String>(badRequest, headers)
                val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

                it("should return fail with 400")
                {
                    val jsonResponse = JSONObject(response.body)

                    expect(response.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
                    expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.StartInPastException")
                    expect(jsonResponse.get("message")).to.be.equal("Start must be in the future")
                }
            }

            on("POSTing with a end date before the start date")
            {
                val tomorrow = LocalDateTime.now(ZoneId.of("America/New_York")).plusDays(1)
                val badRequest =
                    """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "start": "$tomorrow",
                    "end": "${tomorrow.minusHours(1)}"
                }
                """.trimIndent()

                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON

                val entity = HttpEntity<String>(badRequest, headers)
                val response = restTemplate.postForEntity("/v1/booking", entity, String::class.java)

                it("should fail with 400")
                {
                    val jsonResponse = JSONObject(response.body)

                    expect(response.statusCode).to.be.equal(HttpStatus.BAD_REQUEST)
                    expect(jsonResponse.get("exception")).to.be.equal("com.buildit.bookit.v1.booking.EndBeforeStartException")
                    expect(jsonResponse.get("message")).to.be.equal("End must be after Start")
                }
            }

            on("POSTing with a bad formatted date")
            {
                val badRequest =
                    """
                {
                    "bookableId": 1,
                    "subject": "My meeting in the past",
                    "start": "foo",
                    "end": "bar"
                }
                """.trimIndent()

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
