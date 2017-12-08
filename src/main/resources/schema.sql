DROP TABLE IF EXISTS USER;
DROP TABLE IF EXISTS BOOKABLE;
DROP TABLE IF EXISTS LOCATION;
DROP TABLE IF EXISTS BOOKING;

CREATE TABLE USER (
    USER_ID            VARCHAR(36) NOT NULL,
    PRIMARY KEY        (USER_ID),
    EXTERNAL_USER_ID   VARCHAR(128) NOT NULL,
    GIVEN_NAME         VARCHAR(128),
    FAMILY_NAME        VARCHAR(128)
);
CREATE INDEX IDX_EXTERNAL_USER_ID
    ON USER (EXTERNAL_USER_ID);

CREATE TABLE BOOKABLE (
    BOOKABLE_ID        VARCHAR(36) NOT NULL,
    PRIMARY KEY        (BOOKABLE_ID),
    LOCATION_ID        VARCHAR(36) NOT NULL,
    BOOKABLE_NAME      VARCHAR(50) NOT NULL,
    DISPOSITION_CLOSED CHAR(1)     NOT NULL,
    DISPOSITION_REASON VARCHAR(50) NOT NULL
);
CREATE INDEX IDX_LOCATION_ID
    ON BOOKABLE (LOCATION_ID);

CREATE TABLE LOCATION (
    LOCATION_ID        VARCHAR(36) NOT NULL,
    PRIMARY KEY        (LOCATION_ID),
    LOCATION_NAME      VARCHAR(50) NOT NULL,
    LOCATION_TZ        VARCHAR(50) NOT NULL
);

CREATE TABLE BOOKING (
    BOOKING_ID         VARCHAR(36)  NOT NULL,
    PRIMARY KEY        (BOOKING_ID),
    BOOKABLE_ID        VARCHAR(36)  NOT NULL,
    SUBJECT            VARCHAR(255) NOT NULL,
    START_DATE         DATETIME     NOT NULL,
    END_DATE           DATETIME     NOT NULL,
    USER_ID            VARCHAR(36)  NOT NULL
--    FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID)
);

