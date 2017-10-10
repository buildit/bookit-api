package com.buildit.bookit.v1.ping

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = arrayOf("*"))
@RestController
@RequestMapping("/v1/ping", "/")
class PingController {
    @GetMapping
    fun ping(): Ping {
        return Ping()
    }

    class Ping {
        val status: String = "UP"
    }
}
