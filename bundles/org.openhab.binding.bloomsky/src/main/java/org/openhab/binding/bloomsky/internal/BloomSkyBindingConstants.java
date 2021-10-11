/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BloomSkyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
public class BloomSkyBindingConstants {

    private static final String BINDING_ID = "bloomsky";

    public static final String API = "api";
    public static final String IMPERIAL_UNITS = "Imperial";
    public static final String METRIC_UNITS = "Metric";
    public static final String API_KEY_VALUE = "apiKeyValue";
    public static final String CONFIG_UNITS = "configUnits";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String PARAM_UNITS = "unit";
    public static final String TOO_WARM_FOR_WIND_CHILL = "Too warm for wind chill";
    public static final Integer BATTERY_FULLY_CHARGED = 2600;

    // Bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge-api");
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_BRIDGE).collect(Collectors.toSet()));

    // Sky Thing
    public static final ThingTypeUID THING_TYPE_SKY = new ThingTypeUID(BINDING_ID, "sky");

    // Sky Thing Properties
    public static final String SKY_DEVICE_ID = "deviceID";
    public static final String SKY_DEVICE_NAME = "skyName";
    public static final String SKY_DEVICE_MODEL = "skyModel";

    // Base URL for the API in debug log messages
    public static final String BASE_API_URL = "https://api.bloomsky.com/api/skydata/";

    // Channel Group sky
    public static final String CH_GROUP_SKY = "sky-device-information";

    // List of sky Channel IDs
    public static final String CH_SKY_UTC = "utc";
    public static final String CH_SKY_CITY_NAME = "cityName";
    public static final String CH_SKY_SEARCHABLE = "searchable";
    public static final String CH_SKY_DEVICE_NAME = "deviceName";
    public static final String CH_SKY_DST = "dst";
    public static final String CH_SKY_BOUNDED_POINT = "boundedPoint";
    public static final String CH_SKY_LON = "lon";
    public static final String CH_SKY_VIDEO_LIST = "videoList";
    public static final String CH_SKY_VIDEO_LIST_C = "videoListC";
    public static final String CH_SKY_DEVICE_ID = "deviceID";
    public static final String CH_SKY_NUM_OF_FOLLOWERS = "numOfFollowers";
    public static final String CH_SKY_LAT = "lat";
    public static final String CH_SKY_ALT = "alt";
    public static final String CH_SKY_LOCATION = "location";
    public static final String CH_SKY_FULL_ADDRESS = "fullAddress";
    public static final String CH_SKY_STREET_NAME = "streetName";
    public static final String CH_SKY_PREVIEW_IMAGE_LIST = "previewImageList";
    public static final String CH_SKY_REGISTER_TIME = "registerTime";

    // Channel Group sky-observations
    public static final String CH_GROUP_SKY_OBSERVATIONS = "sky-observations";

    // List of sky weather sensor observations Channel IDs
    public static final String CH_SKY_LUMINANCE = "luminance";
    public static final String CH_SKY_TEMPERATURE = "temperature";
    public static final String CH_SKY_IMAGE_URL = "imageURL";
    public static final String CH_SKY_CURRENT_IMAGE = "currentSkyImage";
    public static final String CH_SKY_TS = "tS"; // Time Stamp of last set of observations
    public static final String CH_SKY_RAIN = "rain";
    public static final String CH_SKY_HUMIDITY = "humidity";
    public static final String CH_SKY_PRESSURE = "pressure";
    public static final String CH_SKY_DEVICE_TYPE = "deviceType";
    public static final String CH_SKY_VOLTAGE = "batteryLevel";
    public static final String CH_SKY_NIGHT = "night";
    public static final String CH_SKY_UVINDEX = "skyUVIndex";
    public static final String CH_SKY_IMAGE_TS = "imageTS";

    // Channel Group sky-video-list
    public static final String CH_GROUP_SKY_VIDEO_LIST = "sky-video-list";

    // List of sky-video-list Channel IDs
    public static final String CH_SKY_SKY_VIDEO_DAY_1 = "videoDay1";
    public static final String CH_SKY_SKY_VIDEO_DAY_2 = "videoDay2";
    public static final String CH_SKY_SKY_VIDEO_DAY_3 = "videoDay3";
    public static final String CH_SKY_SKY_VIDEO_DAY_4 = "videoDay4";
    public static final String CH_SKY_SKY_VIDEO_DAY_5 = "videoDay5";

    // Channel Group sky-video-list-c
    public static final String CH_GROUP_SKY_VIDEO_LIST_C = "sky-video-list-c";

    // List of sky-video-list Channel IDs
    public static final String CH_SKY_SKY_VIDEO_C_DAY_1 = "videoCDay1";
    public static final String CH_SKY_SKY_VIDEO_C_DAY_2 = "videoCDay2";
    public static final String CH_SKY_SKY_VIDEO_C_DAY_3 = "videoCDay3";
    public static final String CH_SKY_SKY_VIDEO_C_DAY_4 = "videoCDay4";
    public static final String CH_SKY_SKY_VIDEO_C_DAY_5 = "videoCDay5";

    // Channel Group sky-preview-image-list
    public static final String CH_GROUP_SKY_PREVIEW_IMAGE_LIST = "sky-preview-image-list";

    // List of sky-preview-image-list Channel IDs
    public static final String CH_SKY_SKY_PREVIEW_IMAGE_1 = "previewImage1";
    public static final String CH_SKY_SKY_PREVIEW_IMAGE_2 = "previewImage2";
    public static final String CH_SKY_SKY_PREVIEW_IMAGE_3 = "previewImage3";
    public static final String CH_SKY_SKY_PREVIEW_IMAGE_4 = "previewImage4";
    public static final String CH_SKY_SKY_PREVIEW_IMAGE_5 = "previewImage5";

    // STORM Thing
    public static final ThingTypeUID THING_TYPE_STORM = new ThingTypeUID(BINDING_ID, "storm");
    public static final Set<ThingTypeUID> SUPPORTED_BLOOMSKY_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_SKY, THING_TYPE_STORM).collect(Collectors.toSet()));

    // Sky Thing Properties
    public static final String STORM_ASSOCIATED_WITH_SKY_DEVICE_ID = "stormAssociatedWithSkyDeviceID";
    public static final String STORM_ASSOCAITED_WITH_SKY_DEVICE_NAME = "stormAssociatedWithSkyName";
    public static final String STORM_DEVICE_MODEL = "stormModel";
    public static final String STORM_MODEL = "STORM";

    // Channel Group storm-observations
    public static final String CH_GROUP_STORM = "storm-observations";

    // List of storm weather sensor observations Channel IDs
    public static final String CH_STORM_UV_INDEX = "stormUVIndex";
    public static final String CH_STORM_WIND_DIRECTION_COMPASS_ANGLE = "windDirectionAngle";
    public static final String CH_STORM_WIND_DIRECTION_COMPASS_POINT = "windDirectionCompass";
    public static final String CH_STORM_RAIN_DAILY = "rainDaily";
    public static final String CH_STORM_WIND_GUST = "windGust";
    public static final String CH_STORM_SUSTAINED_WIND_SPEED = "sustainedWindSpeed";
    public static final String CH_STORM_RAIN_RATE = "rainRate";
    public static final String CH_STORM_RAIN_24H = "rain24h";
    public static final String CH_STORM_TIME_STAMP = "stormTimeStamp";

    // Calculated values using observations Dew Point, Wind Chill, and Heat Index
    public static final String CH_CALCULATED_DEW_POINT = "dewPoint";
    public static final String CH_CALCULATED_WIND_CHILL = "windChill";
    public static final String CH_CALCULATED_HEAT_INDEX = "heatIndex";

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.concat(SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream(), SUPPORTED_BLOOMSKY_THING_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));
}
