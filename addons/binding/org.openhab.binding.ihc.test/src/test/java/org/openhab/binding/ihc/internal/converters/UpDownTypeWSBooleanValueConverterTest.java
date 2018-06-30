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

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class UpDownTypeWSBooleanValueConverterTest {

    @Test
    public void TestOpen() {
        final boolean inverted = false;
        WSBooleanValue val = new WSBooleanValue(12345);

        val = convertFromOHType(val, UpDownType.UP, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(true, val.isValue());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.UP, type);
    }

    @Test
    public void TestClosed() {
        final boolean inverted = false;

        WSBooleanValue val = new WSBooleanValue(12345);
        val = convertFromOHType(val, UpDownType.DOWN, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(false, val.isValue());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.DOWN, type);
    }

    @Test
    public void TestOpenInverted() {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345);
        val = convertFromOHType(val, UpDownType.UP, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(false, val.isValue());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.UP, type);
    }

    @Test
    public void TestClosedInverted() {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345);
        val = convertFromOHType(val, UpDownType.DOWN, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(true, val.isValue());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.DOWN, type);
    }

    private WSBooleanValue convertFromOHType(WSBooleanValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                UpDownType.class);
        return (WSBooleanValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private UpDownType convertFromResourceValue(WSBooleanValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                UpDownType.class);
        return (UpDownType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
