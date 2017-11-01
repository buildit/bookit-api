package com.buildit.bookit

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun String.toEntity(): HttpEntity<String> {
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON

    val entity = HttpEntity<String>(this.trimIndent(), headers)
    return entity
}

object Global {
    val URI = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
    val REST_TEMPLATE = TestRestTemplate(RestTemplateBuilder().rootUri(URI).build())
}
