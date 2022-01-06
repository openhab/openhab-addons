/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link YamahaMusiccastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastBindingConstants {

    private static final String BINDING_ID = "yamahamusiccast";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Channel Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_UID_POWER = new ChannelTypeUID("system:power");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_MUTE = new ChannelTypeUID("system:mute");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_VOLUME = new ChannelTypeUID("system:volume");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_VOLUMEABS = new ChannelTypeUID(BINDING_ID, "volumeAbs");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_INPUT = new ChannelTypeUID(BINDING_ID, "input");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SOUNDPROGRAM = new ChannelTypeUID(BINDING_ID, "soundProgram");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SELECTPRESET = new ChannelTypeUID(BINDING_ID, "selectPreset");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_SLEEP = new ChannelTypeUID(BINDING_ID, "sleep");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_RECALLSCENE = new ChannelTypeUID(BINDING_ID, "recallScene");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_MCLINKSTATUS = new ChannelTypeUID(BINDING_ID, "mclinkStatus");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_VOLUMEABS = "volumeAbs";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_SOUNDPROGRAM = "soundProgram";
    public static final String CHANNEL_SELECTPRESET = "selectPreset";
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_SLEEP = "sleep";
    public static final String CHANNEL_RECALLSCENE = "recallScene";
    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_TRACK = "track";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_ALBUMART = "albumArt";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_SHUFFLE = "shuffle";
    public static final String CHANNEL_MCLINKSTATUS = "mclinkStatus";
    public static final String CHANNEL_PLAYTIME = "playTime";
    public static final String CHANNEL_TOTALTIME = "totalTime";

    public static final int CONNECTION_TIMEOUT_MILLISEC = 5000;
    public static final int LONG_CONNECTION_TIMEOUT_MILLISEC = 60000;
    public static final String HTTP = "http://";
    public static final String YAMAHA_EXTENDED_CONTROL = "/YamahaExtendedControl/v1/";
}
