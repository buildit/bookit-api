package com.buildit.bookit.auth

import com.buildit.bookit.BookitProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import javax.servlet.http.HttpServletRequest

interface JwtAuthenticator {
    fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken?
}

class OpenIdAuthenticator(private val props: BookitProperties) : JwtAuthenticator {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Suppress("TooGenericExceptionCaught")
    override fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val user: Claims? = try {
            Jwts.parser()
                .setSigningKeyResolver(OpenidSigningKeyResolver(request, props))
                .parseClaimsJws(jwtToken)
                .body
        } catch (e: RuntimeException) {
            log.info("Unable to parse token", e)
            null
        }

        if (user != null) {
            log.info("Request token verification success: $user")
            return UsernamePasswordAuthenticationToken(UserPrincipal(user["oid", String::class.java] ?: user["sub", String::class.java], user["given_name", String::class.java] ?: "", user["family_name", String::class.java] ?: user["name", String::class.java] ?: user.subject), null, emptyList())
        }

        log.info("Request token verification failure.")
        return null
    }
}

