/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSEnumValueConverterTest {

    @Test
    public void testConversion() throws ConversionException {
        WSEnumValue val = new WSEnumValue(12345, 100, 0, "testB");

        val = convertFromOHType(val, new DecimalType(2), new ConverterAdditionalInfo(null, false, null));
        assertEquals(12345, val.resourceID);
        assertEquals(100, val.definitionTypeID);
        assertEquals(2, val.enumValueID);
        assertEquals("testB", val.enumName);

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false, null));
        assertEquals(new DecimalType(2), type);
    }

    private WSEnumValue convertFromOHType(WSEnumValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSEnumValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSEnumValue IHCvalue, ConverterAdditionalInfo converterAdditionalInfo)
            throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
