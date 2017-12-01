package com.buildit.bookit.auth

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class JwtAuthenticationFilter(
    authManager: AuthenticationManager,
    private val jwtAuthenticator: JwtAuthenticator) : BasicAuthenticationFilter(authManager) {

    private val tokenPrefix: String = "Bearer "
    private val tokenHeader: String = "Authorization"

    public override fun doFilterInternal(req: HttpServletRequest,
                                         res: HttpServletResponse,
                                         chain: FilterChain) {
        val header = req.getHeader(tokenHeader)

        if (header == null || !header.startsWith(tokenPrefix)) {
            chain.doFilter(req, res)
            return
        }

        SecurityContextHolder.getContext().authentication =
            jwtAuthenticator.getAuthentication(header.replace(tokenPrefix, ""))

        chain.doFilter(req, res)
    }
}
