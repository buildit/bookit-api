package com.buildit.bookit.v1.booking.dto

import java.time.LocalDateTime

/**
 * Booking request
 */
data class BookingRequest(val bookableId: Int, val subject: String, val startDateTime: LocalDateTime, val endDateTime: LocalDateTime)

/**
 * Booking response
 */
data class Booking(
    val bookingId: Int,
    val bookableId: Int,
    val subject: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime)
