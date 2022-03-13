FROM openjdk:11-jdk-slim
WORKDIR /app
COPY . /app
RUN ./gradlew bootJar
EXPOSE 8080
CMD java $JAVA_OPTS -jar build/libs/account-0.0.1.jar
