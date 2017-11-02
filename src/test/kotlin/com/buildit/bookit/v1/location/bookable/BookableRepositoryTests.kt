package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.Disposition
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
class BookableRepositoryTests @Autowired constructor(
    val jdbcTemplate: JdbcTemplate
) {
    @Test
    fun getAllBookables() {
        // arrange
        val bookableRepository = BookableDatabaseRepository(jdbcTemplate)

        // act
        val bookables = bookableRepository.getAllBookables()

        // assert
        expect(bookables.size).to.be.equal(1)
        expect(bookables).to.contain(Bookable(1, 1, "Red", Disposition()))
    }
}
