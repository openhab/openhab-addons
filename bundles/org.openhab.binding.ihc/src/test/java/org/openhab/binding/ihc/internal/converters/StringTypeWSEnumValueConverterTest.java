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

import java.util.ArrayList;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.internal.ws.exeptions.ConversionException;
import org.openhab.binding.ihc.internal.ws.projectfile.IhcEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class StringTypeWSEnumValueConverterTest {

    @Test
    public void test() throws ConversionException {
        ArrayList<IhcEnumValue> enumValues = new ArrayList<>();
        enumValues.add(new IhcEnumValue(101, "testA"));
        enumValues.add(new IhcEnumValue(102, "testB"));
        enumValues.add(new IhcEnumValue(103, "testC"));
        enumValues.add(new IhcEnumValue(104, "testD"));

        WSEnumValue val = new WSEnumValue(12345, 5555, 0, "");

        val = convertFromOHType(val, new StringType("testC"), new ConverterAdditionalInfo(enumValues, false, null));
        assertEquals(12345, val.resourceID);
        assertEquals(5555, val.definitionTypeID);
        assertEquals(103, val.enumValueID);
        assertEquals("testC", val.enumName);

        StringType type = convertFromResourceValue(val, new ConverterAdditionalInfo(enumValues, false, null));
        assertEquals(new StringType("testC"), type);
    }

    private WSEnumValue convertFromOHType(WSEnumValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                StringType.class);
        return (WSEnumValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private StringType convertFromResourceValue(WSEnumValue IHCvalue, ConverterAdditionalInfo converterAdditionalInfo)
            throws ConversionException {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                StringType.class);
        return (StringType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
