/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.matter.internal.util.ValueUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;

/**
 * Test class for GenericConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class GenericConverterTest {

    @Test
    void testLevelToPercentWhenOn() {
        // the Lighting feature has no level for off, so the minimum level is the dimmest a light can be on
        assertEquals(new PercentType(1), ValueUtils.levelToPercentWhenOn(1));
        assertEquals(new PercentType(1), ValueUtils.levelToPercentWhenOn(2));
        assertEquals(new PercentType(1), ValueUtils.levelToPercentWhenOn(4));
        assertEquals(new PercentType(50), ValueUtils.levelToPercentWhenOn(128));
        assertEquals(new PercentType(100), ValueUtils.levelToPercentWhenOn(254));
    }

    @Test
    void testPercentToLevel() {
        assertEquals(1, ValueUtils.percentToLevel(PercentType.ZERO));
        assertEquals(4, ValueUtils.percentToLevel(new PercentType(1)));
        assertEquals(128, ValueUtils.percentToLevel(new PercentType(50)));
        assertEquals(254, ValueUtils.percentToLevel(PercentType.HUNDRED));
    }

    @Test
    void testLevelPercentRoundTrip() {
        // a level set from a percentage must read back as the same percentage, or values drift on every hop
        // between a Matter device and a bridged item
        for (int percent = 1; percent <= 100; percent++) {
            PercentType value = new PercentType(percent);
            assertEquals(value, ValueUtils.levelToPercentWhenOn(ValueUtils.percentToLevel(value)));
        }
    }

    @Test
    void testLevelToPercentWithoutTheLightingFeature() {
        // level 0 is valid without the Lighting feature, so the range starts there
        assertEquals(PercentType.ZERO, ValueUtils.levelToPercent(0, 0));
        assertEquals(new PercentType(50), ValueUtils.levelToPercent(127, 0));
        assertEquals(new PercentType(100), ValueUtils.levelToPercent(254, 0));
        assertEquals(0, ValueUtils.percentToLevel(PercentType.ZERO, 0));
        assertEquals(254, ValueUtils.percentToLevel(PercentType.HUNDRED, 0));
    }

    @Test
    void testSaturationRoundTrip() {
        // every saturation the device reports must convert back to itself, the item is the only place it is kept.
        // The other direction cannot be exact, since a percentage is finer grained than the 255 saturation steps.
        for (int saturation = 0; saturation <= 254; saturation++) {
            assertEquals(saturation, ValueUtils.percentToSaturation(ValueUtils.saturationToPercent(saturation)));
        }
    }

    @Test
    void testTemperatureToValueCelsius() {
        assertEquals(2000, ValueUtils.temperatureToValue(new QuantityType<Temperature>(20.0, SIUnits.CELSIUS)));
        assertEquals(-500, ValueUtils.temperatureToValue(new QuantityType<Temperature>(-5.0, SIUnits.CELSIUS)));
        assertEquals(2250, ValueUtils.temperatureToValue(new QuantityType<Temperature>(22.5, SIUnits.CELSIUS)));
    }

    @Test
    void testTemperatureToValueFahrenheit() {
        assertEquals(0, ValueUtils.temperatureToValue(new QuantityType<Temperature>(32.0, ImperialUnits.FAHRENHEIT)));
        assertEquals(2000,
                ValueUtils.temperatureToValue(new QuantityType<Temperature>(68.0, ImperialUnits.FAHRENHEIT)));
    }

    @Test
    void testTemperatureToValueNumber() {
        assertEquals(2000, ValueUtils.temperatureToValue(new DecimalType(20)));
        assertEquals(-500, ValueUtils.temperatureToValue(new DecimalType(-5)));
    }

    @Test
    void testTemperatureToValueInvalid() {
        assertNull(ValueUtils.temperatureToValue(new QuantityType<>(20.0, ImperialUnits.MILES_PER_HOUR)));
    }

    @Test
    void testValueToTemperature() {
        assertEquals(new QuantityType<Temperature>(20.0, SIUnits.CELSIUS), ValueUtils.valueToTemperature(2000));
        assertEquals(new QuantityType<Temperature>(-5.0, SIUnits.CELSIUS), ValueUtils.valueToTemperature(-500));
        assertEquals(new QuantityType<Temperature>(22.5, SIUnits.CELSIUS), ValueUtils.valueToTemperature(2250));
    }
}
