package com.buildit.bookit.database

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager

private val logger = LoggerFactory.getLogger(DerbyConnectionProvider::class.java)

class DerbyConnectionProvider : ConnectionProvider {
    override fun initializeDriver() {
        val driverName = "org.apache.derby.jdbc.EmbeddedDriver"
        Class.forName(driverName).newInstance()
        logger.info("Initialized driver: $driverName")
    }

    @Suppress("TooGenericExceptionCaught")
    override fun newConnection(): Connection? {
        try {
            return DriverManager.getConnection("jdbc:derby:derbyDB;create=true")
        } catch (e: Throwable) {
            logger.error("Failed to get a connection!", e)
        }

        return null
    }
}
