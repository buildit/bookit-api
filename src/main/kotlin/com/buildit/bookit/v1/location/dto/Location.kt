package com.buildit.bookit.v1.location.dto

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Location response
 */
data class Location(val name: String,
                    val timeZone: String)

/**
 * 404 location not found
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class LocationNotFound : RuntimeException("Location not found")
