package com.buildit.bookit.database

import org.slf4j.LoggerFactory

val createBookableTable = "CREATE TABLE BOOKABLE (BOOKABLE_ID INTEGER, BOOKABLE_NAME VARCHAR(50), BOOKABLE_STATUS CHAR(1))"
val insertRedBookable = "INSERT INTO BOOKABLE(BOOKABLE_ID, BOOKABLE_NAME, BOOKABLE_STATUS) VALUES (1, 'Red', 'N')"

val createLocationTable = "CREATE TABLE LOCATION (LOCATION_ID INTEGER, LOCATION_NAME VARCHAR(50), LOCATION_TZ VARCHAR(50))"
val insertNYCLocation = "INSERT INTO LOCATION(LOCATION_ID, LOCATION_NAME, LOCATION_TZ) VALUES(1, 'NYC', 'America/NewYork')"
val insertLondonLocation = "INSERT INTO LOCATION(LOCATION_ID, LOCATION_NAME, LOCATION_TZ) VALUES(2, 'LON', 'Europe/London')"

val createBookingTable = "CREATE TABLE BOOKING (BOOKING_ID INTEGER, BOOKABLE_ID INTEGER, SUBJECT VARCHAR(255), START_DATE TIMESTAMP, END_DATE TIMESTAMP)"

val dropBookable = "DROP TABLE BOOKABLE"
val dropLocation = "DROP TABLE LOCATION"
val dropBooking = "DROP TABLE BOOKING"

private val logger = LoggerFactory.getLogger(BookItSchema::class.java)

class BookItSchema(private val dataAccess: DataAccess) {

    fun initializeSchema() {
        dataAccess.execute(arrayOf(createLocationTable, createBookableTable, createBookingTable))
        logger.info("Schema has been initialized!")
    }

    fun dropSchema() {
        dataAccess.execute(arrayOf(dropBookable, dropLocation, dropBooking))
        logger.info("Schema has been dropped!")
    }

    fun initializeTables() {
        dataAccess.execute(arrayOf(insertRedBookable, insertNYCLocation, insertLondonLocation))
        logger.info("Tables have been initialized!")
    }

}
