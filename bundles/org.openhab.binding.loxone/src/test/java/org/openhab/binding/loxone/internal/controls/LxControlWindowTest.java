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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;

/**
 * Test class for {@link LxControlWindow}
 *
 * @author Robert Pollai - Initial contribution
 *
 */
public class LxControlWindowTest extends LxControlTest {

    private static final String WINDOW_CHANNEL = null;

    @BeforeEach
    public void setup() {
        setupControl("0e367c09-0161-e2c1-ffff403fb0c34b9f", "0e368d32-014f-4604-ffff403fb0c34b9e",
                "0b734138-033e-02d8-ffff403fb0c34b9e", "Window");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlWindow.class, 1, 0, 1, 1, 4);
    }

    @Test
    public void testChannels() {
        testChannel("Rollershutter");
    }

    @Test
    public void testStates() {
        testChannelState(WINDOW_CHANNEL, null);
        changeLoxoneState("position", 1.0);
        testChannelState(WINDOW_CHANNEL, PercentType.ZERO);
        changeLoxoneState("position", 0.0);
        testChannelState(WINDOW_CHANNEL, PercentType.HUNDRED);
        changeLoxoneState("position", 0.29);
        testChannelState(WINDOW_CHANNEL, new PercentType(71));
        changeLoxoneState("direction", 1.0);
        testChannelState(WINDOW_CHANNEL, new PercentType(71));
        changeLoxoneState("targetposition", 1.0);
        testChannelState(WINDOW_CHANNEL, new PercentType(71));
        changeLoxoneState("position", 1.1);
        testChannelState(WINDOW_CHANNEL, null);
        changeLoxoneState("position", -0.1);
        testChannelState(WINDOW_CHANNEL, null);
    }

    @Test
    public void testCommands() {
        executeCommand(WINDOW_CHANNEL, UpDownType.UP);
        testAction("fullopen");
        executeCommand(WINDOW_CHANNEL, UpDownType.DOWN);
        testAction("fullclose");
        executeCommand(WINDOW_CHANNEL, StopMoveType.STOP);
        testAction("stop");
        executeCommand(WINDOW_CHANNEL, StopMoveType.MOVE);
        testAction(null);
        executeCommand(WINDOW_CHANNEL, PercentType.ZERO);
        testAction("fullopen");
        executeCommand(WINDOW_CHANNEL, PercentType.HUNDRED);
        testAction("fullclose");
        executeCommand(WINDOW_CHANNEL, new PercentType(30));
        testAction("moveToPosition/70");
        executeCommand(WINDOW_CHANNEL, new PercentType(1));
        testAction("moveToPosition/99");
        executeCommand(WINDOW_CHANNEL, new PercentType(99));
        testAction("moveToPosition/1");
        executeCommand(WINDOW_CHANNEL, DecimalType.ZERO);
        testAction(null);
    }

    @Test
    public void testInvertedStatesAndCommands() {
        setInverted(true);
        changeLoxoneState("position", 0.0);
        testChannelState(WINDOW_CHANNEL, PercentType.ZERO);
        changeLoxoneState("position", 0.25);
        testChannelState(WINDOW_CHANNEL, new PercentType(25));
        changeLoxoneState("position", 1.0);
        testChannelState(WINDOW_CHANNEL, PercentType.HUNDRED);
        executeCommand(WINDOW_CHANNEL, PercentType.ZERO);
        testAction("fullclose");
        executeCommand(WINDOW_CHANNEL, PercentType.HUNDRED);
        testAction("fullopen");
        executeCommand(WINDOW_CHANNEL, new PercentType(30));
        testAction("moveToPosition/30");
        executeCommand(WINDOW_CHANNEL, UpDownType.UP);
        testAction("fullclose");
        executeCommand(WINDOW_CHANNEL, UpDownType.DOWN);
        testAction("fullopen");
        executeCommand(WINDOW_CHANNEL, StopMoveType.STOP);
        testAction("stop");
    }

    @Test
    public void testInvertedConfigurationFromString() {
        Configuration configuration = new Configuration();
        configuration.put("inverted", "true");
        setChannelConfiguration(WINDOW_CHANNEL, configuration);
        changeLoxoneState("position", 0.4);
        testChannelState(WINDOW_CHANNEL, new PercentType(40));
    }

    @Test
    public void testConfigurationUpdateTakesEffect() {
        changeLoxoneState("position", 0.25);
        testChannelState(WINDOW_CHANNEL, new PercentType(75));
        setInverted(true);
        testChannelState(WINDOW_CHANNEL, new PercentType(25));
        setInverted(false);
        testChannelState(WINDOW_CHANNEL, new PercentType(75));
    }

    private void setInverted(boolean inverted) {
        Configuration configuration = new Configuration();
        configuration.put("inverted", inverted);
        setChannelConfiguration(WINDOW_CHANNEL, configuration);
    }
}
