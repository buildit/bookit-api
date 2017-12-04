package com.buildit.bookit.auth

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.ArrayList
import javax.servlet.http.HttpServletRequest

interface JwtAuthenticator {
    fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken?
}

class OpenIdAuthenticator : JwtAuthenticator {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Suppress("ReturnCount")
    override fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        // if (listOf("localhost", "integration").any { request.serverName.startsWith(it) } && jwtToken == "FAKE") {
        if (listOf("localhost", "integration").any { request.serverName.startsWith(it) }) {
            log.info("Request token FAKE success.")
            return UsernamePasswordAuthenticationToken("fakeuser", null, ArrayList<GrantedAuthority>())
        }
        // parse the token.
        val user: String? = try {
            Jwts.parser()
                .setSigningKeyResolver(OpenidSigningKeyResolver())
                .parseClaimsJws(jwtToken)
                .body
                .subject
        } catch (e: JwtException) {
            log.info("Unable to parse token", e)
            null
        }

        if (user != null) {
            log.info("Request token verification success: $user")
            return UsernamePasswordAuthenticationToken(user, null, ArrayList<GrantedAuthority>())
        }

        log.info("Request token verification failure.")
        return null
    }
}
