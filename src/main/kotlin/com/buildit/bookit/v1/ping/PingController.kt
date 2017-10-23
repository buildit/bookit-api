package com.buildit.bookit.v1.ping

import com.buildit.bookit.BookitProperties
import com.buildit.bookit.v1.ping.dto.Ping
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Ping - for operational information
 */
@RestController
@RequestMapping("/v1/ping", "/")
class PingController(val bookitProperties: BookitProperties) {
    private val logger = LoggerFactory.getLogger(PingController::class.java)
    /**
     * Gets ping information
     */
    @GetMapping
    fun ping(): Ping {
        return Ping(bookitProperties = bookitProperties)
    }

    /**
     * Gets ping information
     */
    @GetMapping("error")
    @Suppress("TooGenericExceptionThrown")
    fun error(): Ping {
        logger.error("PING ERROR")
        throw RuntimeException("PING EXCEPTION")
    }

}
