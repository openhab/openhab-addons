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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * Test class for (@link LxControlInfoOnlyAnalog}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlInfoOnlyAnalogTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("0fec5dc3-003e-8800-ffff403fb0c34b9e", "0fe3a451-0283-2afa-ffff403fb0c34b9e",
                "0fe665f4-0161-4773-ffff403fb0c34b9e", "Info Only Analog");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlInfoOnlyAnalog.class, 2, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        testChannel("Number", null, null, null, null, "%.2f", true, null);
    }

    @Test
    public void testLoxoneStateChanges() {
        for (Double i = -1000.0; i < 1000.0; i += 33.7324323) {
            changeLoxoneState("value", i);
            testChannelState(new DecimalType(i));
        }
        changeLoxoneState("value", 0.0);
        testChannelState(DecimalType.ZERO);
        changeLoxoneState("value", Double.NaN);
        testChannelState(UnDefType.UNDEF);
        changeLoxoneState("value", Double.MAX_VALUE);
        testChannelState(new DecimalType(Double.MAX_VALUE));
        changeLoxoneState("value", Double.POSITIVE_INFINITY);
        testChannelState(UnDefType.UNDEF);
        changeLoxoneState("value", Double.MIN_VALUE);
        testChannelState(new DecimalType(Double.MIN_VALUE));
        changeLoxoneState("value", Double.NEGATIVE_INFINITY);
        testChannelState(UnDefType.UNDEF);
    }
}
