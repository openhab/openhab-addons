/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.ambientweather.internal.handler.AmbientWeatherStationHandler;
import org.openhab.binding.ambientweather.internal.model.EventDataJson;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link Ws2902aProcessor} is responsible for updating
 * the channels associated with the WS-2902A weather stations in
 * response to the receipt of a weather data update from the Ambient
 * Weather real-time API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class Ws2902aProcessor extends AbstractProcessor {

    @Override
    public void setChannelGroupId() {
        channelGroupId = CHGRP_WS2902A;
    }

    @Override
    public void setNumberOfSensors() {
        // This station doesn't support remote sensors
    }

    @Override
    public void processInfoUpdate(AmbientWeatherStationHandler handler, String station, String name, String location) {
        // Update name and location channels
        handler.updateString(CHGRP_STATION, CH_NAME, name);
        handler.updateString(CHGRP_STATION, CH_LOCATION, location);
    }

    @Override
    public void processWeatherData(AmbientWeatherStationHandler handler, String station, String jsonData) {
        EventDataJson data = parseEventData(station, jsonData);
        if (data == null) {
            return;
        }

        // Update the weather data channels
        handler.updateDate(channelGroupId, CH_OBSERVATION_TIME, data.date);
        handler.updateString(channelGroupId, CH_BATTERY_INDICATOR, NOT_APPLICABLE);
        handler.updateQuantity(channelGroupId, CH_TEMPERATURE, data.tempf, ImperialUnits.FAHRENHEIT);
        handler.updateQuantity(channelGroupId, CH_FEELING_TEMPERATURE, data.feelsLike, ImperialUnits.FAHRENHEIT);
        handler.updateQuantity(channelGroupId, CH_DEW_POINT, data.dewPoint, ImperialUnits.FAHRENHEIT);
        handler.updateQuantity(channelGroupId, CH_HUMIDITY, data.humidity, Units.PERCENT);
        handler.updateQuantity(channelGroupId, CH_PRESSURE_ABSOLUTE, data.baromabsin, ImperialUnits.INCH_OF_MERCURY);
        handler.updateQuantity(channelGroupId, CH_PRESSURE_RELATIVE, data.baromrelin, ImperialUnits.INCH_OF_MERCURY);
        handler.updateQuantity(channelGroupId, CH_WIND_SPEED, data.windspeedmph, ImperialUnits.MILES_PER_HOUR);
        handler.updateQuantity(channelGroupId, CH_WIND_DIRECTION_DEGREES, data.winddir, Units.DEGREE_ANGLE);
        handler.updateQuantity(channelGroupId, CH_WIND_GUST, data.windgustmph, ImperialUnits.MILES_PER_HOUR);
        handler.updateQuantity(channelGroupId, CH_WIND_GUST_MAX_DAILY, data.maxdailygust, ImperialUnits.MILES_PER_HOUR);
        handler.updateQuantity(channelGroupId, CH_SOLAR_RADIATION, data.solarradiation, Units.IRRADIANCE);
        handler.updateNumber(channelGroupId, CH_UV_INDEX, data.uv);
        handler.updateQuantity(channelGroupId, CH_RAIN_HOURLY_RATE, data.hourlyrainin, Units.INCHES_PER_HOUR);
        handler.updateQuantity(channelGroupId, CH_RAIN_DAY, data.dailyrainin, ImperialUnits.INCH);
        handler.updateQuantity(channelGroupId, CH_RAIN_WEEK, data.weeklyrainin, ImperialUnits.INCH);
        handler.updateQuantity(channelGroupId, CH_RAIN_MONTH, data.monthlyrainin, ImperialUnits.INCH);
        handler.updateQuantity(channelGroupId, CH_RAIN_YEAR, data.yearlyrainin, ImperialUnits.INCH);
        handler.updateQuantity(channelGroupId, CH_RAIN_TOTAL, data.totalrainin, ImperialUnits.INCH);
        handler.updateQuantity(channelGroupId, CH_RAIN_EVENT, data.eventrainin, ImperialUnits.INCH);
        handler.updateDate(channelGroupId, CH_RAIN_LAST_TIME, data.lastRain);

        // Update calculated channels
        if (data.baromrelin != null) {
            pressureTrend.put(data.baromrelin);
            handler.updateString(channelGroupId, CH_PRESSURE_TREND, pressureTrend.getPressureTrend());
        }
        if (data.winddir != null) {
            handler.updateString(channelGroupId, CH_WIND_DIRECTION, convertWindDirectionToString(data.winddir));
        }
        if (data.uv != null) {
            handler.updateString(channelGroupId, CH_UV_DANGER, convertUVIndexToString(data.uv));
        }

        // Update indoor sensor channels
        handler.updateQuantity(CHGRP_INDOOR_SENSOR, CH_TEMPERATURE, data.tempinf, ImperialUnits.FAHRENHEIT);
        handler.updateQuantity(CHGRP_INDOOR_SENSOR, CH_HUMIDITY, data.humidityin, Units.PERCENT);
        handler.updateString(CHGRP_INDOOR_SENSOR, CH_BATTERY_INDICATOR, NOT_APPLICABLE);
    }
}
