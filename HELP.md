
With Docker Compose
docker-compose -f ./kafka-config/docker-compose.yml up -d

Manual Run One by One
============================
Run Zookeeper broker: 
docker run -d --name zookeeper -p 2181:2181 confluent/zookeeper

Run Kafka server: 
docker run -d -p 9094:9094 -e KAFKA_CREATE_TOPICS="test_topic:1:1:compact" -e HOSTNAME_COMMAND="docker info | grep ^Name: | cut -d' ' -f 2" -e KAFKA_ZOOKEEPER_CONNECT="172.17.0.2:2181" -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP="INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT" -e KAFKA_ADVERTISED_LISTENERS="INSIDE://:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094" -e KAFKA_LISTENERS="INSIDE://:9092,OUTSIDE://:9094" -e KAFKA_INTER_BROKER_LISTENER_NAME="INSIDE" -v /var/run/docker.sock:/var/run/docker.sock --link zookeeper:zookeeper --name kafka wurstmeister/kafka:latest

Check Container Ip's: 
docker container ls
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' zookeeper //172.17.0.2
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' kafka //172.17.0.3


If you want to create Kafka topic inside zookeeper
docker run --rm ches/kafka kafka-topics.sh --create --topic test_topic_1 --replication-factor 1 --partitions 1 --zookeeper 172.17.0.2:2181

Kafka UI: 
docker run -e "KMAGIC_ALLOW_TOPIC_DELETE=true" -d -p 8080:80 --name kafka-magic digitsy/kafka-magic
give this details to access: clusterName/Bootstrap Servers = 172.17.0.3:9092


Request
==========
curl --location --request POST 'http://localhost:8085/users' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "dhananjaya",
    "mobile": "9040010697",
    "address": "Bangalore"
}'