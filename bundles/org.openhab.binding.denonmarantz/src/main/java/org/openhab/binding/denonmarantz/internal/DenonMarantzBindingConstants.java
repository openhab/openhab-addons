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
package org.openhab.binding.denonmarantz.internal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link DenonMarantzBindingConstants} class defines common constants, which are
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
    public static final String CHANNEL_SURROUND_PROGRAM = "general#surroundProgram";
    public static final String CHANNEL_COMMAND = "general#command";
    public static final String CHANNEL_NOW_PLAYING_ARTIST = "general#artist";
    public static final String CHANNEL_NOW_PLAYING_ALBUM = "general#album";
    public static final String CHANNEL_NOW_PLAYING_TRACK = "general#track";

    public static final String CHANNEL_MAIN_ZONE_POWER = "mainZone#power";
    public static final String CHANNEL_MAIN_VOLUME = "mainZone#volume";
    public static final String CHANNEL_MAIN_VOLUME_DB = "mainZone#volumeDB";
    public static final String CHANNEL_MUTE = "mainZone#mute";
    public static final String CHANNEL_INPUT = "mainZone#input";

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

    public static final String CHANNEL_ZONE4_POWER = "zone4#power";
    public static final String CHANNEL_ZONE4_VOLUME = "zone4#volume";
    public static final String CHANNEL_ZONE4_VOLUME_DB = "zone4#volumeDB";
    public static final String CHANNEL_ZONE4_MUTE = "zone4#mute";
    public static final String CHANNEL_ZONE4_INPUT = "zone4#input";

    // Map of Zone2 Channel Type UIDs (to be added to Thing later when needed)
    public static final Map<String, ChannelTypeUID> ZONE2_CHANNEL_TYPES = new LinkedHashMap<>();
    static {
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_POWER, new ChannelTypeUID(BINDING_ID, "zonePower"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_VOLUME, new ChannelTypeUID(BINDING_ID, "volume"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_VOLUME_DB, new ChannelTypeUID(BINDING_ID, "volumeDB"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_MUTE, new ChannelTypeUID(BINDING_ID, "mute"));
        ZONE2_CHANNEL_TYPES.put(CHANNEL_ZONE2_INPUT, new ChannelTypeUID(BINDING_ID, "input"));
    }

    // Map of Zone3 Channel Type UIDs (to be added to Thing later when needed)
    public static final Map<String, ChannelTypeUID> ZONE3_CHANNEL_TYPES = new LinkedHashMap<>();
    static {
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_POWER, new ChannelTypeUID(BINDING_ID, "zonePower"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_VOLUME, new ChannelTypeUID(BINDING_ID, "volume"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_VOLUME_DB, new ChannelTypeUID(BINDING_ID, "volumeDB"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_MUTE, new ChannelTypeUID(BINDING_ID, "mute"));
        ZONE3_CHANNEL_TYPES.put(CHANNEL_ZONE3_INPUT, new ChannelTypeUID(BINDING_ID, "input"));
    }

    // Map of Zone4 Channel Type UIDs (to be added to Thing later when needed)
    public static final Map<String, ChannelTypeUID> ZONE4_CHANNEL_TYPES = new LinkedHashMap<>();
    static {
        ZONE4_CHANNEL_TYPES.put(CHANNEL_ZONE4_POWER, new ChannelTypeUID(BINDING_ID, "zonePower"));
        ZONE4_CHANNEL_TYPES.put(CHANNEL_ZONE4_VOLUME, new ChannelTypeUID(BINDING_ID, "volume"));
        ZONE4_CHANNEL_TYPES.put(CHANNEL_ZONE4_VOLUME_DB, new ChannelTypeUID(BINDING_ID, "volumeDB"));
        ZONE4_CHANNEL_TYPES.put(CHANNEL_ZONE4_MUTE, new ChannelTypeUID(BINDING_ID, "mute"));
        ZONE4_CHANNEL_TYPES.put(CHANNEL_ZONE4_INPUT, new ChannelTypeUID(BINDING_ID, "input"));
    }

    /**
     * Static mapping of ChannelType-to-ItemType (workaround while waiting for
     * https://github.com/eclipse/smarthome/issues/4950 as yet there is no convenient way to extract the item type from
     * thing-types.xml)
     * See https://github.com/eclipse/smarthome/pull/4787#issuecomment-362287430
     */
    public static final Map<String, String> CHANNEL_ITEM_TYPES = new HashMap<>();
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
