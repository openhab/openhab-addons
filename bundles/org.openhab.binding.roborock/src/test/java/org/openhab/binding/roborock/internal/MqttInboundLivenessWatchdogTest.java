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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.roborock.internal.MqttInboundLivenessWatchdog.Decision;

@NonNullByDefault({})
class MqttInboundLivenessWatchdogTest {

    @Test
    void noteInboundMessageUpdatesInboundTimestamp() {
        MqttInboundLivenessWatchdog watchdog = new MqttInboundLivenessWatchdog(Duration.ofMinutes(2),
                Duration.ofMinutes(3), Duration.ofMinutes(1));
        Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t1 = t0.plusSeconds(42);

        assertNull(watchdog.getLastInboundMessageAt());
        watchdog.noteInboundMessage(t1);

        assertEquals(t1, watchdog.getLastInboundMessageAt());
    }

    @Test
    void evaluateReturnsIdleWhenConnectedButNoRecentOutboundUsage() {
        MqttInboundLivenessWatchdog watchdog = new MqttInboundLivenessWatchdog(Duration.ofMinutes(2),
                Duration.ofMinutes(3), Duration.ofMinutes(1));
        Instant t0 = Instant.parse("2026-01-01T00:00:00Z");

        watchdog.reset(t0);

        assertEquals(Decision.IDLE, watchdog.evaluate(t0.plusSeconds(30), true));
        assertEquals(Decision.NOT_CONNECTED, watchdog.evaluate(t0.plusSeconds(30), false));
    }

    @Test
    void evaluateReturnsStaleForInboundSilenceDuringActiveUsageAndCooldownAfterRecycleAttempt() {
        MqttInboundLivenessWatchdog watchdog = new MqttInboundLivenessWatchdog(Duration.ofMinutes(10),
                Duration.ofMinutes(2), Duration.ofMinutes(1));
        Instant t0 = Instant.parse("2026-01-01T00:00:00Z");

        watchdog.reset(t0);
        watchdog.noteOutboundPublish(t0.plusSeconds(30));

        Instant staleCheck = t0.plusSeconds(181);
        assertEquals(Decision.STALE, watchdog.evaluate(staleCheck, true));

        watchdog.noteRecycleAttempt(staleCheck);
        assertEquals(Decision.COOLDOWN, watchdog.evaluate(staleCheck.plusSeconds(30), true));
    }
}
