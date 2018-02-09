/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OnkyoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Pauli Anttila
 */
public class OnkyoBindingConstants {

    public static final String BINDING_ID = "onkyo";

    // List of all supported Onkyo Models
    public static final String ONKYO_TYPE_TXNR414 = "TX-NR414";
    public static final String ONKYO_TYPE_TXNR509 = "TX-NR509";
    public static final String ONKYO_TYPE_TXNR515 = "TX-NR515";
    public static final String ONKYO_TYPE_TXNR525 = "TX-NR525";
    public static final String ONKYO_TYPE_TXNR535 = "TX-NR535";
    public static final String ONKYO_TYPE_TXNR555 = "TX-NR555";
    public static final String ONKYO_TYPE_TXNR616 = "TX-NR616";
    public static final String ONKYO_TYPE_TXNR626 = "TX-NR626";
    public static final String ONKYO_TYPE_TXNR646 = "TX-NR646";
    public static final String ONKYO_TYPE_TXNR656 = "TX-NR656";
    public static final String ONKYO_TYPE_TXNR717 = "TX-NR717";
    public static final String ONKYO_TYPE_TXNR727 = "TX-NR727";
    public static final String ONKYO_TYPE_TXNR737 = "TX-NR737";
    public static final String ONKYO_TYPE_TXNR747 = "TX-NR747";
    public static final String ONKYO_TYPE_TXNR757 = "TX-NR757";
    public static final String ONKYO_TYPE_TXNR818 = "TX-NR818";
    public static final String ONKYO_TYPE_TXNR828 = "TX-NR828";
    public static final String ONKYO_TYPE_TXNR838 = "TX-NR838";

    // Extend this set with all successfully tested models
    public static final Set<String> SUPPORTED_DEVICE_MODELS = Stream
            .of(ONKYO_TYPE_TXNR414, ONKYO_TYPE_TXNR509, ONKYO_TYPE_TXNR515, ONKYO_TYPE_TXNR525, ONKYO_TYPE_TXNR535,
                    ONKYO_TYPE_TXNR555, ONKYO_TYPE_TXNR616, ONKYO_TYPE_TXNR626, ONKYO_TYPE_TXNR646, ONKYO_TYPE_TXNR656,
                    ONKYO_TYPE_TXNR717, ONKYO_TYPE_TXNR727, ONKYO_TYPE_TXNR737, ONKYO_TYPE_TXNR747, ONKYO_TYPE_TXNR757,
                    ONKYO_TYPE_TXNR818, ONKYO_TYPE_TXNR828, ONKYO_TYPE_TXNR838)
            .collect(Collectors.toSet());

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ONKYOAV = new ThingTypeUID(BINDING_ID, "onkyoAVR");
    public static final ThingTypeUID THING_TYPE_ONKYO_UNSUPPORTED = new ThingTypeUID(BINDING_ID, "onkyoUnsupported");
    public static final ThingTypeUID THING_TYPE_TXNR414 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR414);
    public static final ThingTypeUID THING_TYPE_TXNR509 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR509);
    public static final ThingTypeUID THING_TYPE_TXNR515 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR515);
    public static final ThingTypeUID THING_TYPE_TXNR525 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR525);
    public static final ThingTypeUID THING_TYPE_TXNR535 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR535);
    public static final ThingTypeUID THING_TYPE_TXNR555 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR555);
    public static final ThingTypeUID THING_TYPE_TXNR616 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR616);
    public static final ThingTypeUID THING_TYPE_TXNR626 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR626);
    public static final ThingTypeUID THING_TYPE_TXNR646 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR646);
    public static final ThingTypeUID THING_TYPE_TXNR656 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR656);
    public static final ThingTypeUID THING_TYPE_TXNR717 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR717);
    public static final ThingTypeUID THING_TYPE_TXNR727 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR727);
    public static final ThingTypeUID THING_TYPE_TXNR737 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR737);
    public static final ThingTypeUID THING_TYPE_TXNR747 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR747);
    public static final ThingTypeUID THING_TYPE_TXNR757 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR757);
    public static final ThingTypeUID THING_TYPE_TXNR818 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR818);
    public static final ThingTypeUID THING_TYPE_TXNR828 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR828);
    public static final ThingTypeUID THING_TYPE_TXNR838 = new ThingTypeUID(BINDING_ID, ONKYO_TYPE_TXNR838);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_ONKYOAV, THING_TYPE_ONKYO_UNSUPPORTED, THING_TYPE_TXNR414, THING_TYPE_TXNR515,
                    THING_TYPE_TXNR525, THING_TYPE_TXNR535, THING_TYPE_TXNR555, THING_TYPE_TXNR616, THING_TYPE_TXNR626,
                    THING_TYPE_TXNR646, THING_TYPE_TXNR656, THING_TYPE_TXNR717, THING_TYPE_TXNR727, THING_TYPE_TXNR737,
                    THING_TYPE_TXNR747, THING_TYPE_TXNR757, THING_TYPE_TXNR818, THING_TYPE_TXNR828, THING_TYPE_TXNR838)
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

    // Used for Discovery service
    public static final String MANUFACTURER = "ONKYO";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

}
