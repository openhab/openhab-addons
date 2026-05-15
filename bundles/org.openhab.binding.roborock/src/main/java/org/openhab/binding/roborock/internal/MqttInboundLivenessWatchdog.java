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

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tracks cloud MQTT inbound/outbound activity and determines whether the MQTT client should be recycled.
 * This exists because the Roborock cloud session may appear connected while inbound MQTT delivery silently stalls;
 * recycling the client is used as a recovery mitigation when outbound traffic is active but inbound stays silent.
 *
 * @author reyhard - Initial contribution
 */
@NonNullByDefault
final class MqttInboundLivenessWatchdog {

    enum Decision {
        NOT_CONNECTED,
        IDLE,
        HEALTHY,
        COOLDOWN,
        STALE
    }

    private final Duration activeUsageWindow;
    private final Duration inboundSilenceThreshold;
    private final Duration recycleCooldown;

    private volatile @Nullable Instant lastInboundMessageAt;
    private volatile @Nullable Instant lastOutboundPublishAt;
    private volatile @Nullable Instant lastRecycleAttemptAt;

    MqttInboundLivenessWatchdog(Duration activeUsageWindow, Duration inboundSilenceThreshold,
            Duration recycleCooldown) {
        this.activeUsageWindow = activeUsageWindow;
        this.inboundSilenceThreshold = inboundSilenceThreshold;
        this.recycleCooldown = recycleCooldown;
    }

    void reset(Instant now) {
        lastInboundMessageAt = now;
        lastOutboundPublishAt = null;
        lastRecycleAttemptAt = null;
    }

    void noteInboundMessage(Instant now) {
        lastInboundMessageAt = now;
    }

    void noteOutboundPublish(Instant now) {
        lastOutboundPublishAt = now;
    }

    void noteRecycleAttempt(Instant now) {
        lastRecycleAttemptAt = now;
    }

    @Nullable
    Instant getLastInboundMessageAt() {
        return lastInboundMessageAt;
    }

    Decision evaluate(Instant now, boolean mqttConnected) {
        if (!mqttConnected) {
            return Decision.NOT_CONNECTED;
        }

        Instant outboundAt = lastOutboundPublishAt;
        if (outboundAt == null || Duration.between(outboundAt, now).compareTo(activeUsageWindow) > 0) {
            return Decision.IDLE;
        }

        Instant recycleAt = lastRecycleAttemptAt;
        if (recycleAt != null && Duration.between(recycleAt, now).compareTo(recycleCooldown) <= 0) {
            return Decision.COOLDOWN;
        }

        Instant inboundAt = lastInboundMessageAt;
        if (inboundAt == null || Duration.between(inboundAt, now).compareTo(inboundSilenceThreshold) > 0) {
            return Decision.STALE;
        }

        return Decision.HEALTHY;
    }
}
