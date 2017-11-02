package com.buildit.bookit.v1.location

import com.buildit.bookit.v1.location.dto.Location

interface LocationRepository : org.springframework.data.repository.CrudRepository<Location, Int> {
}

//@Repository
//class LocationDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : LocationRepository {
//
//    override fun getLocations(): Collection<Location> = jdbcTemplate.query(
//        "SELECT LOCATION_ID, LOCATION_NAME, LOCATION_TZ FROM LOCATION") { rs, _ ->
//        Location(
//            rs.getInt("LOCATION_ID"),
//            rs.getString("LOCATION_NAME"),
//            rs.getString("LOCATION_TZ")
//        )
//    }
//}
