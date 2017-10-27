package com.buildit.bookit.v1.booking.dto

import java.time.LocalDateTime

/**
 * Booking request
 */
data class BookingRequest(val bookableId: Int, val subject: String, val start: LocalDateTime, val end: LocalDateTime)

/**
 * Booking response
 */
data class Booking(
    val id: Int,
    val bookableId: Int,
    val subject: String,
    val start: LocalDateTime,
    val end: LocalDateTime)

