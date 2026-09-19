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
package org.openhab.binding.ocpp.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalInt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The liveness window is {@code max(180 floor, 2*heartbeat + 60)}, the heartbeat taken from the Thing
 * override, else the charger's reported {@code HeartbeatInterval}, else the server default.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class OcppLivenessThresholdTest {

    @Test
    void anExplicitThingOverrideWins() {
        assertEquals(300, OcppChargePointHandler.livenessThreshold(120, OptionalInt.of(10), 300));
    }

    @Test
    void aStaleReportedHeartbeatDoesNotShrinkBelowTheNegotiatedInterval() {
        // Charger reports a small/stale 10s but was negotiated the 300s server default: size from 300, not 10, so a
        // healthy charger beating every 300s is not reaped.
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(10), 300));
    }

    @Test
    void theReportedHeartbeatIsUsedWhenNothingWasNegotiated() {
        // No override and server default 0 means the charger keeps its own interval, so size from what it reports.
        assertEquals(180, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(10), 0));
    }

    @Test
    void aSlowReportedHeartbeatWidensTheWindow() {
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(300), 300));
    }

    @Test
    void anAbsentReportedHeartbeatFallsBackToTheServerDefault() {
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.empty(), 300));
    }

    @Test
    void aReportedZeroIsNotUsedToReapAnIdleNonHeartbeatingCharger() {
        // HeartbeatInterval 0 = no periodic heartbeat (no liveness signal): fall back to server default, not the floor.
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(0), 300));
    }

    @Test
    void theFloorIsNeverBreached() {
        assertEquals(180, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(5), 0));
    }

    @Test
    void everythingUnsetStillYieldsASaneWindow() {
        // Nothing set and server default 0 falls back to a 300 s heartbeat, so 2*300+60.
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.empty(), 0));
    }
}
