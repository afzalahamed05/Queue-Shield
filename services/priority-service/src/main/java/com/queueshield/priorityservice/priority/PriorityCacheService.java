package com.queueshield.priorityservice.priority;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Write-through cache for priority-service's OWN data. Unlike {@link com.queueshield.priorityservice.resource.ResourceAvailabilityClient}
 * (which can only rely on a TTL because it doesn't own the data it's caching), every write here
 * goes through {@link #put} at the same time the durable Postgres row is saved - so the cache is
 * never allowed to be stale relative to the database; the TTL below is a safety net (so a
 * priority nobody ever queries again eventually falls out of Redis) rather than the primary
 * invalidation mechanism. A read that misses the cache (expired, or Redis was restarted) falls
 * back to Postgres and repopulates it - see {@code PriorityController}.
 */
@Component
@Slf4j
public class PriorityCacheService {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public PriorityCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void put(IncidentPriority priority) {
        try {
            String json = objectMapper.writeValueAsString(CachedPriority.from(priority));
            redisTemplate.opsForValue().set(cacheKey(priority.getIncidentId()), json, TTL);
        } catch (Exception ex) {
            log.warn("Failed to write priority cache for incident {}: {}", priority.getIncidentId(), ex.getMessage());
        }
    }

    public Optional<CachedPriority> get(Long incidentId) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey(incidentId));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, CachedPriority.class));
        } catch (Exception ex) {
            log.warn("Failed to read priority cache for incident {}: {}", incidentId, ex.getMessage());
            return Optional.empty();
        }
    }

    private String cacheKey(Long incidentId) {
        return "priority:incident:" + incidentId;
    }

    public record CachedPriority(
            Long incidentId, double score, PriorityTier tier,
            double severityComponent, double peopleAffectedComponent, double vulnerabilityComponent,
            double urgencyComponent, double resourceScarcityComponent,
            java.time.Instant computedAt
    ) {
        static CachedPriority from(IncidentPriority p) {
            return new CachedPriority(p.getIncidentId(), p.getScore(), p.getTier(),
                    p.getSeverityComponent(), p.getPeopleAffectedComponent(), p.getVulnerabilityComponent(),
                    p.getUrgencyComponent(), p.getResourceScarcityComponent(), p.getComputedAt());
        }
    }
}
