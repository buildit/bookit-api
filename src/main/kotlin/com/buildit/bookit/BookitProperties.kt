package com.buildit.bookit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bookit")
data class BookitProperties (
    var databaseUrl: String? = ""
)
