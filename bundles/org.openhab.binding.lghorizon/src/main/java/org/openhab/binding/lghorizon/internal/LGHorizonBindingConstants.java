/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LGHorizonBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class LGHorizonBindingConstants {

    public static final String BINDING_ID = "lghorizon";

    // Thing type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_BOX = new ThingTypeUID(BINDING_ID, "box");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_BOX);

    // Account (bridge) config
    public static final String CONFIG_PROVIDER = "provider";
    public static final String CONFIG_COUNTRY = "country";
    public static final String CONFIG_API_URL = "apiUrl";
    public static final String CONFIG_USE_REFRESH_TOKEN = "useRefreshToken";
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_REFRESH_TOKEN = "refreshToken";

    // Account properties
    public static final String PROPERTY_HOUSEHOLD_ID = "householdId";
    public static final String PROPERTY_CUSTOMER_ID = "customerId";
    public static final String PROPERTY_COUNTRY_ID = "countryId";
    public static final String PROPERTY_CITY_ID = "cityId";

    // Box config
    public static final String CONFIG_DEVICE_ID = "deviceId";
    public static final String CONFIG_PROFILE_ID = "profileId";

    // Box properties
    public static final String PROPERTY_DEFAULT_PROFILE_ID = "defaultProfileId";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_PLATFORM_TYPE = "platformType";
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String PROPERTY_WIFI_MAC_ADDRESS = "wifiMacAddress";
    public static final String PROPERTY_ETHERNET_MAC_ADDRESS = "ethernetMacAddress";
    public static final int DEFAULT_DISPLAY_MESSAGE_DURATION_SECONDS = 10;

    // Channel IDs
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_SOURCE_TYPE = "source-type";
    public static final String CHANNEL_CHANNEL_NAME = "channel-name";
    public static final String CHANNEL_CHANNEL_NUMBER = "channel-number";
    public static final String CHANNEL_FAVORITE_CHANNEL_NUMBER = "favorite-channel-number";
    public static final String CHANNEL_PROGRAM_TITLE = "program-title";
    public static final String CHANNEL_SERIES_TITLE = "series-title";
    public static final String CHANNEL_EPISODE_TITLE = "episode-title";
    public static final String CHANNEL_SEASON = "season";
    public static final String CHANNEL_EPISODE = "episode";
    public static final String CHANNEL_MEDIA_IMAGE = "media-image";
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_RECORD = "record";
    public static final String CHANNEL_CHANNEL_UP = "channel-up";
    public static final String CHANNEL_CHANNEL_DOWN = "channel-down";
    public static final String CHANNEL_ARROW_UP = "arrow-up";
    public static final String CHANNEL_ARROW_DOWN = "arrow-down";
    public static final String CHANNEL_ARROW_LEFT = "arrow-left";
    public static final String CHANNEL_ARROW_RIGHT = "arrow-right";
    public static final String CHANNEL_TOP_MENU = "top-menu";
    public static final String CHANNEL_INFO = "info";
    public static final String CHANNEL_CONTEXT_MENU = "context-menu";
    public static final String CHANNEL_TV = "tv";
    public static final String CHANNEL_ENTER = "enter";
    public static final String CHANNEL_ESCAPE = "escape";
    public static final String CHANNEL_KEY_CODE = "key-code";

    private LGHorizonBindingConstants() {
        // constants class
    }
}
