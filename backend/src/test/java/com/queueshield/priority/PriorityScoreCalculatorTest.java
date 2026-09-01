package com.queueshield.priority;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityScoreCalculatorTest {

    private final PriorityScoreCalculator calculator = new PriorityScoreCalculator();

    @Test
    void lowSeverityWithNoOtherFactorsProducesLowTier() {
        Instant now = Instant.now();
        PriorityScoreResult result = calculator.calculate(
                Severity.LOW, 0, 0, now, now, true, 1.0);

        assertThat(result.tier()).isEqualTo(PriorityTier.LOW);
        assertThat(result.score()).isLessThan(35.0);
    }

    @Test
    void criticalSeverityWithManyVulnerablePeopleProducesCriticalTier() {
        Instant now = Instant.now();
        Instant reportedFourHoursAgo = now.minus(4, ChronoUnit.HOURS);

        PriorityScoreResult result = calculator.calculate(
                Severity.CRITICAL, 80, 60, reportedFourHoursAgo, now, true, 0.1);

        assertThat(result.tier()).isEqualTo(PriorityTier.CRITICAL);
        assertThat(result.score()).isGreaterThanOrEqualTo(80.0);
    }

    @Test
    void higherSeverityAlwaysOutranksLowerSeverityAllElseEqual() {
        Instant now = Instant.now();

        PriorityScoreResult low = calculator.calculate(Severity.LOW, 20, 5, now, now, true, 0.5);
        PriorityScoreResult high = calculator.calculate(Severity.HIGH, 20, 5, now, now, true, 0.5);

        assertThat(high.score()).isGreaterThan(low.score());
    }

    @Test
    void resolvedIncidentsGetNoUrgencyComponentRegardlessOfAge() {
        Instant now = Instant.now();
        Instant reportedTenDaysAgo = now.minus(10, ChronoUnit.DAYS);

        PriorityScoreResult result = calculator.calculate(
                Severity.MODERATE, 10, 2, reportedTenDaysAgo, now, false, 1.0);

        assertThat(result.urgencyComponent()).isZero();
    }

    @Test
    void unresolvedIncidentsAccrueUrgencyOverTime() {
        Instant now = Instant.now();
        Instant justReported = now;
        Instant reportedSixHoursAgo = now.minus(6, ChronoUnit.HOURS);

        PriorityScoreResult fresh = calculator.calculate(Severity.MODERATE, 10, 2, justReported, now, true, 1.0);
        PriorityScoreResult stale = calculator.calculate(Severity.MODERATE, 10, 2, reportedSixHoursAgo, now, true, 1.0);

        assertThat(stale.urgencyComponent()).isGreaterThan(fresh.urgencyComponent());
        assertThat(stale.urgencyComponent()).isEqualTo(100.0); // saturates at 6h
    }

    @Test
    void vulnerabilityComponentReflectsProportionNotRawCount() {
        Instant now = Instant.now();

        // 5 of 5 affected are vulnerable -> 100% ratio
        PriorityScoreResult allVulnerable = calculator.calculate(Severity.MODERATE, 5, 5, now, now, true, 1.0);
        // 5 of 500 affected are vulnerable -> 1% ratio
        PriorityScoreResult mostlyNotVulnerable = calculator.calculate(Severity.MODERATE, 500, 5, now, now, true, 1.0);

        assertThat(allVulnerable.vulnerabilityComponent()).isEqualTo(100.0);
        assertThat(mostlyNotVulnerable.vulnerabilityComponent()).isLessThan(5.0);
    }

    @Test
    void scarceResourcesIncreaseScoreComparedToPlentifulResources() {
        Instant now = Instant.now();

        PriorityScoreResult plentiful = calculator.calculate(Severity.MODERATE, 10, 2, now, now, true, 1.0);
        PriorityScoreResult scarce = calculator.calculate(Severity.MODERATE, 10, 2, now, now, true, 0.0);

        assertThat(scarce.score()).isGreaterThan(plentiful.score());
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

        PriorityScoreResult extreme = calculator.calculate(
                Severity.CRITICAL, 100_000, 100_000, longAgo, now, true, 0.0);

        assertThat(extreme.score()).isLessThanOrEqualTo(100.0);
        assertThat(extreme.score()).isGreaterThanOrEqualTo(0.0);
    }
}
