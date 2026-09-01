package com.queueshield.shelterservice.shelter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Write-through cache of aggregate shelter capacity - same pattern as resource-service/responder-service's caches. */
@Component
public class ShelterCapacityCacheService {

    private static final String CACHE_KEY = "shelter:capacity:cached";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ShelterRepository shelterRepository;
    private final StringRedisTemplate redisTemplate;

    public ShelterCapacityCacheService(ShelterRepository shelterRepository, StringRedisTemplate redisTemplate) {
        this.shelterRepository = shelterRepository;
        this.redisTemplate = redisTemplate;
    }

    public void refresh() {
        long total = shelterRepository.sumCapacityTotal();
        long occupied = shelterRepository.sumCapacityOccupied();
        redisTemplate.opsForValue().set(CACHE_KEY, total + ":" + occupied, TTL);
    }

    /** @return [total, occupied, available] */
    public long[] getCapacity() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        long total;
        long occupied;
        if (cached != null) {
            String[] parts = cached.split(":");
            total = Long.parseLong(parts[0]);
            occupied = Long.parseLong(parts[1]);
        } else {
            total = shelterRepository.sumCapacityTotal();
            occupied = shelterRepository.sumCapacityOccupied();
            redisTemplate.opsForValue().set(CACHE_KEY, total + ":" + occupied, TTL);
        }
        return new long[]{total, occupied, Math.max(0, total - occupied)};
    }
}
