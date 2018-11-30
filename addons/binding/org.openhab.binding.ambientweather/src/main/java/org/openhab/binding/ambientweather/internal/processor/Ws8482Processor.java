/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Ws8482Processor} is responsible for updating
 * the channels associated with the WS-1400-IP series weather stations in
 * response to the receipt of a weather data update from the Ambient
 * Weather real-time API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Ws8482Processor extends AbstractProcessor {
    // Define the channels we will update
    private static final String NAME = CHGRP_STATION + "#" + CH_NAME;
    private static final String LOCATION = CHGRP_STATION + "#" + CH_LOCATION;

    private static final String OBSERVATION_TIME = CHGRP_WS8482 + "#" + CH_OBSERVATION_TIME;
    private static final String TEMPERATURE = CHGRP_WS8482 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY = CHGRP_WS8482 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR = CHGRP_WS8482 + "#" + CH_BATTERY_INDICATOR;

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
    private static final String TEMPERATURE9 = CHGRP_REMOTE_SENSOR_9 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY9 = CHGRP_REMOTE_SENSOR_9 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_9 = CHGRP_REMOTE_SENSOR_9 + "#" + CH_BATTERY_INDICATOR;
    private static final String TEMPERATURE10 = CHGRP_REMOTE_SENSOR_10 + "#" + CH_TEMPERATURE;
    private static final String HUMIDITY10 = CHGRP_REMOTE_SENSOR_10 + "#" + CH_HUMIDITY;
    private static final String BATTERY_INDICATOR_10 = CHGRP_REMOTE_SENSOR_10 + "#" + CH_BATTERY_INDICATOR;

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
            // Update the weather data channels for the WS-8482
            handler.updateChannel(OBSERVATION_TIME, getLocalDateTimeType(data.date, handler.getZoneId()));
            handler.updateChannel(TEMPERATURE, new DecimalType(data.tempinf));
            handler.updateChannel(HUMIDITY, new DecimalType(data.humidityin));
            handler.updateChannel(BATTERY_INDICATOR, new StringType(data.battout));

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
            if (remoteSensor.remoteSensor9Exists()) {
                handler.updateChannel(TEMPERATURE9, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY9, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_9, new StringType(remoteSensor.getBattery()));
            }
            if (remoteSensor.remoteSensor10Exists()) {
                handler.updateChannel(TEMPERATURE10, new DecimalType(remoteSensor.getTemperature()));
                handler.updateChannel(HUMIDITY10, new DecimalType(remoteSensor.getHumidity()));
                handler.updateChannel(BATTERY_INDICATOR_10, new StringType(remoteSensor.getBattery()));
            }
        } catch (JsonSyntaxException e) {
            logger.info("Station {}: Data event cannot be parsed: {}", station, e.getMessage());
            return;
        }
    }
}
