/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.processor;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.openhab.binding.ambientweather.internal.json.EventDataJson;
import org.openhab.binding.ambientweather.internal.util.PressureTrend;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Ws1400ipProcessor} is responsible for updating
 * the channels associated with the WS-1400-IP series weather stations in
 * response to the receipt of a weather data update from the Ambient
 * Weather real-time API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Ws1400ipProcessor extends AbstractProcessor {
    /*
     * Define the channels we will update for this weather station
     */
    // Channels returned from ambientweather.net
    private static final String NAME = CHGRP_STATION + "#" + CH_NAME;
    private static final String LOCATION = CHGRP_STATION + "#" + CH_LOCATION;
    private static final String OBSERVATION_TIME = CHGRP_WS1400IP + "#" + CH_OBSERVATION_TIME;
    private static final String BATTERY_INDICATOR = CHGRP_WS1400IP + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE = CHGRP_WS1400IP + "#" + CH_TEMPERATURE;
    private static final String FEELING_TEMPERATURE = CHGRP_WS1400IP + "#" + CH_FEELING_TEMPERATURE;
    private static final String DEW_POINT = CHGRP_WS1400IP + "#" + CH_DEW_POINT;
    private static final String HUMIDITY = CHGRP_WS1400IP + "#" + CH_HUMIDITY;
    private static final String PRESSURE_ABSOLUTE = CHGRP_WS1400IP + "#" + CH_PRESSURE_ABSOLUTE;
    private static final String PRESSURE_RELATIVE = CHGRP_WS1400IP + "#" + CH_PRESSURE_RELATIVE;
    private static final String SOLAR_RADIATION = CHGRP_WS1400IP + "#" + CH_SOLAR_RADIATION;
    private static final String UV_INDEX = CHGRP_WS1400IP + "#" + CH_UV_INDEX;
    private static final String WIND_SPEED = CHGRP_WS1400IP + "#" + CH_WIND_SPEED;
    private static final String WIND_DIRECTION_DEGREES = CHGRP_WS1400IP + "#" + CH_WIND_DIRECTION_DEGREES;
    private static final String WIND_GUST = CHGRP_WS1400IP + "#" + CH_WIND_GUST;
    private static final String WIND_GUST_MAX_DAILY = CHGRP_WS1400IP + "#" + CH_WIND_GUST_MAX_DAILY;
    private static final String RAIN_HOURLY_RATE = CHGRP_WS1400IP + "#" + CH_RAIN_HOURLY_RATE;
    private static final String RAIN_DAY = CHGRP_WS1400IP + "#" + CH_RAIN_DAY;
    private static final String RAIN_WEEK = CHGRP_WS1400IP + "#" + CH_RAIN_WEEK;
    private static final String RAIN_MONTH = CHGRP_WS1400IP + "#" + CH_RAIN_MONTH;
    private static final String RAIN_YEAR = CHGRP_WS1400IP + "#" + CH_RAIN_YEAR;
    private static final String RAIN_TOTAL = CHGRP_WS1400IP + "#" + CH_RAIN_TOTAL;
    private static final String RAIN_EVENT = CHGRP_WS1400IP + "#" + CH_RAIN_EVENT;
    private static final String RAIN_LAST_TIME = CHGRP_WS1400IP + "#" + CH_RAIN_LAST_TIME;
    private static final String INDOOR_TEMPERATURE = CHGRP_INDOOR_SENSOR + "#" + CH_TEMPERATURE;
    private static final String INDOOR_HUMIDITY = CHGRP_INDOOR_SENSOR + "#" + CH_HUMIDITY;
    private static final String INDOOR_BATTERY_INDICATOR = CHGRP_INDOOR_SENSOR + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE1 = CHGRP_REMOTE_SENSOR_1 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY1 = CHGRP_REMOTE_SENSOR_1 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_1 = CHGRP_REMOTE_SENSOR_1 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE2 = CHGRP_REMOTE_SENSOR_2 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY2 = CHGRP_REMOTE_SENSOR_2 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_2 = CHGRP_REMOTE_SENSOR_2 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE3 = CHGRP_REMOTE_SENSOR_3 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY3 = CHGRP_REMOTE_SENSOR_3 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_3 = CHGRP_REMOTE_SENSOR_3 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE4 = CHGRP_REMOTE_SENSOR_4 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY4 = CHGRP_REMOTE_SENSOR_4 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_4 = CHGRP_REMOTE_SENSOR_4 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE5 = CHGRP_REMOTE_SENSOR_5 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY5 = CHGRP_REMOTE_SENSOR_5 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_5 = CHGRP_REMOTE_SENSOR_5 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE6 = CHGRP_REMOTE_SENSOR_6 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY6 = CHGRP_REMOTE_SENSOR_6 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_6 = CHGRP_REMOTE_SENSOR_6 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE7 = CHGRP_REMOTE_SENSOR_7 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY7 = CHGRP_REMOTE_SENSOR_7 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_7 = CHGRP_REMOTE_SENSOR_7 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE8 = CHGRP_REMOTE_SENSOR_8 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY8 = CHGRP_REMOTE_SENSOR_8 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_8 = CHGRP_REMOTE_SENSOR_8 + "#" + CH_BATTERY_INDICATOR;

    // Calculated channels
    private static final String UV_DANGER = CHGRP_WS1400IP + "#" + CH_UV_DANGER;
    private static final String PRESSURE_TREND = CHGRP_WS1400IP + "#" + CH_PRESSURE_TREND;
    private static final String WIND_DIRECTION = CHGRP_WS1400IP + "#" + CH_WIND_DIRECTION;

    // Used to calculate barometric pressure trend
    private PressureTrend pressureTrend = new PressureTrend();

    @Override
    public void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name, String location) {
        // Update name and location channels
        logger.debug("Station {}: Updating station information channels", station);
        handler.updateChannel(NAME, new StringType(name));
        handler.updateChannel(LOCATION, new StringType(location));
    }

    @Override
    public void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData) {
        Gson gson = new Gson();
        try {
            EventDataJson data = gson.fromJson(jsonData, EventDataJson.class);

            logger.debug("Station {}: Updating weather data channels", station);
            // Update the weather data channels for the WS-1400-IP
            handler.updateChannel(OBSERVATION_TIME, getLocalDateTimeType(data.date, handler.getZoneId()));
            handler.updateChannel(BATTERY_INDICATOR, new StringType(data.battout));
            handler.updateChannel(TEMPERATURE, new DecimalType(data.tempf));
            handler.updateChannel(FEELING_TEMPERATURE, new DecimalType(data.feelsLike));
            handler.updateChannel(DEW_POINT, new DecimalType(data.dewPoint));
            handler.updateChannel(HUMIDITY, new DecimalType(data.humidity));
            handler.updateChannel(PRESSURE_ABSOLUTE, new DecimalType(data.baromabsin));
            handler.updateChannel(PRESSURE_RELATIVE, new DecimalType(data.baromrelin));
            handler.updateChannel(SOLAR_RADIATION, new DecimalType(data.solarradiation));
            handler.updateChannel(UV_INDEX, new DecimalType(data.uv));
            handler.updateChannel(WIND_SPEED, new DecimalType(data.windspeedmph));
            handler.updateChannel(WIND_DIRECTION_DEGREES, new DecimalType(data.winddir));
            handler.updateChannel(WIND_GUST, new DecimalType(data.windgustmph));
            handler.updateChannel(WIND_GUST_MAX_DAILY, new DecimalType(data.maxdailygust));
            handler.updateChannel(RAIN_HOURLY_RATE, new DecimalType(data.hourlyrainin));
            handler.updateChannel(RAIN_DAY, new DecimalType(data.dailyrainin));
            handler.updateChannel(RAIN_WEEK, new DecimalType(data.weeklyrainin));
            handler.updateChannel(RAIN_MONTH, new DecimalType(data.monthlyrainin));
            handler.updateChannel(RAIN_YEAR, new DecimalType(data.yearlyrainin));
            handler.updateChannel(RAIN_TOTAL, new DecimalType(data.totalrainin));
            handler.updateChannel(RAIN_EVENT, new DecimalType(data.eventrainin));
            handler.updateChannel(RAIN_LAST_TIME, getLocalDateTimeType(data.lastRain, handler.getZoneId()));
            handler.updateChannel(INDOOR_TEMPERATURE, new DecimalType(data.tempinf));
            handler.updateChannel(INDOOR_HUMIDITY, new DecimalType(data.humidityin));
            handler.updateChannel(INDOOR_BATTERY_INDICATOR, new StringType(data.battin));

            // Calculated channels
            pressureTrend.put(new Double(data.baromrelin));
            handler.updateChannel(PRESSURE_TREND, pressureTrend.getPressureTrend());
            handler.updateChannel(UV_DANGER, new StringType(convertUVIndexToString(data.uv)));
            handler.updateChannel(WIND_DIRECTION, new StringType(convertWindDirectionToString(data.winddir)));

            // Update the remote sensor channels. Since the JSON object doesn't include an indicator,
            // it's necessary to explicitly look for the existence of the remote sensor fields.
            // This would be easier if the remote sensor data was a JSON array.
            remoteSensor.setData(jsonData);

            if (remoteSensor.remoteSensor1Exists()) {
                handler.updateChannel(TEMPERATURE1, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY1, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_1, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor2Exists()) {
                handler.updateChannel(TEMPERATURE2, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY2, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_2, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor3Exists()) {
                handler.updateChannel(TEMPERATURE3, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY3, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_3, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor4Exists()) {
                handler.updateChannel(TEMPERATURE4, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY4, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_4, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor5Exists()) {
                handler.updateChannel(TEMPERATURE5, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY5, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_5, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor6Exists()) {
                handler.updateChannel(TEMPERATURE6, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY6, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_6, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor7Exists()) {
                handler.updateChannel(TEMPERATURE7, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY7, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_7, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor8Exists()) {
                handler.updateChannel(TEMPERATURE8, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY8, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_8, new StringType(remoteSensor.getBattery()));
            }
        } catch (JsonSyntaxException e) {
            logger.info("Station {}: Data event cannot be parsed: {}", station, e.getMessage());
            return;
        }
    }
}
