package com.buildit.bookit.v1.bookable

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/bookable")
class BookableController
{
    val theBookable = Bookable(1, 1, "The best bookable ever")

    @GetMapping(value = "/{id}")
    fun getBookable(@PathVariable("id") bookableId: Int): Bookable
    {
        if (bookableId == 1)
        {
            return theBookable
        }

        throw BookableNotFound()
    }
}
