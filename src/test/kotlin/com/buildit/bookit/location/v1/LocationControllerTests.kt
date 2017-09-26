package com.buildit.bookit.location.v1

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object LocationControllerTests : Spek(
    {
        describe("get known location")
        {
            on("GET")
            {
                it("should return UP")
                {
                    val locationController = LocationController()
                    expect(locationController.getLocation(1).locationName).to.be.equal("The best location ever")
                }
            }
        }

        describe("get unknown location")
        {
            on("GET")
            {
                it("should throw an exception")
                {
                    assertThat({LocationController().getLocation(2) }, throws<LocationNotFound>())
                }
            }
        }
    })
