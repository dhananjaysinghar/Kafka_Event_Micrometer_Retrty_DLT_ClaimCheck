# Kafka_Event_Micrometer_Retrty_DLT_ClaimCheck

## HTTP Request
~~~
curl --location --request POST 'http://localhost:8080/users/send' \
--data-raw ''
~~~

## Kafka UI:
~~~
docker run -e "KMAGIC_ALLOW_TOPIC_DELETE=true" -d -p 8090:80 --name kafka-magic digitsy/kafka-magic
~~~
