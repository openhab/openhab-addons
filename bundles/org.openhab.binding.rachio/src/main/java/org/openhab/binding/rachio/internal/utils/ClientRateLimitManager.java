/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rachio.internal.utils;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.RACHIO_RATE_LIMIT_CRITICAL;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.RACHIO_RATE_LIMIT_WARNING;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link ClientRateLimitManager} maintains a running average of recent API calls and provides
 * throttling guidance based on rate limit headers.
 *
 * @author Jeff James - Initial contribution
 * @author Kovacs Istvan - Adaptation and integration into the openHAB 5.1+ Rachio binding
 */
@NonNullByDefault
public class ClientRateLimitManager {

    public enum Priority {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH;
    }

    public enum RequestPurpose {
        BACKGROUND_REFRESH,
        CORE_STATUS_POLL,
        INITIALIZATION,
        USER_COMMAND;
    }

    private static final int INITIALIZATION_BOOTSTRAP_BURST_MAX = 20;
    private static final int INITIALIZATION_BOOTSTRAP_BURST_MIN = 5;
    private static final int INITIALIZATION_BOOTSTRAP_REMAINING_HEADROOM = 5;

    private final int numBuckets;
    private final long bucketSizeMillis;
    // Mutable rate limit state is guarded by the synchronized public methods.
    private int rateLimitCap;
    private int rateRemaining;
    private Instant rateResetTime = Instant.MAX;
    private int initializationBootstrapRemaining = 0;
    private final int[] buckets;
    private long bucket0EndMillis = 0;
    private long total = 0;
    private boolean rateLimitKnown = false;

    public ClientRateLimitManager(int numBuckets, Duration bucketSize) {
        this.numBuckets = numBuckets;
        this.bucketSizeMillis = bucketSize.toMillis();
        this.buckets = new int[numBuckets];
    }

    public synchronized void updateRateLimit(int rateLimitCap, int rateRemaining, @Nullable String rateReset) {
        if (rateLimitCap > 0 && rateRemaining >= 0) {
            Instant updatedResetTime = parseRateReset(rateReset);
            boolean resetWindowChanged = !updatedResetTime.equals(rateResetTime);
            boolean remainingIncreased = this.rateRemaining >= 0 && rateRemaining > this.rateRemaining;
            this.rateLimitCap = rateLimitCap;
            this.rateRemaining = rateRemaining;
            rateLimitKnown = true;
            if (rateReset != null && !rateReset.isBlank()) {
                this.rateResetTime = updatedResetTime;
            }
            if (resetWindowChanged || remainingIncreased) {
                initializationBootstrapRemaining = calculateInitializationBootstrapAllowance();
            }
        }
        logRequest();
    }

    public synchronized boolean shouldThrottle(Priority priority) {
        try {
            tryThrottle(priority);
        } catch (RateLimitThrottleException e) {
            return true;
        }
        return false;
    }

    public synchronized void tryThrottle(Priority priority) throws RateLimitThrottleException {
        tryThrottle(priority, RequestPurpose.BACKGROUND_REFRESH);
    }

    public synchronized void tryThrottle(Priority priority, RequestPurpose requestPurpose)
            throws RateLimitThrottleException {
        if (priority == Priority.HIGH || !rateLimitKnown
                || (rateResetTime != Instant.MAX && System.currentTimeMillis() >= rateResetTime.toEpochMilli())) {
            return;
        }

        if (rateRemaining <= 0) {
            throw new RateLimitThrottleException(priority, requestPurpose, 0.0, currentRate());
        }

        if (requestPurpose == RequestPurpose.CORE_STATUS_POLL) {
            if (rateRemaining <= RACHIO_RATE_LIMIT_CRITICAL) {
                throw new RateLimitThrottleException(priority, requestPurpose, budgetRate(), currentRate());
            }
            // Only the minimal medium-priority person/current_schedule polling path may bypass the local average.
            // Optional enrichments remain LOW/BACKGROUND_REFRESH and are still throttled normally.
            if (priority == Priority.MEDIUM && rateRemaining >= RACHIO_RATE_LIMIT_WARNING) {
                return;
            }
        }

        if (rateResetTime == Instant.MAX) {
            return;
        }

        double budgetRate = budgetRate();
        double currentRate = currentRate();

        boolean throttle;
        switch (priority) {
            case HIGH:
                throttle = currentRate > budgetRate * 1.1;
                break;
            case MEDIUM:
                throttle = currentRate > budgetRate * 0.9;
                break;
            case LOW:
                throttle = currentRate > budgetRate * 0.8;
                break;
            default:
                throttle = true;
                break;
        }

        if (throttle) {
            if (requestPurpose == RequestPurpose.INITIALIZATION && useInitializationBootstrapAllowance()) {
                return;
            }
            throw new RateLimitThrottleException(priority, requestPurpose, budgetRate, currentRate);
        }
    }

    public synchronized int getRateLimitCap() {
        return rateLimitCap;
    }

