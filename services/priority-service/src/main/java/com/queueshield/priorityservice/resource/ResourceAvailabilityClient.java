package com.queueshield.priorityservice.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * The one synchronous, cross-service call in priority-service: it needs a single aggregate
 * number (system-wide resource availability ratio) from resource-service to feed the scarcity
 * factor. This is deliberately NOT event-driven - there's no natural "event" for "here is the
 * current ratio", and a plain cached GET is simpler and just as correct for a number that only
 * needs to be approximately fresh.
 *
 * <p>Cached in Redis for {@code TTL_SECONDS} under key {@code resource:availability-ratio}. This
 * is a read-through cache of data priority-service does NOT own, so TTL expiry is the entire
 * invalidation strategy here (contrast with {@link com.queueshield.priorityservice.priority.PriorityCacheService},
 * which write-throughs on every update because it owns that data) - priority-service has no way
 * to know when resource-service's data changes, so it accepts up to {@code TTL_SECONDS} of
 * staleness in exchange for not hammering resource-service on every priority calculation.
 *
 * <p>If resource-service is unreachable, this degrades gracefully to NaN ("no data") rather than
 * failing the whole priority calculation - {@link com.queueshield.priorityservice.priority.PriorityScoreCalculator}
 * already treats NaN as neutral (no scarcity penalty).
 */
@Component
@Slf4j
public class ResourceAvailabilityClient {

    private static final String CACHE_KEY = "resource:availability-ratio";
    private static final Duration TTL = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final StringRedisTemplate redisTemplate;

    public ResourceAvailabilityClient(@Value("${resource-service.base-url:http://resource-service:8085}") String baseUrl,
                                       StringRedisTemplate redisTemplate) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.redisTemplate = redisTemplate;
    }

    public double getAvailabilityRatio() {
        String cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return Double.parseDouble(cached);
        }

        double ratio = fetchFromResourceService();
        redisTemplate.opsForValue().set(CACHE_KEY, String.valueOf(ratio), TTL);
        return ratio;
    }

    private double fetchFromResourceService() {
        try {
            ResourceAvailabilityResponse response = restClient.get()
                    .uri("/api/resources/availability-ratio")
                    .retrieve()
                    .body(ResourceAvailabilityResponse.class);
            return response == null || response.ratio() == null ? Double.NaN : response.ratio();
        } catch (Exception ex) {
            log.warn("Could not reach resource-service for availability ratio - treating as neutral: {}", ex.getMessage());
            return Double.NaN;
        }
    }

    /** {@code ratio} is null (not NaN - NaN isn't valid JSON) when resource-service has no data yet. */
    private record ResourceAvailabilityResponse(long available, long total, Double ratio) {
    }
}
