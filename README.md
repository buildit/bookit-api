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
```$xslt
./gradlew
```  

You can run tests by running the following:
```$xslt
./gradlew test
```

You can run the web server by running the following:
```$xslt
./gradlew bootRun
```

##

Build information:

https://console.aws.amazon.com/codebuild/home?region=us-east-1#/projects/buildit-bookit-bookit-api-master-build/view
