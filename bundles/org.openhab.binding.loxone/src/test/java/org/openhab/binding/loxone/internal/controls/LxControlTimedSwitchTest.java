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

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for (@link LxControlTimedSwitch}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlTimedSwitchTest extends LxControlTest {
    private static final String DELAY_CHANNEL = " / Deactivation Delay";

    @BeforeEach
    public void setup() {
        setupControl("1326771c-030e-3a7c-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Stairwell Light Switch");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlTimedSwitch.class, 1, 0, 2, 2, 2);
    }

    @Test
    public void testChannels() {
        testChannel("Switch", Collections.singleton("Switchable"));
        testChannel("Number", DELAY_CHANNEL, new BigDecimal(-1), null, null, null, true, null);
    }

    @Test
    public void testLoxoneStateChanges() {
        testChannelState(null);
        testChannelState(DELAY_CHANNEL, null);
        changeLoxoneState("deactivationdelaytotal", 100.0);
        for (int i = 0; i < 100; i++) {
            changeLoxoneState("deactivationdelay", 0.0);
            testChannelState(OnOffType.OFF);
            testChannelState(DELAY_CHANNEL, DecimalType.ZERO);
            changeLoxoneState("deactivationdelay", -1.0);
            testChannelState(OnOffType.ON);
            testChannelState(DELAY_CHANNEL, DecimalType.ZERO);
        }
        for (Double i = 100.0; i >= 1.0; i--) {
            changeLoxoneState("deactivationdelay", i);
            testChannelState(OnOffType.ON);
            testChannelState(DELAY_CHANNEL, new DecimalType(i));
        }
        changeLoxoneState("deactivationdelay", 0.0);
        testChannelState(OnOffType.OFF);
        testChannelState(DELAY_CHANNEL, DecimalType.ZERO);
    }

    @Test
    public void testCommands() {
        for (int i = 0; i < 100; i++) {
            executeCommand(OnOffType.ON);
            testAction("Pulse");
            executeCommand(OnOffType.OFF);
            testAction("Off");
        }
    }
}
