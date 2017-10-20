package com.buildit.bookit.v1.location

import com.buildit.bookit.database.BookItDBConnectionProvider
import com.buildit.bookit.v1.location.dto.Location
import java.sql.ResultSet

private fun mapFromResultSet(rs: ResultSet): Location {
    return Location(rs.getInt(1), rs.getString(2), rs.getString(3))
}

class LocationRepository {
    private val fields = "LOCATION_ID, LOCATION_NAME, LOCATION_TZ"
    private val baseProjection = "SELECT $fields FROM LOCATION"

    fun getLocations(): Collection<Any> {
        return BookItDBConnectionProvider.fetch(baseProjection, ::mapFromResultSet)
    }
}
