/* Licensed under Apache-2.0 */
package com.buildit.bookit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Main class (needed for spring boot integration)
 */
@SpringBootApplication
@EnableSwagger2
class BookitApplication

/**
 * Main entry point of the application
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(BookitApplication::class.java, *args)
}

/**
 * Swagger configuration
 */
@Bean
fun api(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()
}

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
