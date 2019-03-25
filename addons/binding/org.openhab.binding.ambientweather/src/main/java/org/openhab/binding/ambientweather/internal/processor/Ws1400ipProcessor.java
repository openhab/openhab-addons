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
package org.openhab.binding.ambientweather.internal.processor;

import static org.openhab.binding.ambientweather.internal.AmbientWeatherBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.openhab.binding.ambientweather.internal.model.EventDataJson;
import org.openhab.binding.ambientweather.internal.util.PressureTrend;

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

    // Used to calculate barometric pressure trend
    private PressureTrend pressureTrend = new PressureTrend();

    public Ws1400ipProcessor() {
        // Set the number of remote sensor channels supported by this station
        remoteSensor.setNumberOfSensors(8);
    }

    @Override
    public void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name, String location) {
        // Update name and location channels
        logger.debug("Station {}: Updating station information channels", station);
        handler.updateChannel(CHGRP_STATION + "#" + CH_NAME, new StringType(name));
        handler.updateChannel(CHGRP_STATION + "#" + CH_LOCATION, new StringType(location));
    }

    @Override
    public void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData) {
        try {
            EventDataJson data = gson.fromJson(jsonData, EventDataJson.class);
            logger.debug("Station {}: Updating weather data channels", station);

            // Update the weather data channels for the WS-1400-IP
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_OBSERVATION_TIME,
                    getLocalDateTimeType(data.date, handler.getZoneId()));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_BATTERY_INDICATOR, new StringType(data.battout));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_TEMPERATURE,
                    new QuantityType<>(data.tempf, ImperialUnits.FAHRENHEIT));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_FEELING_TEMPERATURE,
                    new QuantityType<>(data.feelsLike, ImperialUnits.FAHRENHEIT));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_DEW_POINT,
                    new QuantityType<>(data.dewPoint, ImperialUnits.FAHRENHEIT));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_HUMIDITY,
                    new QuantityType<>(data.humidity, SmartHomeUnits.PERCENT));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_PRESSURE_ABSOLUTE,
                    new QuantityType<>(data.baromabsin, ImperialUnits.INCH_OF_MERCURY));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_PRESSURE_RELATIVE,
                    new QuantityType<>(data.baromrelin, ImperialUnits.INCH_OF_MERCURY));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_SOLAR_RADIATION,
                    new QuantityType<>(data.solarradiation, SmartHomeUnits.IRRADIANCE));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_UV_INDEX, new DecimalType(data.uv));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_WIND_SPEED,
                    new QuantityType<>(data.windspeedmph, ImperialUnits.MILES_PER_HOUR));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_WIND_DIRECTION_DEGREES,
                    new QuantityType<>(data.winddir, SmartHomeUnits.DEGREE_ANGLE));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_WIND_GUST,
                    new QuantityType<>(data.windgustmph, ImperialUnits.MILES_PER_HOUR));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_WIND_GUST_MAX_DAILY,
                    new QuantityType<>(data.maxdailygust, ImperialUnits.MILES_PER_HOUR));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_HOURLY_RATE, new DecimalType(data.hourlyrainin));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_DAY,
                    new QuantityType<>(data.dailyrainin, ImperialUnits.INCH));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_WEEK,
                    new QuantityType<>(data.weeklyrainin, ImperialUnits.INCH));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_MONTH,
                    new QuantityType<>(data.monthlyrainin, ImperialUnits.INCH));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_YEAR,
                    new QuantityType<>(data.yearlyrainin, ImperialUnits.INCH));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_TOTAL,
                    new QuantityType<>(data.totalrainin, ImperialUnits.INCH));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_EVENT,
                    new QuantityType<>(data.eventrainin, ImperialUnits.INCH));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_RAIN_LAST_TIME,
                    getLocalDateTimeType(data.lastRain, handler.getZoneId()));

            // Update the calculated channels
            pressureTrend.put(new Double(data.baromrelin));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_PRESSURE_TREND, pressureTrend.getPressureTrend());
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_UV_DANGER, new StringType(convertUVIndexToString(data.uv)));
            handler.updateChannel(CHGRP_WS1400IP + "#" + CH_WIND_DIRECTION,
                    new StringType(convertWindDirectionToString(data.winddir)));

            // Update channels for the indoor sensor
            handler.updateChannel(CHGRP_INDOOR_SENSOR + "#" + CH_TEMPERATURE,
                    new QuantityType<>(data.tempinf, ImperialUnits.FAHRENHEIT));
            handler.updateChannel(CHGRP_INDOOR_SENSOR + "#" + CH_HUMIDITY,
                    new QuantityType<>(data.humidityin, SmartHomeUnits.PERCENT));
            handler.updateChannel(CHGRP_INDOOR_SENSOR + "#" + CH_BATTERY_INDICATOR, new StringType(data.battin));

            // Update channels for the remote sensors
            remoteSensor.updateChannels(handler, jsonData);
        } catch (JsonSyntaxException e) {
            logger.info("Station {}: Data event cannot be parsed: {}", station, e.getMessage());
            return;
        }
    }
}
