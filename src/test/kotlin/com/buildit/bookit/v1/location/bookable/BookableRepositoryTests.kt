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

@ExtendWith(SpringExtension::class)
@JdbcTest
class BookableRepositoryTests @Autowired constructor(
    val jdbcTemplate: JdbcTemplate
) {
    @Test
    fun getAllBookables() {
        val bookableRepository = BookableDatabaseRepository(jdbcTemplate)
        val bookables = bookableRepository.getAllBookables()
        expect(bookables.size).to.be.equal(6)
        expect(bookables).to.contain(Bookable("aab6d676-d3cb-4b9b-b285-6e63058aeda8", "b1177996-75e2-41da-a3e9-fcdd75d1ab31", "Red Room", Disposition()))
    }
}
