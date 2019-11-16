FROM openjdk:8-jdk-alpine
EXPOSE 8082
COPY target/ms_comments.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
