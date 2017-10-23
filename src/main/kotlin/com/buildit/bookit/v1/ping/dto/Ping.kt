package com.buildit.bookit.v1.ping.dto

import com.buildit.bookit.BookitProperties

/**
 * Ping response
 */
data class Ping(val status: String = "UP", val bookitProperties: BookitProperties)
