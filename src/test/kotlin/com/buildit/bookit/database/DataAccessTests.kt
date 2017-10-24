package com.buildit.bookit.database

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.winterbe.expekt.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Bookable controller unit tests
 */
object DataAccessTests : Spek({
//    describe("get a derby connection") {
//        on("newConnection()") {
//            it("should throw provide a connection") {
//                DefaultConnectionProvider().initializeDriver()
//                val conn = DefaultConnectionProvider().newConnection()
//                expect(conn).to.be.not.`null`
//            }
//        }
//    }

    describe("fetch records") {
        on("calling fetch()") {
            it("should generate DataRecord objects") {
                class TestRecord(val id: Int) : DataRecord()
                fun mapTestRecord(resultSet: ResultSet) = TestRecord(resultSet.getInt("ID"))

                val idQuery = "SELECT ID FROM TABLE"

                val rSet = mock<ResultSet> {
                    on { next() }.doReturn(true).thenReturn(false)
                    on { getInt("ID") }.doReturn(1)
                }

                val pStatement = mock<PreparedStatement> {
                    on { executeQuery() }.doReturn(rSet)
                }

                val connection = mock<Connection> {
                    on { prepareStatement(idQuery) }.doReturn(pStatement)
                }
                val connProvider = mock<ConnectionProvider> {
                    on { newConnection() }.doReturn(connection)
                }
                val dataAccess = DefaultDataAccess(connProvider)

                val testRecords = dataAccess.fetch(idQuery, ::mapTestRecord)
                expect(testRecords.size).to.be.equal(1)
            }
        }
    }

    describe("fetch records with no results") {
        on("calling fetch()") {
            it("should generate an empty list") {
                class TestRecord(val id: Int) : DataRecord()
                fun mapTestRecord(resultSet: ResultSet) = TestRecord(resultSet.getInt("ID"))

                val idQuery = "SELECT ID FROM TABLE"

                @Suppress("UnsafeCast") // to resolve the method
                val pStatement = mock<PreparedStatement> {
                    on { executeQuery() }.doReturn(null as ResultSet?)
                }

                val connection = mock<Connection> {
                    on { prepareStatement(idQuery) }.doReturn(pStatement)
                }

                val connProvider = mock<ConnectionProvider> {
                    on { newConnection() }.doReturn(connection)
                }
                val dataAccess = DefaultDataAccess(connProvider)

                val testRecords = dataAccess.fetch(idQuery, ::mapTestRecord)
                expect(testRecords.size).to.be.equal(0)
            }
        }
    }

    describe("execute a single statement") {
        on("calling execute()") {
            it("should run successfully") {
                val idQuery = "IT DOESN'T MATTER THIS ISN'T SQL"

                val pStatement = mock<PreparedStatement> {
                    on { execute() }.doReturn(true)
                }

                val connection = mock<Connection> {
                    on { prepareStatement(idQuery) }.doReturn(pStatement)
                }

                val connProvider = mock<ConnectionProvider> {
                    on { newConnection() }.doReturn(connection)
                }
                val dataAccess = DefaultDataAccess(connProvider)

                val executed = dataAccess.execute(idQuery)
                expect(executed).to.be.`true`
            }
        }
    }

})
