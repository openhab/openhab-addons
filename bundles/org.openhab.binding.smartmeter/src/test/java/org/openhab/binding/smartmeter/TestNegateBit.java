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
package org.openhab.binding.smartmeter;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.conformity.negate.NegateBitModel;
import org.openhab.binding.smartmeter.internal.conformity.negate.NegateBitParser;
import org.openhab.binding.smartmeter.internal.conformity.negate.NegateHandler;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class TestNegateBit {

    @Test
    public void testNegateBitParsing() {
        String negateProperty = "1-0_1-8-0:5:1";
        NegateBitModel parseNegateProperty = NegateBitParser.parseNegateProperty(negateProperty);
        assertEquals("1-0_1-8-0", parseNegateProperty.getNegateChannelId());
        assertEquals(5, parseNegateProperty.getNegatePosition());
        assertEquals(true, parseNegateProperty.isNegateBit());
    }

    @Test
    public void testNegateHandlingTrue() {
        String negateProperty = "1-0_1-8-0:5:1";

        boolean negateState = NegateHandler.shouldNegateState(negateProperty,
                obis -> new MeterValue<>(obis, "65954", null));

        assertTrue(negateState);
    }

    @Test
    public void testNegateHandlingDecimalTrue() {
        String negateProperty = "1-0_16-7-0:31:0";

        boolean negateStateDot = NegateHandler.shouldNegateState(negateProperty,
                obis -> new MeterValue<>(obis, "49.0", null));

        assertTrue(negateStateDot);
    }

    @Test
    public void testNegateHandlingFalse() {
        String negateProperty = "1-0_1-8-0:5:1";

        boolean negateState = NegateHandler.shouldNegateState(negateProperty,
                obis -> new MeterValue<>(obis, "0", null, "65922"));

        assertFalse(negateState);
    }
}
