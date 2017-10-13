# BookIt-API

A services api for booking bookable items such as rooms and/or resources

## High level project information

The project uses:
 
 - language: Kotlin (1.8 JVM) 
 - web framework: SpringBoot
 - build managers: Gradle
 - testing: Spek, Jupiter

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

## Build information

https://console.aws.amazon.com/codepipeline/home?region=us-east-1#/view/buildit-bookit-build-bookit-api-master-Pipeline-UQC3AP7IZMK7

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
