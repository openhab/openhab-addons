/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.link;

import org.junit.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.library.types.StringType;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author Gregor Roth - Initial contribution
 */
public class TypeConverterTest {
    
    @Test
    public void testStringType() throws Exception {
        var typeConverter = TypeConverters.create("String", "String");
        var state = typeConverter.toStateCommand("motion");
        assertEquals(true, state instanceof StringType);
        assertEquals("motion", typeConverter.toPropertyValue((State) state));
    }

    @Test
    public void testNumberType() throws Exception {
        var typeConverter = TypeConverters.create("Number", "Number");
        var state = typeConverter.toStateCommand(45.6);
        assertEquals(true, state instanceof DecimalType);
        assertEquals(45.6, typeConverter.toPropertyValue((State) state));
    }

    @Test
    public void testNumberIntegerType() throws Exception {
        var typeConverter = TypeConverters.create("Number", "Integer");
        var state = typeConverter.toStateCommand(45);
        assertEquals(true, state instanceof DecimalType);
        assertEquals(45, typeConverter.toPropertyValue((State) state));

        state = typeConverter.toStateCommand(45.2);
        assertEquals(true, state instanceof DecimalType);
        assertEquals(45, typeConverter.toPropertyValue((State) state));

    }
}
