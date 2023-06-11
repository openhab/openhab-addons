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
package org.openhab.binding.ambientweather.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AmbientWeatherBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AmbientWeatherBindingConstants {

    private static final String BINDING_ID = "ambientweather";

    // Bridge
    public static final String THING_TYPE_BRIDGE = "bridge";
    public static final ThingTypeUID UID_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_BRIDGE).collect(Collectors.toSet()));

    // WS-1400-IP series weather stations
    public static final String THING_TYPE_WS1400IP = "ws1400ip";
    public static final ThingTypeUID UID_WS1400IP = new ThingTypeUID(BINDING_ID, THING_TYPE_WS1400IP);

    // WS-2902A series weather stations
    public static final String THING_TYPE_WS2902A = "ws2902a";
    public static final ThingTypeUID UID_WS2902A = new ThingTypeUID(BINDING_ID, THING_TYPE_WS2902A);

    // WS-2902B series weather stations
    public static final String THING_TYPE_WS2902B = "ws2902b";
    public static final ThingTypeUID UID_WS2902B = new ThingTypeUID(BINDING_ID, THING_TYPE_WS2902B);

    // WS-8482 weather station
    public static final String THING_TYPE_WS8482 = "ws8482";
    public static final ThingTypeUID UID_WS8482 = new ThingTypeUID(BINDING_ID, THING_TYPE_WS8482);

    // WS-0900-IP series weather stations
    public static final String THING_TYPE_WS0900IP = "ws0900ip";
    public static final ThingTypeUID UID_WS0900IP = new ThingTypeUID(BINDING_ID, THING_TYPE_WS0900IP);

    // WS-0265 weather station
    public static final String THING_TYPE_WS0265 = "ws0265";
    public static final ThingTypeUID UID_WS0265 = new ThingTypeUID(BINDING_ID, THING_TYPE_WS0265);

    // Collection of weather station thing types
    public static final Set<ThingTypeUID> SUPPORTED_STATION_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_WS1400IP, UID_WS2902A, UID_WS2902B, UID_WS8482, UID_WS0900IP, UID_WS0265)
                    .collect(Collectors.toSet()));

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.concat(SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream(), SUPPORTED_STATION_THING_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));

    // Channel groups for specific weather stations
    public static final String CHGRP_WS1400IP = "weatherDataWs1400ip";
    public static final String CHGRP_WS2902A = "weatherDataWs2902a";
    public static final String CHGRP_WS2902B = "weatherDataWs2902b";
    public static final String CHGRP_WS8482 = "weatherDataWs8482";
    public static final String CHGRP_WS0900IP = "weatherDataWs0900ip";
    public static final String CHGRP_WS0265 = "weatherDataWs0265";

    // Channel groups used across weather station types
    public static final String CHGRP_STATION = "station";
    public static final String CHGRP_INDOOR_SENSOR = "indoorSensor";
    public static final String CHGRP_REMOTE_SENSOR = "remoteSensor";

    // Channels Ids for data returned by the ambientweather.net API
    public static final String CH_NAME = "name";
    public static final String CH_LOCATION = "location";
    public static final String CH_BATTERY_INDICATOR = "batteryIndicator";
    public static final String CH_OBSERVATION_TIME = "observationTime";
    public static final String CH_TEMPERATURE = "temperature";
    public static final String CH_FEELING_TEMPERATURE = "feelingTemperature";
    public static final String CH_DEW_POINT = "dewPoint";
    public static final String CH_HUMIDITY = "relativeHumidity";
    public static final String CH_PRESSURE_ABSOLUTE = "pressureAbsolute";
    public static final String CH_PRESSURE_RELATIVE = "pressureRelative";
    public static final String CH_WIND_SPEED = "windSpeed";
    public static final String CH_WIND_DIRECTION_DEGREES = "windDirectionDegrees";
    public static final String CH_WIND_SPEED_MAX_DAILY = "windSpeedMaxDaily";
    public static final String CH_WIND_GUST = "windGust";
    public static final String CH_WIND_GUST_MAX_DAILY = "windGustMaxDaily";
    public static final String CH_WIND_SPEED_AVG_2MIN = "windSpeedAvg2Minute";
    public static final String CH_WIND_DIRECTION_AVG_2MIN = "windDirectionDegreesAvg2Min";
    public static final String CH_WIND_SPEED_AVG_10MIN = "windSpeedAvg10Minute";
    public static final String CH_WIND_DIRECTION_AVG_10MIN = "windDirectionDegreesAvg10Min";
    public static final String CH_RAIN_HOURLY_RATE = "rainHourlyRate";
    public static final String CH_RAIN_DAY = "rainDay";
    public static final String CH_RAIN_WEEK = "rainWeek";
    public static final String CH_RAIN_MONTH = "rainMonth";
    public static final String CH_RAIN_YEAR = "rainYear";
    public static final String CH_RAIN_TOTAL = "rainTotal";
    public static final String CH_RAIN_EVENT = "rainEvent";
    public static final String CH_RAIN_LAST_TIME = "rainLastTime";
    public static final String CH_SOLAR_RADIATION = "solarRadiation";
    public static final String CH_UV_INDEX = "uvIndex";
    public static final String CH_CO2 = "co2";
    public static final String CH_SOIL_TEMPERATURE = "soilTemperature";
    public static final String CH_SOIL_MOISTURE = "soilMoisture";
    public static final String CH_RELAY = "relay";

    // Channel Ids for calculated channels
    public static final String CH_PRESSURE_TREND = "pressureTrend";
    public static final String CH_WIND_DIRECTION = "windDirection";
    public static final String CH_UV_DANGER = "uvDanger";
    public static final String CH_SOIL_MOISTURE_LEVEL = "soilMoistureLevel";

    // Bridge configuration parameters
    public static final String CONFIG_API_KEY = "apiKey";
    public static final String CONFIG_APPLICATION_KEY = "applicationKey";

    // Weather station configuration parameters
    public static final String CONFIG_MAC_ADDRESS = "macAddress";
}
