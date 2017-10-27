package com.buildit.bookit.v1.location

import com.buildit.bookit.v1.location.dto.Location
import com.buildit.bookit.v1.location.dto.LocationNotFound
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Location controller unit tests
 */
object LocationControllerTests : Spek({
    val mockRepository = mock<LocationRepository> {
        on { getLocations() }.doReturn(listOf(Location(1, "NYC", "Americas/New_York")))
    }

    describe("get all locations") {
        on("GET") {
            it("should return the location") {
                val locationController = LocationController(mockRepository)
                expect(locationController.getLocations().size).to.be.equal(1)
            }
        }
    }

    describe("get 1 location") {
        on("GET") {
            it("should return the location") {
                val locationController = LocationController(mockRepository)
                expect(locationController.getLocation(1).name).to.be.equal("NYC")
            }
        }
    }

    describe("fail to get an unknown location") {
        on("GET") {
            it("should throw an exception") {
                val locationController = LocationController(mockRepository)
                assertThat({ locationController.getLocation(2) }, throws<LocationNotFound>())
            }
        }
    }
})
