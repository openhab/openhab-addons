/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DecimalTypeWSFloatingPointValueConverterTest {

    @Test
    public void testConversion() throws ConversionException {
        WSFloatingPointValue val = new WSFloatingPointValue(12345, 0, -100, 100);

        val = convertFromOHType(val, new DecimalType(2.54), new ConverterAdditionalInfo(null, false, null));
        assertEquals(12345, val.resourceID);
        assertEquals(-100, val.minimumValue, 0.001);
        assertEquals(100, val.maximumValue, 0.001);
        assertEquals(2.54, val.value, 0.001);

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false, null));
        assertEquals(new DecimalType(2.54), type);
    }

    @Test
    public void testMinExceed() {
        WSFloatingPointValue val = new WSFloatingPointValue(12345, 0, -100, 100);
        assertThrows(ConversionException.class,
                () -> convertFromOHType(val, new DecimalType(-101.5), new ConverterAdditionalInfo(null, false, null)));
    }

    @Test
    public void testMaxExceed() {
        WSFloatingPointValue val = new WSFloatingPointValue(12345, 0, -100, 100);
        assertThrows(ConversionException.class,
                () -> convertFromOHType(val, new DecimalType(101.5), new ConverterAdditionalInfo(null, false, null)));
    }

    private WSFloatingPointValue convertFromOHType(WSFloatingPointValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSFloatingPointValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSFloatingPointValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
