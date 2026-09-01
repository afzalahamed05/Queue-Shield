package com.queueshield.resourceservice.resource;

/**
 * {@code ratio} is {@code null} (not NaN) when there's no data yet (total == 0) - NaN doesn't
 * survive a JSON round-trip cleanly (it's not valid JSON), so "no data" is represented the same
 * way any other missing value would be.
 */
public record AvailabilityRatioResponse(long available, long total, Double ratio) {

    public static AvailabilityRatioResponse of(long available, long total) {
        Double ratio = total <= 0 ? null : (double) available / total;
        return new AvailabilityRatioResponse(available, total, ratio);
    }
}
