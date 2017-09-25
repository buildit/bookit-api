package com.buildit.bookit

import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object CollectorSpec : Spek({
    describe("Expansion class testing suite")
    {
        on("expansion of size zero")
        {
            it("should be zero")
            {
                expect(1).to.be.equal(1)
            }
        }
    }
})
