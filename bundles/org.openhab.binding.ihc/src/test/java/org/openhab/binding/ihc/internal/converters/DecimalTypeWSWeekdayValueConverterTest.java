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
package org.openhab.binding.ihc.internal.converters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSWeekdayValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSWeekdayValueConverterTest {

    @Test
    public void testConversion() throws ConversionException {
        WSWeekdayValue val = new WSWeekdayValue(12345, 0);

        val = convertFromOHType(val, new DecimalType(6), new ConverterAdditionalInfo(null, false, null));
        assertEquals(12345, val.resourceID);
        assertEquals(6, val.weekdayNumber);

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false, null));
        assertEquals(new DecimalType(6), type);
    }

    private WSWeekdayValue convertFromOHType(WSWeekdayValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSWeekdayValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSWeekdayValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
