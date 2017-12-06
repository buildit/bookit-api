/* Licensed under Apache-2.0 */
package com.buildit.bookit

import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.oidc.client.AzureAdClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.springframework.security.web.SecurityFilter
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
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
        }
    }
}

@Configuration
class Pac4JConfiguration {
    @Bean
    fun config(): Config {
        // https://github.com/pac4j/spring-security-pac4j
        val oidcConfiguration = OidcConfiguration()
        oidcConfiguration.clientId = "08538ace-8720-4dd7-8e69-9bf79981aa9a"
        oidcConfiguration.secret = "wxwUD257wvykNKEIX40*}=["
        oidcConfiguration.discoveryURI = "https://login.microsoftonline.com/organizations/v2.0/.well-known/openid-configuration"
        val oidcClient = AzureAdClient(oidcConfiguration)
        oidcClient.name = "AzureAdClient"

//        val simpleTestUsernamePasswordAuthenticator = SimpleTestUsernamePasswordAuthenticator()
//        val directBasicAuthClient = DirectBasicAuthClient(simpleTestUsernamePasswordAuthenticator)
//        val anonymousClient = AnonymousClient()

//        val clients = Clients("http://localhost:8080/callback", oidcClient, directBasicAuthClient, anonymousClient)
        val clients = Clients(oidcClient)

        val config = Config(clients)
//        config.addAuthorizer("admin", RequireAnyRoleAuthorizer("ROLE_ADMIN"))
//        config.addMatcher("excludedPath", PathMatcher().excludeRegex("^/facebook/notprotected\\.html$"))
        return config
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    @Bean
    fun securityConfigurer(config: Config) = object : WebSecurityConfigurerAdapter() {
        override fun configure(security: HttpSecurity) {
            super.configure(security)
            val filter = SecurityFilter(config, "AzureAdClient")
            http
                .cors().and()
                .authorizeRequests().anyRequest().authenticated().and()
                .addFilterBefore(filter, BasicAuthenticationFilter::class.java)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
    }
}
//@Configuration
//@EnableWebSecurity
//class WebSecurityConfiguration {
//    @Bean
//    fun securityConfigurer() = object : WebSecurityConfigurerAdapter() {
//        override fun configure(security: HttpSecurity) {
//            security.cors()
//            security.httpBasic()
//            security.authorizeRequests().antMatchers(
//                "/",
//                "/index.html",
//                // these are just swagger stuffs
//                "/swagger-ui.html",
//                "/swagger-resources/**",
//                "/webjars/springfox-swagger-ui/**",
//                "/api-docs/**",
//                "/v2/api-docs",
//                "/configuration/ui",
//                "/configuration/security"
//            ).permitAll()
//
//            // we only host RESTful API and every services are protected.
//            security.authorizeRequests().antMatchers("/v1/ping").permitAll()
//            security.authorizeRequests().anyRequest().authenticated()
//
//            // we are using token based authentication. csrf is not required.
//            security.csrf().disable()
//
//            security.addFilterBefore(
//                JwtAuthenticationFilter(authenticationManager(),
//                    OpenIdAuthenticator(),
//                    SecurityContextHolderWrapper()),
//                BasicAuthenticationFilter::class.java)
//            security.sessionManagement().sessionCreationPolicy(STATELESS)
//        }
//    }
//}

/**
 * Main entry point of the application
 *
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(BookitApplication::class.java, *args)
}

