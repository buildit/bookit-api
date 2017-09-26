package com.buildit.bookit.v1.location

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

data class Location(val locationId: Int,
                    val locationName: String)


@ResponseStatus(value=HttpStatus.NOT_FOUND)
class LocationNotFound : RuntimeException("Location not found")