    public synchronized int getRateRemaining() {
        return rateRemaining;
    }

    public synchronized String getRateResetAsString() {
        return rateResetTime == Instant.MAX ? "" : rateResetTime.toString();
    }

    public synchronized int getInitializationBootstrapRemaining() {
        return initializationBootstrapRemaining;
    }

    private int calculateInitializationBootstrapAllowance() {
        int available = Math.max(0, rateRemaining - INITIALIZATION_BOOTSTRAP_REMAINING_HEADROOM);
        if (available <= 0) {
            return 0;
        }

        int capBasedAllowance = Math.max(INITIALIZATION_BOOTSTRAP_BURST_MIN, rateLimitCap / 100);
        return Math.min(INITIALIZATION_BOOTSTRAP_BURST_MAX, Math.min(available, capBasedAllowance));
    }

    private boolean useInitializationBootstrapAllowance() {
        if (initializationBootstrapRemaining <= 0) {
            return false;
        }
        initializationBootstrapRemaining--;
        return true;
    }

    private void logRequest() {
        long now = System.currentTimeMillis();
        if (bucket0EndMillis == 0) {
            bucket0EndMillis = now + bucketSizeMillis;
        }

        if (now >= bucket0EndMillis) {
            int shiftBuckets = (int) ((now - bucket0EndMillis) / bucketSizeMillis) + 1;
            shift(shiftBuckets);
            bucket0EndMillis = bucket0EndMillis + bucketSizeMillis * shiftBuckets;
        }

        buckets[0]++;
        total = 0;
        for (int bucket : buckets) {
            total += bucket;
        }
    }

    private void shift(int n) {
        if (n <= 0) {
            return;
        }

        if (n >= numBuckets) {
            Arrays.fill(buckets, 0);
            total = 0;
            return;
        }

        for (int i = numBuckets - 1; i >= n; i--) {
            buckets[i] = buckets[i - n];
        }
        for (int i = 0; i < n; i++) {
            buckets[i] = 0;
        }

        total = 0;
        for (int bucket : buckets) {
            total += bucket;
        }
    }

    private double currentRate() {
        if (numBuckets <= 0 || bucketSizeMillis <= 0) {
            return 0.0;
        }
        return total / ((double) numBuckets * bucketSizeMillis) * 1000.0;
    }

    private double budgetRate() {
        long remainingMillis = rateResetTime.toEpochMilli() - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return Double.MAX_VALUE;
        }
        if (rateRemaining <= 0) {
            return 0.0;
        }
        return rateRemaining / (remainingMillis / 1000.0);
    }

    private Instant parseRateReset(@Nullable String rateReset) {
        if (rateReset == null || rateReset.isBlank()) {
            return Instant.MAX;
        }

        String resetTime = rateReset.trim();
        try {
            return Instant.ofEpochSecond(Long.parseLong(resetTime));
        } catch (DateTimeException | NumberFormatException e) {
            // ignore and try ISO format
        }

        try {
            return Instant.parse(resetTime);
        } catch (DateTimeParseException e) {
            // ignore and try RFC 1123 format
        }

        try {
            return ZonedDateTime.parse(resetTime, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (DateTimeParseException e) {
            return Instant.MAX;
        }
    }

    public static class RateLimitThrottleException extends Exception {
        private static final long serialVersionUID = 1L;

        public final Priority priority;
        public final RequestPurpose requestPurpose;
        public final double budgetRate;
        public final double currentRate;
        public final Duration suggestedRetryDelay;

        public RateLimitThrottleException(Priority priority, double budgetRate, double currentRate) {
            this(priority, RequestPurpose.BACKGROUND_REFRESH, budgetRate, currentRate);
        }

        public RateLimitThrottleException(Priority priority, RequestPurpose requestPurpose, double budgetRate,
                double currentRate) {
            super();
            this.priority = priority;
            this.requestPurpose = requestPurpose;
            this.budgetRate = budgetRate;
            this.currentRate = currentRate;
            this.suggestedRetryDelay = calculateSuggestedRetryDelay(priority, requestPurpose, budgetRate);
        }

        private Duration calculateSuggestedRetryDelay(Priority priority, RequestPurpose requestPurpose,
                double budgetRate) {
            if (requestPurpose == RequestPurpose.INITIALIZATION) {
                return budgetRate <= 0 ? Duration.ofSeconds(30) : Duration.ofSeconds(10);
            }
            return switch (priority) {
                case VERY_LOW -> Duration.ofSeconds(60);
                case LOW -> Duration.ofSeconds(30);
                case MEDIUM -> Duration.ofSeconds(15);
                case HIGH -> Duration.ZERO;
            };
        }

        @Override
        public String toString() {
            return String.format(
                    "Throttling REST API call with priority %s (budgeted rate: %.3f, running average rate: %.3f)",
                    priority.toString(), budgetRate, currentRate);
        }
    }
}
