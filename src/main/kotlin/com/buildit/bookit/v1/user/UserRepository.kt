package com.buildit.bookit.v1.user

import com.buildit.bookit.v1.booking.dto.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

interface UserRepository {
    fun getUser(id: String): User
    fun getUserByExternalId(externalId: String): User
    fun getAllUsers(): Collection<User>
    fun insertUser(externalUserId: String, givenName: String, familyName: String): User
}

@Repository
class UserDatabaseRepository(private val jdbcTemplate: JdbcTemplate) : UserRepository {
    private val tableName = "USER"
    private val userSelect = "SELECT USER_ID, GIVEN_NAME, FAMILY_NAME, EXTERNAL_USER_ID FROM $tableName"

    override fun getUser(id: String): User =
        jdbcTemplate.queryForObject<User>(
            "$userSelect WHERE USER_ID = ?", arrayOf(id)) { rs, _ ->
            makeUser(rs)
        }

    override fun getUserByExternalId(externalId: String): User =
        jdbcTemplate.queryForObject<User>(
            "$userSelect WHERE EXTERNAL_USER_ID = ?", arrayOf(externalId)) { rs, _ ->
            makeUser(rs)
        }

    override fun getAllUsers(): Collection<User> = jdbcTemplate.query(
        "$userSelect") { rs, _ ->
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
        return User(userId, "$givenName $familyName", externalUserId)
    }

    private fun makeUser(rs: ResultSet): User =
        User(
            rs.getString("USER_ID"),
            "${rs.getString("GIVEN_NAME")} ${rs.getString("FAMILY_NAME")}",
            rs.getString("EXTERNAL_USER_ID")
        )
}

