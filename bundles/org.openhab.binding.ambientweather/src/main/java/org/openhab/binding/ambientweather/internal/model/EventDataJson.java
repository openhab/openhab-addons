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
package org.openhab.binding.ambientweather.internal.model;

/**
 * The {@link EventDataJson} is the JSON object returned
 * from the Ambient Weather real-time API for the weather stations
 * whose weather data is hosted on ambientweather.net. This object is
 * generated whenever the data for the station changes on
 * ambientweather.net.
 *
 * @author Mark Hilbush - Initial Contribution
 */
public class EventDataJson {
    /*
     * The weather station's MAC address
     */
    public String macAddress;

    /*
     * Date of the weather observation in UTC format
     */
    public long dateutc;

    /*
     * Date of the weather observation as a string
     */
    public String date;

    /*
     * Temperature from station in degrees F
     */
    public Double tempf;

    /*
     * Real-feel temperature
     */
    public Double feelsLike;

    /*
     * Dew point
     */
    public Double dewPoint;

    /*
     * Relative humidity from station
     */
    public Double humidity;

    /*
     * Relative barometric pressure in inches
     */
    public Double baromrelin;

    /*
     * Absolute barometric pressure in inches
     */
    public Double baromabsin;

    /*
     * Temperature from indoor sensor
     */
    public Double tempinf;

    /*
     * Humidity from indoor sensor
     */
    public Double humidityin;

    /*
     * Battery from indoor sensor
     */
    public String battin;

    /*
     * Solar radiation
     */
    public Double solarradiation;

    /*
     * UV Index
     */
    public Integer uv;

    /*
     * Carbon dioxide
     */
    public Double co;

    /*
     * Battery level
     */
    public String battout;

    /*
     * Wind speed
     * Instantaneous wind speed, mph
     */
    public Double windspeedmph;

    /*
     * Wind direction
     * Instantaneous wind direction, 0-360 degrees
     */
    public Integer winddir;

    /*
     * Wind gust speed
     * Max wind speed in the last 10 minutes, mph
     */
    public Double windgustmph;

    /*
     * Wind gust direction
     * Wind direction at which the wind gust occurred, 0-360 degrees
     */
    public Integer windgustdir;

    /*
     * Maximum daily wind gust speed
     * Maximum wind speed in last day, mph
     */
    public Double maxdailygust;

    /*
     * Wind speed 2-minute average
     * Average wind speed, 2 minute average, mph
     * Name must contain underscore
     */
    public Double windspdmph_avg2m;

    /*
     * Wind direction 2 minute average
     * Average wind direction, 2 minute average, 0-360 degrees
     * Name must contain underscore
     */
    public Integer winddir_avg2m;

    /*
     * Wind speed 10-minute average
     * Average wind speed, 10 minute average, mph
     * Name must contain underscore
     */
    public Double windspdmph_avg10m;

    /*
     * Wind direction 10 minute average
     * Average wind direction, 10 minute average, 0-360 degrees
     * Name must contain underscore
     */
    public Integer winddir_avg10m;

    /*
     * Hourly rain rate in inches/hour
     */
    public Double hourlyrainin;

    /*
     * Daily rain, inches
     */
    public Double dailyrainin;

    /*
     * Weekly rain, inches
     */
    public Double weeklyrainin;

    /*
     * Monthly rain, inches
     */
    public Double monthlyrainin;

    /*
     * Yearly rain, inches
     */
    public Double yearlyrainin;

    /*
     * Event rain, inches
     */
    public Double eventrainin;

    /*
     * Rain in past 24 hours, inches
     * Can't use since it starts with numbers
     */
    // public double 24hourrainin;

    /*
     * Total rain since last factory reset, inches
     */
    public Double totalrainin;

    /*
     * lastRain - Last time hourlyrainin > 0,
     */
    public String lastRain;

    /*
     * Temperature from remote sensors 1-10
     */
    public Double temp1f;
    public Double temp2f;
    public Double temp3f;
    public Double temp4f;
    public Double temp5f;
    public Double temp6f;
    public Double temp7f;
    public Double temp8f;
    public Double temp9f;
    public Double temp10f;

    /*
     * Relative humidity from remote sensors 1-10
     */
    public Double humidity1;
    public Double humidity2;
    public Double humidity3;
    public Double humidity4;
    public Double humidity5;
    public Double humidity6;
    public Double humidity7;
    public Double humidity8;
    public Double humidity9;
    public Double humidity10;

    /*
     * Battery status of remote sensors 1-10
     * Good/Bad indication, String, 1=Good, 0=Bad
     */
    public String batt1;
    public String batt2;
    public String batt3;
    public String batt4;
    public String batt5;
    public String batt6;
    public String batt7;
    public String batt8;
    public String batt9;
    public String batt10;

    /*
     * Relay 1-10
     */
    public String relay1;
    public String relay2;
    public String relay3;
    public String relay4;
    public String relay5;
    public String relay6;
    public String relay7;
    public String relay8;
    public String relay9;
    public String relay10;

    /*
     * Soil temperature from remote sensors 1-10
     */
    public Double soiltemp1;
    public Double soiltemp2;
    public Double soiltemp3;
    public Double soiltemp4;
    public Double soiltemp5;
    public Double soiltemp6;
    public Double soiltemp7;
    public Double soiltemp8;
    public Double soiltemp9;
    public Double soiltemp10;

    /*
     * Soil moisture from remote sensors 1-10
     */
    public Double soilhum1;
    public Double soilhum2;
    public Double soilhum3;
    public Double soilhum4;
    public Double soilhum5;
    public Double soilhum6;
    public Double soilhum7;
    public Double soilhum8;
    public Double soilhum9;
    public Double soilhum10;
}
