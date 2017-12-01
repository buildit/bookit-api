package com.buildit.bookit.auth

import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.ArrayList

interface JwtAuthenticator {
    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken?
}

class OpenIdAuthenticator : JwtAuthenticator {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getAuthentication(token: String): UsernamePasswordAuthenticationToken? {
        val user: String? = Jwts.parser()
            .setSigningKeyResolver(OpenidSigningKeyResolver())
            .parseClaimsJws(token)
            .body
            .subject

        return if (user != null) {
            log.debug("Request token verification success. {}", user)
            UsernamePasswordAuthenticationToken(user, null, ArrayList<GrantedAuthority>())
        } else {
            log.debug("Request token verification failure. {}", user)
            null
        }
    }
}
