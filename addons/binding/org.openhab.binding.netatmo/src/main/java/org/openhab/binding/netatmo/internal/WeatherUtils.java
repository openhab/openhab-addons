/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

/**
 * This class holds various unit/measurement conversion methods
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class WeatherUtils {

    /**
     * Compute the HeatIndex temperature given temperature and hygrometry
     * https://fr.wikipedia.org/wiki/Indice_de_chaleur
     *
     * @param temperature in (°C)
     * @param hygro relative level (%)
     * @return heatIndex
     */
    public static double getHeatIndex(double temperature, double humidity) {
        double t = (9 / 5) * temperature + 32; // switch to °F
        double hi = 16.923 + (1.85212 * Math.pow(10, -1) * t) + (5.37941 * humidity)
                - (1.00254 * Math.pow(10, -1) * t * humidity) + (9.41695 * Math.pow(10, -3) * Math.pow(t, 2))
                + (7.28898 * Math.pow(10, -3) * Math.pow(humidity, 2))
                + (3.45372 * Math.pow(10, -4) * Math.pow(t, 2) * humidity)
                - (8.14971 * Math.pow(10, -4) * t * Math.pow(humidity, 2))
                + (1.02102 * Math.pow(10, -5) * Math.pow(t, 2) * Math.pow(humidity, 2))
                - (3.8646 * Math.pow(10, -5) * Math.pow(t, 3)) + (2.91583 * Math.pow(10, -5) * Math.pow(humidity, 3))
                + (1.42721 * Math.pow(10, -6) * Math.pow(t, 3) * humidity)
                + (1.97483 * Math.pow(10, -7) * t * Math.pow(humidity, 3))
                - (2.18429 * Math.pow(10, -8) * Math.pow(t, 3) * Math.pow(humidity, 2))
                + (8.43296 * Math.pow(10, -10) * Math.pow(t, 2) * Math.pow(humidity, 3))
                - (4.81975 * Math.pow(10, -11) * Math.pow(t, 3) * Math.pow(humidity, 3));
        hi = (5.0 / 9.0) * (hi - 32); // get back to °C
        return hi;
    }

    public static double getDewPointDep(double temperature, double dewpoint) {
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
    public static double getDewPoint(double temperature, double humidity) {
        double a = 17.271, b = 237.2;
        double gamma = ((a * temperature) / (b + temperature)) + Math.log(humidity / 100.0);
        return b * gamma / (a - gamma);
    }

    /**
     * Compute the Humidex index given temperature and hygrometry
     *
     *
     * @param temperature in (°C)
     * @param hygro relative level (%)
     * @return Humidex index value
     */
    public static double getHumidex(double temperature, double hygro) {
        double result = 6.112 * Math.pow(10, 7.5 * temperature / (237.7 + temperature)) * hygro / 100;
        result = temperature + 0.555555556 * (result - 10);
        return result;
    }

}
