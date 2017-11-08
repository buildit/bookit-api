package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.Booking
import com.buildit.bookit.v1.location.bookable.dto.Bookable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface BookingRepository : CrudRepository<Booking, Int> {
    fun findByBookable(bookable: Bookable): List<Booking>
}
