FROM registry.gitlab.com/tokenmill/crawling-framework/base:latest as builder

RUN mkdir -p /usr/src/cf
WORKDIR /usr/src/cf

COPY . .

RUN cd administration-ui && mvn clean package -Pbigjar

FROM maven:3.5.4-jdk-8-alpine
RUN mkdir -p /usr/src/cf
WORKDIR /usr/src/cf

COPY --from=builder /usr/src/cf/administration-ui/target/administration-ui-standalone.jar administration-ui-standalone.jar
COPY --from=builder /usr/src/cf/administration-ui/conf/docker-compose.properties docker-compose.properties

CMD ["java", "-Dconfig=docker-compose.properties", "-jar", "administration-ui-standalone.jar"]
