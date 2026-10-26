FROM eclipse-temurin:27-jdk-alpine AS compilacao
WORKDIR /aplicacao
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && sh mvnw -B -ntp dependency:go-offline
COPY src src
RUN sh mvnw -B -ntp -DskipTests package && cp target/provas-*.jar /aplicacao/provas.jar

FROM eclipse-temurin:27-jdk-alpine AS execucao
WORKDIR /aplicacao
RUN addgroup -S provas && adduser -S -G provas provas
COPY --from=compilacao --chown=provas:provas /aplicacao/provas.jar ./provas.jar
USER provas
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "provas.jar"]
