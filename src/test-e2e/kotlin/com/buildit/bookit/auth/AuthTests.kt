package com.buildit.bookit.auth

import com.buildit.bookit.Global
import com.winterbe.expekt.expect
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestInterceptor
import java.util.Base64
import java.util.Date
import javax.xml.bind.DatatypeConverter

/**
 * Test security
 */
class `Security E2E Tests` {
    private val testRestTemplate = TestRestTemplate(RestTemplateBuilder().rootUri(Global.URI).build())

    @BeforeEach
    fun `clear interceptors`() {
        testRestTemplate.restTemplate.interceptors.clear()
    }

    @Test
    fun `unparsable token`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            request.headers["Authorization"] = "Bearer FooBarBaz"
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `blank token`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            request.headers["Authorization"] = "Bearer "
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `no signature`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            val jwt = Jwts.builder()
                .setSubject("fakeuser")
                .compact()
            request.headers["Authorization"] = "Bearer $jwt"
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `invalid signature`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString("secret".toByteArray()))
            val jwt = Jwts.builder()
                .setSubject("fakeuser")
                .signWith(SignatureAlgorithm.HS256, apiKeySecretBytes)
                .compact().dropLast(1)

            request.headers["Authorization"] = "Bearer $jwt"
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `expired`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString("secret".toByteArray()))
            val jwt = Jwts.builder()
                .setSubject("fakeuser")
                .setExpiration(Date(System.currentTimeMillis() - 3600000))
                .signWith(SignatureAlgorithm.HS256, apiKeySecretBytes)
                .compact().dropLast(1)

            request.headers["Authorization"] = "Bearer $jwt"
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `issued after now`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString("secret".toByteArray()))
            val jwt = Jwts.builder()
                .setSubject("fakeuser")
                .setIssuedAt(Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS256, apiKeySecretBytes)
                .compact().dropLast(1)

            request.headers["Authorization"] = "Bearer $jwt"
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `bad token behaves as anonymous`() {
        testRestTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString("secret".toByteArray()))
            val jwt = Jwts.builder()
                .setSubject("fakeuser")
                .signWith(SignatureAlgorithm.HS256, apiKeySecretBytes)
                .compact().dropLast(1)

            request.headers["Authorization"] = "Bearer $jwt"
            execution.execute(request, body)
        })

        val response = testRestTemplate.getForEntity("/v1/ping", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.OK)
        expect(JSONObject(response.body).has("user")).to.be.`false`
    }

    @Test
    fun `basic auth against API`() {
        val response = Global.BASIC_AUTH_REST_TEMPLATE.getForEntity("/v1/location", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.OK)
    }

    @Test
    fun `basic auth against Actuator`() {
        val response = Global.BASIC_AUTH_REST_TEMPLATE.getForEntity("/management/env", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.OK)
    }

    @Test
    fun `bearer auth against Actuator fails`() {
        val response = Global.BEARER_AUTH_REST_TEMPLATE.getForEntity("/management/env", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `anonymous auth against Actuator fails`() {
        val response = Global.ANONYMOUS_REST_TEMPLATE.getForEntity("/management/env", String::class.java)

        expect(response.statusCode).to.equal(HttpStatus.UNAUTHORIZED)
    }

}
