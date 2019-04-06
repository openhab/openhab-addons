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

import org.eclipse.smarthome.core.library.types.PercentType;
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
public class PercentTypeWSIntegerValueConverterTest {

    @Test
    public void test() throws ConversionException {
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);

        val = convertFromOHType(val, new PercentType(2), new ConverterAdditionalInfo(null, false, null));
        assertEquals(12345, val.resourceID);
        assertEquals(-100, val.minimumValue);
        assertEquals(100, val.maximumValue);
        assertEquals(2, val.value);

        PercentType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false, null));
        assertEquals(new PercentType(2), type);
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                PercentType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private PercentType convertFromResourceValue(WSIntegerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                PercentType.class);
        return (PercentType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
