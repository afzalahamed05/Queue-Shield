package com.queueshield.resourceservice.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Write-through cache of the system-wide availability ratio - resource-service OWNS this data,
 * so every mutation (create/update/reserve/release) recomputes and writes the cache immediately
 * rather than waiting for a TTL. The TTL here is just a safety net against a missed invalidation,
 * not the primary mechanism (contrast with priority-service's cache of *this* value, which has
 * no choice but to rely on TTL since it doesn't own the data).
 */
@Component
@Slf4j
public class ResourceAvailabilityCacheService {

    private static final String CACHE_KEY = "resource:availability-ratio:cached";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ResourceRepository resourceRepository;
    private final StringRedisTemplate redisTemplate;

    public ResourceAvailabilityCacheService(ResourceRepository resourceRepository, StringRedisTemplate redisTemplate) {
        this.resourceRepository = resourceRepository;
        this.redisTemplate = redisTemplate;
    }

    public void refresh() {
        long total = resourceRepository.sumQuantityTotal();
        long available = resourceRepository.sumQuantityAvailable();
        String value = available + ":" + total;
        redisTemplate.opsForValue().set(CACHE_KEY, value, TTL);
    }

    public long[] getAvailableAndTotal() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            String[] parts = cached.split(":");
            return new long[]{Long.parseLong(parts[0]), Long.parseLong(parts[1])};
        }
        long total = resourceRepository.sumQuantityTotal();
        long available = resourceRepository.sumQuantityAvailable();
        redisTemplate.opsForValue().set(CACHE_KEY, available + ":" + total, TTL);
        return new long[]{available, total};
    }
}
