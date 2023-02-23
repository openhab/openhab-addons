/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;

/**
 * Test class for (@link LxControlJalousie}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlJalousieTest extends LxControlTest {

    private static final String ROLLERSHUTTER_CHANNEL = null;
    private static final String SHADE_CHANNEL = " / Shade";
    private static final String AUTO_SHADE_CHANNEL = " / Auto Shade";

    @BeforeEach
    public void setup() {
        setupControl("0e367c09-0161-e2c1-ffff403fb0c34b9e", "0e368d32-014f-4604-ffff403fb0c34b9e",
                "0b734138-033e-02d8-ffff403fb0c34b9e", "Window Blinds");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlJalousie.class, 1, 0, 3, 3, 11);
    }

    @Test
    public void testChannels() {
        testChannel("Rollershutter", Collections.singleton("Blinds"));
        Set<String> tags = Collections.singleton("Switchable");
        testChannel("Switch", SHADE_CHANNEL, tags);
        testChannel("Switch", AUTO_SHADE_CHANNEL, tags);
    }

    @Test
    public void testLoxonePositionAutoShadeStates() {
        boolean a = false;
        testChannelState(ROLLERSHUTTER_CHANNEL, null);
        testChannelState(SHADE_CHANNEL, OnOffType.OFF);
        testChannelState(AUTO_SHADE_CHANNEL, null);
        for (int i = 0; i <= 100; i++) {
            changeLoxoneState("position", i / 100.0);
            testChannelState(ROLLERSHUTTER_CHANNEL, new PercentType(i));
            testChannelState(SHADE_CHANNEL, OnOffType.OFF);
            changeLoxoneState("autoactive", a ? 1.0 : 0.0);
            testChannelState(AUTO_SHADE_CHANNEL, a ? OnOffType.ON : OnOffType.OFF);
            a = !a;
        }
        changeLoxoneState("position", 100.1);
        testChannelState(ROLLERSHUTTER_CHANNEL, null);
        changeLoxoneState("position", -0.1);
        testChannelState(ROLLERSHUTTER_CHANNEL, null);
    }

    @Test
    public void testCommands() {
        for (int i = 0; i < 20; i++) {
            executeCommand(SHADE_CHANNEL, OnOffType.ON);
            testAction("shade");
            executeCommand(SHADE_CHANNEL, OnOffType.OFF);
            testAction(null);
            executeCommand(SHADE_CHANNEL, DecimalType.ZERO);
            testAction(null);
            executeCommand(AUTO_SHADE_CHANNEL, OnOffType.ON);
            testAction("auto");
            executeCommand(AUTO_SHADE_CHANNEL, OnOffType.OFF);
            testAction("NoAuto");
            executeCommand(AUTO_SHADE_CHANNEL, DecimalType.ZERO);
            testAction(null);
            executeCommand(ROLLERSHUTTER_CHANNEL, UpDownType.UP);
            testAction("FullUp");
            executeCommand(ROLLERSHUTTER_CHANNEL, UpDownType.DOWN);
            testAction("FullDown");
            executeCommand(ROLLERSHUTTER_CHANNEL, StopMoveType.STOP);
            testAction("Stop");
            executeCommand(ROLLERSHUTTER_CHANNEL, StopMoveType.MOVE);
            testAction(null);
        }
    }

    @Test
    public void testMovingToPosition() {
        changeLoxoneState("position", 0.1);
        testChannelState(ROLLERSHUTTER_CHANNEL, new PercentType(10));
        executeCommand(ROLLERSHUTTER_CHANNEL, new PercentType(73));
        testAction("FullDown");
        changeLoxoneState("up", 0.0);
        changeLoxoneState("down", 1.0);
        for (int i = 10; i <= 72; i++) {
            changeLoxoneState("position", i / 100.0);
            testChannelState(ROLLERSHUTTER_CHANNEL, new PercentType(i));
        }
        changeLoxoneState("position", 0.73);
        testAction("Stop");
        changeLoxoneState("position", 0.74);
        testAction(null);
        changeLoxoneState("up", 0.0);
        changeLoxoneState("down", 0.0);
        executeCommand(ROLLERSHUTTER_CHANNEL, new PercentType(10));
        testAction("FullUp");
        changeLoxoneState("up", 1.0);
        changeLoxoneState("down", 0.0);
        for (int i = 74; i >= 11; i--) {
            changeLoxoneState("position", i / 100.0);
            testChannelState(ROLLERSHUTTER_CHANNEL, new PercentType(i));
        }
        changeLoxoneState("position", 0.10);
        testAction("Stop");
        changeLoxoneState("position", 0.09);
        testAction(null);

        executeCommand(ROLLERSHUTTER_CHANNEL, new PercentType(50));
        testAction("FullDown");
        changeLoxoneState("up", 0.0);
        changeLoxoneState("down", 1.0);
        changeLoxoneState("position", 0.80);
        testAction("Stop");
        changeLoxoneState("position", 0.50);
        testAction(null);

        executeCommand(ROLLERSHUTTER_CHANNEL, new PercentType(90));
        testAction("FullDown");
        changeLoxoneState("up", 0.0);
        changeLoxoneState("down", 0.0);
        changeLoxoneState("position", 0.95);
        testAction(null);
        changeLoxoneState("position", 0.85);
        testAction(null);

        changeLoxoneState("down", 1.0);
        changeLoxoneState("position", 0.85);
        testAction(null);
        changeLoxoneState("position", 0.95);
        testAction("Stop");
        changeLoxoneState("position", 0.85);
        testAction(null);
        changeLoxoneState("position", 0.95);
        testAction(null);
        changeLoxoneState("down", 0.0);

        executeCommand(ROLLERSHUTTER_CHANNEL, new PercentType(30));
        testAction("FullUp");
        changeLoxoneState("up", 1.0);
        changeLoxoneState("down", 0.0);
        changeLoxoneState("position", 0.40);
        testAction(null);
        changeLoxoneState("position", 0.20);
        testAction("Stop");
        changeLoxoneState("position", 0.40);
        testAction(null);
        changeLoxoneState("position", 0.20);
        testAction(null);
        changeLoxoneState("up", 0.0);

        executeCommand(ROLLERSHUTTER_CHANNEL, PercentType.HUNDRED);
        testAction("FullDown");
        changeLoxoneState("up", 0.0);
        changeLoxoneState("down", 1.0);
        changeLoxoneState("position", 0.80);
        testAction(null);
        changeLoxoneState("position", 1.00);
        testAction(null);
        changeLoxoneState("down", 0.0);

        executeCommand(ROLLERSHUTTER_CHANNEL, PercentType.ZERO);
        testAction("FullUp");
        changeLoxoneState("up", 1.0);
        changeLoxoneState("down", 0.0);
        changeLoxoneState("position", 0.20);
        testAction(null);
        changeLoxoneState("position", 0.00);
        testAction(null);
        changeLoxoneState("up", 0.0);
    }
}
