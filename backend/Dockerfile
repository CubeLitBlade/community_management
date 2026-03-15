FROM dhi.io/eclipse-temurin:25-alpine3.23

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]