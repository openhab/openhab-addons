/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link DenonMarantzBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
public class DenonMarantzBindingConstants {

    public static final String BINDING_ID = "denonmarantz";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AVR = new ThingTypeUID(BINDING_ID, "avr");

    // List of thing Parameters names
    public static final String PARAMETER_ZONE_COUNT = "zoneCount";
    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_TELNET_ENABLED = "telnetEnabled";
    public static final String PARAMETER_TELNET_PORT = "telnetPort";
    public static final String PARAMETER_HTTP_PORT = "httpPort";
    public static final String PARAMETER_POLLING_INTERVAL = "httpPollingInterval";

    // List of all Channel ids
    public static final String CHANNEL_POWER = "general#power";
    public static final String CHANNEL_COMMAND = "general#command";
    public static final String CHANNEL_NOW_PLAYING_ARTIST = "general#artist";
    public static final String CHANNEL_NOW_PLAYING_ALBUM = "general#album";
    public static final String CHANNEL_NOW_PLAYING_TRACK = "general#track";

    public static final String CHANNEL_MAIN_ZONE_POWER = "mainZone#power";
    public static final String CHANNEL_MAIN_VOLUME = "mainZone#volume";
    public static final String CHANNEL_MAIN_VOLUME_DB = "mainZone#volumeDB";
    public static final String CHANNEL_MUTE = "mainZone#mute";
    public static final String CHANNEL_INPUT = "mainZone#input";
    public static final String CHANNEL_SURROUND_PROGRAM = "mainZone#surroundProgram";

    public static final String CHANNEL_ZONE2_POWER = "zone2#power";
    public static final String CHANNEL_ZONE2_VOLUME = "zone2#volume";
    public static final String CHANNEL_ZONE2_VOLUME_DB = "zone2#volumeDB";
    public static final String CHANNEL_ZONE2_MUTE = "zone2#mute";
    public static final String CHANNEL_ZONE2_INPUT = "zone2#input";

    public static final String CHANNEL_ZONE3_POWER = "zone3#power";
    public static final String CHANNEL_ZONE3_VOLUME = "zone3#volume";
    public static final String CHANNEL_ZONE3_VOLUME_DB = "zone3#volumeDB";
    public static final String CHANNEL_ZONE3_MUTE = "zone3#mute";
    public static final String CHANNEL_ZONE3_INPUT = "zone3#input";

    // HashMap of Zone2 Channel Type UIDs (to be added to Thing later when needed)
    public static final LinkedHashMap<String, ChannelTypeUID> ZONE2_CHANNEL_TYPES = new LinkedHashMap<String, ChannelTypeUID>();
    static {
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_POWER, new ChannelTypeUID(BINDING_ID, "zonePower"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_VOLUME, new ChannelTypeUID(BINDING_ID, "volume"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_VOLUME_DB, new ChannelTypeUID(BINDING_ID, "volumeDB"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_MUTE, new ChannelTypeUID(BINDING_ID, "mute"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_INPUT, new ChannelTypeUID(BINDING_ID, "input"));
    }

    // HashMap of Zone3 Channel Type UIDs (to be added to Thing later when needed)
    public static final LinkedHashMap<String, ChannelTypeUID> ZONE3_CHANNEL_TYPES = new LinkedHashMap<String, ChannelTypeUID>();
    static {
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_POWER, new ChannelTypeUID(BINDING_ID, "zonePower"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_VOLUME, new ChannelTypeUID(BINDING_ID, "volume"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_VOLUME_DB, new ChannelTypeUID(BINDING_ID, "volumeDB"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_MUTE, new ChannelTypeUID(BINDING_ID, "mute"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_INPUT, new ChannelTypeUID(BINDING_ID, "input"));
    }

    /**
     * Static mapping of ChannelType-to-ItemType (workaround while waiting for
     * https://github.com/eclipse/smarthome/issues/4950 as yet there is no convenient way to extract the item type from
     * thing-types.xml)
     * See https://github.com/eclipse/smarthome/pull/4787#issuecomment-362287430
     */
    public static final HashMap<String, String> CHANNEL_ITEM_TYPES = new HashMap<String, String>();
    static {
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE2_POWER, "Switch");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE2_VOLUME, "Dimmer");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE2_VOLUME_DB, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE2_MUTE, "Switch");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE2_INPUT, "String");

        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE3_POWER, "Switch");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE3_VOLUME, "Dimmer");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE3_VOLUME_DB, "Number");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE3_MUTE, "Switch");
        CHANNEL_ITEM_TYPES.put(CHANNEL_ZONE3_INPUT, "String");
    }

    // Offset in dB from the actual dB value to the volume as presented by the AVR (0 == -80 dB)
    public static final BigDecimal DB_OFFSET = new BigDecimal("80");
}
