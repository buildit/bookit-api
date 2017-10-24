package com.buildit.bookit.v1.location

import com.buildit.bookit.database.DataAccess
import com.buildit.bookit.v1.location.dto.Location
import org.springframework.stereotype.Repository
import java.sql.ResultSet

fun mapFromResultSet(rs: ResultSet): Location {
    return Location(rs.getInt("LOCATION_ID"), rs.getString("LOCATION_NAME"), rs.getString("LOCATION_TZ"))
}

interface LocationRepository {
    fun getLocations(): Collection<Location>
}

@Repository
class LocationStorageRepository(private val dataAccess: DataAccess) : LocationRepository {
    private val fields = "LOCATION_ID, LOCATION_NAME, LOCATION_TZ"
    private val baseProjection = "SELECT $fields FROM LOCATION"

    override fun getLocations(): Collection<Location> {
        return dataAccess.fetch(baseProjection, ::mapFromResultSet)
    }
}
