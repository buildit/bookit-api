package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import org.springframework.data.repository.CrudRepository

interface BookingRepository : CrudRepository<Booking, String> {
    fun findByBookable(bookable: Bookable): List<Booking>
}
