package com.buildit.bookit

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bookit")
data class BookitProperties (
    var databaseUser: String = "",
    @JsonIgnore var databasePassword: String = "",
    var databaseUrl: String = "jdbc:h2:./bookit-db",
    var databaseDriver: String = "org.h2.Driver"
)
