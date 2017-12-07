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
    val BASIC_AUTH_REST_TEMPLATE = TestRestTemplate(RestTemplateBuilder().rootUri(URI).basicAuthorization("admin", "password").build())
    val ANONYMOUS_REST_TEMPLATE = TestRestTemplate(RestTemplateBuilder().rootUri(URI).build())
    //    val BEARER_AUTH_REST_TEMPLATE = TestRestTemplate(RestTemplateBuilder().rootUri(URI).additionalInterceptors(ClientHttpRequestInterceptor { request, body, execution ->
//        val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString("secret".toByteArray()))
//        val jwt = Jwts.builder()
//            .setSubject("fakeuser")
//            .signWith(SignatureAlgorithm.HS256, apiKeySecretBytes)
//            .compact()
//        request.headers["Authorization"] = "Bearer $jwt"
//        execution.execute(request, body)
//    }).build())
//    val REST_TEMPLATE = BEARER_AUTH_REST_TEMPLATE
    val REST_TEMPLATE = ANONYMOUS_REST_TEMPLATE

}
