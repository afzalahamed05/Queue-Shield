package com.queueshield.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Backs the Redis-based rate limiter applied to incident creation (see application.yml). Keys by
 * client IP: a fixed-window/token-bucket counter per address, implemented via Spring Cloud
 * Gateway's built-in {@code RedisRateLimiter} filter (a Lua script run atomically in Redis, not
 * hand-rolled here) - Redis is what makes this correct across multiple gateway instances sharing
 * one limit, not just an in-memory counter local to one process.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = remoteAddress != null && remoteAddress.getAddress() != null
                    ? remoteAddress.getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }
}
