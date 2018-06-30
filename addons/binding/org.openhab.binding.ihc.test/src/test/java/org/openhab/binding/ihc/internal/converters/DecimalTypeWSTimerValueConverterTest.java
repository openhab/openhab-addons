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
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimerValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSTimerValueConverterTest {

    @Test
    public void Test() {
        WSTimerValue val = new WSTimerValue(12345);

        val = convertFromOHType(val, new DecimalType(123456), new ConverterAdditionalInfo(null, false));
        assertEquals(12345, val.getResourceID());
        assertEquals(123456, val.getMilliseconds());

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false));
        assertEquals(new DecimalType(123456), type);
    }

    private WSTimerValue convertFromOHType(WSTimerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSTimerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSTimerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
