/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
