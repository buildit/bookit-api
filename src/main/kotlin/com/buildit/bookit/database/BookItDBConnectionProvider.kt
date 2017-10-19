package com.buildit.bookit.database

import com.buildit.bookit.database.ddl.createBookable
import java.sql.Connection
import java.sql.DriverManager


object BookItDBConnectionProvider {
    fun getNewConnection(): Connection? {
        try {
            return DriverManager.getConnection("jdbc:derby:derbyDB;create=true")
        } catch (e: Throwable) {
            println("Failed to get a connection!")
            println(e)
        }

        return null
    }

    fun execute(conn: Connection, sql: String): Unit {
        try {
            conn.prepareStatement(sql).execute()
        } catch (e: Throwable) {
            println("Failed to execute the statement: '$sql'")
            println(e)
        }
    }

    fun initializeSchema(): Boolean {
        val conn = getNewConnection()
        if (conn != null) {
            execute(conn, createBookable)
        }

        println("Schema has been initialized!")
        return true
    }
}
