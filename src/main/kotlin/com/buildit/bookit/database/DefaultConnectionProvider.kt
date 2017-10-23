package com.buildit.bookit.database

import com.buildit.bookit.BookitProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager

private val logger = LoggerFactory.getLogger(DefaultConnectionProvider::class.java)

@Component
class DefaultConnectionProvider(val bookitProperties: BookitProperties) : ConnectionProvider {
    override fun initializeDriver() {
        Class.forName(bookitProperties.databaseDriver).newInstance()
        logger.info("Initialized driver: ${bookitProperties.databaseDriver}")
    }

    @Suppress("TooGenericExceptionCaught")
    override fun newConnection(): Connection? {
        try {
            return DriverManager.getConnection(bookitProperties.databaseUrl)
        } catch (e: Throwable) {
            logger.error("Failed to get a connection!", e)
        }

        return null
    }
}
