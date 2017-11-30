package com.buildit.bookit.auth

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.util.ArrayList
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// TODO: kotlinify it
// TODO: check log levels
class JwtAuthenticationFilter(authManager: AuthenticationManager) : BasicAuthenticationFilter(authManager) {
    private val tokenPrefix = "Bearer "
    private val tokenHeader = "Authorization"
    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(req: HttpServletRequest,
                                  res: HttpServletResponse,
                                  chain: FilterChain) {
        val header = req.getHeader(tokenHeader)

        if (header == null || !header.startsWith(tokenPrefix)) {
            chain.doFilter(req, res)
            return
        }

        val authentication = getAuthentication(req)

        SecurityContextHolder.getContext().authentication = authentication
        chain.doFilter(req, res)
    }

    @Suppress("ReturnCount")
    private fun getAuthentication(request: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = request.getHeader(tokenHeader)
        if (token != null) {
            val jwtToken = token.replace(tokenPrefix, "")
            if (listOf("localhost", "integration").any { request.serverName.startsWith(it) } && jwtToken == "FAKE") {
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
        }
        return null
    }
}
