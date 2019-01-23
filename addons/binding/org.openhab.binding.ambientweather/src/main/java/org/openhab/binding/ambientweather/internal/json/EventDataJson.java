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
package org.openhab.binding.ambientweather.internal.json;

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
    public double tempf;

    /*
     * Real-feel temperature
     */
    public double feelsLike;

    /*
     * Dew point
     */
    public double dewPoint;

    /*
     * Relative humidity from station
     */
    public double humidity;

    /*
     * Relative barometric pressure in inches
     */
    public double baromrelin;

    /*
     * Absolute barometric pressure in inches
     */
    public double baromabsin;

    /*
     * Temperature from indoor sensor
     */
    public double tempinf;

    /*
     * Humidity from indoor sensor
     */
    public double humidityin;

    /*
     * Battery from indoor sensor
     */
    public String battin;

    /*
     * Solar radiation
     */
    public double solarradiation;

    /*
     * UV Index
     */
    public int uv;

    /*
     * Carbon dioxide
     */
    public double co;

    /*
     * Battery level
     */
    public String battout;

    /*
     * Wind speed
     * Instantaneous wind speed, mph
     */
    public double windspeedmph;

    /*
     * Wind direction
     * Instantaneous wind direction, 0-360 degrees
     */
    public long winddir;

    /*
     * Wind gust speed
     * Max wind speed in the last 10 minutes, mph
     */
    public double windgustmph;

    /*
     * Wind gust direction
     * Wind direction at which the wind gust occurred, 0-360 degrees
     */
    public double windgustdir;

    /*
     * Maximum daily wind gust speed
     * Maximum wind speed in last day, mph
     */
    public double maxdailygust;

    /*
     * Wind speed 2-minute average
     * Average wind speed, 2 minute average, mph
     * Name must contain underscore
     */
    public double windspdmph_avg2m;

    /*
     * Wind direction 2 minute average
     * Average wind direction, 2 minute average, mph
     * Name must contain underscore
     */
    public double winddir_avg2m;

    /*
     * Wind speed 10-minute average
     * Average wind speed, 10 minute average, mph
     * Name must contain underscore
     */
    public double windspdmph_avg10m;

    /*
     * Wind direction 10 minute average
     * Average wind direction, 10 minute average, mph
     * Name must contain underscore
     */
    public double winddir_avg10m;

    /*
     * Hourly rain rate in inches/hour
     */
    public double hourlyrainin;

    /*
     * Daily rain, inches
     */
    public double dailyrainin;

    /*
     * Weekly rain, inches
     */
    public double weeklyrainin;

    /*
     * Monthly rain, inches
     */
    public double monthlyrainin;

    /*
     * Yearly rain, inches
     */
    public double yearlyrainin;

    /*
     * Event rain, inches
     */
    public double eventrainin;

    /*
     * Rain in past 24 hours, inches
     * Can't use since it starts with numbers
     */
    // public double 24hourrainin;

    /*
     * Total rain since last factory reset, inches
     */
    public double totalrainin;

    /*
     * lastRain - Last time hourlyrainin > 0,
     */
    public String lastRain;

    /*
     * Temperature from remote sensors 1-10
     */
    public double temp1f;
    public double temp2f;
    public double temp3f;
    public double temp4f;
    public double temp5f;
    public double temp6f;
    public double temp7f;
    public double temp8f;
    public double temp9f;
    public double temp10f;

    /*
     * Relative humidity from remote sensors 1-10
     */
    public double humidity1;
    public double humidity2;
    public double humidity3;
    public double humidity4;
    public double humidity5;
    public double humidity6;
    public double humidity7;
    public double humidity8;
    public double humidity9;
    public double humidity10;

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
    public double soiltemp1;
    public double soiltemp2;
    public double soiltemp3;
    public double soiltemp4;
    public double soiltemp5;
    public double soiltemp6;
    public double soiltemp7;
    public double soiltemp8;
    public double soiltemp9;
    public double soiltemp10;

    /*
     * Soil moisture from remote sensors 1-10
     */
    public double soilhum1;
    public double soilhum2;
    public double soilhum3;
    public double soilhum4;
    public double soilhum5;
    public double soilhum6;
    public double soilhum7;
    public double soilhum8;
    public double soilhum9;
    public double soilhum10;
}
