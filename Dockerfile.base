FROM maven:3.5.4-jdk-8-alpine as builder

RUN mkdir -p /usr/src/cf
WORKDIR /usr/src/cf

COPY . .

RUN mvn clean install

FROM maven:3.5.4-jdk-8-alpine
COPY --from=builder /root/.m2/ /root/.m2/
