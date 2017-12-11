package com.buildit.bookit.v1.ping

import com.buildit.bookit.auth.UserPrincipal
import org.hamcrest.Matchers.equalToIgnoringCase
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/**
 * Tests PingController spring integration
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(PingController::class, includeFilters = [ComponentScan.Filter(EnableWebSecurity::class)])
@WithMockCustomUser()
class PingControllerMockMvcTests @Autowired constructor(
    private val context: WebApplicationContext
) {
    @Autowired
    private lateinit var mvc: MockMvc

    @BeforeEach
    fun configureSecurityFilters() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `ping success`() {
        mvc.perform(get("/v1/ping"))
            .andExpect(status().isOk)
            .andExpect(jsonPath<String>("$.status", equalToIgnoringCase("up")))
            .andExpect(jsonPath("$.user", notNullValue()))
    }

    @Test
    @WithAnonymousUser
    fun `ping no user - v1|ping`() {
        mvc.perform(get("/v1/ping"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user").doesNotExist())
    }

    @Test
    @WithAnonymousUser
    fun `ping no user - root|`() {
        mvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user").doesNotExist())
    }
}

@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithMockCustomUser(
    val subject: String = "123abc",
    val givenName: String = "Fake",
    val familyName: String = "User"
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()

        val principal = UserPrincipal(customUser.subject, customUser.familyName, customUser.givenName)
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        context.authentication = auth
        return context
    }
}

