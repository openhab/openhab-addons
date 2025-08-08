/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    void testLevelToPercent() {
        assertEquals(PercentType.ZERO, ValueUtils.levelToPercent(0));
        assertEquals(new PercentType(1), ValueUtils.levelToPercent(1));
        assertEquals(new PercentType(50), ValueUtils.levelToPercent(127));
        assertEquals(new PercentType(100), ValueUtils.levelToPercent(254));
    }

    @Test
    void testPercentToLevel() {
        assertEquals(0, ValueUtils.percentToLevel(PercentType.ZERO));
        assertEquals(127, ValueUtils.percentToLevel(new PercentType(50)));
        assertEquals(254, ValueUtils.percentToLevel(PercentType.HUNDRED));
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
