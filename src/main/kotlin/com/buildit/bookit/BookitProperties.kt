package com.buildit.bookit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bookit")
data class BookitProperties (
    var databaseUser: String = "",
    var databasePassword: String = "",
    var databaseUrl: String = "jdbc:h2:./bookit-db",
    var databaseDriver: String = "org.h2.Driver"
)
