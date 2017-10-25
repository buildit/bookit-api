# BookIt-API

A services api for booking bookable items such as rooms and/or resources

## High level project information

The project uses:
 
 - language: Kotlin (1.8 JVM) 
 - web framework: SpringBoot
 - build managers: Gradle
 - testing: Spek, Jupiter
 - database: H2 (embedded - dev & test), AWS Aurora MySql (integration/staging/prod)

You will need to have a 1.8 JVM installed to run.  Gradle will take care of the 
dependencies.

## Quick start

We have bundled a gradle wrapper so you can run the below commands.  Alternatively, you can use gradle if you have it installed.

You can build the project by running:
```$sh
./gradlew
```  

You can see the list of available tasks in the project by running:
```$sh
./gradlew tasks
```  

You can run tests by running the following:
```$sh
./gradlew test
```

You can run all checks (linting, unit tests, static analysis) by running the following:
```$sh
./gradlew check
```

You can run e2e tests by running the following:
```$sh
./gradlew test-e2e
```

You can run the web server by running the following:
```$sh
./gradlew bootRun
```

## Configuration Properties

Following the [12 Factor App methodology](https://12factor.net) configuration is primarily driven via Environment Variables.  Spring Boot makes this quite easy via [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

In addition to the [common Spring Boot properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html), Bookit API honors the following:

| Parameter             | Description | Example |
| :---                  | :---          | :---   |       
| BOOKIT_DATABASE_URL    | The JDBC Connection URL | jdbc:mysql:aurora://aurora.bookit.internal/bookit  |
| BOOKIT_DATABASE_DRIVER    | The JDBC Driver Class (can be inferred via BOOKIT_DATABASE_URL, required if using MariaDB) | org.mariadb.jdbc.Driver |
| BOOKIT_DATABASE_USER | The username to use when logging into database | admin |
| BOOKIT_DATABASE_PASSWORD | The password to use when logging into the database | <password> |

The example values specified above are the values used in integration, staging, and production.  BOOKIT_DATABASE_PASSWORD is acquired via the appropriate AWS SSM Parameter Store value.


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
