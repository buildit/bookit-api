# TODO figure out how to change to oracle JRE
# FROM frolvlad/alpine-oraclejdk8:slim
FROM openjdk:8-jre-alpine
WORKDIR /usr/src/app
EXPOSE 8080
ADD build/libs/bookit-api*.jar bookit-api.jar
CMD ["java", "-jar", "bookit-api.jar"]
