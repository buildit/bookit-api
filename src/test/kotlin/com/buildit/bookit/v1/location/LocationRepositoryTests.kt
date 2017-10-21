package com.buildit.bookit.v1.location

import com.buildit.bookit.database.DataAccess
import com.buildit.bookit.v1.location.dto.Location
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.sql.ResultSet

/**
 * Booking controller unit tests
 */
object LocationRepositoryTests : Spek({
    describe("map result set") {
        on("mapping a result set") {
            it("should map to a location properly") {
                val resultSet = mock<ResultSet> {
                    on { getInt("LOCATION_ID") }.doReturn(1)
                    on { getString("LOCATION_NAME") }.doReturn("The location")
                    on { getString("LOCATION_TZ") }.doReturn("The TZ")
                }

                val location = mapFromResultSet(resultSet)

                expect(location.locationId).to.be.equal(1)
                expect(location.locationName).to.be.equal("The location")
                expect(location.timeZone).to.be.equal("The TZ")
            }
        }
    }

    describe("get all locations") {
        on("calling get locations") {
            it("should get a list of locations") {
                val dataAccess = mock<DataAccess> {
                    on { fetch("SELECT LOCATION_ID, LOCATION_NAME, LOCATION_TZ FROM LOCATION", ::mapFromResultSet) }.doReturn(listOf(Location(1, "NYC", "Americas/NewYork")))
                }

                val locationRepo = LocationStorageRepository(dataAccess)
                val locations = locationRepo.getLocations()
                expect(locations.size).to.be.equal(1)
            }
        }
    }
})
