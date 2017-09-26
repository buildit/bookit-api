package com.buildit.bookit.v1.booking

import java.time.ZonedDateTime

data class Booking(val bookingId: Int,
                   val bookableId: Int,
                   val subject: String,
                   val startDateTime: ZonedDateTime,
                   val endDateTime: ZonedDateTime)
