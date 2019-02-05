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
package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.junit.Test;
import org.openhab.binding.mysensors.converter.MySensorsRGBTypeConverter;

/**
 * Test case for the RGB converter
 *
 * @author Tim Oberföll
 *
 */
public class RGBConverterTest {

    MySensorsRGBTypeConverter rgbConverter = new MySensorsRGBTypeConverter();
    String hexColor = "62f09b";
    
    @Test
    public void fromString() {
        
        HSBType hsb = (HSBType)rgbConverter.fromString(hexColor);
        
        String hexColor2 = rgbConverter.fromCommand(hsb);
        
        assertTrue(hexColor.equals(hexColor2));
    }
    
    @Test
    public void toHexString() {
        
    }
}
