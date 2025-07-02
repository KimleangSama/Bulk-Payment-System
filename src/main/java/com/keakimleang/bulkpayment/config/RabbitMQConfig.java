package com.keakimleang.bulkpayment.config;

import com.keakimleang.bulkpayment.quartz.BulkPaymentDataProdMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitMQConfig {
    @Value("${bulk.payment.exchange.name:bulk-payment-exchange}")
    private String exchangeName;

    @Value("${bulk.payment.queue.name:bulk-payment-queue}")
    private String queueName;

    @Value("${bulk.payment.routing.key:bulk.payment.process}")
    private String routingKey;

    @Bean
    public TopicExchange bulkPaymentExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue bulkPaymentQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding bulkPaymentBinding() {
        return BindingBuilder
                .bind(bulkPaymentQueue())
                .to(bulkPaymentExchange())
                .with(routingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(
                "com.keakimleang.bulkpayment.quartz.BulkPaymentDataProdMessage",
                BulkPaymentDataProdMessage.class
        );
        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);
        return converter;
    }

}