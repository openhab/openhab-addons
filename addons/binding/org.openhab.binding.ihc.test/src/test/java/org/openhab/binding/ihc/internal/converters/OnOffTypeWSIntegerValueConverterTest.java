/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnOffTypeWSIntegerValueConverterTest {

    @Test
    public void testOn() throws ConversionException {
        final boolean inverted = false;
        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);

        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOff() throws ConversionException {
        final boolean inverted = false;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.OFF, type);
    }

    @Test
    public void testOnInverted() throws ConversionException {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOffInverted() throws ConversionException {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.OFF, type);
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private OnOffType convertFromResourceValue(WSIntegerValue IHCvalue, ConverterAdditionalInfo converterAdditionalInfo)
            throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (OnOffType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
