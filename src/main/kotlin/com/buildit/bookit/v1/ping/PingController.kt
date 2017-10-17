package com.buildit.bookit.v1.ping

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Ping - for operational information
 */
@RestController
@RequestMapping("/v1/ping", "/")
class PingController {
    private val logger = LoggerFactory.getLogger(PingController::class.java)
    /**
     * Gets ping information
     */
    @GetMapping
    fun ping(): Ping {
        return Ping()
    }

    /**
     * Gets ping information
     */
    @GetMapping("error")
    fun error(): Ping {
        logger.error("PING ERROR")
        throw RuntimeException("PING EXCEPTION")
    }

    /**
     * Ping response
     */
    data class Ping(val status: String = "UP")
}
