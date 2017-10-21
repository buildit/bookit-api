package com.buildit.bookit.v1.bookable.dto

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.ResultSet

fun mapFromResultSet(rs: ResultSet): Any {
    @Suppress("MagicNumber")
    return Bookable(rs.getInt(1), rs.getInt(2), rs.getString(3))
}

/**
 * Bookable resource response
 */
data class Bookable(val bookableId: Int,
                    val locationId: Int,
                    val bookableName: String)

/**
 * Bookable not found
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND)
class BookableNotFound : RuntimeException("Bookable not found")
