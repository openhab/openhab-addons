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
package org.openhab.binding.onewire.internal;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.dimension.Density;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link Util} is a set of helper functions
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {
    /**
     * calculate absolute humidity in g/m³ from measured values
     *
     * @param temperature the measured temperature
     * @param relativeHumidity the measured relative humidity
     * @return the corresponding absolute humidity
     */
    public static State calculateAbsoluteHumidity(QuantityType<Temperature> temperature,
            QuantityType<Dimensionless> relativeHumidity) {
        Double theta = temperature.toUnit(SIUnits.CELSIUS).doubleValue();
        // saturation vapor pressure in kg/(m s^2)
        Double saturationVaporPressure = 611.2 * Math.exp(17.62 * theta / (243.12 + theta));
        // absolute humidity in kg/m^3
        Double aH = relativeHumidity.doubleValue() / 100 * saturationVaporPressure / (461.52 * (273.15 + theta));
        State absoluteHumidity = new QuantityType<Density>(aH, SmartHomeUnits.KILOGRAM_PER_CUBICMETRE).toUnit("g/m³");
        if (absoluteHumidity != null) {
            return absoluteHumidity;
        } else {
            throw new IllegalArgumentException("could not change unit");
        }
    }

    /**
     * calculates the dewpoint in °C from measured values
     *
     * @param temperature the measured temperature
     * @param relativeHumidity the measured relative humidity
     * @return the corresponding dewpoint
     */
    public static State calculateDewpoint(QuantityType<Temperature> temperature,
            QuantityType<Dimensionless> relativeHumidity) {
        Double theta = temperature.toUnit(SIUnits.CELSIUS).doubleValue();
        Double rH = relativeHumidity.doubleValue() / 100;
        // dewpoint in °C
        Double dP = 243.12 * (((17.62 * theta) / (243.12 + theta) + Math.log(rH))
                / (((17.62 * 243.12) / (243.12 + theta) - Math.log(rH))));
        State dewPoint = new QuantityType<Temperature>(dP, SIUnits.CELSIUS);
        return dewPoint;
    }
}
