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
package org.openhab.binding.loxone.internal.controls;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link LxControlGate}.
 *
 * @author Robert Pollai - Initial contribution
 */
@NonNullByDefault
class LxControlGateTest extends LxControlTest {

    private static final String MOVEMENT_CHANNEL = " / Movement";
    private static final String PREVENT_OPEN_CHANNEL = " / Opening Prevented";
    private static final String PREVENT_CLOSE_CHANNEL = " / Closing Prevented";
    private static final String PARTIALLY_OPEN_CHANNEL = " / Partially Open";

    @BeforeEach
    void setup() {
        setupControl("0e367c09-0161-e2c2-ffff403fb0c34b9e", "0e368d32-014f-4604-ffff403fb0c34b9e",
                "0b734138-033e-02d8-ffff403fb0c34b9e", "Garage Door");
    }

    @Test
    void testControlCreationAndChannels() {
        testControlCreation(LxControlGate.class, 1, 0, 5, 5, 4);
        testChannel("Rollershutter");
        testChannel("String", MOVEMENT_CHANNEL);
        testChannel("Switch", PREVENT_OPEN_CHANNEL);
        testChannel("Switch", PREVENT_CLOSE_CHANNEL);
        testChannel("Switch", PARTIALLY_OPEN_CHANNEL);
        LxControl gate = handler.controls.get(controlUuid);
        assertNotNull(gate);
        for (int i = 0; i < 5; i++) {
            assertEquals(controlUuid + (i == 0 ? "" : "-" + i), gate.getChannels().get(i).getUID().getId());
        }
    }

    @Test
    void testPositionFeedback() {
        testChannelState(UnDefType.UNDEF);
        for (int i = 0; i <= 100; i++) {
            changeLoxoneState("position", i / 100.0);
            testChannelState(new PercentType(100 - i));
        }
        changeLoxoneState("position", 0.2949);
        testChannelState(new PercentType(71));
        changeLoxoneState("position", 0.2951);
        testChannelState(new PercentType(70));
        for (Object value : new Object[] { -0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                "invalid" }) {
            changeLoxoneState("position", value);
            testChannelState(UnDefType.UNDEF);
        }
        changeLoxoneState("position", 1.0);
        testChannelState(PercentType.ZERO);
        testAction(null);
    }

    @Test
    void testMovementFeedback() {
        testChannelState(MOVEMENT_CHANNEL, UnDefType.UNDEF);
        changeLoxoneState("active", 1.0);
        testChannelState(MOVEMENT_CHANNEL, new StringType("OPENING"));
        changeLoxoneState("active", -1.0);
        testChannelState(MOVEMENT_CHANNEL, new StringType("CLOSING"));
        changeLoxoneState("active", 0.0);
        testChannelState(MOVEMENT_CHANNEL, new StringType("STOPPED"));
        for (Object value : new Object[] { 0.5, 2.0, Double.NaN, "invalid" }) {
            changeLoxoneState("active", value);
            testChannelState(MOVEMENT_CHANNEL, UnDefType.UNDEF);
        }
        changeLoxoneState("active", 1.0);
        testChannelState(MOVEMENT_CHANNEL, new StringType("OPENING"));
        testChannelState(UnDefType.UNDEF);
        testAction(null);
    }

    @Test
    void testPreventionFeedback() {
        testChannelState(PREVENT_OPEN_CHANNEL, UnDefType.UNDEF);
        testChannelState(PREVENT_CLOSE_CHANNEL, UnDefType.UNDEF);
        changeLoxoneState("preventopen", 1.0);
        changeLoxoneState("preventclose", 0.0);
        testChannelState(PREVENT_OPEN_CHANNEL, OnOffType.ON);
        testChannelState(PREVENT_CLOSE_CHANNEL, OnOffType.OFF);
        changeLoxoneState("preventopen", 0.0);
        changeLoxoneState("preventclose", 1.0);
        testChannelState(PREVENT_OPEN_CHANNEL, OnOffType.OFF);
        testChannelState(PREVENT_CLOSE_CHANNEL, OnOffType.ON);
        for (Object value : new Object[] { -1.0, 0.5, 2.0, Double.NaN, "invalid" }) {
            changeLoxoneState("preventopen", value);
            changeLoxoneState("preventclose", value);
            testChannelState(PREVENT_OPEN_CHANNEL, UnDefType.UNDEF);
            testChannelState(PREVENT_CLOSE_CHANNEL, UnDefType.UNDEF);
        }
        changeLoxoneState("preventopen", 0.0);
        changeLoxoneState("preventclose", 0.0);
        testChannelState(PREVENT_OPEN_CHANNEL, OnOffType.OFF);
        testChannelState(PREVENT_CLOSE_CHANNEL, OnOffType.OFF);
        testAction(null);
    }

    @Test
    void testDirectionalCommands() {
        for (double active : new double[] { -1.0, 0.0, 1.0 }) {
            changeLoxoneState("active", active);
            executeCommand(UpDownType.UP);
            testAction("forceOpen");
            executeCommand(UpDownType.DOWN);
            testAction("forceClose");
            executeCommand(StopMoveType.STOP);
            testAction("stop");
            executeCommand(PercentType.ZERO);
            testAction("forceOpen");
            executeCommand(PercentType.HUNDRED);
            testAction("forceClose");
        }
        testChannelState(UnDefType.UNDEF);
    }

    @Test
    void testUnsupportedTargetsAndCommands() {
        for (String percent : new String[] { "0.1", "1", "50", "99", "99.9" }) {
            executeCommand(new PercentType(percent));
            testAction(null);
        }
        executeCommand(StopMoveType.MOVE);
        executeCommand(DecimalType.ZERO);
        executeCommand(RefreshType.REFRESH);
        executeCommand(MOVEMENT_CHANNEL, new StringType("OPENING"));
        executeCommand(PREVENT_OPEN_CHANNEL, OnOffType.OFF);
        executeCommand(PREVENT_CLOSE_CHANNEL, OnOffType.OFF);
        testAction(null);
    }

    @Test
    void testPartiallyOpenIsMomentary() {
        changeLoxoneState("position", 0.4);
        testChannelState(PARTIALLY_OPEN_CHANNEL, OnOffType.OFF);
        for (int i = 0; i < 2; i++) {
            executeCommand(PARTIALLY_OPEN_CHANNEL, OnOffType.ON);
            testAction("PartiallyOpen");
            testChannelState(PARTIALLY_OPEN_CHANNEL, OnOffType.OFF);
            testChannelState(new PercentType(60));
        }
        executeCommand(PARTIALLY_OPEN_CHANNEL, OnOffType.OFF);
        executeCommand(PARTIALLY_OPEN_CHANNEL, DecimalType.ZERO);
        executeCommand(PARTIALLY_OPEN_CHANNEL, RefreshType.REFRESH);
        testAction(null);
        changeLoxoneState("position", 0.2);
        testChannelState(PARTIALLY_OPEN_CHANNEL, OnOffType.OFF);
        testChannelState(new PercentType(80));
        testAction(null);
    }
}
