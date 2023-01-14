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
package org.openhab.binding.weathercompany.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WeatherCompanyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class WeatherCompanyBindingConstants {
    private static final String BINDING_ID = "weathercompany";

    // Bridge
    public static final String THING_TYPE_BRIDGE = "account";
    public static final ThingTypeUID UID_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_BRIDGE).collect(Collectors.toSet()));

    // Thing Types
    public static final ThingTypeUID THING_TYPE_WEATHER_OBSERVATIONS = new ThingTypeUID(BINDING_ID,
            "weather-observations");
    public static final ThingTypeUID THING_TYPE_WEATHER_FORECAST = new ThingTypeUID(BINDING_ID, "weather-forecast");

    // Collection of weather station thing types
    public static final Set<ThingTypeUID> SUPPORTED_WEATHER_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_WEATHER_OBSERVATIONS, THING_TYPE_WEATHER_FORECAST).collect(Collectors.toSet()));

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.concat(SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream(), SUPPORTED_WEATHER_THING_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));

    // Thing configuration
    public static final String CONFIG_LOCATION_TYPE = "locationType";
    public static final String CONFIG_LOCATION_TYPE_GEOCODE = "geocode";
    public static final String CONFIG_LOCATION_TYPE_POSTAL_CODE = "postalCode";
    public static final String CONFIG_LOCATION_TYPE_IATA_CODE = "iataCode";
    public static final String CONFIG_GEOCODE = "geocode";
    public static final String CONFIG_POSTAL_CODE = "postalCode";
    public static final String CONFIG_IATA_CODE = "iataCode";
    public static final String CONFIG_LANGUAGE = "language";
    public static final String CONFIG_LANGUAGE_DEFAULT = "en-US";
    public static final String CONFIG_PWS_STATION_ID = "pwsStationId";

    // Used to sanitize the API key in the URL in debug log messages
    public static final String REPLACE_API_KEY = "XXXXXXXXXXXXXXXXXXXXX";

    // List of pwsObservations channel IDs
    public static final String CH_PWS_HUMIDITY = "currentHumidity";
    public static final String CH_PWS_PRECIPTATION_RATE = "currentPrecipitationRate";
    public static final String CH_PWS_PRECIPITATION_TOTAL = "currentPrecipitationTotal";
    public static final String CH_PWS_PRESSURE = "currentPressure";
    public static final String CH_PWS_SOLAR_RADIATION = "currentSolarRadiation";
    public static final String CH_PWS_TEMP = "currentTemperature";
    public static final String CH_PWS_TEMP_DEW_POINT = "currentTemperatureDewPoint";
    public static final String CH_PWS_TEMP_HEAT_INDEX = "currentTemperatureHeatIndex";
    public static final String CH_PWS_TEMP_WIND_CHILL = "currentTemperatureWindChill";
    public static final String CH_PWS_UV = "currentUv";
    public static final String CH_PWS_WIND_DIRECTION = "currentWindDirection";
    public static final String CH_PWS_WIND_SPEED = "currentWindSpeed";
    public static final String CH_PWS_WIND_GUST = "currentWindSpeedGust";
    public static final String CH_PWS_COUNTRY = "country";
    public static final String CH_PWS_LOCATION = "location";
    public static final String CH_PWS_ELEVATION = "elevation";
    public static final String CH_PWS_NEIGHBORHOOD = "neighborhood";
    public static final String CH_PWS_OBSERVATION_TIME_LOCAL = "observationTimeLocal";
    public static final String CH_PWS_QC_STATUS = "qcStatus";
    public static final String CH_PWS_SOFTWARE_TYPE = "softwareType";
    public static final String CH_PWS_STATION_ID = "stationId";

    // Channel group forecastDay
    public static final String CH_GROUP_FORECAST_DAY = "forecastDay";

    // List of forecastDay channel IDs
    public static final String CH_DAY_OF_WEEK = "dayOfWeek";
    public static final String CH_VALID_TIME_LOCAL = "validTimeLocal";
    public static final String CH_EXPIRATION_TIME_LOCAL = "expirationTimeLocal";
    public static final String CH_NARRATIVE = "narrative";
    public static final String CH_TEMP_MIN = "temperatureMin";
    public static final String CH_TEMP_MAX = "temperatureMax";
    public static final String CH_PRECIP_RAIN = "precipitationRain";
    public static final String CH_PRECIP_SNOW = "precipitationSnow";

    // Channel group forecastDaypart
    public static final String CH_GROUP_FORECAST_DAYPART_DAY = "Day";
    public static final String CH_GROUP_FORECAST_DAYPART_NIGHT = "Night";

    // List of forecastDaypart channel IDs
    public static final String CH_DP_NAME = "daypartName";
    public static final String CH_DP_DAY_OR_NIGHT = "dayOrNight";
    public static final String CH_DP_NARRATIVE = "narrative";
    public static final String CH_DP_WX_PHRASE_SHORT = "wxPhraseShort";
    public static final String CH_DP_WX_PHRASE_LONG = "wxPhraseLong";
    public static final String CH_DP_TEMP = "temperature";
    public static final String CH_DP_TEMP_HEAT_INDEX = "temperatureHeatIndex";
    public static final String CH_DP_TEMP_WIND_CHILL = "temperatureWindChill";
    public static final String CH_DP_HUMIDITY = "relativeHumidity";
    public static final String CH_DP_CLOUD_COVER = "cloudCover";
    public static final String CH_DP_WIND_SPEED = "windSpeed";
    public static final String CH_DP_WIND_DIR = "windDirection";
    public static final String CH_DP_WIND_DIR_CARDINAL = "windDirectionCardinal";
    public static final String CH_DP_WIND_PHRASE = "WindPhrase";
    public static final String CH_DP_PRECIP_CHANCE = "precipitationChance";
    public static final String CH_DP_PRECIP_TYPE = "precipitationType";
    public static final String CH_DP_PRECIP_RAIN = "precipitationRain";
    public static final String CH_DP_PRECIP_SNOW = "precipitationSnow";
    public static final String CH_DP_SNOW_RANGE = "snowRange";
    public static final String CH_DP_THUNDER_CATEGORY = "thunderCategory";
    public static final String CH_DP_THUNDER_INDEX = "thunderIndex";
    public static final String CH_DP_UV_DESCRIPTION = "uvDescription";
    public static final String CH_DP_UV_INDEX = "uvIndex";
    public static final String CH_DP_ICON_CODE = "iconCode";
    public static final String CH_DP_ICON_CODE_EXTEND = "iconCodeExtend";
    public static final String CH_DP_ICON_IMAGE = "iconImage";
    public static final String CH_DP_QUALIFIER_PHRASE = "qualifierPhrase";
    public static final String CH_DP_QUALIFIER_CODE = "qualifierCode";
}
