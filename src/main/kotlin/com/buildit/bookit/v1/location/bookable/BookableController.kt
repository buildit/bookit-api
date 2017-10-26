package com.buildit.bookit.v1.location.bookable

import com.buildit.bookit.v1.location.bookable.dto.Bookable
import com.buildit.bookit.v1.location.bookable.dto.BookableNotFound
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoint to manage bookables (aka rooms and resources)
 */
@RestController
@RequestMapping("/v1/location/{location}/bookable")
@Transactional
class BookableController {
    val theBookable = Bookable("The best bookable ever", "NYC")

    /**
     * Get a bookable
     */
    @GetMapping(value = "/{name}")
    fun getBookable(@PathVariable("location") location: String, @PathVariable("name") bookable: String): Bookable {
        if (bookable.equals(theBookable.name, true) &&
            location.equals(theBookable.location, true)) {
            return theBookable
        }

        throw BookableNotFound()
    }

    /**
     * Get all bookables
     */
    @GetMapping()
    fun getAllBookables(@PathVariable("location") location: String): Collection<Bookable> {
        return listOf(theBookable)
    }
}
