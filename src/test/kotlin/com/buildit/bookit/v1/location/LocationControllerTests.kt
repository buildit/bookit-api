package com.buildit.bookit.v1.location

import com.buildit.bookit.database.BookItDBConnectionProvider
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
    describe("get known location") {
        on("GET") {
            it("should return the location") {
                val connProvider = mock<LocationRepository> {
                    on { getLocations() }.doReturn( listOf(Location(1, "NYC", "Americas/NewYork")))
                }

                val locationController = LocationController(connProvider)
                expect(locationController.getLocations().size).to.be.equal(1)
            }
        }
    }

    describe("get known location") {
        on("GET") {
            it("should return the location") {
                val locationController = LocationController(LocationStorageRepository(BookItDBConnectionProvider))
                expect(locationController.getLocation(1).locationName).to.be.equal("The best location ever")
            }
        }
    }

    describe("fail to get an unknown location") {
        on("GET") {
            it("should throw an exception") {
                assertThat({ LocationController(LocationStorageRepository(BookItDBConnectionProvider)).getLocation(2) }, throws<LocationNotFound>())
            }
        }
    }
})
