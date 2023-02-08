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
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;

/**
 * Test class for (@link LxControlDimmer}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlEIBDimmerTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("faa30f5c-4b4f-11e2-8928b8ba17ef51ee", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Kitchen Dimmer");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlDimmer.class, 1, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        testChannel("Dimmer");
    }

    @Test
    public void testLoxonePositionChanges() {
        // filling in missing state values
        testChannelState(null);
        for (Double i = 0.0; i <= 100.0; i += 1.0) {
            changeLoxoneState("position", i);
            testChannelState(new PercentType(i.intValue()));
        }
        // out of range
        changeLoxoneState("position", 199.9);
        testChannelState(null);
        changeLoxoneState("position", 400.1);
        testChannelState(null);
    }

    @Test
    public void testOnOffPercentCommands() {
        executeCommand(OnOffType.ON);
        testAction("On");
        executeCommand(OnOffType.OFF);
        testAction("Off");
        for (Double i = 0.0; i <= 100.0; i += 1.0) {
            executeCommand(new PercentType(i.intValue()));
            testAction(i.toString());
        }
        executeCommand(StopMoveType.MOVE);
        testAction(null);
    }

    @Test
    public void testIncreaseDecreaseCommands() {
        for (Double i = 0.0; i <= 95.0; i += 1.0) {
            changeLoxoneState("position", i);
            testChannelState(new PercentType(i.intValue()));
            testAction(null);
            executeCommand(IncreaseDecreaseType.INCREASE);
            Double j = i + 5.0;
            testAction(j.toString());
        }
        for (Double i = 100.0; i >= 5.0; i -= 1.0) {
            changeLoxoneState("position", i);
            testChannelState(new PercentType(i.intValue()));
            testAction(null);
            executeCommand(IncreaseDecreaseType.DECREASE);
            Double j = i - 5.0;
            testAction(j.toString());
        }
        // test not exceeding range
        changeLoxoneState("position", 100.0);
        testChannelState(PercentType.HUNDRED);
        testAction(null);
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("100.0");

        changeLoxoneState("position", 0.0);
        testChannelState(PercentType.ZERO);
        testAction(null);
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("0.0");
    }
}
