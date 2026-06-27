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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;

/**
 * Tests local client-side rate limit decisions.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class ClientRateLimitManagerTest {
    @Test
    void lowPriorityRequestEmitsDistinctThrottleExceptionWhenLocalBudgetIsExceeded() {
        ClientRateLimitManager manager = new ClientRateLimitManager(1, Duration.ofSeconds(60));
        manager.updateRateLimit(100, 1, Long.toString(Instant.now().plusSeconds(3600).getEpochSecond()));

        RateLimitThrottleException exception = assertThrows(RateLimitThrottleException.class,
                () -> manager.tryThrottle(PRIORITY.LOW));

        assertThat(exception.priority, is(PRIORITY.LOW));
    }

    @Test
    void coreStatusPollBypassesLocalAverageOnlyWhenServerHeadroomIsHealthy() {
        ClientRateLimitManager manager = new ClientRateLimitManager(1, Duration.ofSeconds(60));
        manager.updateRateLimit(3500, 3000, Long.toString(Instant.now().plusSeconds(7 * 24 * 3600).getEpochSecond()));

        assertThrows(RateLimitThrottleException.class,
                () -> manager.tryThrottle(PRIORITY.MED, RequestPurpose.BACKGROUND_REFRESH));
        assertThrows(RateLimitThrottleException.class,
                () -> manager.tryThrottle(PRIORITY.LOW, RequestPurpose.BACKGROUND_REFRESH));
        assertDoesNotThrow(() -> manager.tryThrottle(PRIORITY.MED, RequestPurpose.CORE_STATUS_POLL));
    }

    @Test
    void coreStatusPollStillRespectsCriticalAndExhaustedServerLimits() {
        ClientRateLimitManager criticalManager = new ClientRateLimitManager(1, Duration.ofSeconds(60));
        criticalManager.updateRateLimit(3500, 100, Long.toString(Instant.now().plusSeconds(3600).getEpochSecond()));
        ClientRateLimitManager exhaustedManager = new ClientRateLimitManager(1, Duration.ofSeconds(60));
        exhaustedManager.updateRateLimit(3500, 0, Long.toString(Instant.now().plusSeconds(3600).getEpochSecond()));

        RateLimitThrottleException critical = assertThrows(RateLimitThrottleException.class,
                () -> criticalManager.tryThrottle(PRIORITY.MED, RequestPurpose.CORE_STATUS_POLL));
        RateLimitThrottleException exhausted = assertThrows(RateLimitThrottleException.class,
                () -> exhaustedManager.tryThrottle(PRIORITY.MED, RequestPurpose.CORE_STATUS_POLL));

        assertThat(critical.requestPurpose, is(RequestPurpose.CORE_STATUS_POLL));
        assertThat(exhausted.requestPurpose, is(RequestPurpose.CORE_STATUS_POLL));
    }

    @Test
    void initializationRequestUsesBoundedBootstrapAllowanceWhenBackgroundRequestWouldThrottle() {
        ClientRateLimitManager manager = new ClientRateLimitManager(1, Duration.ofSeconds(60));
        manager.updateRateLimit(100, 50, Long.toString(Instant.now().plusSeconds(3600).getEpochSecond()));

        RateLimitThrottleException backgroundException = assertThrows(RateLimitThrottleException.class,
                () -> manager.tryThrottle(PRIORITY.MED, RequestPurpose.BACKGROUND_REFRESH));

        assertThat(backgroundException.requestPurpose, is(RequestPurpose.BACKGROUND_REFRESH));
        assertDoesNotThrow(() -> manager.tryThrottle(PRIORITY.MED, RequestPurpose.INITIALIZATION));
        assertThat(manager.getInitializationBootstrapRemaining(), is(4));
    }

    @Test
    void initializationBootstrapAllowanceIsBoundedAndThenReturnsInitializationThrottle() {
        ClientRateLimitManager manager = new ClientRateLimitManager(1, Duration.ofSeconds(60));
        manager.updateRateLimit(100, 50, Long.toString(Instant.now().plusSeconds(3600).getEpochSecond()));

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> manager.tryThrottle(PRIORITY.MED, RequestPurpose.INITIALIZATION));
        }

        RateLimitThrottleException exception = assertThrows(RateLimitThrottleException.class,
                () -> manager.tryThrottle(PRIORITY.MED, RequestPurpose.INITIALIZATION));

        assertThat(exception.priority, is(PRIORITY.MED));
        assertThat(exception.requestPurpose, is(RequestPurpose.INITIALIZATION));
        assertThat(exception.suggestedRetryDelay.getSeconds(), is(10L));
    }
}
