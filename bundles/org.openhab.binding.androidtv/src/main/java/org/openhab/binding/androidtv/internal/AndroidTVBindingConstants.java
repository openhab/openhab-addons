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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GOOGLETV, THING_TYPE_SHIELDTV);

    // List of all Channel ids
    public static final String CHANNEL_DEBUG = "debug";
    public static final String CHANNEL_KEYBOARD = "keyboard";
    public static final String CHANNEL_KEYPRESS = "keypress";
    public static final String CHANNEL_KEYCODE = "keycode";
    public static final String CHANNEL_PINCODE = "pincode";
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_APPNAME = "appname";
    public static final String CHANNEL_APPURL = "appurl";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_PLAYER = "player";

    // List of all config properties
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_GTV_ENABLED = "gtvEnabled";

    // List of all static String literals
    public static final String PIN_REQUEST = "REQUEST";
}
