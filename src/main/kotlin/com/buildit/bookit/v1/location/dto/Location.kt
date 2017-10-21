package com.buildit.bookit.v1.location.dto

import com.buildit.bookit.database.DataRecord
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Location response
 */
data class Location(val locationId: Int,
                    val locationName: String,
                    val timeZone: String) : DataRecord()

/**
 * 404 location not found
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND)
class LocationNotFound : RuntimeException("Location not found")