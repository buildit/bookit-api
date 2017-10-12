package com.buildit.bookit.v1.booking

import java.time.ZonedDateTime

/**
 * Booking request
 */
data class BookingRequest(val bookableId: Int, val subject: String, val startDateTime: ZonedDateTime, val endDateTime: ZonedDateTime)

/**
 * Booking response
 */
data class Booking(val bookingId: Int, val bookableId: Int, val subject: String, val startDateTime: ZonedDateTime, val endDateTime: ZonedDateTime)
