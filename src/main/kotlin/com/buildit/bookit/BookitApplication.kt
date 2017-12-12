/* Licensed under Apache-2.0 */
package com.buildit.bookit

import com.buildit.bookit.auth.JwtAuthenticationFilter
import com.buildit.bookit.auth.OpenIdAuthenticator
import com.buildit.bookit.auth.SecurityContextHolderWrapper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
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
                .allowedMethods("GET", "HEAD", "POST", "DELETE")
        }
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    @Bean
    fun securityConfigurer() = @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER) object : WebSecurityConfigurerAdapter() {
        override fun configure(security: HttpSecurity) {
            security.cors()
            security.httpBasic()
            // we are using token based authentication. csrf is not required.
            security.csrf().disable()
            security.sessionManagement().sessionCreationPolicy(STATELESS)

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

            security.addFilterBefore(
                JwtAuthenticationFilter(authenticationManager(),
                    OpenIdAuthenticator(),
                    SecurityContextHolderWrapper()),
                BasicAuthenticationFilter::class.java)
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

