FROM openjdk:11-jdk-oracle
VOLUME /tmp
WORKDIR /app
COPY target/kafka-producer-consumer-example-0.0.1-SNAPSHOT.jar /app/
EXPOSE 8085
ENTRYPOINT ["java","-jar","kafka-producer-consumer-example-0.0.1-SNAPSHOT.jar"]

#mvn clean install
#docker build -f Dockerfile -t kafka-producer-consumer-example .
#docker images
#docker compose -f ./kafka-config/docker-compose.yml up -d
#docker run --name kafka-producer-consumer-example --rm -p 8080:8080 kafka-producer-consumer-example


