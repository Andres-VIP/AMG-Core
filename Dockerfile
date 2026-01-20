FROM gradle:8.8-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
