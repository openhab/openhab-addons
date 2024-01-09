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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnOffTypeWSBooleanValueConverterTest {

    @Test
    public void testOn() throws ConversionException {
        final boolean inverted = false;
        WSBooleanValue val = new WSBooleanValue(12345, false);

        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(true, val.value);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOff() throws ConversionException {
        final boolean inverted = false;

        WSBooleanValue val = new WSBooleanValue(12345, true);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(false, val.value);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.OFF, type);
    }

    @Test
    public void testOnInverted() throws ConversionException {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345, true);
        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(false, val.value);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOffInverted() throws ConversionException {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345, false);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(true, val.value);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.OFF, type);
    }

    private WSBooleanValue convertFromOHType(WSBooleanValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (WSBooleanValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private OnOffType convertFromResourceValue(WSBooleanValue IHCvalue, ConverterAdditionalInfo converterAdditionalInfo)
            throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (OnOffType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
