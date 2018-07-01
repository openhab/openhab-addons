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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSEnumValueConverterTest {

    @Test
    public void Test() {
        WSEnumValue val = new WSEnumValue(12345, 100, 0, "testB");

        val = convertFromOHType(val, new DecimalType(2), new ConverterAdditionalInfo(null, false));
        assertEquals(12345, val.getResourceID());
        assertEquals(100, val.getDefinitionTypeID());
        assertEquals(2, val.getEnumValueID());
        assertEquals("testB", val.getEnumName());

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false));
        assertEquals(new DecimalType(2), type);
    }

    private WSEnumValue convertFromOHType(WSEnumValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSEnumValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSEnumValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
