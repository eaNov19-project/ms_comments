FROM openjdk:8-jdk-alpine
ADD target/ms_comments.jar ms_comments.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","ms_comments.jar"]
