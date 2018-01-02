# BookIt-API

A services api for booking bookable items such as rooms and/or resources

## High level project information

The project uses:
 
 - language: Kotlin (1.8 JVM) 
 - web framework: SpringBoot
 - build managers: Gradle
 - testing: Jupiter (Spek was also tried, and found wanting)
 - database: H2 (embedded - dev & test), AWS Aurora MySql (integration/staging/prod)
 - CI/CD: [Bookit-Infrastructure](https://github.com/buildit/bookit-infrastructure) - based on AWS Bare Metal Rig

You will need to have a 1.8 JVM installed to run.  Gradle will take care of the 
dependencies.

## Architectural Decisions

We are documenting our decisions [here](../master/docs/architecture/decisions)

## Quick Start

### Docker

If you need to simply run the project locally, you can with 1 line via Docker.

1. [Install Docker](https://www.docker.com/) and run it if you haven't already
1. Clone this repo
1. Run `docker-compose up` in the root
1. Visit [http://localhost:8080/](http://localhost:8080/) in your web browser
1. When you're finished run `docker-compose down` to cleanup

### Development

To setup a proper local development environment follow these steps.

1. [Install Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 8 if you haven't already
    * If you need to change to version 8, install it. Then [swap your version](https://stackoverflow.com/questions/46513639/how-to-downgrade-java-from-9-to-8-on-a-macos-eclipse-is-not-running-with-java-9) via command line
1. Run `./gradlew bootRun` and wait until the app reads *85% EXECUTING*
1. Visit [http://localhost:8080/](http://localhost:8080/) in your web browser

## Commands

We have bundled a gradle wrapper so you can run the below commands.  Alternatively, you can use gradle if you have it installed.
    
| Command                                           |     Description                                  | Notes                    
| :---                                              | :---                                             | :---                            
| `./gradlew`                                       | Builds the project                               |                          
| `./gradlew tasks`                                 | Lists available tasks in the project             |                          
| `./gradlew check`                                 | Runs linting, unit tests, static analysis, etc   | Good for pre-push checking                         
| `./gradlew test`                                  | Runs unit/integration tests                      |                          
| `./gradlew test-e2e`                              | Runs end-to-end tests                            | Requires running server  
| `./gradlew bootRun`                               | Runs server                                      | Magically creates/updates the database and loads basic test data from `import.sql` using an in-memory H2 database.
| `BOOKIT_DATABASE_URL=jdbc:mariadb://localhost/bookit BOOKIT_DATABASE_USER=root BOOKIT_DATABASE_DIALECT=org.hibernate.dialect.MySQL55Dialect BOOKIT_DATABASE_DIALECT=org.hibernate.dialect.MySQL55Dialect ./gradlew bootRun`| Runs server                                      | Magically creates the database and loads basic test data from `import.sql` using an (already running) MySql/MariaDB instance.                         
| `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`    | Runs server                                      | Magically creates the database and loads more voluminous `import-dev.sql` file into the H2 database.                         

## Configuration Properties

Following the [12 Factor App methodology](https://12factor.net) configuration is primarily driven via Environment Variables.  Spring Boot makes this quite easy via [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

In addition to the [common Spring Boot properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html), Bookit API honors the following:

| Parameter                | Description   | Example 
| :---                     | :---          | :---          
| BOOKIT_REQUIRE_SSL       | Force ssl/https, redirect if insecure      | true/false (defaults to false)
| BOOKIT_DATABASE_URL      | The JDBC Connection URL                                                                    | jdbc:mariadb:aurora://aurora.bookit.internal/bookit
| BOOKIT_DATABASE_DRIVER   | The JDBC Driver Class (can be inferred via BOOKIT_DATABASE_URL) | org.mariadb.jdbc.Driver
| BOOKIT_DATABASE_USER     | The username to use when logging into database                                             | admin
| BOOKIT_DATABASE_PASSWORD | The password to use when logging into the database                                         | _password_
| BOOKIT_DATABASE_DIALECT | The hibernate dialect to use                                         | org.hibernate.dialect.MySQL55Dialect
| BOOKIT_ALLOW_TEST_TOKENS | Allow and verify test JWTs.  Default: allow only for localhost & integration      | true/false

The example values specified above are the values used in integration, staging, and production.  BOOKIT_DATABASE_PASSWORD is acquired via the appropriate AWS SSM Parameter Store value.

> _Note that when running via `./gradlew bootRun`, the database is magically configured to use an in-memory (H2) database.  See Quick Start, above._

## Maintenance

Common maintenance tasks:

* Add/Update locations and/or bookables - This is currently accomplished by updating the database tables.  The easiest way is to add/update to the [data.sql](./src/main/resources/data.sql)
    * if updating make sure you also update the fields in the `ON DUPLICATE KEY UPDATE XXX` area
    * these SQL statements are MySQL specific but work with H2 when in MySQL compatibility mode

## Build information

* [Build Pipeline](https://console.aws.amazon.com/codepipeline/home?region=us-east-1#/view/buildit-bookit-build-bookit-api-master-pipeline)
* [Build Reports](http://rig.buildit.bookit.us-east-1.build.s3-website-us-east-1.amazonaws.com/buildit-bookit-build-bookit-api-master/reports/)

## Deployment information

### Deployments
* [Integration](https://integration-bookit-api.buildit.tools)
* [Staging](https://staging-bookit-api.buildit.tools)
* [Production](https://bookit-api.buildit.tools)

### Swagger Docs

* [Integration](https://integration-bookit-api.buildit.tools/swagger-ui.html)
* [Staging](https://staging-bookit-api.buildit.tools/swagger-ui.html)
* [Production](https://bookit-api.buildit.tools/swagger-ui.html)

### Logging

* [Integration](https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#logStream:group=buildit-bookit-integration-app-bookit-api-master)
* [Staging](https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#logStream:group=buildit-bookit-staging-app-bookit-api-master)
* [Production](https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#logStream:group=buildit-bookit-production-app-bookit-api-master)

## Contributing

See [Contributing](./CONTRIBUTING.md)
