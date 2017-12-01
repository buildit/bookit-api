package com.buildit.bookit.auth

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class JwtAuthenticationFilterUnitTests {

    @Test
    fun doFilterInternal() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer test")

        val jwtAuthenticator: JwtAuthenticator = mock {
            on { getAuthentication("test") }
                .doReturn(UsernamePasswordAuthenticationToken("principal", "credentials"))
        }

        val filter = JwtAuthenticationFilter(mock {}, jwtAuthenticator)
        filter.doFilter(request, MockHttpServletResponse(), mock {})
    }
}
