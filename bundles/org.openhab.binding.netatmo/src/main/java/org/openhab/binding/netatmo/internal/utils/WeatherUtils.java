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
package org.openhab.binding.netatmo.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class holds various unit/measurement conversion methods
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Rob Nielsen - updated heat index
 */
@NonNullByDefault
public class WeatherUtils {

    /**
     * Calculate the heat index using temperature and humidity
     * https://www.wpc.ncep.noaa.gov/html/heatindex_equation.shtml
     *
     * @param temperature in (°C)
     * @param humidity relative level (%)
     * @return heatIndex in (°C)
     */
    public static double heatIndex(double temperature, double humidity) {
        double tempF = (temperature * 9.0 / 5.0) + 32.0; // calculations are done in Fahrenheit
        double heatIndex;
        if (tempF >= 80.0) {
            heatIndex = -42.379 + (2.04901523 * tempF) + (10.14333127 * humidity) - (0.22475541 * tempF * humidity)
                    - (0.00683783 * tempF * tempF) - (0.05481717 * humidity * humidity)
                    + (0.00122874 * tempF * tempF * humidity) + (0.00085282 * tempF * humidity * humidity)
                    - (0.00000199 * tempF * tempF * humidity * humidity);
            if (humidity < 13.0 && tempF <= 112.0) {
                heatIndex -= ((13.0 - humidity) / 4.0) * Math.sqrt((17.0 - Math.abs(tempF - 95.0)) / 17.0);
            } else if (humidity > 85.0 && tempF <= 87.0) {
                heatIndex += ((humidity - 85.0) / 10.0) * ((87.0 - tempF) / 5.0);
            }
        } else {
            heatIndex = 0.5 * (tempF + 61.0 + ((tempF - 68.0) * 1.2) + (humidity * 0.094));
        }

        return (heatIndex - 32) * 5.0 / 9.0; // convert back to Celsius
    }

    public static double dewPointDep(double temperature, double dewpoint) {
        return temperature - dewpoint;
    }

    /**
     * Compute the Dewpoint temperature given temperature and hygrometry
     * valid up to 60 degrees, from
     * http://en.wikipedia.org/wiki/Dew_point#Calculating_the_dew_point
     *
     * @param temperature in (°C)
     * @param humidity relative level (%)
     * @return dewpoint temperature
     */
    public static double dewPoint(double temperature, double humidity) {
        double a = 17.271, b = 237.2;
        double gamma = ((a * temperature) / (b + temperature)) + Math.log(humidity / 100.0);
        return b * gamma / (a - gamma);
    }

    /**
     * Compute the Humidex index given temperature and hygrometry
     *
     * @param temperature in (°C)
     * @param hygro relative level (%)
     * @return Humidex index value
     */
    public static double humidex(double temperature, double hygro) {
        double result = 6.112 * Math.pow(10, 7.5 * temperature / (237.7 + temperature)) * hygro / 100;
        return temperature + 0.555555556 * (result - 10);
    }

    /**
     * Compute the associated scale appreciation of a given humidex index
     * https://www.researchgate.net/figure/The-scale-of-Humidex-and-the-degree-of-comfort_tbl1_335293174
     *
     * @param Humidex index value
     * @return scale between 0 and 4
     */
    public static int humidexScale(double humidex) {
        return humidex < 30 ? 0 : humidex < 40 ? 1 : humidex < 45 ? 2 : humidex < 55 ? 3 : 4;
    }
}
