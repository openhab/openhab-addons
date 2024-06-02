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
package org.openhab.binding.weatherunderground.internal.json;

import java.math.BigDecimal;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The {@link WeatherUndergroundJsonForecastDay} is the Java class used
 * to map the list element of the entry "forecast.simpleforecast.forecastday"
 * from the JSON response to a Weather Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonForecastDay {

    // Commented members indicate properties returned by the API not used by the binding

    private ForecastDate date;
    private Integer period;

    private ForecastTemperature high;
    private ForecastTemperature low;

    private String conditions;

    private String icon;
    private String icon_url;
    // private String skyicon;

    private Integer pop;

    private ForecastPrecipitation qpf_allday;
    // private ForecastPrecipitation qpf_day;
    // private ForecastPrecipitation qpf_night;
    private ForecastPrecipitation snow_allday;
    // private ForecastPrecipitation snow_day;
    // private ForecastPrecipitation snow_night;

    private ForecastWind maxwind;
    private ForecastWind avewind;

    private Integer avehumidity;
    // private Integer minhumidity;
    // private Integer maxhumidity;

    public WeatherUndergroundJsonForecastDay() {
    }

    /**
     * Get the forecast date and time
     *
     * Used to update the channel forecastDayX#forecastTime
     *
     * @return the forecast date and time or null if not defined
     */
    public ZonedDateTime getForecastTime(ZoneId zoneId) {
        return WeatherUndergroundJsonUtils.convertToZonedDateTime((date == null) ? null : date.getEpoch(), zoneId);
    }

    /**
     * Get the period number
     *
     * @return the period number
     */
    public Integer getPeriod() {
        return period;
    }

    /**
     * Get the weather forecast conditions
     *
     * Used to update the channel forecastDayX#conditions
     *
     * @return the weather forecast conditions or null if not defined
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * Get the icon URL representing the weather forecast conditions
     *
     * Used to update the channel forecastDayX#icon
     *
     * @return the icon URL representing the weather forecast conditions or null if not defined
     */
    public URL getIcon() {
        return WeatherUndergroundJsonUtils.getValidUrl(icon_url);
    }

    /**
     * Get the icon key used in the URL representing the weather forecast conditions
     *
     * Used to update the channel forecastDayX#iconKey
     *
     * @return the icon key used in the URL representing the weather forecast conditions
     */
    public String getIconKey() {
        return icon;
    }

    /**
     * Get the minimum temperature in degrees Celsius
     *
     * Used to update the channel forecastDayX#minTemperature
     *
     * @return the minimum temperature in degrees Celsius or null if not defined
     */
    public BigDecimal getMinTemperatureC() {
        return (low == null) ? null : WeatherUndergroundJsonUtils.convertToBigDecimal(low.getCelsius());
    }

    /**
     * Get the minimum temperature in degrees Fahrenheit
     *
     * Used to update the channel forecastDayX#minTemperature
     *
     * @return the minimum temperature in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getMinTemperatureF() {
        return (low == null) ? null : WeatherUndergroundJsonUtils.convertToBigDecimal(low.getFahrenheit());
    }

    /**
     * Get the maximum temperature in degrees Celsius
     *
     * Used to update the channel forecastDayX#maxTemperature
     *
     * @return the maximum temperature in degrees Celsius or null if not defined
     */
    public BigDecimal getMaxTemperatureC() {
        return (high == null) ? null : WeatherUndergroundJsonUtils.convertToBigDecimal(high.getCelsius());
    }

    /**
     * Get the maximum temperature in degrees Fahrenheit
     *
     * Used to update the channel forecastDayX#maxTemperature
     *
     * @return the maximum temperature in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getMaxTemperatureF() {
        return (high == null) ? null : WeatherUndergroundJsonUtils.convertToBigDecimal(high.getFahrenheit());
    }

    /**
     * Get the relative humidity
     *
     * Used to update the channel forecastDayX#relativeHumidity
     *
     * @return the relative humidity or null if not defined
     */
    public Integer getRelativeHumidity() {
        return avehumidity;
    }

    /**
     * Get the probability of precipitation
     *
     * Used to update the channel forecastDayX#probaPrecipitation
     *
     * @return the probability of precipitation or null if not defined
     */
    public Integer getProbaPrecipitation() {
        return pop;
    }

    /**
     * Get the precipitation for the full day in millimeters
     *
     * Used to update the channel forecastDayX#precipitationDay
     *
     * @return the precipitation for the full day in millimeters or null if not defined
     */
    public BigDecimal getPrecipitationDayMm() {
        return (qpf_allday == null) ? null : qpf_allday.mm;
    }

    /**
     * Get the precipitation for the full day in inches
     *
     * Used to update the channel forecastDayX#precipitation
     *
     * @return the precipitation for the full day in inches or null if not defined
     */
    public BigDecimal getPrecipitationDayIn() {
        return (qpf_allday == null) ? null : qpf_allday.in;
    }

    /**
     * Get the amount of snow for the full day in centimeters
     *
     * Used to update the channel forecastDayX#snow
     *
     * @return the amount of snow for the full day in centimeters or null if not defined
     */
    public BigDecimal getSnowCm() {
        return (snow_allday == null) ? null : snow_allday.cm;
    }

    /**
     * Get the amount of snow for the full day in inches
     *
     * Used to update the channel forecastDayX#snow
     *
     * @return the amount of snow for the full day in inches or null if not defined
     */
    public BigDecimal getSnowIn() {
        return (snow_allday == null) ? null : snow_allday.in;
    }

    /**
     * Get the maximum wind direction as a text
     *
     * Used to update the channel forecastDayX#maxWindDirection
     *
     * @return the maximum wind direction or null if not defined
     */
    public String getMaxWindDirection() {
        return (maxwind == null) ? null : maxwind.getDir();
    }

    /**
     * Get the maximum wind direction in degrees
     *
     * Used to update the channel forecastDayX#maxWindDirectionDegrees
     *
     * @return the maximum wind direction in degrees or null if not defined
     */
    public BigDecimal getMaxWindDirectionDegrees() {
        return (maxwind == null) ? null : WeatherUndergroundJsonUtils.convertToBigDecimal(maxwind.getDegrees());
    }

    /**
     * Get the maximum wind speed in km/h
     *
     * Used to update the channel forecastDayX#maxWindSpeed
     *
     * @return the maximum wind speed in km/h or null if not defined
     */
    public BigDecimal getMaxWindSpeedKmh() {
        return (maxwind == null) ? null : maxwind.getKph();
    }

    /**
     * Get the maximum wind speed in mph
     *
     * Used to update the channel forecastDayX#maxWindSpeed
     *
     * @return the maximum wind speed in mph or null if not defined
     */
    public BigDecimal getMaxWindSpeedMph() {
        return (maxwind == null) ? null : maxwind.getMph();
    }

    /**
     * Get the average wind direction as a text
     *
     * Used to update the channel forecastDayX#averageWindDirection
     *
     * @return the average wind direction or null if not defined
     */
    public String getAverageWindDirection() {
        return (avewind == null) ? null : avewind.getDir();
    }

    /**
     * Get the average wind direction in degrees
     *
     * Used to update the channel forecastDayX#averageWindDirectionDegrees
     *
     * @return the average wind direction in degrees or null if not defined
     */
    public BigDecimal getAverageWindDirectionDegrees() {
        return (avewind == null) ? null : WeatherUndergroundJsonUtils.convertToBigDecimal(avewind.getDegrees());
    }

    /**
     * Get the average wind speed in km/h
     *
     * Used to update the channel forecastDayX#averageWindSpeed
     *
     * @return the average wind speed in km/h or null if not defined
     */
    public BigDecimal getAverageWindSpeedKmh() {
        return (avewind == null) ? null : avewind.getKph();
    }

    /**
     * Get the average wind speed in mph
     *
     * Used to update the channel forecastDayX#averageWindSpeed
     *
     * @return the average wind speed in mph or null if not defined
     */
    public BigDecimal getAverageWindSpeedMph() {
        return (avewind == null) ? null : avewind.getMph();
    }

    class ForecastDate {

        // Commented members indicate properties returned by the API not used by the binding

        private String epoch;
        // private String pretty;
        // private Integer day;
        // private Integer month;
        // private Integer year;
        // private Integer yday;
        // private Integer hour;
        // private String min;
        // private Integer sec;
        // private String isdst;
        // private String monthname;
        // private String monthname_short;
        // private String weekday_short;
        // private String weekday;
        // private String ampm;
        // private String tz_short;
        // private String tz_long;

        ForecastDate() {
        }

        public String getEpoch() {
            return epoch;
        }
    }

    class ForecastTemperature {
        private String fahrenheit;
        private String celsius;

        ForecastTemperature() {
        }

        public String getFahrenheit() {
            return fahrenheit;
        }

        public String getCelsius() {
            return celsius;
        }
    }

    class ForecastPrecipitation {
        private BigDecimal in;
        private BigDecimal mm;
        private BigDecimal cm;

        ForecastPrecipitation() {
        }

        public BigDecimal getIn() {
            return in;
        }

        public BigDecimal getMm() {
            return mm;
        }

        public BigDecimal getCm() {
            return cm;
        }
    }

    class ForecastWind {
        private BigDecimal mph;
        private BigDecimal kph;
        private String dir;
        private String degrees;

        ForecastWind() {
        }

        public BigDecimal getMph() {
            return mph;
        }

        public BigDecimal getKph() {
            return kph;
        }

        public String getDir() {
            return dir;
        }

        public String getDegrees() {
            return degrees;
        }
    }
}
