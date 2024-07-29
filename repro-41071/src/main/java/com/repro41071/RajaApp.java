package com.repro41071;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.models.DeadLetterOptions;

import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RajaApp {
    private static final String connectionString = "MyConnectionString";
    private static final String TOPIC_NAME = "MyTopicName";
    private static final String SUBSCRIBER_NAME = "MySubscriberName";

    public static void main( String[] args ) throws InterruptedException {
        //final TopicSubscription resource = createTopicAndSubscription();
        final ServiceBusMessage messageToSend = createMessage();

        final ServiceBusClientBuilder builder =
                new ServiceBusClientBuilder().connectionString(connectionString);

        // STEP_1: Send the message.
        //
        System.out.println("Sending a message.");
        final ServiceBusSenderClient topicSender = builder.connectionString(connectionString)
                .sender()
                .topicName(TOPIC_NAME)
                .buildClient();

        topicSender.sendMessage(messageToSend);
        TimeUnit.SECONDS.sleep(5);

        // STEP_2: Receive the message.
        //
        System.out.println("Press any key to receive the message and send it to DLQ.");
        waitKeyPress();
        System.out.println("Receiving message...");

        final ServiceBusReceiverClient receiver1 =
                builder.receiver()
                        .topicName(TOPIC_NAME)
                        .subscriptionName(SUBSCRIBER_NAME)
                        .buildClient();

        receiver1.receiveMessages(1).stream().collect(Collectors.toList()).forEach(message -> {
            System.out.println("Message received:" + message.getBody());
            deadLetter(receiver1, message);
            System.out.println("Sent to DLQ");
        });
        TimeUnit.SECONDS.sleep(5);
        receiver1.close();

        // STEP_3: Go to portal and Resend the message from DLQ to the queue.
        //
        System.out.println("Press any key after resending the from DLQ to back to the topic.");
        waitKeyPress();

        // STEP_4: Receive the message again.
        //
        System.out.println("Receiving message again...");

        final ServiceBusReceiverClient receiver2 = builder.receiver()
                .topicName(TOPIC_NAME)
                .subscriptionName(SUBSCRIBER_NAME)
                .buildClient();

        receiver2.receiveMessages(1).stream().collect(Collectors.toList()).forEach(message -> {
            System.out.println("Recevied it from DLQ" + message.getBody());
        });

        topicSender.close();
        receiver2.close();
    }

    /*private static TopicSubscription createTopicAndSubscription() {
        final ServiceBusAdministrationClient adminClient = new ServiceBusAdministrationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        final long millis = Instant.now().toEpochMilli();
        final String topicName = "topic-" + millis;
        final String subscriptionName = "sub0";

        adminClient.createTopic(topicName);
        adminClient.createSubscription(topicName, subscriptionName);
        System.out.println("Created topic: " + topicName + " and subscription: " + subscriptionName);
        return new TopicSubscription(topicName, subscriptionName);
    }*/

    private static ServiceBusMessage createMessage() {
        final String jsonRequest = "{\"uri\":\"null\",\"queryParams\":{\"msgType\":\"AzureServiceBus\",\"warehouseId\":\"0000000\"},\"body\":\"{\\r\\n" + //
                "    \\\"messageHeader\\\": {\\r\\n" + //
                "        \\\"msgId\\\": 14317,\\r\\n" + //
                "        \\\"msgTime\\\": \\\"2023-04-18T14:11:31.5515919-04:00\\\",\\r\\n" + //
                "        \\\"msgType\\\": \\\"AzureServiceBus\\\",\\r\\n" + //
                "        \\\"sender\\\": \\\"Raja\\\",\\r\\n" + //
                "        \\\"receiver\\\": \\\"Microsoft\\\",\\r\\n" + //
                "        \\\"version\\\": \\\"1.0\\\",\\r\\n" + //
                "    }\\r\\n" + //
                "}\",\"headers\":{\"accept\":\"*/*\",\"connection\":\"keep-alive\",\"host\":\"localhost:7071\",\"user-agent\":\"PostmanRuntime/7.40.0\",\"accept-encoding\":\"gzip, deflate, br\",\"content-type\":\"application/json\",\"content-length\":\"502\",\"msgid\":\"14317\",\"postman-token\":\"eaba8efc-d0ff-43df-a70d-7cc96f56d115\"},\"destination\":\"Microsoft\",\"destinationEndPointUrl\":null,\"responseMsgTopicName\":null,\"publishMsgTopicName\":null,\"blobContainerEndpointDetails\":null,\"blobNamePrefixIncludeFlag\":null,\"serviceBusSubjectValue\":null}";

        final ServiceBusMessage serviceBusMessage = new ServiceBusMessage(jsonRequest)
                .setMessageId("14317")
                .setContentType("application/json")
                .setSubject("AzureServiceBus")
                .setCorrelationId("14317")
                .setSessionId("serialize-me")
                .setTo("Microsoft");

        serviceBusMessage.getApplicationProperties().put("property", "custom");
        return serviceBusMessage;
    }

    private static void deadLetter(ServiceBusReceiverClient receiver, ServiceBusReceivedMessage message) {
        final DeadLetterOptions deadLetterOptions = new DeadLetterOptions();
        deadLetterOptions.setDeadLetterReason("TESTREASON");
        deadLetterOptions.setDeadLetterErrorDescription("TESTREASONDESC");
        receiver.deadLetter(message, deadLetterOptions);
    }

    private static void waitKeyPress() {
        Scanner input = new Scanner(System.in);
        input.nextLine();
    }

    /*private static class TopicSubscription {
        private final String topicName;
        private final String subscriptionName;

        public TopicSubscription(String topicName, String subscriptionName) {
            this.topicName = topicName;
            this.subscriptionName = subscriptionName;
        }

        public String getTopicName() {
            return topicName;
        }

        public String getSubscriptionName() {
            return subscriptionName;
        }
    }*/
}
