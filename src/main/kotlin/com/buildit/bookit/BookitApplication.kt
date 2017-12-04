/* Licensed under Apache-2.0 */
package com.buildit.bookit

import com.buildit.bookit.auth.JwtAuthenticationFilter
import com.buildit.bookit.auth.OpenIdAuthenticator
import com.buildit.bookit.auth.SecurityContextHolderWrapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.net.URL
import java.time.Clock

/**
 * Main class (needed for spring boot integration)
 */
@SpringBootApplication
@EnableConfigurationProperties(BookitProperties::class)
class BookitApplication {
    @Bean
    fun defaultClock(): Clock = Clock.systemUTC()
}

/**
 * Swagger configuration
 */
@Configuration
@EnableSwagger2
class SwaggerConfiguration {
    /**
     * Swagger configuration
     */
    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage(BookitApplication::class.java.`package`.name))
        .build()
}

/**
 * CORS configuration
 */
@Configuration
class WebMvcConfiguration {
    /**
     * CORS configuration
     */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer = object : WebMvcConfigurerAdapter() {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry
                .addMapping("/**")
                .allowedOrigins("*")
        }
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    @Bean
    fun securityConfigurer() = object : WebSecurityConfigurerAdapter() {
        override fun configure(security: HttpSecurity) {
            security.cors()
            security.httpBasic()
            security.authorizeRequests().antMatchers(
                "/",
                "/index.html",
                // these are just swagger stuffs
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/springfox-swagger-ui/**",
                "/api-docs/**",
                "/v2/api-docs",
                "/configuration/ui",
                "/configuration/security"
            ).permitAll()

            // we only host RESTful API and every services are protected.
            security.authorizeRequests().antMatchers("/v1/ping").permitAll()
            security.authorizeRequests().anyRequest().authenticated()

            // we are using token based authentication. csrf is not required.
            security.csrf().disable()

            // Set up a JWT processor to parse the tokens and then check their signature
// and validity time window (bounded by the "iat", "nbf" and "exp" claims)
            val jwtProcessor = DefaultJWTProcessor<SecurityContext>()

// The public RSA keys to validate the signatures will be sourced from the
// OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
// object caches the retrieved keys to speed up subsequent look-ups and can
// also gracefully handle key-rollover
            val keySource = RemoteJWKSet<SecurityContext>(URL("https://login.microsoftonline.com/organizations/discovery/v2.0/keys"))

// The expected JWS algorithm of the access tokens (agreed out-of-band)
            val expectedJWSAlg = JWSAlgorithm.RS256

// Configure the JWT processor with a key selector to feed matching public
// RSA keys sourced from the JWK set URL
            val keySelector = JWSVerificationKeySelector(expectedJWSAlg, keySource)
            jwtProcessor.setJWSKeySelector(keySelector)

            security.addFilterBefore(
                JwtAuthenticationFilter(authenticationManager(),
                    OpenIdAuthenticator(jwtProcessor),
                    SecurityContextHolderWrapper()),
                BasicAuthenticationFilter::class.java)
            security.sessionManagement().sessionCreationPolicy(STATELESS)
        }
    }
}

/**
 * Main entry point of the application
 *
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(BookitApplication::class.java, *args)
}

