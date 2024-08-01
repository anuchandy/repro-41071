package com.repro41071;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.models.DeadLetterOptions;

import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class App {
    private static final String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");

    public static void main( String[] args ) throws InterruptedException {
        run();
    }

    private static void run() throws InterruptedException {
        final TopicSubscription resource = createTopicAndSubscription();
        final ServiceBusMessage messageToSend = createMessage();

        final ServiceBusClientBuilder builder =
                new ServiceBusClientBuilder().connectionString(connectionString);

        // STEP_1: Send the message.
        //
        System.out.println("Sending a message.");
        final ServiceBusSenderClient topicSender = builder.connectionString(connectionString)
                .sender()
                .topicName(resource.getTopicName())
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
                        .topicName(resource.getTopicName())
                        .subscriptionName(resource.getSubscriptionName())
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
                .topicName(resource.getTopicName())
                .subscriptionName(resource.getSubscriptionName())
                .buildClient();

        receiver2.receiveMessages(1).stream().collect(Collectors.toList()).forEach(message -> {
            System.out.println("Recevied it from DLQ" + message.getBody());
        });

        topicSender.close();
        receiver2.close();

    }

    private static TopicSubscription createTopicAndSubscription() {
        final ServiceBusAdministrationClient adminClient = new ServiceBusAdministrationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        final long millis = Instant.now().toEpochMilli();
        final String topicName = "topic-" + millis;
        final String subscriptionName = "sub0";

        adminClient.createTopic(topicName);
        adminClient.createSubscription(topicName, subscriptionName);
        System.out.println("Created topic: " + topicName + " and subscription: " + subscriptionName);

        final TopicProperties topicProperties = adminClient.getTopic(topicName);
        // Default max message size for topic is 1024KB
        System.out.println("Max message size:" + topicProperties.getMaxSizeInMegabytes());
        final int newMaxSizeInKb = 102400; // ~100MB
        // Update max message size from 1024KB to ~100MB
        topicProperties.setMaxMessageSizeInKilobytes(newMaxSizeInKb);
        adminClient.updateTopic(topicProperties);

        return new TopicSubscription(topicName, subscriptionName);
    }

    private static ServiceBusMessage createMessage() {
        final String body = "hello-world";
        System.out.println(body);

        final ServiceBusMessage serviceBusMessage = new ServiceBusMessage(body)
                .setMessageId("14317")
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

    private static class TopicSubscription {
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
    }
}
