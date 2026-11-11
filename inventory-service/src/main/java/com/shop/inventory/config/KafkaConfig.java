package com.shop.inventory.config;

import com.shop.events.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configuration file for Kafka.
 */
@Configuration
public class KafkaConfig {

    /**
     * Deliberately declared here so that the service starts correctly even if
     * order-service hasn't been run yet.
     */
    @Bean
    NewTopic orderEventsTopic() {
        return TopicBuilder.name(Topics.ORDER_EVENTS)
                           .partitions(3)
                           .replicas(1)
                           .build();
    }

    /**
     * Deliberately declared here so that the service starts correctly even if
     * inventory-service hasn't been run yet.
     */
    @Bean
    NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(Topics.INVENTORY_EVENTS)
                           .partitions(3)
                           .replicas(1)
                           .build();
    }

    /**
     * On an exception: retry the record 3 times, 1 second apart
     * (e.g. Postgres briefly down), then log it and move on. Unparseable JSON
     * is never retried. Spring Boot plugs this bean into every listener.
     * <p>
     * "Log and skip" loses the message. That's not acceptable for stock. So,
     * dead letter topic handling is important.
     */
    @Bean
    DefaultErrorHandler kafkaErrorHandler() {
        final var handler = new DefaultErrorHandler(new FixedBackOff(1000L, 3));
        handler.addNotRetryableExceptions(JsonProcessingException.class);

        return handler;
    }
}