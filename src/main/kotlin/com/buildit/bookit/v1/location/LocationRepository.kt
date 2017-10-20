package com.buildit.bookit.v1.location

import com.buildit.bookit.database.ConnectionProvider
import com.buildit.bookit.v1.location.dto.Location
import java.sql.ResultSet

fun mapFromResultSet(rs: ResultSet): Location {
    return Location(rs.getInt("LOCATION_ID"), rs.getString("LOCATION_NAME"), rs.getString("LOCATION_TZ"))
}

interface LocationRepository {
    fun getLocations(): Collection<Location>
}

class LocationStorageRepository(private val provider: ConnectionProvider) : LocationRepository {
    private val fields = "LOCATION_ID, LOCATION_NAME, LOCATION_TZ"
    private val baseProjection = "SELECT $fields FROM LOCATION"

    override fun getLocations(): Collection<Location> {
        return provider.fetch(baseProjection, ::mapFromResultSet)
    }
}
