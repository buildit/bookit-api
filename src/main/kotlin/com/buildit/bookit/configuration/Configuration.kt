package com.buildit.bookit.configuration

import com.buildit.bookit.database.ConnectionProvider
import com.buildit.bookit.database.DerbyConnectionProvider

object Configuration {
    fun connectionProvider(): ConnectionProvider {
        return DerbyConnectionProvider()
    }
}
