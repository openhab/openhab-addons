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
package org.openhab.binding.bticinosmarther.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@code SmartherBindingConstants} class defines the common constants used across the whole binding.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherBindingConstants {

    private static final String BINDING_ID = "bticinosmarther";

    // Date and time formats used by the binding
    public static final String DTF_DATE = "dd/MM/yyyy";
    public static final String DTF_DATETIME = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DTF_DATETIME_EXT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String DTF_TODAY = "'Today at' HH:mm";
    public static final String DTF_TOMORROW = "'Tomorrow at' HH:mm";
    public static final String DTF_DAY_HHMM = "dd/MM/yyyy 'at' HH:mm";

    // Generic constants
    public static final String HTTPS_SCHEMA = "https";
    public static final String NAME_SEPARATOR = ", ";
    public static final String UNAVAILABLE = "N/A";
    public static final String DEFAULT_PROGRAM = "Default";

    // List of BTicino/Legrand API gateway related urls, information
    public static final String SMARTHER_ACCOUNT_URL = "https://partners-login.eliotbylegrand.com";
    public static final String SMARTHER_AUTHORIZE_URL = SMARTHER_ACCOUNT_URL + "/authorize";
    public static final String SMARTHER_API_TOKEN_URL = SMARTHER_ACCOUNT_URL + "/token";
    public static final String SMARTHER_API_SCOPES = Stream.of("comfort.read", "comfort.write")
            .collect(Collectors.joining(" "));
    public static final String SMARTHER_API_URL = "https://api.developer.legrand.com/smarther/v2.0";

    // Servlets and resources aliases
    public static final String AUTH_SERVLET_ALIAS = "/" + BINDING_ID + "/connectsmarther";
    public static final String NOTIFY_SERVLET_ALIAS = "/" + BINDING_ID + "/notifysmarther";
    public static final String IMG_SERVLET_ALIAS = "/img";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_MODULE = new ThingTypeUID(BINDING_ID, "module");

    // List of all common properties
    public static final String PROPERTY_STATUS_REFRESH_PERIOD = "statusRefreshPeriod";

    // List of all bridge properties
    public static final String PROPERTY_SUBSCRIPTION_KEY = "subscriptionKey";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_CLIENT_SECRET = "clientSecret";
    public static final String PROPERTY_NOTIFICATION_URL = "notificationUrl";
    public static final String PROPERTY_NOTIFICATIONS = "notifications";

    // List of all module properties
    public static final String PROPERTY_PLANT_ID = "plantId";
    public static final String PROPERTY_MODULE_ID = "moduleId";
    public static final String PROPERTY_MODULE_NAME = "moduleName";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";

    // List of all bridge Status Channel ids
    public static final String CHANNEL_STATUS_API_CALLS_HANDLED = "status#apiCallsHandled";
    public static final String CHANNEL_STATUS_NOTIFS_RECEIVED = "status#notifsReceived";
    public static final String CHANNEL_STATUS_NOTIFS_REJECTED = "status#notifsRejected";
    // List of all bridge Config Channel ids
    public static final String CHANNEL_CONFIG_FETCH_LOCATIONS = "config#fetchLocations";

    // List of all module Measures Channel ids
    public static final String CHANNEL_MEASURES_TEMPERATURE = "measures#temperature";
    public static final String CHANNEL_MEASURES_HUMIDITY = "measures#humidity";
    // List of all module Status Channel ids
    public static final String CHANNEL_STATUS_STATE = "status#state";
    public static final String CHANNEL_STATUS_FUNCTION = "status#function";
    public static final String CHANNEL_STATUS_MODE = "status#mode";
    public static final String CHANNEL_STATUS_TEMPERATURE = "status#temperature";
    public static final String CHANNEL_STATUS_PROGRAM = "status#program";
    public static final String CHANNEL_STATUS_ENDTIME = "status#endTime";
    public static final String CHANNEL_STATUS_TEMP_FORMAT = "status#temperatureFormat";
    // List of all module Settings Channel ids
    public static final String CHANNEL_SETTINGS_MODE = "settings#mode";
    public static final String CHANNEL_SETTINGS_TEMPERATURE = "settings#temperature";
    public static final String CHANNEL_SETTINGS_PROGRAM = "settings#program";
    public static final String CHANNEL_SETTINGS_BOOSTTIME = "settings#boostTime";
    public static final String CHANNEL_SETTINGS_ENDDATE = "settings#endDate";
    public static final String CHANNEL_SETTINGS_ENDHOUR = "settings#endHour";
    public static final String CHANNEL_SETTINGS_ENDMINUTE = "settings#endMinute";
    public static final String CHANNEL_SETTINGS_POWER = "settings#power";
    // List of all module Config Channel ids
    public static final String CHANNEL_CONFIG_FETCH_PROGRAMS = "config#fetchPrograms";

    // List of all adressable things
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_BRIDGE, THING_TYPE_MODULE).collect(Collectors.toSet()));
}
