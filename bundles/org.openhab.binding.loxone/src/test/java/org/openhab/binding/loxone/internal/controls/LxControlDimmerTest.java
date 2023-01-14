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
public class LxControlDimmerTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("131b19cd-03c0-640f-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Dimmer Control");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlDimmer.class, 1, 0, 1, 1, 4);
    }

    @Test
    public void testChannels() {
        testChannel("Dimmer");
    }

    @Test
    public void testLoxonePositionMinMaxChanges() {
        // filling in missing state values
        testChannelState(null);
        changeLoxoneState("step", 1.0);
        testChannelState(null);
        changeLoxoneState("position", 50.0);
        testChannelState(null);
        changeLoxoneState("min", 0.0);
        testChannelState(null);
        changeLoxoneState("max", 100.0);
        testChannelState(new PercentType(50));

        // potential division by zero
        changeLoxoneState("min", 55.0);
        changeLoxoneState("max", 55.0);
        testChannelState(null);

        changeLoxoneState("min", 200.0);
        changeLoxoneState("max", 400.0);
        // out of range
        changeLoxoneState("position", 199.9);
        testChannelState(null);
        changeLoxoneState("position", 400.1);
        testChannelState(null);
        // scaling within range
        changeLoxoneState("position", 200.0);
        testChannelState(PercentType.ZERO);
        changeLoxoneState("position", 400.0);
        testChannelState(PercentType.HUNDRED);
        changeLoxoneState("position", 300.0);
        testChannelState(new PercentType(50));
        // special value meaning switched off
        changeLoxoneState("position", 0.0);
        testChannelState(PercentType.ZERO);

        // reversed range boundaries
        changeLoxoneState("min", 50.0);
        changeLoxoneState("max", 20.0);
        // here dimmer still turned off
        testChannelState(PercentType.ZERO);
        // here within wrong range
        changeLoxoneState("position", 30.0);
        testChannelState(null);
    }

    @Test
    public void testOnOffPercentCommands() {
        executeCommand(OnOffType.ON);
        testAction("On");
        executeCommand(OnOffType.OFF);
        testAction("Off");

        changeLoxoneState("min", 1000.0);
        changeLoxoneState("max", 3000.0);
        changeLoxoneState("step", 100.0);
        executeCommand(PercentType.HUNDRED);
        testAction("3000.0");
        executeCommand(new PercentType(50));
        testAction("2000.0");
        executeCommand(new PercentType(1));
        testAction("1020.0");
        executeCommand(PercentType.ZERO);
        testAction("0.0");

        executeCommand(StopMoveType.MOVE);
        testAction(null);
    }

    @Test
    public void testIncreaseDecreaseCommands() {
        changeLoxoneState("min", 123.0);
        changeLoxoneState("max", 456.0);
        changeLoxoneState("step", 23.0);
        changeLoxoneState("position", 400.0);
        testChannelState(new PercentType(83));
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("423.0");
        changeLoxoneState("position", 423.0);
        testChannelState(new PercentType(90));
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("446.0");
        changeLoxoneState("position", 446.0);
        testChannelState(new PercentType(96));
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("456.0"); // trim to max
        changeLoxoneState("position", 456.0);
        testChannelState(PercentType.HUNDRED);
        changeLoxoneState("step", 100.0);
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("356.0");
        changeLoxoneState("position", 356.0);
        testChannelState(new PercentType(69));
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("256.0");
        changeLoxoneState("position", 256.0);
        testChannelState(new PercentType(39));
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("156.0");
        changeLoxoneState("position", 156.0);
        testChannelState(new PercentType(9));
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("123.0"); // trim to min
        changeLoxoneState("position", 123.0);
        testChannelState(PercentType.ZERO);
    }
}
