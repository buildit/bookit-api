/* Licensed under Apache-2.0 */
package com.buildit.bookit

import com.buildit.bookit.database.BookItDBConnectionProvider
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.sql.Connection
import java.sql.DriverManager

/**
 * Main class (needed for spring boot integration)
 */
@SpringBootApplication
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
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage(BookitApplication::class.java.`package`.name))
            .build()
    }

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
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurerAdapter() {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedOrigins("*")
            }
        }
    }

}


fun initializeDriver(): Unit {
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance()
}

/**
 * Main entry point of the application
 *
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    initializeDriver()
    BookItDBConnectionProvider.initializeSchema()
    SpringApplication.run(BookitApplication::class.java, *args)
}
