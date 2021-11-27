#
# Build Stage
#
FROM maven:3.8.4-eclipse-temurin-17 as builder
ARG TAG
RUN echo ${TAG}
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Drevision=${TAG}

#
# Package
#
FROM eclipse-temurin:17-alpine
ARG TAG
COPY --from=builder /home/app/target/redis-proxy-${TAG}.jar app.jar
COPY docker-entrypoint.sh /usr/local/bin/
EXPOSE 8080
EXPOSE 6379

RUN chmod +x /usr/local/bin/docker-entrypoint.sh && ln -s /usr/local/bin/docker-entrypoint.sh /
ENTRYPOINT ["sh", "docker-entrypoint.sh"]