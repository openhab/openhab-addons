package org.openhab.binding.smartmeter;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.NegateBitModel;
import org.openhab.binding.smartmeter.internal.NegateBitParser;
import org.openhab.binding.smartmeter.internal.NegateHandler;

public class TestNegateBit {

    @Test
    public void testNegateBitParsing() {
        String negateProperty = "1-0#1-8-0:5:1";
        NegateBitModel parseNegateProperty = NegateBitParser.parseNegateProperty(negateProperty);
        Assert.assertEquals("1-0#1-8-0", parseNegateProperty.getNegateChannelId());
        Assert.assertEquals(5, parseNegateProperty.getNegatePosition());
        Assert.assertEquals(true, parseNegateProperty.isNegateBit());
    }

    @Test
    public void testNegateHandlingTrue() {
        String negateProperty = "1-0#1-8-0:5:1";

        boolean negateState = NegateHandler.shouldNegateState(negateProperty, obis -> {
            return new MeterValue(obis, "65954", null);
        });

        Assert.assertTrue(negateState);
    }

    @Test
    public void testNegateHandlingFalse() {
        String negateProperty = "1-0#1-8-0:5:1";

        boolean negateState = NegateHandler.shouldNegateState(negateProperty, obis -> {
            return new MeterValue(obis, "0", null, "65922");
        });

        Assert.assertFalse(negateState);
    }
}
