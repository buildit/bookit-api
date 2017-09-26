package com.buildit.bookit.v1.bookable

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

data class Bookable(val bookableId: Int,
                    val locationId: Int,
                    val bookableName: String)

@ResponseStatus(value= HttpStatus.NOT_FOUND)
class BookableNotFound : RuntimeException("Bookable not found")
