package com.buildit.bookit.v1.booking

import com.buildit.bookit.v1.booking.dto.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

interface UserRepository {
    fun getUser(id: String): User?
    fun getAllUsers(): Collection<User>
    fun insertUser(externalUserId: String, givenName: String, familyName: String): User
}

@Repository
class UserDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : UserRepository {
    private val tableName = "USER"

    override fun getUser(id: String): User? =
        jdbcTemplate.queryForObject<User>(
            "SELECT USER_ID, GIVEN_NAME, FAMILY_NAME FROM $tableName WHERE USER_ID = ?", arrayOf(id)) { rs, _ ->
            makeUser(rs)
        }

    override fun getAllUsers(): Collection<User> = jdbcTemplate.query(
        "SELECT USER_ID, GIVEN_NAME, FAMILY_NAME FROM $tableName") { rs, _ ->
        makeUser(rs)
    }

    override fun insertUser(externalUserId: String, givenName: String, familyName: String): User {
        val userId = UUID.randomUUID().toString()

        SimpleJdbcInsert(jdbcTemplate).withTableName(tableName).apply {
            execute(
                mapOf("USER_ID" to userId,
                    "EXTERNAL_USER_ID" to externalUserId,
                    "GIVEN_NAME" to givenName,
                    "FAMILY_NAME" to familyName
                )
            )
        }
        return User(userId, "$givenName $familyName")
    }

    private fun makeUser(rs: ResultSet): User =
        User(
            rs.getString("USER_ID"),
            "${rs.getString("GIVEN_NAME")} ${rs.getString("FAMILY_NAME")}"
        )
}

@Component
class UserRegistrar {
    companion object {
        // Returns actual DB user for E2E tests.  Is there a better way?
        fun getFakeLoggedInUser() = User("c40c724e-36c3-465f-9094-6e02e13d1802", "Fake DB User")
    }

//    fun register(authentication: Authentication): User {
//        val token: UsernamePasswordAuthenticationToken? = authentication.principal as? UsernamePasswordAuthenticationToken
//        val principal: UserPrincipal? = token?.principal as? UserPrincipal?
//        return User(principal?.subject ?: "broken", "${principal?.givenName} ${principal?.familyName}")
//    }

    fun register(authentication: Authentication) = User()
}
