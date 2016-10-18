/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RokuBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jarod Peters - Initial contribution
 */
public class RokuBindingConstants {

    public static final String BINDING_ID = "roku";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ROKU = new ThingTypeUID(BINDING_ID, "roku");

    // List of all Channel ids
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_ACTIVE = "active";
    public final static String CHANNEL_UDN = "udn";
    public final static String CHANNEL_SERIAL = "serial-number";
    public final static String CHANNEL_DEVICEID = "device-id";
    public final static String CHANNEL_ADID = "advertising-id";
    public final static String CHANNEL_VENDOR = "vendor";
    public final static String CHANNEL_MODELNAME = "model-name";
    public final static String CHANNEL_MODELNUMBER = "model-number";
    public final static String CHANNEL_MODELREGION = "model-region";
    public final static String CHANNEL_WIFI = "wifi-mac";
    public final static String CHANNEL_ETHERNET = "ethernet-mac";
    public final static String CHANNEL_NETWORK = "network-type";
    public final static String CHANNEL_DEVICENAME = "user-device-name";
    public final static String CHANNEL_SOFTWAREV = "software-version";
    public final static String CHANNEL_SOFTWAREB = "software-build";
    public final static String CHANNEL_SECUREDEVICE = "secure-device";
    public final static String CHANNEL_LANGUAGE = "language";
    public final static String CHANNEL_COUNTRY = "country";
    public final static String CHANNEL_LOCALE = "locale";
    public final static String CHANNEL_TIMEZONE = "time-zone";
    public final static String CHANNEL_TIMEZONEOFF = "time-zone-offset";
    public final static String CHANNEL_SUSPENDED = "supports-suspended";
    public final static String CHANNEL_DEVELOPERENABLED = "developer-enabled";
    public final static String CHANNEL_SEARCHENABLED = "search-enabled";
    public final static String CHANNEL_VOICESEARCHENABLED = "voice-search-enabled";
    public final static String CHANNEL_NOTIFICATIONSENABLED = "notifications-enabled";
    public final static String CHANNEL_HEADPHONESCONNECTED = "headphones-connected";
    public final static String CHANNEL_HOME = "home";
    public final static String CHANNEL_PLAY = "play";
    public final static String CHANNEL_BACK = "back";
    public final static String CHANNEL_REV = "rev";
    public final static String CHANNEL_FWD = "fwd";
    public final static String CHANNEL_SELECT = "select";
    public final static String CHANNEL_LEFT = "left";
    public final static String CHANNEL_RIGHT = "right";
    public final static String CHANNEL_DOWN = "down";
    public final static String CHANNEL_UP = "up";
    public final static String CHANNEL_INSTANTREPLAY = "instant-replay";
    public final static String CHANNEL_INFO = "info";
    public final static String CHANNEL_BACKSPACE = "backspace";
    public final static String CHANNEL_SEARCH = "search";
    public final static String CHANNEL_ENTER = "enter";
    public final static String CHANNEL_ICON = "icon";
    public final static String CHANNEL_APPBROWSER = "appbrowser";

    // Roku config properties
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String SERIAL_NUMBER = "serialNumber";

    // Roku rest context
    public static final String ROKU_DEVICE_INFO = "/query/device-info";
    public static final String ROKU_ACTIVE_APP = "/query/active-app";
    public static final String ROKU_QUERY_APPS = "/query/apps";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ROKU);

}
