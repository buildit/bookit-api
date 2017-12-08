package com.buildit.bookit.v1.ping.dto

import com.buildit.bookit.BookitProperties
import java.security.Principal

/**
 * Ping response
 */
data class Ping(val bookitProperties: BookitProperties, val user: Principal?, val status: String = "UP")
