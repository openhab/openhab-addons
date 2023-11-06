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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;

/**
 * Test class for (@link LxControlValueSelector}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlValueSelectorTest extends LxControlTest {
    private static final String NUMBER_CHANNEL = " / Number";

    @BeforeEach
    public void setup() {
        setupControl("432a7b7e-0022-3aac-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Selection Switch");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlValueSelector.class, 2, 0, 2, 2, 4);
    }

    @Test
    public void testChannels() {
        testChannel("Dimmer");
        testChannel("Number", NUMBER_CHANNEL);
    }

    @Test
    public void testLoxoneValueMinMaxChanges() {
        // filling in missing state values
        testChannelState(null);
        changeLoxoneState("step", 1.0);
        testChannelState(null);
        changeLoxoneState("value", 50.0);
        testChannelState(null);
        changeLoxoneState("min", 0.0);
        testChannelState(null);
        changeLoxoneState("max", 100.0);
        testChannelState(new PercentType(50));
        testChannel("Dimmer", null, BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE, "%.0f", false, null);
        testChannel("Number", NUMBER_CHANNEL, BigDecimal.ZERO, new BigDecimal(100), BigDecimal.ONE, "%.0f", false,
                null);

        // potential division by zero
        changeLoxoneState("min", 55.0);
        changeLoxoneState("max", 55.0);
        testChannelState(null);

        changeLoxoneState("min", 200.0);
        changeLoxoneState("max", 400.0);
        testChannel("Dimmer", null, new BigDecimal(200), new BigDecimal(400), BigDecimal.ONE, "%.0f", false, null);
        testChannel("Number", NUMBER_CHANNEL, new BigDecimal(200), new BigDecimal(400), BigDecimal.ONE, "%.0f", false,
                null);

        // out of range
        changeLoxoneState("value", 199.9);
        testChannelState(null);
        changeLoxoneState("value", 400.1);
        testChannelState(null);
        changeLoxoneState("value", 0.0);
        testChannelState(null);
        // scaling within range
        changeLoxoneState("value", 200.0);
        testChannelState(PercentType.ZERO);
        changeLoxoneState("value", 400.0);
        testChannelState(PercentType.HUNDRED);
        changeLoxoneState("value", 300.0);
        testChannelState(new PercentType(50));
        // reversed range boundaries
        changeLoxoneState("min", 50.0);
        changeLoxoneState("max", 20.0);
        changeLoxoneState("value", 30.0);
        testChannelState(null);
    }

    @Test
    public void testOnOffPercentCommands() {
        changeLoxoneState("min", 1000.0);
        changeLoxoneState("max", 3000.0);
        changeLoxoneState("step", 100.0);

        executeCommand(OnOffType.ON);
        testAction("3000.0");
        executeCommand(OnOffType.OFF);
        testAction("1000.0");

        executeCommand(PercentType.HUNDRED);
        testAction("3000.0");
        executeCommand(new PercentType(50));
        testAction("2000.0");
        executeCommand(new PercentType(1));
        testAction("1020.0");
        executeCommand(PercentType.ZERO);
        testAction("1000.0");

        executeCommand(StopMoveType.MOVE);
        testAction(null);
    }

    @Test
    public void testIncreaseDecreaseCommands() {
        changeLoxoneState("min", 123.0);
        changeLoxoneState("max", 456.0);
        changeLoxoneState("step", 23.0);
        changeLoxoneState("value", 400.0);
        testChannelState(new PercentType(83));
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("423.0");
        changeLoxoneState("value", 423.0);
        testChannelState(new PercentType(90));
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("446.0");
        changeLoxoneState("value", 446.0);
        testChannelState(new PercentType(96));
        executeCommand(IncreaseDecreaseType.INCREASE);
        testAction("456.0"); // trim to max
        changeLoxoneState("value", 456.0);
        testChannelState(PercentType.HUNDRED);
        changeLoxoneState("step", 100.0);
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("356.0");
        changeLoxoneState("value", 356.0);
        testChannelState(new PercentType(69));
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("256.0");
        changeLoxoneState("value", 256.0);
        testChannelState(new PercentType(39));
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("156.0");
        changeLoxoneState("value", 156.0);
        testChannelState(new PercentType(9));
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction("123.0"); // trim to min
        changeLoxoneState("value", 123.0);
        testChannelState(PercentType.ZERO);
    }

    @Test
    public void testNumberCommands() {
        changeLoxoneState("min", 100.0);
        changeLoxoneState("max", 300.0);
        changeLoxoneState("step", 10.0);
        for (Double i = 0.0; i < 100.0; i += 0.35) {
            executeCommand(NUMBER_CHANNEL, new DecimalType(i));
            testAction(null);
        }
        for (Double i = 100.0; i <= 300.0; i += 0.47) {
            executeCommand(NUMBER_CHANNEL, new DecimalType(i));
            testAction(i.toString());
        }
        for (Double i = 300.01; i < 400.0; i += 0.59) {
            executeCommand(NUMBER_CHANNEL, new DecimalType(i));
            testAction(null);
        }
    }

    @Test
    public void testLoxoneNumberChanges() {
        testChannelState(null);
        changeLoxoneState("min", 100.0);
        changeLoxoneState("max", 300.0);
        changeLoxoneState("step", 10.0);
        for (Double i = 0.0; i < 100.0; i += 0.35) {
            changeLoxoneState("value", i);
            testChannelState(NUMBER_CHANNEL, null);
        }
        for (Double i = 100.0; i <= 300.0; i += 0.47) {
            changeLoxoneState("value", i);
            testChannelState(NUMBER_CHANNEL, new DecimalType(i));
        }
        for (Double i = 300.01; i < 400.0; i += 0.59) {
            changeLoxoneState("value", i);
            testChannelState(NUMBER_CHANNEL, null);
        }
    }
}
