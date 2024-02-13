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
package org.openhab.binding.androidtv.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AndroidTVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class AndroidTVBindingConstants {

    private static final String BINDING_ID = "androidtv";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GOOGLETV = new ThingTypeUID(BINDING_ID, "googletv");
    public static final ThingTypeUID THING_TYPE_SHIELDTV = new ThingTypeUID(BINDING_ID, "shieldtv");
    public static final ThingTypeUID THING_TYPE_PHILIPSTV = new ThingTypeUID(BINDING_ID, "philipstv");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GOOGLETV, THING_TYPE_SHIELDTV,
            THING_TYPE_PHILIPSTV);

    // List of all Channel ids
    public static final String CHANNEL_DEBUG = "debug";
    public static final String CHANNEL_KEYBOARD = "keyboard";
    public static final String CHANNEL_KEYPRESS = "keypress";
    public static final String CHANNEL_KEYCODE = "keycode";
    public static final String CHANNEL_PINCODE = "pincode";
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_APPNAME = "appname";
    public static final String CHANNEL_APPURL = "appurl";
    public static final String CHANNEL_APP_ICON = "appicon";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_CONTRAST = "contrast";
    public static final String CHANNEL_SHARPNESS = "sharpness";
    public static final String CHANNEL_TV_CHANNEL = "tvChannel";
    public static final String CHANNEL_SEARCH_CONTENT = "searchContent";
    public static final String CHANNEL_AMBILIGHT = "ambilight";
    public static final String CHANNEL_AMBILIGHT_POWER = "ambilightPower";
    public static final String CHANNEL_AMBILIGHT_HUE_POWER = "ambilightHuePower";
    public static final String CHANNEL_AMBILIGHT_LOUNGE_POWER = "ambilightLoungePower";
    public static final String CHANNEL_AMBILIGHT_STYLE = "ambilightStyle";
    public static final String CHANNEL_AMBILIGHT_COLOR = "ambilightColor";
    public static final String CHANNEL_AMBILIGHT_LEFT_COLOR = "ambilightLeftColor";
    public static final String CHANNEL_AMBILIGHT_RIGHT_COLOR = "ambilightRightColor";
    public static final String CHANNEL_AMBILIGHT_TOP_COLOR = "ambilightTopColor";
    public static final String CHANNEL_AMBILIGHT_BOTTOM_COLOR = "ambilightBottomColor";

    // List of all config properties
    public static final String PARAMETER_IP_ADDRESS = "ipAddress";
    public static final String PARAMETER_GOOGLETV_PORT = "googletvPort";
    public static final String PARAMETER_SHIELDTV_PORT = "shieldtvPort";
    public static final String PARAMETER_PHILIPSTV_PORT = "philipstvPort";
    public static final String PARAMETER_GTV_ENABLED = "gtvEnabled";

    // List of all static String literals
    public static final String PIN_REQUEST = "REQUEST";
}
