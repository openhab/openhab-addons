/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.library.types.PercentType;

/**
 * Test class for (@link LxControlValueSelector} - version which allows for increasing only
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlValueSelectorIncrTest extends LxControlValueSelectorTest {
    @Override
    @BeforeEach
    public void setup() {
        setupControl("132a7b7e-0022-3aac-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Selection Switch Increase Only");
    }

    @Override
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
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction(null);
        changeLoxoneState("step", 100.0);
        executeCommand(IncreaseDecreaseType.DECREASE);
        testAction(null);
        changeLoxoneState("value", 123.0);
        testChannelState(PercentType.ZERO);
    }
}
