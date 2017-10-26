package com.buildit.bookit.v1.location

import com.winterbe.expekt.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Booking controller unit tests
 */
@ExtendWith(SpringExtension::class)
@JdbcTest
class LocationRepositoryTests @Autowired constructor (
    val jdbcTemplate: JdbcTemplate
) {
    @Test
    fun getLocations() {
        // arrange
        val locationRepo = LocationStorageRepository(jdbcTemplate)

        // act
        val locations = locationRepo.getLocations()

        // assert
        expect(locations.size).to.be.equal(2)
    }
}
