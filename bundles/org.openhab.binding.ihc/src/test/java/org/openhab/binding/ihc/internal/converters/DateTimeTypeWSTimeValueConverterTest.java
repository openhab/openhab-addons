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
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimeValue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.Type;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class DateTimeTypeWSTimeValueConverterTest {

    @Test
    public void testConversion() throws ConversionException {
        final DateTimeType dateTimeType = new DateTimeType("2000-12-30T13:59:30");
        WSTimeValue val = new WSTimeValue(12345, 0, 0, 0);

        val = convertFromOHType(val, dateTimeType, null);
        assertEquals(12345, val.resourceID);
        assertEquals(13, val.hours);
        assertEquals(59, val.minutes);
        assertEquals(30, val.seconds);

        DateTimeType type = convertFromResourceValue(val, null);
        assertEquals(dateTimeType.format("HH:mm:ss"), type.format("HH:mm:ss"));
    }

    private WSTimeValue convertFromOHType(WSTimeValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DateTimeType.class);
        return (WSTimeValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DateTimeType convertFromResourceValue(WSTimeValue IHCvalue, ConverterAdditionalInfo converterAdditionalInfo)
            throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DateTimeType.class);
        return (DateTimeType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
