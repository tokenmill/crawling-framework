FROM registry.gitlab.com/tokenmill/crawling-framework/base:latest as builder

RUN mkdir -p /usr/src/cf
WORKDIR /usr/src/cf

COPY . .

RUN cd crawler && \
    mvn package -Dstorm.scope=compile -Dlog4j.scope=compile -Pbigjar -DskipTests

FROM maven:3.5.4-jdk-8-alpine
RUN mkdir -p /usr/src/cf
WORKDIR /usr/src/cf

COPY --from=builder /usr/src/cf/crawler/target/crawler-standalone.jar crawler-standalone.jar
COPY --from=builder /usr/src/cf/crawler/conf/docker-compose.yaml docker-compose.yaml

CMD ["java", "-cp", "crawler-standalone.jar", "lt.tokenmill.crawling.crawler.CrawlerTopology", "-local", "-conf", "docker-compose.yaml"]
