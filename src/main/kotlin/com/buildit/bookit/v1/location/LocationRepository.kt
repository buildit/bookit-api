package com.buildit.bookit.v1.location

import com.buildit.bookit.v1.location.dto.Location
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface LocationRepository : CrudRepository<Location, Int> {
}
