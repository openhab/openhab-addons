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
package org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.BaseSensorValues;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link WeatherSensorData} acts as container for the digitalSTROM json-method <i>getSensorValues</i>. The
 * {@link WeatherSensorData} contains all
 * {@link org.openhab.binding.digitalstrom.internal.lib.climate.datatypes.CachedSensorValue}s and weather service
 * information of the digitalSTROM-server, if a weather service is set.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class WeatherSensorData extends BaseSensorValues {

    private final Logger logger = LoggerFactory.getLogger(WeatherSensorData.class);

    private String weatherIconId;
    private String weatherConditionId;
    private String weatherServiceId;
    private String weatherServiceTime;

    /**
     * Creates a new {@link SensorValues} through the {@link JsonObject} that will be returned by an apartment call.
     *
     * @param jObject must not be null
     */
    public WeatherSensorData(JsonObject jObject) {
        super.addSensorValue(jObject, true);
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_ICON_ID.getKey()) != null) {
            weatherIconId = jObject.get(JSONApiResponseKeysEnum.WEATHER_ICON_ID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_CONDITION_ID.getKey()) != null) {
            weatherConditionId = jObject.get(JSONApiResponseKeysEnum.WEATHER_CONDITION_ID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_ID.getKey()) != null) {
            weatherServiceId = jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_ID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_TIME.getKey()) != null) {
            weatherServiceTime = jObject.get(JSONApiResponseKeysEnum.WEATHER_SERVICE_TIME.getKey()).getAsString();
        }
    }

    /**
     * Returns the weather icon id of the set weather service.
     *
     * @return the weatherIconId
     */
    public String getWeatherIconId() {
        return weatherIconId;
    }

    /**
     * Returns the weather condition id of the set weather service.
     *
     * @return the weatherConditionId
     */
    public String getWeatherConditionId() {
        return weatherConditionId;
    }

    /**
     * Returns the weather service id of the set weather service.
     *
     * @return the weatherServiceId
     */
    public String getWeatherServiceId() {
        return weatherServiceId;
    }

    /**
     * Returns the weather service time as {@link String} of the set weather service.
     *
     * @return the weatherServiceTime as {@link String}
     */
    public String getWeatherServiceTimeAsSting() {
        return weatherServiceTime;
    }

    /**
     * Returns the weather service time as {@link Date} of the set weather service.
     *
     * @return the weatherServiceTime as {@link Date}
     */
    public Date getWeatherServiceTimeAsDate() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
        try {
            return formatter.parse(weatherServiceTime);
        } catch (ParseException e) {
            logger.error("A ParseException occurred by parsing date string: {}", weatherServiceTime, e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WeatherSensorData [weatherIconId=" + weatherIconId + ", weatherConditionId=" + weatherConditionId
                + ", weatherServiceId=" + weatherServiceId + ", weatherServiceTime=" + weatherServiceTime + ", "
                + super.toString() + "]";
    }
}
