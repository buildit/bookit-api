package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.Disposition
import com.buildit.bookit.v1.location.dto.Location
import com.winterbe.expekt.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZoneId

/**
 * Booking controller unit tests
 */
@ExtendWith(SpringExtension::class)
@DataJpaTest
class BookableRepositoryTests @Autowired constructor(
    val bookableRepo: BookableRepository,
    val entityManager: TestEntityManager
) {
    @Test
    fun getBookable() {

        // act
        val bookable = bookableRepo.findOne(1)

        // assert
        expect(bookable).to.equal(Bookable(1, Location(1, "NYC", ZoneId.of("America/New_York")), "Red", Disposition()))
    }
}
