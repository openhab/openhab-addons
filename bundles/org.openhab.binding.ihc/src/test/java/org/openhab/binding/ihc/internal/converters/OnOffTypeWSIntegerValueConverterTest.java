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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnOffTypeWSIntegerValueConverterTest {

    @Test
    public void testOn() throws ConversionException {
        final boolean inverted = false;
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);

        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(100, val.value);
        assertEquals(-100, val.minimumValue);
        assertEquals(100, val.maximumValue);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOnLevelled() throws ConversionException {
        final boolean inverted = false;
        final int onLevel = 70;
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);

        Map<Command, Object> commandLevels = new HashMap<>();
        commandLevels.put(OnOffType.ON, onLevel);

        val = convertFromOHType(val, OnOffType.ON,
                new ConverterAdditionalInfo(null, inverted, Collections.unmodifiableMap(commandLevels)));

        assertEquals(12345, val.resourceID);
        assertEquals(onLevel, val.value);
        assertEquals(-100, val.minimumValue);
        assertEquals(100, val.maximumValue);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOnLevelledError() {
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);

        Map<Command, Object> commandLevels = new HashMap<>();
        commandLevels.put(OnOffType.ON, "70");

        assertThrows(ConversionException.class, () -> convertFromOHType(val, OnOffType.ON,
                new ConverterAdditionalInfo(null, false, Collections.unmodifiableMap(commandLevels))));
    }

    @Test
    public void testOff() throws ConversionException {
        final boolean inverted = false;

        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(-100, val.value);
        assertEquals(-100, val.minimumValue);
        assertEquals(100, val.maximumValue);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.OFF, type);
    }

    @Test
    public void testOnInverted() throws ConversionException {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);
        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(-100, val.value);
        assertEquals(-100, val.minimumValue);
        assertEquals(100, val.maximumValue);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void testOffInverted() throws ConversionException {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted, null));
        assertEquals(12345, val.resourceID);
        assertEquals(100, val.value);
        assertEquals(-100, val.minimumValue);
        assertEquals(100, val.maximumValue);

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted, null));
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
