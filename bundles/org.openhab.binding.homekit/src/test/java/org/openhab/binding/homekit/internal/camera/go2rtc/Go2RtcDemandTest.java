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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Go2RtcDemand}. Times are explicit so no test depends on the wall clock.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class Go2RtcDemandTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void opensWhenWantedAndClosed() {
        Go2RtcDemand demand = new Go2RtcDemand();
        assertEquals(Go2RtcDemand.Action.OPEN, demand.update(true, false, T0));
    }

    @Test
    void doesNothingWithoutDemandWhenClosed() {
        Go2RtcDemand demand = new Go2RtcDemand();
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, false, T0));
    }

    @Test
    void doesNothingWhileWantedAndOpen() {
        Go2RtcDemand demand = new Go2RtcDemand();
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(true, true, T0));
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(true, true, T0.plusSeconds(600)));
    }

    @Test
    void closesOnlyAfterGracePeriod() {
        Go2RtcDemand demand = new Go2RtcDemand();
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, true, T0));
        assertEquals(Go2RtcDemand.Action.NONE,
                demand.update(false, true, T0.plus(Go2RtcDemand.CLOSE_GRACE).minusSeconds(1)));
        assertEquals(Go2RtcDemand.Action.CLOSE, demand.update(false, true, T0.plus(Go2RtcDemand.CLOSE_GRACE)));
    }

    @Test
    void returningDemandCancelsPendingClose() {
        Go2RtcDemand demand = new Go2RtcDemand();
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, true, T0));
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(true, true, T0.plusSeconds(5)));
        // the grace period restarts from the next time demand disappears
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, true, T0.plusSeconds(6)));
        assertEquals(Go2RtcDemand.Action.NONE,
                demand.update(false, true, T0.plusSeconds(6).plus(Go2RtcDemand.CLOSE_GRACE).minusSeconds(1)));
        assertEquals(Go2RtcDemand.Action.CLOSE,
                demand.update(false, true, T0.plusSeconds(6).plus(Go2RtcDemand.CLOSE_GRACE)));
    }

    @Test
    void failedOpenIsNotRetriedBeforeDelay() {
        Go2RtcDemand demand = new Go2RtcDemand();
        assertEquals(Go2RtcDemand.Action.OPEN, demand.update(true, false, T0));
        demand.openFailed(T0);
        assertEquals(Go2RtcDemand.Action.NONE,
                demand.update(true, false, T0.plus(Go2RtcDemand.OPEN_RETRY_DELAY).minusSeconds(1)));
        assertEquals(Go2RtcDemand.Action.OPEN, demand.update(true, false, T0.plus(Go2RtcDemand.OPEN_RETRY_DELAY)));
    }

    @Test
    void resetClearsRetryDelay() {
        Go2RtcDemand demand = new Go2RtcDemand();
        demand.openFailed(T0);
        demand.reset();
        assertEquals(Go2RtcDemand.Action.OPEN, demand.update(true, false, T0));
    }

    @Test
    void holdOpensWithoutConsumersUntilExpired() {
        Go2RtcDemand demand = new Go2RtcDemand();
        demand.hold(T0.plusSeconds(30));
        assertEquals(Go2RtcDemand.Action.OPEN, demand.update(false, false, T0));
    }

    @Test
    void holdKeepsSessionOpenThenClosesAfterGrace() {
        Go2RtcDemand demand = new Go2RtcDemand();
        Instant holdEnd = T0.plusSeconds(30);
        demand.hold(holdEnd);
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, true, T0));
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, true, holdEnd));
        assertEquals(Go2RtcDemand.Action.NONE,
                demand.update(false, true, holdEnd.plus(Go2RtcDemand.CLOSE_GRACE).minusSeconds(1)));
        assertEquals(Go2RtcDemand.Action.CLOSE, demand.update(false, true, holdEnd.plus(Go2RtcDemand.CLOSE_GRACE)));
    }

    @Test
    void resetClearsHold() {
        Go2RtcDemand demand = new Go2RtcDemand();
        demand.hold(T0.plusSeconds(30));
        demand.reset();
        assertEquals(Go2RtcDemand.Action.NONE, demand.update(false, false, T0));
    }
}
