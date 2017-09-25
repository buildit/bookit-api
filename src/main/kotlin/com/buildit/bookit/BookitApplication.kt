/* Licensed under Apache-2.0 */
package com.buildit.bookit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BookitApplication

fun main(args: Array<String>) {
    SpringApplication.run(BookitApplication::class.java, *args)
}
