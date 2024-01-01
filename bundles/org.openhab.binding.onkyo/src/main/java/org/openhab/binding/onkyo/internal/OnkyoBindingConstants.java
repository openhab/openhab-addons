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
package org.openhab.binding.onkyo.internal;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OnkyoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Pauli Anttila - update for openhab 2
 * @author Stewart Cossey - add additional receiver models
 * @author Wouter Born - Enumerate supported models using OnkyoModel enum
 */
@NonNullByDefault
public class OnkyoBindingConstants {

    public static final String BINDING_ID = "onkyo";

    // List of Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ONKYOAV = new ThingTypeUID(BINDING_ID, "onkyoAVR");
    public static final ThingTypeUID THING_TYPE_ONKYO_UNSUPPORTED = new ThingTypeUID(BINDING_ID, "onkyoUnsupported");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(Stream.of(THING_TYPE_ONKYOAV, THING_TYPE_ONKYO_UNSUPPORTED),
                    Arrays.stream(OnkyoModel.values()).map(model -> new ThingTypeUID(BINDING_ID, model.getId())))
            .collect(Collectors.toSet());

    // List of thing parameters names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String TCP_PORT_PARAMETER = "port";
    public static final String UDN_PARAMETER = "udn";
    public static final String REFRESH_INTERVAL = "refreshInterval";

    // List of all Channel ids
    public static final String CHANNEL_POWER = "zone1#power";
    public static final String CHANNEL_INPUT = "zone1#input";
    public static final String CHANNEL_MUTE = "zone1#mute";
    public static final String CHANNEL_VOLUME = "zone1#volume";

    public static final String CHANNEL_POWERZONE2 = "zone2#power";
    public static final String CHANNEL_INPUTZONE2 = "zone2#input";
    public static final String CHANNEL_MUTEZONE2 = "zone2#mute";
    public static final String CHANNEL_VOLUMEZONE2 = "zone2#volume";

    public static final String CHANNEL_POWERZONE3 = "zone3#power";
    public static final String CHANNEL_INPUTZONE3 = "zone3#input";
    public static final String CHANNEL_MUTEZONE3 = "zone3#mute";
    public static final String CHANNEL_VOLUMEZONE3 = "zone3#volume";

    public static final String CHANNEL_CONTROL = "player#control";
    public static final String CHANNEL_CURRENTPLAYINGTIME = "player#currentPlayingTime";
    public static final String CHANNEL_ARTIST = "player#artist";
    public static final String CHANNEL_TITLE = "player#title";
    public static final String CHANNEL_ALBUM = "player#album";
    public static final String CHANNEL_ALBUM_ART = "player#albumArt";
    public static final String CHANNEL_ALBUM_ART_URL = "player#albumArtUrl";
    public static final String CHANNEL_LISTENMODE = "player#listenmode";
    public static final String CHANNEL_AUDIOINFO = "player#audioinfo";
    public static final String CHANNEL_PLAY_URI = "player#playuri";

    public static final String CHANNEL_NET_MENU_TITLE = "netmenu#title";
    public static final String CHANNEL_NET_MENU_CONTROL = "netmenu#control";
    public static final String CHANNEL_NET_MENU_SELECTION = "netmenu#selection";
    public static final String CHANNEL_NET_MENU0 = "netmenu#item0";
    public static final String CHANNEL_NET_MENU1 = "netmenu#item1";
    public static final String CHANNEL_NET_MENU2 = "netmenu#item2";
    public static final String CHANNEL_NET_MENU3 = "netmenu#item3";
    public static final String CHANNEL_NET_MENU4 = "netmenu#item4";
    public static final String CHANNEL_NET_MENU5 = "netmenu#item5";
    public static final String CHANNEL_NET_MENU6 = "netmenu#item6";
    public static final String CHANNEL_NET_MENU7 = "netmenu#item7";
    public static final String CHANNEL_NET_MENU8 = "netmenu#item8";
    public static final String CHANNEL_NET_MENU9 = "netmenu#item9";

    public static final String CHANNEL_AUDIO_IN_INFO = "info#audioIn";
    public static final String CHANNEL_AUDIO_OUT_INFO = "info#audioOut";
    public static final String CHANNEL_VIDEO_IN_INFO = "info#videoIn";
    public static final String CHANNEL_VIDEO_OUT_INFO = "info#videoOut";

    // Used for Discovery service
    public static final String MANUFACTURER = "ONKYO";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";
}
