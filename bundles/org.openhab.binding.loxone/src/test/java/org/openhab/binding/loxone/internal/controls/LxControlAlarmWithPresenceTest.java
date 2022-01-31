/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for (@link LxControlAlarm} - version with motion sensors
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlAlarmWithPresenceTest extends LxControlAlarmNoPresenceTest {
    private static final String MOTION_SENSORS_CHANNEL = " / Motion Sensors";

    @Override
    @BeforeEach
    public void setup() {
        setupControl("133d5db0-0333-5865-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Burglar Alarm With Presence");
    }

    @Override
    @Test
    public void testControlCreation() {
        testControlCreation(LxControlAlarm.class, 2, 1, 12, 13, 10);
    }

    @Override
    @Test
    public void testChannels() {
        super.testChannels();
        testChannel("Switch", MOTION_SENSORS_CHANNEL);
    }

    @Override
    @Test
    public void testCommandsDefaultChannel() {
        testAction(null);
        for (int i = 0; i < 20; i++) {
            changeLoxoneState("disabledmove", 0.0);
            executeCommand(OnOffType.ON);
            testAction("on/1");
            executeCommand(OnOffType.OFF);
            testAction("off");
            changeLoxoneState("disabledmove", 1.0);
            executeCommand(OnOffType.ON);
            testAction("on/0");
            executeCommand(OnOffType.OFF);
            testAction("off");
        }
    }

    @Override
    @Test
    public void testCommandsArmWithDelayChannel() {
        testAction(null);
        for (int i = 0; i < 20; i++) {
            changeLoxoneState("disabledmove", 0.0);
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.ON);
            testAction("delayedon/1");
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.OFF);
            testAction("off");
            changeLoxoneState("disabledmove", 1.0);
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.ON);
            testAction("delayedon/0");
            executeCommand(ARM_DELAYED_CHANNEL, OnOffType.OFF);
            testAction("off");
        }
    }

    @Test
    public void testCommandsMotionSensors() {
        testAction(null);
        for (int i = 0; i < 20; i++) {
            executeCommand(MOTION_SENSORS_CHANNEL, OnOffType.ON);
            testAction("dismv/0");
            executeCommand(MOTION_SENSORS_CHANNEL, OnOffType.OFF);
            testAction("dismv/1");
        }
    }

    @Test
    public void testLoxoneMotionSensorsChanges() {
        for (int i = 0; i < 20; i++) {
            changeLoxoneState("disabledmove", 1.0);
            testChannelState(MOTION_SENSORS_CHANNEL, OnOffType.OFF);
            changeLoxoneState("disabledmove", 0.0);
            testChannelState(MOTION_SENSORS_CHANNEL, OnOffType.ON);
        }
    }
}
