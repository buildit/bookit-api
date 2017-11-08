package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.dto.Location
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface BookableRepository : CrudRepository<Bookable, Int> {
    fun findByLocation(location: Location): List<Bookable>
}

//@Repository
//class BookableDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : BookableRepository {
//    private val tableName = "BOOKABLE"
//
//    override fun getAllBookables(): Collection<Bookable> = jdbcTemplate.query(
//        "SELECT BOOKABLE_ID, LOCATION_ID, BOOKABLE_NAME, DISPOSITION_CLOSED, DISPOSITION_REASON FROM $tableName") { rs, _ ->
//
//        Bookable(
//            rs.getInt("BOOKABLE_ID"),
//            rs.getInt("LOCATION_ID"),
//            rs.getString("BOOKABLE_NAME"),
//            Disposition(rs.getBoolean("DISPOSITION_CLOSED"), rs.getString("DISPOSITION_REASON"))
//        )
//    }
//}
