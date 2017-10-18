package com.buildit.bookit.v1.ping

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.throws
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 * Ping controller unit tests
 */
object PingControllerTests : Spek({
    describe("/v1/ping") {
        on("GET") {
            it("should return UP") {
                val pingController = PingController()
                expect(pingController.ping().status).to.be.equal("UP")
            }
        }
    }

    describe("/v1/ping/error") {
        on("GET") {
            it("should throw exception") {
                val pingController = PingController()
                assertThat({ pingController.error() }, throws<RuntimeException>())
            }
        }
    }
})
