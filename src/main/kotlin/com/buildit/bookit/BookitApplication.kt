/* Licensed under Apache-2.0 */
package com.buildit.bookit

import com.buildit.bookit.database.BookItSchema
import com.buildit.bookit.database.DefaultDataAccess
import com.buildit.bookit.database.DefaultConnectionProvider
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Main class (needed for spring boot integration)
 */
@SpringBootApplication
@EnableConfigurationProperties(BookitProperties::class)
class BookitApplication

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

/**
 * Main entry point of the application
 *
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(BookitApplication::class.java, *args)
}

@Component
class InitializationBean(val bookitProperties: BookitProperties) {

    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        val connProvider = DefaultConnectionProvider(bookitProperties)
        connProvider.initializeDriver()

        val defaultDataAccess = DefaultDataAccess(connProvider)
        val bookItSchema = BookItSchema(defaultDataAccess)
        bookItSchema.dropSchema()
        bookItSchema.initializeSchema()
        bookItSchema.initializeTables()
    }
}
