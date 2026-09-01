package com.queueshield.responderservice.responder;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Write-through cache of the available responder count - resource-service's pattern, applied here for the same reason: this service owns the data, so every status change refreshes the cache immediately. */
@Component
public class ResponderAvailabilityCacheService {

    private static final String CACHE_KEY = "responder:available:count";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ResponderRepository responderRepository;
    private final StringRedisTemplate redisTemplate;

    public ResponderAvailabilityCacheService(ResponderRepository responderRepository, StringRedisTemplate redisTemplate) {
        this.responderRepository = responderRepository;
        this.redisTemplate = redisTemplate;
    }

    public void refresh() {
        long available = responderRepository.countByStatus(ResponderStatus.AVAILABLE);
        redisTemplate.opsForValue().set(CACHE_KEY, String.valueOf(available), TTL);
    }

    public long getAvailableCount() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        long available = responderRepository.countByStatus(ResponderStatus.AVAILABLE);
        redisTemplate.opsForValue().set(CACHE_KEY, String.valueOf(available), TTL);
        return available;
    }
}
