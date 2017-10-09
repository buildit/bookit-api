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

If you have gradle installed, you can simply run the following commands with gradle.  
Otherwise, we have bundled a gradle wrapper so you can substitute that in the below commands.

You can run build the project by running:
```$xslt
gradle
```  

You can run tests by running the following:
```$xslt
gradle test
```

You can run the web server by running the following:
```$xslt
gradle bootRun
```
