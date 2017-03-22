/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
