package com.queueshield.priorityservice.priority;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Same test suite as the Phase 1 monolith's calculator - the algorithm didn't change, so neither should its tests. */
class PriorityScoreCalculatorTest {

    private final PriorityScoreCalculator calculator = new PriorityScoreCalculator();

    @Test
    void lowSeverityWithNoOtherFactorsProducesLowTier() {
        Instant now = Instant.now();
        PriorityScoreResult result = calculator.calculate(Severity.LOW, 0, 0, now, now, true, 1.0);

        assertThat(result.tier()).isEqualTo(PriorityTier.LOW);
        assertThat(result.score()).isLessThan(35.0);
    }

    @Test
    void criticalSeverityWithManyVulnerablePeopleProducesCriticalTier() {
        Instant now = Instant.now();
        Instant reportedFourHoursAgo = now.minus(4, ChronoUnit.HOURS);

        PriorityScoreResult result = calculator.calculate(Severity.CRITICAL, 80, 60, reportedFourHoursAgo, now, true, 0.1);

        assertThat(result.tier()).isEqualTo(PriorityTier.CRITICAL);
        assertThat(result.score()).isGreaterThanOrEqualTo(80.0);
    }

    @Test
    void resolvedIncidentsGetNoUrgencyComponentRegardlessOfAge() {
        Instant now = Instant.now();
        Instant reportedTenDaysAgo = now.minus(10, ChronoUnit.DAYS);

        PriorityScoreResult result = calculator.calculate(Severity.MODERATE, 10, 2, reportedTenDaysAgo, now, false, 1.0);

        assertThat(result.urgencyComponent()).isZero();
    }

    @Test
    void noResourceDataIsTreatedAsNeutralNotPenalized() {
        Instant now = Instant.now();

        PriorityScoreResult withData = calculator.calculate(Severity.MODERATE, 10, 2, now, now, true, 1.0);
        PriorityScoreResult noData = calculator.calculate(Severity.MODERATE, 10, 2, now, now, true, Double.NaN);

        assertThat(noData.resourceScarcityComponent()).isEqualTo(withData.resourceScarcityComponent());
    }

    @Test
    void scoreIsAlwaysClampedBetweenZeroAndHundred() {
        Instant now = Instant.now();
        Instant longAgo = now.minus(30, ChronoUnit.DAYS);

        PriorityScoreResult extreme = calculator.calculate(Severity.CRITICAL, 100_000, 100_000, longAgo, now, true, 0.0);

        assertThat(extreme.score()).isBetween(0.0, 100.0);
    }
}
