/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.siemenshvac.internal.converter.type;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.unit.ProductUnit;

/**
 * Specific SiemensHvac Unit
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public enum SiemensUnit {

    CELSIUS_PER_MINUTE(0x2700, "org.siemens.unit.celcius_per_minute", SIEUnits.CELSIUS_PER_MINUTE),
    FAHRENHEIT_PER_MINUTE(0x2701, "org.siemens.unit.fahrenheit_per_minute", SIEUnits.FAHRENHEIT_PER_MINUTE);

    private SiemensUnit(long key, String type, Unit<?> unit) {
    }

    public static class SIEUnits {
        public static final Unit<TemperatureChangeRate> CELSIUS_PER_MINUTE = addUnit(
                new ProductUnit<>(SIUnits.CELSIUS.multiply(Units.MINUTE)));

        public static final Unit<TemperatureChangeRate> FAHRENHEIT_PER_MINUTE = addUnit(
                new ProductUnit<>(ImperialUnits.FAHRENHEIT.multiply(Units.MINUTE)));

        private static <U extends Unit<?>> U addUnit(U unit) {
            return unit;
        }

        public interface TemperatureChangeRate extends Quantity<TemperatureChangeRate> {
        }

        // public QuantityType<TemperatureChangeRate> temperatureChangeRate;

        static {
            SimpleUnitFormat.getInstance().label(CELSIUS_PER_MINUTE, "°C*min");
            SimpleUnitFormat.getInstance().label(FAHRENHEIT_PER_MINUTE, "°F*min");
        }
    }
}
