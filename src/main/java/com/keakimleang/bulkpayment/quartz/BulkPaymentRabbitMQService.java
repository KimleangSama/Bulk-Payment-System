package com.keakimleang.bulkpayment.quartz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keakimleang.bulkpayment.config.props.RabbitMQProperties;
import com.keakimleang.bulkpayment.entities.BulkPaymentDataProd;
import com.keakimleang.bulkpayment.payloads.ProcessingStatus;
import com.keakimleang.bulkpayment.repos.BulkPaymentDataProdRepository;
import com.keakimleang.bulkpayment.repos.BulkPaymentInfoRepository;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkPaymentRabbitMQService {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RabbitMQProperties rabbitMQProperties;
    private final BulkPaymentInfoRepository bulkPaymentInfoRepository;
    private final BulkPaymentDataProdRepository bulkPaymentDataProdRepository;

    @Value("${bulk.payment.queue.name:bulk-payment-queue}")
    private String queueName;

    @Value("${bulk.payment.exchange.name:bulk-payment-exchange}")
    private String exchangeName;

    @Value("${bulk.payment.routing.key:bulk.payment.process}")
    private String routingKey;

    @Value("${bulk.payment.consumer.concurrency:5}")
    private int consumerConcurrency;

    private Sender sender;
    private Receiver receiver;

    @PostConstruct
    public void init() {
        if (rabbitMQProperties == null) {
            throw new IllegalStateException("RabbitMQProperties must be configured");
        }
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMQProperties.getHost());
        connectionFactory.setPort(rabbitMQProperties.getPort());
        connectionFactory.setUsername(rabbitMQProperties.getUsername());
        connectionFactory.setPassword(rabbitMQProperties.getPassword());
        this.sender = RabbitFlux.createSender(new SenderOptions().connectionFactory(connectionFactory));
        this.receiver = RabbitFlux.createReceiver(new ReceiverOptions().connectionFactory(connectionFactory));
    }

    @PreDestroy
    public void cleanup() {
        if (sender != null) sender.close();
        if (receiver != null) receiver.close();
    }

    /**
     * Sends a bulk payment message to RabbitMQ.
     * Returns Mono<Void> so the caller controls subscription.
     */
    public Mono<Void> sendBulkPaymentForProcessing(Long bulkPaymentInfoId, List<BulkPaymentDataProd> records) {
        return Mono.fromCallable(() -> {
                    BulkPaymentDataProdMessage message = BulkPaymentDataProdMessage.builder()
                            .bulkPaymentInfoId(bulkPaymentInfoId)
                            .records(records)
                            .timestamp(Instant.now())
                            .build();
                    return objectMapper.writeValueAsBytes(message);
                })
                .flatMap(body -> {
                    OutboundMessage outboundMessage = new OutboundMessage(
                            exchangeName,
                            routingKey,
                            body
                    );
                    return sender.send(Mono.just(outboundMessage));
                })
                .doOnSuccess(v -> log.info("Sent bulk payment message for ID: {} with {} records",
                        bulkPaymentInfoId, records.size()))
                .doOnError(e -> log.error("Failed to send bulk payment message for ID {}",
                        bulkPaymentInfoId, e));
    }

    /**
     * Starts consuming messages as a Flux stream.
     */
    @EventListener(ContextRefreshedEvent.class)
    public Flux<BulkPaymentDataProdMessage> receiveBulkPaymentMessages() {
        return receiver.consumeAutoAck(queueName)
                .limitRate(consumerConcurrency, consumerConcurrency / 2)
                .flatMap(this::deserializeMessage)
                .flatMap(this::processBulkPaymentMessage)
                .doOnNext(message ->
                        log.info("Processed bulk payment message for ID: {} with {} records at {}",
                                message.getBulkPaymentInfoId(),
                                message.getRecords().size(),
                                message.getTimestamp()))
                .doOnError(e ->
                        log.error("Error consuming messages", e));
    }

    private Mono<BulkPaymentDataProdMessage> deserializeMessage(Delivery delivery) {
        return Mono.fromCallable(() ->
                objectMapper.readValue(delivery.getBody(), BulkPaymentDataProdMessage.class)
        );
    }

    private Mono<BulkPaymentDataProdMessage> processBulkPaymentMessage(BulkPaymentDataProdMessage message) {
        return Flux.fromIterable(message.getRecords())
                // Parallel processing with concurrency limit of 5
                .flatMap(this::processRecord, consumerConcurrency)
                .collectList()
                .flatMap(statuses -> updateBulkPaymentInfoStatus(message.getBulkPaymentInfoId(), statuses))
                .thenReturn(message);
    }

    private Mono<String> processRecord(BulkPaymentDataProd record) {
        return webClient.get()
                .uri("/posts/{id}", record.getId())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)).maxBackoff(Duration.ofSeconds(5)))
                .flatMap(result -> updateBulkPaymentDataStatus(record, SUCCESS, null).thenReturn(SUCCESS))
                .onErrorResume(e -> handleError(record, e));
    }

    private Mono<String> handleError(BulkPaymentDataProd record, Throwable e) {
        Throwable cause = Optional.ofNullable(e.getCause()).orElse(e);
        String failureReason;
        String status;
        switch (cause) {
            case WebClientResponseException ex -> {
                status = FAIL;
                failureReason = ex.getMessage();
            }
            case TimeoutException ignored -> {
                status = FAIL;
                failureReason = "Request timed out";
            }
            case RejectedExecutionException ignored -> {
                status = FAIL;
                failureReason = "Execution rejected (thread pool)";
            }
            default -> {
                status = FAIL;
                failureReason = cause.getMessage();
            }
        }
        log.error("Process failed | recordId={} | status={} | reason={}", record.getId(), status, failureReason, cause);
        return updateBulkPaymentDataStatus(record, status, failureReason).thenReturn(status);
    }

    private Mono<BulkPaymentDataProd> updateBulkPaymentDataStatus(BulkPaymentDataProd record, String status, String failureReason) {
        record.setStatus(status);
        record.setUpdatedAt(LocalDateTime.now());
        record.setExecutedAt(LocalDateTime.now());
        record.setFailureReason(SUCCESS.equals(status) ? null : failureReason);
        return bulkPaymentDataProdRepository.save(record);
    }

    private Mono<Void> updateBulkPaymentInfoStatus(Long bulkPaymentInfoId, List<String> statuses) {
        long failed = statuses.stream().filter(s -> !SUCCESS.equals(s)).count();
        String status = failed == 0 ? "COMPLETED" : "FAILED";

        return bulkPaymentInfoRepository.findById(bulkPaymentInfoId)
                .flatMap(info -> {
                    info.setStatus(ProcessingStatus.valueOf(status));
                    info.setUpdatedAt(LocalDateTime.now());
                    info.setRemark(failed > 0 ? failed + " record(s) failed." : "All records processed successfully.");
                    return bulkPaymentInfoRepository.save(info);
                })
                .doOnSuccess(v -> log.info("Updated bulkPaymentInfo | id={} | status={}", bulkPaymentInfoId, status))
                .doOnError(e -> log.error("Failed to update bulkPaymentInfo | id={}", bulkPaymentInfoId, e))
                .then();
    }
}