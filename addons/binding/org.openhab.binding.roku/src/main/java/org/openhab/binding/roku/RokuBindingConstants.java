/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roku;

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
    public static final ThingTypeUID THING_TYPE_ROKU = new ThingTypeUID(BINDING_ID, "roku");

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_UDN = "udn";
    public static final String CHANNEL_SERIAL = "serial-number";
    public static final String CHANNEL_DEVICEID = "device-id";
    public static final String CHANNEL_ADID = "advertising-id";
    public static final String CHANNEL_VENDOR = "vendor";
    public static final String CHANNEL_MODELNAME = "model-name";
    public static final String CHANNEL_MODELNUMBER = "model-number";
    public static final String CHANNEL_MODELREGION = "model-region";
    public static final String CHANNEL_WIFI = "wifi-mac";
    public static final String CHANNEL_ETHERNET = "ethernet-mac";
    public static final String CHANNEL_NETWORK = "network-type";
    public static final String CHANNEL_DEVICENAME = "user-device-name";
    public static final String CHANNEL_SOFTWAREV = "software-version";
    public static final String CHANNEL_SOFTWAREB = "software-build";
    public static final String CHANNEL_SECUREDEVICE = "secure-device";
    public static final String CHANNEL_LANGUAGE = "language";
    public static final String CHANNEL_COUNTRY = "country";
    public static final String CHANNEL_LOCALE = "locale";
    public static final String CHANNEL_TIMEZONE = "time-zone";
    public static final String CHANNEL_TIMEZONEOFF = "time-zone-offset";
    public static final String CHANNEL_SUSPENDED = "supports-suspended";
    public static final String CHANNEL_DEVELOPERENABLED = "developer-enabled";
    public static final String CHANNEL_SEARCHENABLED = "search-enabled";
    public static final String CHANNEL_VOICESEARCHENABLED = "voice-search-enabled";
    public static final String CHANNEL_NOTIFICATIONSENABLED = "notifications-enabled";
    public static final String CHANNEL_HEADPHONESCONNECTED = "headphones-connected";
    public static final String CHANNEL_HOME = "home";
    public static final String CHANNEL_PLAY = "play";
    public static final String CHANNEL_BACK = "back";
    public static final String CHANNEL_REV = "rev";
    public static final String CHANNEL_FWD = "fwd";
    public static final String CHANNEL_SELECT = "select";
    public static final String CHANNEL_LEFT = "left";
    public static final String CHANNEL_RIGHT = "right";
    public static final String CHANNEL_DOWN = "down";
    public static final String CHANNEL_UP = "up";
    public static final String CHANNEL_INSTANTREPLAY = "instant-replay";
    public static final String CHANNEL_INFO = "info";
    public static final String CHANNEL_BACKSPACE = "backspace";
    public static final String CHANNEL_SEARCH = "search";
    public static final String CHANNEL_ENTER = "enter";
    public static final String CHANNEL_ICON = "icon";
    public static final String CHANNEL_APPBROWSER = "appbrowser";

    // Roku config properties
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String SERIAL_NUMBER = "serialNumber";

    // Roku rest context
    public static final String ROKU_DEVICE_INFO = "/query/device-info";
    public static final String ROKU_ACTIVE_APP = "/query/active-app";
    public static final String ROKU_QUERY_APPS = "/query/apps";
}
