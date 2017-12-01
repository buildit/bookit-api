package com.buildit.bookit.v1.ping

import com.buildit.bookit.BookitProperties
import com.buildit.bookit.v1.ping.dto.Ping
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

/**
 * Ping - for operational information
 */
@RestController
@RequestMapping("/v1/ping")
class PingController(val bookitProperties: BookitProperties) {
    /**
     * Gets ping information
     */
    @GetMapping
    fun ping(user: Principal? = null): Ping = Ping(bookitProperties, user)
}
