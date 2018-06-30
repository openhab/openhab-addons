/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.converters;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSFloatingPointValueConverterTest {

    @Test
    public void Test() {
        WSFloatingPointValue val = new WSFloatingPointValue(12345, 0, -100, 100);

        val = convertFromOHType(val, new DecimalType(2.54), new ConverterAdditionalInfo(null, false));
        assertEquals(12345, val.getResourceID());
        assertEquals(2.54, val.getFloatingPointValue(), 0.001);

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false));
        assertEquals(new DecimalType(2.54), type);
    }

    @Test(expected = NumberFormatException.class)
    public void TestMinExceed() {
        WSFloatingPointValue val = new WSFloatingPointValue(12345, 0, -100, 100);
        val = convertFromOHType(val, new DecimalType(-101.5), new ConverterAdditionalInfo(null, false));
    }

    @Test(expected = NumberFormatException.class)
    public void TestMaxExceed() {
        WSFloatingPointValue val = new WSFloatingPointValue(12345, 0, -100, 100);
        val = convertFromOHType(val, new DecimalType(101.5), new ConverterAdditionalInfo(null, false));
    }

    private WSFloatingPointValue convertFromOHType(WSFloatingPointValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSFloatingPointValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSFloatingPointValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
