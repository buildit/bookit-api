package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface BookableRepository : CrudRepository<Bookable, Int> {
    fun findByLocation(location: Location): List<Bookable>
}
