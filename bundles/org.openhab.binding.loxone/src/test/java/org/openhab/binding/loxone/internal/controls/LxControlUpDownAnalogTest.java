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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * Test class for (@link LxControlUpDownAnalog}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlUpDownAnalogTest extends LxControlTest {
    Double min;
    Double max;
    Double step;
    String format;

    @BeforeEach
    public void setup() {
        min = 50.0;
        max = 150.0;
        step = 10.0;
        format = "%.1f";
        setupControl("131b1a96-02b9-f6e9-eeff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Up Down Analog Input");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlUpDownAnalog.class, 1, 0, 1, 1, 2);
    }

    @Test
    public void testChannels() {
        testChannel("Number", null, new BigDecimal(min), new BigDecimal(max), new BigDecimal(step), format, false,
                null);
    }

    @Test
    public void testLoxoneStateChanges() {
        testChannelState(null);
        for (int j = 0; j < 2; j++) {
            changeLoxoneState("error", 0.0);
            for (Double i = min - 50.0; i < min; i += 0.5) {
                changeLoxoneState("value", i);
                testChannelState(null);
            }
            for (Double i = min; i <= max; i += 0.5) {
                changeLoxoneState("value", i);
                testChannelState(new DecimalType(i));
            }
            for (Double i = max + 0.5; i < max + 50.0; i += 0.5) {
                changeLoxoneState("value", i);
                testChannelState(null);
            }
            changeLoxoneState("error", 1.0);
            for (Double i = min - 50.0; i < max + 50.0; i += 0.5) {
                changeLoxoneState("value", i);
                testChannelState(UnDefType.UNDEF);
            }
        }
    }

    @Test
    public void testCommands() {
        for (Double i = min - 50.0; i < min; i += 0.5) {
            executeCommand(new DecimalType(i));
            testAction(null);
        }
        for (Double i = min; i <= max; i += 0.5) {
            executeCommand(new DecimalType(i));
            testAction(i.toString());
        }
        for (Double i = max + 0.5; i < max + 50.0; i += 0.5) {
            executeCommand(new DecimalType(i));
            testAction(null);
        }
    }
}
