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
 * The liveness window is sized from the heartbeat the charger actually uses. Precedence is Thing
 * override, then the charger's reported {@code HeartbeatInterval} (read via GetConfiguration), then the
 * server default — never below the floor.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class OcppLivenessThresholdTest {

    @Test
    void anExplicitThingOverrideWins() {
        // 2*120+60, ignoring the reported heartbeat and the server default entirely.
        assertEquals(300, OcppChargePointHandler.livenessThreshold(120, OptionalInt.of(10), 300));
    }

    @Test
    void theReportedHeartbeatIsUsedWhenThereIsNoOverride() {
        // The real Wallbox case: it heartbeats every 10 s, so the window collapses to the 180 s floor
        // instead of the 660 s the server default (300) would have imposed.
        assertEquals(180, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(10), 300));
    }

    @Test
    void aSlowReportedHeartbeatWidensTheWindow() {
        // The real CHARX case: heartbeats every 300 s → 2*300+60.
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(300), 300));
    }

    @Test
    void anAbsentReportedHeartbeatFallsBackToTheServerDefault() {
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.empty(), 300));
    }

    @Test
    void aReportedZeroIsNotUsedToReapAnIdleNonHeartbeatingCharger() {
        // HeartbeatInterval 0 means the charger sends no periodic heartbeat, so it gives no useful
        // "how often it speaks" signal — fall back to the server default rather than the tight floor,
        // or a silent-but-healthy charger would be recycled every window.
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(0), 300));
    }

    @Test
    void theFloorIsNeverBreached() {
        assertEquals(180, OcppChargePointHandler.livenessThreshold(0, OptionalInt.of(5), 0));
    }

    @Test
    void everythingUnsetStillYieldsASaneWindow() {
        // No override, nothing reported, no server default: a 300 s fallback, so 2*300+60.
        assertEquals(660, OcppChargePointHandler.livenessThreshold(0, OptionalInt.empty(), 0));
    }
}
