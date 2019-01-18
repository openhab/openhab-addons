/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.conformity.negate.NegateBitModel;
import org.openhab.binding.smartmeter.internal.conformity.negate.NegateBitParser;
import org.openhab.binding.smartmeter.internal.conformity.negate.NegateHandler;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
public class TestNegateBit {

    @Test
    public void testNegateBitParsing() {
        String negateProperty = "1-0_1-8-0:5:1";
        NegateBitModel parseNegateProperty = NegateBitParser.parseNegateProperty(negateProperty);
        Assert.assertEquals("1-0_1-8-0", parseNegateProperty.getNegateChannelId());
        Assert.assertEquals(5, parseNegateProperty.getNegatePosition());
        Assert.assertEquals(true, parseNegateProperty.isNegateBit());
    }

    @Test
    public void testNegateHandlingTrue() {
        String negateProperty = "1-0_1-8-0:5:1";

        boolean negateState = NegateHandler.shouldNegateState(negateProperty, obis -> {
            return new MeterValue<>(obis, "65954", null);
        });

        Assert.assertTrue(negateState);
    }

    @Test
    public void testNegateHandlingFalse() {
        String negateProperty = "1-0_1-8-0:5:1";

        boolean negateState = NegateHandler.shouldNegateState(negateProperty, obis -> {
            return new MeterValue<>(obis, "0", null, "65922");
        });

        Assert.assertFalse(negateState);
    }
}
