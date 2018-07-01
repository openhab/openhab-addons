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

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DateTimeTypeWSDateValueConverterTest {

    @Test
    public void Test() {
        final DateTimeType dateTimeType = new DateTimeType("2000-12-30T00:00:00");
        WSDateValue val = new WSDateValue(12345);

        val = convertFromOHType(val, dateTimeType, null);
        assertEquals(12345, val.getResourceID());
        assertEquals(2000, val.getYear());
        assertEquals(12, val.getMonth());
        assertEquals(30, val.getDay());

        DateTimeType type = convertFromResourceValue(val, null);
        assertEquals(dateTimeType.format("yyyy-MM-dd"), type.format("yyyy-MM-dd"));
    }

    private WSDateValue convertFromOHType(WSDateValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DateTimeType.class);
        return (WSDateValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DateTimeType convertFromResourceValue(WSDateValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DateTimeType.class);
        return (DateTimeType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
