package com.buildit.bookit.v1.location.dto

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.ZoneId
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

/**
 * Location response
 */
@Entity
data class Location(
    @Id @GeneratedValue
    val id: Int? = null,
    @Column(unique = true, nullable = false)
    val name: String,
    @Column(nullable = false)
    val timeZone: ZoneId)

/**
 * 404 location not found
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class LocationNotFound : RuntimeException("Location not found")
