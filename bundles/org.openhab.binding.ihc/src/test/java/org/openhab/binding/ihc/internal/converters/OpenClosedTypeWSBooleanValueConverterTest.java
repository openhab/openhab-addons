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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OpenClosedTypeWSBooleanValueConverterTest {

    @Test
    public void testOpen() throws ConversionException {
        final boolean inverted = false;
        WSBooleanValue val = new WSBooleanValue(12345, false);

        val = convertFromOHType(val, OpenClosedType.OPEN, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(true, val.value);

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OpenClosedType.OPEN, type);
    }

    @Test
    public void testClosed() throws ConversionException {
        final boolean inverted = false;

        WSBooleanValue val = new WSBooleanValue(12345, true);
        val = convertFromOHType(val, OpenClosedType.CLOSED, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(false, val.value);

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OpenClosedType.CLOSED, type);
    }

    @Test
    public void testOpenInverted() throws ConversionException {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345, true);
        val = convertFromOHType(val, OpenClosedType.OPEN, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(false, val.value);

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OpenClosedType.OPEN, type);
    }

    @Test
    public void testClosedInverted() throws ConversionException {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345, false);
        val = convertFromOHType(val, OpenClosedType.CLOSED, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(true, val.value);

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OpenClosedType.CLOSED, type);
    }

    private WSBooleanValue convertFromOHType(WSBooleanValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OpenClosedType.class);
        return (WSBooleanValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private OpenClosedType convertFromResourceValue(WSBooleanValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OpenClosedType.class);
        return (OpenClosedType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
