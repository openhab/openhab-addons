/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.synopanalyzer.internal;

import java.math.BigDecimal;

/**
 * Utility class for different unit conversions.
 *
 * @author Gerhard Riegler - Contribution originating from OH1
 *
 */
public class UnitUtils {
    private static final BigDecimal MILLITMETER_TO_INCH = new BigDecimal("0.0393700787");
    private static final BigDecimal KMH_TO_MPH = new BigDecimal("0.621371192");
    private static final BigDecimal KMH_TO_KNOTS = new BigDecimal("0.539956803");
    private static final BigDecimal KMH_TO_MPS = new BigDecimal("0.277777778");
    private static final BigDecimal METER_TO_FEET = new BigDecimal("3.2808399");
    private static final BigDecimal ONE_POINT_EIGHT = new BigDecimal("1.8");
    private static final BigDecimal THIRTY_TWO = new BigDecimal("32");
    private static final BigDecimal MILLIBAR_TO_INCH = new BigDecimal("0.0295299830714");

    /**
     * Converts celsius to fahrenheit.
     */
    public static Double celsiusToFahrenheit(Double celsius) {
        return celsius == null ? null : new BigDecimal(celsius).multiply(ONE_POINT_EIGHT).add(THIRTY_TWO).doubleValue();
    }

    /**
     * Converts millimeters to inches.
     */
    public static Double millimetersToInches(Double millimeters) {
        return millimeters == null ? null : new BigDecimal(millimeters).multiply(MILLITMETER_TO_INCH).doubleValue();
    }

    /**
     * Converts kilometers per hour to miles per hour.
     */
    public static Double kmhToMph(Double kmh) {
        return kmh == null ? null : new BigDecimal(kmh).multiply(KMH_TO_MPH).doubleValue();
    }

    /**
     * Converts kilometers per hour to knots.
     */
    public static Double kmhToKnots(Double kmh) {
        return kmh == null ? null : new BigDecimal(kmh).multiply(KMH_TO_KNOTS).doubleValue();
    }

    /**
     * Converts knots to kilometers per hour.
     */
    public static Double knotsToKmh(Double knots) {
        if (knots == null) {
            return null;
        }

        BigDecimal retour = new BigDecimal(knots);
        retour = retour.divide(KMH_TO_KNOTS, 2, BigDecimal.ROUND_FLOOR);
        return retour.doubleValue();
    }

    /**
     * Converts kilometers per hour to meter per seconds.
     */
    public static Double kmhToMps(Double kmh) {
        return kmh == null ? null : new BigDecimal(kmh).multiply(KMH_TO_MPS).doubleValue();
    }

    /**
     * Converts meter per seconds to kilometers per hour.
     */
    public static Double mpsToKmh(Double mps) {
        return mps == null ? null : new BigDecimal(mps).divide(KMH_TO_MPH, 2, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    /**
     * Converts kilometers per hour to beaufort.
     */
    public static Double kmhToBeaufort(Double kmh) {
        return kmh == null ? null : new Double(Math.round(Math.pow(kmh / 3.01, 0.666666666)));
    }

    /**
     * Converts millibar to inches.
     */
    public static Double millibarToInches(Double millibar) {
        return millibar == null ? null : new BigDecimal(millibar).multiply(MILLIBAR_TO_INCH).doubleValue();
    }

    /**
     * Converts meter to feet.
     */
    public static Double meterToFeet(Double meter) {
        return meter == null ? null : new BigDecimal(meter).multiply(METER_TO_FEET).doubleValue();
    }

    /**
     * Converts feet to meter.
     */
    public static Double feetToMeter(Double feet) {
        return feet == null ? null
                : new BigDecimal(feet).divide(METER_TO_FEET, 2, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    /**
     * Converts centimeter to millimeter.
     */
    public static Double centimeterToMillimeter(Double centimeter) {
        return centimeter == null ? null : centimeter * 100;
    }

    /**
     * Calculates the humidex (feels like temperature) from temperature and
     * humidity.
     */
    public static double getHumidex(double temp, int humidity) {
        Double x = 7.5 * temp / (237.7 + temp);
        Double e = 6.112 * Math.pow(10, x) * humidity / 100;
        return temp + (5d / 9d) * (e - 10);
    }

    /**
     * Returns the wind direction based on degree.
     */
    public static String getWindDirection(Integer degree) {
        if (degree < 0 || degree > 360) {
            return null;
        }
        String[] directions = new String[] { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW",
                "W", "WNW", "NW", "NNW" };

        double step = 360.0 / directions.length;
        double b = Math.floor((degree + (step / 2.0)) / step);
        return directions[(int) (b % directions.length)];
    }

    /**
     * Converts a value to the unit configured in the item binding.
     */
    public static Double convertUnit(Double value, Unit unit, String property) {
        if (unit != null) {
            switch (unit) {
                case FAHRENHEIT:
                    return celsiusToFahrenheit(value);
                case MPH:
                    return kmhToMph(value);
                case INCHES:
                    if ("atmosphere.pressure".equals(property)) {
                        return millibarToInches(value);
                    } else if ("precipitation.snow".equals(property)) {
                        return millimetersToInches(centimeterToMillimeter(value));
                    } else {
                        return millimetersToInches(value);
                    }
                case BEAUFORT:
                    return kmhToBeaufort(value);
                case KNOTS:
                    return kmhToKnots(value);
                case MPS:
                    return kmhToMps(value);
            }
        }
        return value;
    }

    /**
     * Computes the sea level pressure depending of observed pressure,
     * temperature and altitude of the observed point
     */
    public static double getSeaLevelPressure(double pressure, double temp, double altitude) {
        double x = 0.0065 * altitude;
        x = (1 - x / (temp + x + 273.15));
        return pressure * Math.pow(x, -5.257);
    }

}
