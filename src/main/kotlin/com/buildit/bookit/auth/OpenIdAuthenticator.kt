package com.buildit.bookit.auth

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import java.util.ArrayList
import java.util.Base64
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest
import javax.xml.bind.DatatypeConverter


interface JwtAuthenticator {
    fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken?
}

class OpenIdAuthenticator : JwtAuthenticator {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Suppress("ReturnCount")
    override fun getAuthentication(jwtToken: String, request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        // if (listOf("localhost", "integration").any { request.serverName.startsWith(it) } && jwtToken == "FAKE") {
        // parse the token.
        val user: String? = try {
            Jwts.parser()
                .setSigningKeyResolver(OpenidSigningKeyResolver())
                .parseClaimsJws(jwtToken)
                .body
                .subject
        } catch (e: Exception) {
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
                        .subject
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
            return UsernamePasswordAuthenticationToken(user, null, ArrayList<GrantedAuthority>())
        }

        log.info("Request token verification failure.")
        return null
    }
}
