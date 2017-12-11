package com.buildit.bookit.v1.user

import com.winterbe.expekt.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * User controller unit tests
 */
@ExtendWith(SpringExtension::class)
@JdbcTest
class UserRepositoryTests @Autowired constructor(val jdbcTemplate: JdbcTemplate) {
    val userRepo = UserDatabaseRepository(jdbcTemplate)

    @Test
    fun getAllUsers() {
        val users = userRepo.getAllUsers()
        expect(users.size).to.be.equal(1) // User defined in data.sql
    }

    @Test
    fun getAllUsersWithUsers() {
        val user1 = userRepo.insertUser("guid1", "Test1", "User1")
        val user2 = userRepo.insertUser("guid2", "Test2", "User2")

        val allUsers = userRepo.getAllUsers()
        expect(allUsers).to.contain(user1)
        expect(allUsers).to.contain(user2)
    }

    @Test
    fun getSingleUser() {
        val createdUser = userRepo.insertUser("guid", "Test", "User")
        expect(createdUser.id).not.to.be.`null`
        expect(createdUser.name).to.be.equal("Test User")

        val readUser = userRepo.getUser(createdUser.id)
        expect(readUser).not.to.be.`null`
        expect(readUser?.id).not.to.be.`null`
        expect(readUser?.name).to.be.equal("Test User")
    }
}
