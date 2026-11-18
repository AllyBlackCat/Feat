FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src/ src/

EXPOSE 8080

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=local"]