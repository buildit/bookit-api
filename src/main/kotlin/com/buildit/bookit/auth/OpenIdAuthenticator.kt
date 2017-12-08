package com.buildit.bookit.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.Base64
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest
import javax.xml.bind.DatatypeConverter

interface JwtAuthenticator {
    fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken?
}

class OpenIdAuthenticator : JwtAuthenticator {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val user: Claims? = try {
            Jwts.parser()
                .setSigningKeyResolver(OpenidSigningKeyResolver)
                .parseClaimsJws(jwtToken)
                .body
        } catch (e: RuntimeException) {
            if (listOf("localhost", "integration").any { request.serverName.startsWith(it) }) {
                log.info("Attempt FAKE token validation.")
                try {
                    //We will sign our JWT with our ApiKey secret
                    val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString("secret".toByteArray()))
                    val signingKey = SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.jcaName)
                    Jwts.parser()
                        .setSigningKey(signingKey)
                        .parseClaimsJws(jwtToken)
                        .body
                } catch (ex: JwtException) {
                    log.info("Unable to parse FAKE token", ex)
                    null
                }
            } else {
                log.info("Unable to parse token", e)
                null
            }
        }

        if (user != null) {
            log.info("Request token verification success: $user")
            return UsernamePasswordAuthenticationToken(UserPrincipal(user["oid", String::class.java], user["given_name", String::class.java], user["family_name", String::class.java]), null, ArrayList<GrantedAuthority>())
        }

        log.info("Request token verification failure.")
        return null
    }
}

