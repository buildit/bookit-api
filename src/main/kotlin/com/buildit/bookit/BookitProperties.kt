package com.buildit.bookit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bookit")
data class BookitProperties (
    var databaseUser: String = "",
    var databasePassword: String = "",
    var databaseUrl: String = "jdbc:derby:derbyDB;create=true",
    var databaseDriver: String = "org.apache.derby.jdbc.EmbeddedDriver"
)
