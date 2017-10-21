package com.buildit.bookit.database

import java.sql.Connection

interface ConnectionProvider {
    fun initializeDriver()
    fun newConnection(): Connection?
}
