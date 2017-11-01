package com.buildit.bookit.v1

object Global {
    val URI = System.getenv("ENDPOINT_URI") ?: "http://localhost:8080"
}
