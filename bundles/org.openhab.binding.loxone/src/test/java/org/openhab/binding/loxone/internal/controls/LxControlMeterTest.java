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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;

/**
 * Test class for (@link LxControlMeter}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlMeterTest extends LxControlTest {
    private static final String ACTUAL_VALUE_CHANNEL = " / Current";
    private static final String TOTAL_VALUE_CHANNEL = " / Total";
    private static final String RESET_CHANNEL = " / Reset";

    @BeforeEach
    public void setup() {
        setupControl("13b3ea27-00fc-6f1b-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Energy Meter");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlMeter.class, 1, 0, 3, 3, 2);
    }

    @Test
    public void testChannels() {
        testChannel("Number", ACTUAL_VALUE_CHANNEL, null, null, null, "%.3fkW", true, null);
        testChannel("Number", TOTAL_VALUE_CHANNEL, null, null, null, "%.1fkWh", true, null);
        testChannel("Switch", RESET_CHANNEL);
    }

    @Test
    public void testLoxoneStateChanges() {
        for (Double i = 0.0; i < 50.0; i += 0.25) {
            changeLoxoneState("actual", i);
            changeLoxoneState("total", i * 2.0);
            testChannelState(ACTUAL_VALUE_CHANNEL, new DecimalType(i));
            testChannelState(TOTAL_VALUE_CHANNEL, new DecimalType(i * 2.0));
            testChannelState(RESET_CHANNEL, OnOffType.OFF);
        }
    }

    @Test
    public void testCommands() {
        testAction(null);
        for (int i = 0; i < 100; i++) {
            executeCommand(RESET_CHANNEL, OnOffType.ON);
            testAction("reset");
            testChannelState(RESET_CHANNEL, OnOffType.OFF);
        }
    }
}
