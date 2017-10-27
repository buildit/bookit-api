package com.buildit.bookit.v1.location.bookable.dto

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Bookable resource response
 */
data class Bookable(val id: Int,
                    val locationId: Int,
                    val name: String,
                    val available: Boolean = true)

/**
 * Bookable not found
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class BookableNotFound : RuntimeException("Bookable not found")

/**
 * Bookable not found
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidBookableSearch : RuntimeException("Both startDateTime and endDateTime must be specified if one is specified")
