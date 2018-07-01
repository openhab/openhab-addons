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

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class PercentTypeWSIntegerValueConverterTest {

    @Test
    public void Test() {
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);

        val = convertFromOHType(val, new PercentType(2), new ConverterAdditionalInfo(null, false));
        assertEquals(12345, val.getResourceID());
        assertEquals(2, val.getInteger());

        PercentType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false));
        assertEquals(new PercentType(2), type);
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                PercentType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private PercentType convertFromResourceValue(WSIntegerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                PercentType.class);
        return (PercentType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
