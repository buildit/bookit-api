package com.buildit.bookit.v1.location.bookable.dto

/**
 * Bookable resource response
 */
data class Bookable(val id: Int,
                    val locationId: Int,
                    val name: String,
                    val available: Boolean = true)

