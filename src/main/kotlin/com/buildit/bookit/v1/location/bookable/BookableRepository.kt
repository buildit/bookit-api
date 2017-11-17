package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.Disposition
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

interface BookableRepository {
    fun getAllBookables(): Collection<Bookable>
}

@Repository
class BookableDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : BookableRepository {
    private val tableName = "BOOKABLE"

    override fun getAllBookables(): Collection<Bookable> = jdbcTemplate.query(
        "SELECT BOOKABLE_ID, LOCATION_ID, BOOKABLE_NAME, DISPOSITION_CLOSED, DISPOSITION_REASON FROM $tableName") { rs, _ ->

        Bookable(
            rs.getString("BOOKABLE_ID"),
            rs.getString("LOCATION_ID"),
            rs.getString("BOOKABLE_NAME"),
            Disposition(rs.getBoolean("DISPOSITION_CLOSED"), rs.getString("DISPOSITION_REASON"))
        )
    }
}
