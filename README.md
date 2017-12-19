# BookIt-API

A services api for booking bookable items such as rooms and/or resources

## High level project information

The project uses:
 
 - language: Kotlin (1.8 JVM) 
 - web framework: SpringBoot
 - build managers: Gradle
 - testing: Jupiter (Spek was also tried, and found wanting)
 - database: H2 (embedded - dev & test), AWS Aurora MySql (integration/staging/prod)

You will need to have a 1.8 JVM installed to run.  Gradle will take care of the 
dependencies.

## Quick Start

1. [Install Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 8 if you haven't already
  1. If you need to change to version 8, install it. Then 
    [swap your version](https://stackoverflow.com/questions/46513639/how-to-downgrade-java-from-9-to-8-on-a-macos-eclipse-is-not-running-with-java-9) via command line
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
| `./gradlew bootRun`                               | Runs server                                      | Magically runs `schema.sql` and loads basic test data from `data.sql` using an in-memory H2 database.
| `BOOKIT_DATABASE_URL=jdbc:mariadb://localhost/bookit BOOKIT_DATABASE_USER=root ./gradlew bootRun`| Runs server                                      | Magically runs `schema.sql` and loads basic test data from `data.sql` using an (already running) MySql/MariaDB instance.                         
| `SPRING_DATASOURCE_PLATFORM=dev ./gradlew bootRun`| Runs server                                      | Magically runs `schema.sql` and loads more voluminous `dev-data.sql` file into the H2 database.                         



## Configuration Properties

Following the [12 Factor App methodology](https://12factor.net) configuration is primarily driven via Environment Variables.  Spring Boot makes this quite easy via [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

In addition to the [common Spring Boot properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html), Bookit API honors the following:

| Parameter                | Description   | Example 
| :---                     | :---          | :---          
| BOOKIT_DATABASE_URL      | The JDBC Connection URL                                                                    | jdbc:mysql:aurora://aurora.bookit.internal/bookit
| BOOKIT_DATABASE_DRIVER   | The JDBC Driver Class (can be inferred via BOOKIT_DATABASE_URL, required if using MariaDB) | org.mariadb.jdbc.Driver
| BOOKIT_DATABASE_USER     | The username to use when logging into database                                             | admin
| BOOKIT_DATABASE_PASSWORD | The password to use when logging into the database                                         | _password_

The example values specified above are the values used in integration, staging, and production.  BOOKIT_DATABASE_PASSWORD is acquired via the appropriate AWS SSM Parameter Store value.

> _Note that when running via `./gradlew bootRun`, the database is magically configured to use an in-memory (H2) database.  See Quick Start, above._


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

See [Contributing](./docs/CONTRIBUTING.md)
