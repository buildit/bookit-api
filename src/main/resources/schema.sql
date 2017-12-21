DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS bookable;
DROP TABLE IF EXISTS location;

CREATE TABLE user (
    id          VARCHAR(36)  NOT NULL,
    PRIMARY KEY (id),
    external_id VARCHAR(128) NOT NULL,
    given_name  VARCHAR(128),
    family_name VARCHAR(128)
);
CREATE INDEX idx_external_id
    ON user (external_id);

CREATE TABLE bookable (
    id          VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    location_id VARCHAR(36) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    closed      BOOLEAN     NOT NULL,
    reason      VARCHAR(50) NOT NULL
);
CREATE INDEX IDX_LOCATION_ID
    ON bookable (location_id);

CREATE TABLE location (
    id        VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    name      VARCHAR(50) NOT NULL,
    time_zone VARCHAR(50) NOT NULL
);

CREATE TABLE booking (
    id          VARCHAR(36)  NOT NULL,
    PRIMARY KEY (id),
    bookable_id VARCHAR(36)  NOT NULL,
    subject     VARCHAR(255) NOT NULL,
    start       DATETIME     NOT NULL,
    end         DATETIME     NOT NULL,
    user_id     VARCHAR(36)  NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id)
);

