/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EmbyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyBindingConstants {

    private static final String BINDING_ID = "emby";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_EMBY_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_EMBY_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_CONTROL = "control";

    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_SHOWTITLE = "show-title";

    public static final String CHANNEL_MEDIATYPE = "media-type";

    public static final String CHANNEL_CURRENTTIME = "current-time";

    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_IMAGEURL = "image-url";
    public static final String CHANNEL_IMAGEURL_CONFIG_TYPE = "imageUrlType";
    public static final String CHANNEL_IMAGEURL_CONFIG_MAXWIDTH = "imageUrlMaxWidth";
    public static final String CHANNEL_IMAGEURL_CONFIG_MAXHEIGHT = "imageUrlMaxHeight";
    public static final String CHANNEL_IMAGEURL_CONFIG_PERCENTPLAYED = "imageUrlPercentPlayed";

    // Module Properties

    public static final String CONFIG_HOST_PARAMETER = "ipAddress";
    public static final String CONFIG_WS_PORT_PARAMETER = "port";
    public static final String CONFIG_REFRESH_PARAMETER = "refreshInterval";
    public static final String CONFIG_API_KEY = "api";
    public static final String CONFIG_DEVICE_ID = "deviceID";
    public static final String CONFIG_DISCOVERY_ENABLE = "discovery";
    // control constant commands
    public static final String CONTROL_SESSION = "/Sessions/";
    public static final String CONTROL_GENERALCOMMAND = "/Command/";
    public static final String CONTROL_SENDPLAY = "/Playing";
    public static final String CONTROL_PLAY = "/Playing/Unpause";
    public static final String CONTROL_PAUSE = "/Playing/Pause";
    public static final String CONTROL_MUTE = "/Command/Mute";
    public static final String CONTROL_UNMUTE = "/Command/Unmute";
    public static final String CONTROL_STOP = "/Playing/Stop";
    public static final int CONNECTION_CHECK_INTERVAL_MS = 360000;
}
