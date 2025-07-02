package com.keakimleang.bulkpayment.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitListenerInitializer {
    private final SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory;

    @PostConstruct
    public void init() {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitListenerContainerFactory.createListenerContainer().getConnectionFactory());
        container.setMissingQueuesFatal(false);
        container.start();
    }
}
