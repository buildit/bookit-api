package com.buildit.bookit.v1.ping

import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object PingControllerTests : Spek(
{
    describe("/v1/ping")
    {
        on("GET")
        {
            it("should return UP")
            {
                val pingController = PingController()
                expect(pingController.ping().status).to.be.equal("UP")
            }
        }
    }
})
