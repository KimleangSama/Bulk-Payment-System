package com.keakimleang.bulkpayment.services;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RedisCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> setValue(String key, Object value, Duration ttl) {
        return redisTemplate.opsForValue().set(key, value, ttl);
    }

    public Mono<Object> getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> setListValue(String key, List<?> values, Duration ttl) {
        if (values == null || values.isEmpty()) {
            return Mono.just(false);
        }

        return redisTemplate.opsForList()
                .rightPushAll(key, values.toArray())
                .then(redisTemplate.expire(key, ttl))
                .thenReturn(true)
                .onErrorResume(e -> {
                    log.warn("Failed to set list in Redis for key {}: {}", key, e.getMessage());
                    return Mono.just(false);
                });
    }


    public Flux<Object> getListValue(String key) {
        return redisTemplate.opsForList()
                .range(key, 0, -1)
                .switchIfEmpty(Flux.empty());
    }

    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key).map(deleted -> deleted > 0);
    }
}
