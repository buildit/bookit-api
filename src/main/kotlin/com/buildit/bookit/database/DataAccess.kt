package com.buildit.bookit.database

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.LinkedList

private val logger = LoggerFactory.getLogger("DataAccess")

abstract class DataRecord

interface DataAccess {
    fun <T> fetch(sql: String, mapRecord: (ResultSet) -> T): Collection<T> where T: DataRecord
    fun insert(sql: String, applyParameters: (PreparedStatement) -> Unit)
    fun execute(sql: String): Boolean?
    fun execute(statements: Array<String>): Boolean?
}

@Component
class DefaultDataAccess(private val connProvider: ConnectionProvider) : DataAccess {

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

    override fun execute(sql: String) = execute(arrayOf(sql))

    override fun execute(statements: Array<String>): Boolean? {
        withConnection { conn -> statements.forEach { execute(conn, it) } }
        return true
    }

    // Suppressing for now since we will likely not use this going forward
    @Suppress("TooGenericExceptionCaught")
    private fun withConnection(statements: (Connection) -> Unit) {
        val conn = connProvider.newConnection()

        try {
            if (conn != null) {
                statements(conn)
                conn.close()
            }
        } catch (e: Throwable) {
            logger.error("Failed to execute statements with connection", e)
            conn?.close()
        }
    }

    private fun prepareSQL(conn: Connection?, sql: String): PreparedStatement? {
        return conn?.prepareStatement(sql)
    }

    private fun execute(conn: Connection?, sql: String): Boolean? {
        logger.info("Running statement: '$sql'")
        return prepareSQL(conn, sql)?.execute()
    }

    private fun executeQuery(conn: Connection?, sql: String): ResultSet? {
        logger.info("Running query: '$sql'")
        return prepareSQL(conn, sql)?.executeQuery()
    }
}
