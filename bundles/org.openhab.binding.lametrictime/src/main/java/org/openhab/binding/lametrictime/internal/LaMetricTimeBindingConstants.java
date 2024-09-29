/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LaMetricTimeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeBindingConstants {

    public static final String BINDING_ID = "lametrictime";

    // Bridge (device) thing
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // App things
    public static final ThingTypeUID THING_TYPE_CLOCK_APP = new ThingTypeUID(BINDING_ID, "clockApp");
    public static final ThingTypeUID THING_TYPE_COUNTDOWN_APP = new ThingTypeUID(BINDING_ID, "countdownApp");
    public static final ThingTypeUID THING_TYPE_RADIO_APP = new ThingTypeUID(BINDING_ID, "radioApp");
    public static final ThingTypeUID THING_TYPE_STOPWATCH_APP = new ThingTypeUID(BINDING_ID, "stopwatchApp");
    public static final ThingTypeUID THING_TYPE_WEATHER_APP = new ThingTypeUID(BINDING_ID, "weatherApp");

    // List of all Channel ids
    public static final String CHANNEL_NOTIFICATIONS_INFO = "info";
    public static final String CHANNEL_NOTIFICATIONS_WARN = "warning";
    public static final String CHANNEL_NOTIFICATIONS_ALERT = "alert";

    public static final String CHANNEL_DISPLAY_BRIGHTNESS = "brightness";
    public static final String CHANNEL_DISPLAY_BRIGHTNESS_MODE = "brightnessMode";
    public static final String CHANNEL_AUDIO_VOLUME = "volume";
    public static final String CHANNEL_BLUETOOTH_ACTIVE = "bluetooth";
    public static final String CHANNEL_APP = "app";

    public static final String CHANNEL_APP_COMMAND = "command";
    public static final String CHANNEL_APP_SET_ALARM = "setAlarm";
    public static final String CHANNEL_APP_DURATION = "duration";
    public static final String CHANNEL_APP_CONTROL = "control";

    // List of non-standard Properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BT_DISCOVERABLE = "bluetoothDiscoverable";
    public static final String PROPERTY_BT_AVAILABLE = "bluetoothAvailable";
    public static final String PROPERTY_BT_PAIRABLE = "bluetoothPairable";
    public static final String PROPERTY_BT_MAC = "bluetoothMAC";
    public static final String PROPERTY_BT_NAME = "bluetoothName";
}
