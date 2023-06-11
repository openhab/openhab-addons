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
package org.openhab.binding.onewire.internal;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Util} is a set of helper functions
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /**
     * calculate absolute humidity in g/m³ from measured values
     *
     * @param temperature the measured temperature
     * @param relativeHumidity the measured relative humidity
     * @return the corresponding absolute humidity
     */
    public static State calculateAbsoluteHumidity(QuantityType<Temperature> temperature,
            QuantityType<Dimensionless> relativeHumidity) {
        QuantityType<Temperature> temperatureDegC = temperature.toUnit(SIUnits.CELSIUS);
        if (temperatureDegC == null) {
            throw new IllegalArgumentException("could not change unit");
        }
        Double theta = temperatureDegC.doubleValue();
        // saturation vapor pressure in kg/(m s^2)
        Double saturationVaporPressure = 611.2 * Math.exp(17.62 * theta / (243.12 + theta));
        // absolute humidity in kg/m^3
        Double aH = relativeHumidity.doubleValue() / 100 * saturationVaporPressure / (461.52 * (273.15 + theta));
        State absoluteHumidity = new QuantityType<>(aH, Units.KILOGRAM_PER_CUBICMETRE).toUnit("g/m³");
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
        QuantityType<Temperature> temperatureDegC = temperature.toUnit(SIUnits.CELSIUS);
        if (temperatureDegC == null) {
            throw new IllegalArgumentException("could not change unit");
        }
        Double theta = temperatureDegC.doubleValue();
        Double rH = relativeHumidity.doubleValue() / 100;
        // dewpoint in °C
        Double dP = 243.12 * (((17.62 * theta) / (243.12 + theta) + Math.log(rH))
                / (((17.62 * 243.12) / (243.12 + theta) - Math.log(rH))));
        State dewPoint = new QuantityType<>(dP, SIUnits.CELSIUS);
        return dewPoint;
    }

    public static Map<String, String> readPropertiesFile(String filename) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(filename);
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());
            return properties.entrySet().stream()
                    .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
        } catch (IOException e) {
            LOGGER.warn("Could not read resource file {}, binding will probably fail: {}", filename, e.getMessage());
            return new HashMap<>();
        }
    }
}
