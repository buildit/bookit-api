package com.buildit.bookit.database

import com.buildit.bookit.database.ddl.createBookableTable
import com.buildit.bookit.database.ddl.createBookingTable
import com.buildit.bookit.database.ddl.createLocationTable
import com.buildit.bookit.database.ddl.dropBookable
import com.buildit.bookit.database.ddl.dropBooking
import com.buildit.bookit.database.ddl.dropLocation
import com.buildit.bookit.database.ddl.insertLondonLocation
import com.buildit.bookit.database.ddl.insertNYCLocation
import com.buildit.bookit.database.ddl.insertRedBookable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.LinkedList
import java.util.logging.Level
import java.util.logging.Logger

abstract class DataRecord

interface ConnectionProvider {
    fun <T> fetch(sql: String, mapRecord: (ResultSet) -> T): Collection<T> where T: DataRecord
    fun insert(sql: String, applyParameters: (PreparedStatement) -> Unit)
}

object BookItDBConnectionProvider : ConnectionProvider {
    private val logger = Logger.getLogger(BookItDBConnectionProvider::class.java.simpleName)

    override fun <T> fetch(sql: String, mapRecord: (ResultSet) -> T): Collection<T> where T: DataRecord {
        val records = LinkedList<T>()
        withConnection { conn ->
            val resultSet = executeQuery(conn, sql)
            if (resultSet != null) {
                while (resultSet.next()) {
                    records.add(mapRecord(resultSet))
                }
            }
        }

        return records
    }

    override fun insert(sql: String, applyParameters: (PreparedStatement) -> Unit) {
        withConnection { conn ->
            logger.info("Running insert: '$sql'")
            val preparedStatement = conn.prepareStatement(sql)
            applyParameters(preparedStatement)
            preparedStatement.executeUpdate()
        }
    }

    fun initializeSchema() {
        withConnection { conn ->
            execute(conn, createLocationTable)
            execute(conn, createBookableTable)
            execute(conn, createBookingTable)
        }
        logger.info("Schema has been initialized!")
    }

    fun dropSchema() {
        withConnection { conn ->
            execute(conn, dropBookable)
            execute(conn, dropLocation)
            execute(conn, dropBooking)
        }
        logger.info("Schema has been dropped!")
    }

    fun initializeTables() {
        withConnection { conn ->
            execute(conn, insertRedBookable)

            execute(conn, insertNYCLocation)
            execute(conn, insertLondonLocation)
        }
        logger.info("Tables have been initialized!")
    }

    private fun withConnection(statements: (Connection) -> Unit) {
        val conn = getNewConnection()

        try {
            if (conn != null) {
                statements(conn)
                conn.close()
            }
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Failed to execute statements with connection", e)
            conn?.close()
        }
    }

    private fun getNewConnection(): Connection? {
        try {
            return DriverManager.getConnection("jdbc:derby:derbyDB;create=true")
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Failed to get a connection!", e)
        }

        return null
    }

    private fun execute(conn: Connection?, sql: String) {
        logger.info("Running statement: '$sql'")
        conn?.prepareStatement(sql)?.execute()
    }

    private fun executeQuery(conn: Connection?, sql: String): ResultSet? {
        logger.info("Running query: '$sql'")
        return conn?.prepareStatement(sql)?.executeQuery()
    }

}
