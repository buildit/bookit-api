package com.buildit.bookit.v1.location

import com.winterbe.expekt.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Booking controller unit tests
 */
@ExtendWith(SpringExtension::class)
@DataJpaTest
class LocationRepositoryTests @Autowired constructor(
    val locationRepo: LocationRepository,
    val entityManager: TestEntityManager
) {
    @Test
    fun getLocations() {
        // act
        val locations = locationRepo.findAll()?.toList()

        // assert
        expect(locations).has.size(2)
    }
}
