/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link WeatherUndergroundJsonCurrent} is the Java class used
 * to map the entry "current_observation" from the JSON response to a Weather
 * Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonCurrent {

    // Commented members indicate properties returned by the API not used by the binding

    // private Object image;

    // private Location display_location;
    private Location observation_location;
    // private Object estimated;

    private String station_id;

    // private String observation_time;
    // private String observation_time_rfc822;
    private String observation_epoch;
    // private String local_time_rfc822;
    // private String local_epoch;
    // private String local_tz_short;
    // private String local_tz_long;
    // private String local_tz_offset;

    private String weather;

    // private String ;temperature_string;
    private BigDecimal temp_f;
    private BigDecimal temp_c;

    private String relative_humidity;

    // private String wind_string;
    private String wind_dir;
    private BigDecimal wind_degrees;
    private BigDecimal wind_mph;
    private String wind_gust_mph;
    private BigDecimal wind_kph;
    private String wind_gust_kph;

    private String pressure_mb;
    private String pressure_in;
    private String pressure_trend;

    // private String dewpoint_string;
    private BigDecimal dewpoint_f;
    private BigDecimal dewpoint_c;

    // private String heat_index_string;
    private String heat_index_f;
    private String heat_index_c;

    // private String windchill_string;
    private String windchill_f;
    private String windchill_c;

    // private String feelslike_string;
    private String feelslike_f;
    private String feelslike_c;

    private String visibility_mi;
    private String visibility_km;

    private String solarradiation;
    private String UV;

    // private String precip_1hr_string;
    private String precip_1hr_in;
    private String precip_1hr_metric;
    // private String precip_today_string;
    private String precip_today_in;
    private String precip_today_metric;

    private String icon;
    private String icon_url;
    // private String forecast_url;
    // private String history_url;
    // private String ob_url;

    // private String nowcast;

    public WeatherUndergroundJsonCurrent() {
    }

    /**
     * Get the observation location (full name)
     *
     * Used to update the channel current#location
     *
     * @return the observation location or null if not defined
     */
    public String getLocation() {
        return (observation_location == null) ? null : observation_location.getFull();
    }

    /**
     * Get the station ID
     *
     * Used to update the channel current#stationId
     *
     * @return the station ID or null if not defined
     */
    public String getStationId() {
        return station_id;
    }

    /**
     * Get the observation date and time
     *
     * Used to update the channel current#observationTime
     *
     * @return the observation date and time or null if not defined
     */
    public ZonedDateTime getObservationTime(ZoneId zoneId) {
        return WeatherUndergroundJsonUtils.convertToZonedDateTime(observation_epoch, zoneId);
    }

    /**
     * Get the current weather conditions
     *
     * Used to update the channel current#conditions
     *
     * @return the current weather conditions or null if not defined
     */
    public String getConditions() {
        return weather;
    }

    /**
     * Get the current temperature in degrees Celsius
     *
     * Used to update the channel current#temperature
     *
     * @return the current temperature in degrees Celsius or null if not defined
     */
    public BigDecimal getTemperatureC() {
        return temp_c;
    }

    /**
     * Get the current temperature in degrees Fahrenheit
     *
     * Used to update the channel current#temperature
     *
     * @return the current temperature in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getTemperatureF() {
        return temp_f;
    }

    /**
     * Get the current relative humidity
     *
     * Used to update the channel current#relativeHumidity
     *
     * @return the current relative humidity or null if not defined
     */
    public Integer getRelativeHumidity() {
        if (relative_humidity != null && !relative_humidity.isEmpty() && !relative_humidity.equalsIgnoreCase("N/A")) {
            return WeatherUndergroundJsonUtils.convertToInteger(relative_humidity.replace("%", ""));
        }
        return null;
    }

    /**
     * Get the wind direction as a text
     *
     * Used to update the channel current#windDirection
     *
     * @return the wind direction or null if not defined
     */
    public String getWindDirection() {
        return wind_dir;
    }

    /**
     * Get the wind direction in degrees
     *
     * Used to update the channel current#windDirectionDegrees
     *
     * @return the wind direction in degrees or null if not defined
     */
    public BigDecimal getWindDirectionDegrees() {
        return wind_degrees;
    }

    /**
     * Get the wind speed in km/h
     *
     * Used to update the channel current#windSpeed
     *
     * @return the wind speed in km/h or null if not defined
     */
    public BigDecimal getWindSpeedKmh() {
        return wind_kph;
    }

    /**
     * Get the wind speed in mph
     *
     * Used to update the channel current#windSpeed
     *
     * @return the wind speed in mph or null if not defined
     */
    public BigDecimal getWindSpeedMph() {
        return wind_mph;
    }

    /**
     * Get the wind gust in km/h
     *
     * Used to update the channel current#windGust
     *
     * @return the wind gust in km/h or null if not defined
     */
    public BigDecimal getWindGustKmh() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(wind_gust_kph);
    }

    /**
     * Get the wind gust in mph
     *
     * Used to update the channel current#windGust
     *
     * @return the wind gust in mph or null if not defined
     */
    public BigDecimal getWindGustMph() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(wind_gust_mph);
    }

    /**
     * Get the pressure in hPa
     *
     * Used to update the channel current#pressure
     *
     * @return the pressure in hPa or null if not defined
     */
    public BigDecimal getPressureHPa() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(pressure_mb);
    }

    /**
     * Get the pressure in inHg
     *
     * Used to update the channel current#pressure
     *
     * @return the pressure in inHg or null if not defined
     */
    public BigDecimal getPressureInHg() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(pressure_in);
    }

    /**
     * Get the pressure trend
     *
     * Used to update the channel current#pressureTrend
     *
     * @return the pressure trend or null if not defined
     */
    public String getPressureTrend() {
        return WeatherUndergroundJsonUtils.convertToTrend(pressure_trend);
    }

    /**
     * Get the dew point temperature in degrees Celsius
     *
     * Used to update the channel current#dewPoint
     *
     * @return the dew point temperature in degrees Celsius or null if not defined
     */
    public BigDecimal getDewPointC() {
        return dewpoint_c;
    }

    /**
     * Get the dew point temperature in degrees Fahrenheit
     *
     * Used to update the channel current#dewPoint
     *
     * @return the dew point temperature in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getDewPointF() {
        return dewpoint_f;
    }

    /**
     * Get the heat index in degrees Celsius
     *
     * Used to update the channel current#heatIndex
     *
     * @return the heat index in degrees Celsius or null if not defined
     */
    public BigDecimal getHeatIndexC() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(heat_index_c);
    }

    /**
     * Get the heat index in degrees Fahrenheit
     *
     * Used to update the channel current#heatIndex
     *
     * @return the heat index in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getHeatIndexF() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(heat_index_f);
    }

    /**
     * Get the wind chill temperature in degrees Celsius
     *
     * Used to update the channel current#windChill
     *
     * @return the wind chill temperature in degrees Celsius or null if not defined
     */
    public BigDecimal getWindChillC() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(windchill_c);
    }

    /**
     * Get the wind chill temperature in degrees Fahrenheit
     *
     * Used to update the channel current#windChill
     *
     * @return the wind chill temperature in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getWindChillF() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(windchill_f);
    }

    /**
     * Get the feeling temperature in degrees Celsius
     *
     * Used to update the channel current#feelingTemperature
     *
     * @return the feeling temperature in degrees Celsius or null if not defined
     */
    public BigDecimal getFeelingTemperatureC() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(feelslike_c);
    }

    /**
     * Get the feeling temperature in degrees Fahrenheit
     *
     * Used to update the channel current#feelingTemperature
     *
     * @return the feeling temperature in degrees Fahrenheit or null if not defined
     */
    public BigDecimal getFeelingTemperatureF() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(feelslike_f);
    }

    /**
     * Get the visibility in kilometers
     *
     * Used to update the channel current#visibility
     *
     * @return the visibility in kilometers or null if not defined
     */
    public BigDecimal getVisibilityKm() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(visibility_km);
    }

    /**
     * Get the visibility in miles
     *
     * Used to update the channel current#visibility
     *
     * @return the visibility in miles or null if not defined
     */
    public BigDecimal getVisibilityMi() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(visibility_mi);
    }

    /**
     * Get the precipitation in the last hour in millimeters
     *
     * Used to update the channel current#precipitationHour
     *
     * @return the precipitation in the last hour in millimeters or null if not defined
     */
    public BigDecimal getPrecipitationHourMm() {
        BigDecimal result = WeatherUndergroundJsonUtils.convertToBigDecimal(precip_1hr_metric);
        if ((result != null) && (result.doubleValue() < 0.0)) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    /**
     * Get the precipitation in the last hour in inches
     *
     * Used to update the channel current#precipitationHour
     *
     * @return the precipitation in the last hour in inches or null if not defined
     */
    public BigDecimal getPrecipitationHourIn() {
        BigDecimal result = WeatherUndergroundJsonUtils.convertToBigDecimal(precip_1hr_in);
        if ((result != null) && (result.doubleValue() < 0.0)) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    /**
     * Get the precipitation for the full day in millimeters
     *
     * Used to update the channel current#precipitationDay
     *
     * @return the precipitation for the full day in millimeters or null if not defined
     */
    public BigDecimal getPrecipitationDayMm() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(precip_today_metric);
    }

    /**
     * Get the precipitation for the full day in inches
     *
     * Used to update the channel current#precipitationDay
     *
     * @return the precipitation for the full day in inches or null if not defined
     */
    public BigDecimal getPrecipitationDayIn() {
        return WeatherUndergroundJsonUtils.convertToBigDecimal(precip_today_in);
    }

    /**
     * Get the solar radiation in Watts/sq. m
     *
     * Used to update the channel current#solarRadiation
     *
     * @return the solar radiation or null if not defined or negative
     */
    public BigDecimal getSolarRadiation() {
        BigDecimal value = WeatherUndergroundJsonUtils.convertToBigDecimal(solarradiation);
        // We check that the index is not negative
        if (value != null && value.signum() == -1) {
            value = null;
        }
        return value;
    }

    /**
     * Get the UV Index
     *
     * Used to update the channel current#UVIndex
     *
     * @return the UV Index or null if not defined or negative
     */
    public BigDecimal getUVIndex() {
        BigDecimal value = WeatherUndergroundJsonUtils.convertToBigDecimal(UV);
        // We check that the index is not negative
        if (value != null && value.signum() == -1) {
            value = null;
        }
        return value;
    }

    /**
     * Get the icon URL representing the current weather conditions
     *
     * Used to update the channel current#icon
     *
     * @return the icon URL representing the current weather conditions or null if not defined
     */
    public URL getIcon() {
        return WeatherUndergroundJsonUtils.getValidUrl(icon_url);
    }

    /**
     * Get the icon key used in the URL representing the current weather conditions
     *
     * Used to update the channel current#iconKey
     *
     * @return the icon key used in the URL representing the current weather conditions
     */
    public String getIconKey() {
        return icon;
    }

    class Location {
        private String full;
        private String city;
        private String state;
        private String state_name;
        private String country;
        private String country_iso3166;
        private String zip;
        private String magic;
        private String wmo;
        private String latitude;
        private String longitude;
        private String elevation;

        Location() {
        }

        public String getFull() {
            return full;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getStateName() {
            return state_name;
        }

        public String getCountry() {
            return country;
        }

        public String getCountryIso3166() {
            return country_iso3166;
        }

        public String getZip() {
            return zip;
        }

        public String getMagic() {
            return magic;
        }

        public String getWmo() {
            return wmo;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getElevation() {
            return elevation;
        }
    }
}
