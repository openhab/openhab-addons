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
package org.openhab.binding.arcam.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ArcamBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamBindingConstants {

    public static final String BINDING_ID = "arcam";

    // List of all Thing Type UIDs
    public static final ThingTypeUID AVR5_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "AVR5");
    public static final ThingTypeUID AVR10_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "AVR10");
    public static final ThingTypeUID AVR20_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "AVR20");
    public static final ThingTypeUID AVR30_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "AVR30");
    public static final ThingTypeUID AVR40_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "AVR40");
    public static final ThingTypeUID SA10_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "SA10");
    public static final ThingTypeUID SA20_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "SA20");
    public static final ThingTypeUID SA30_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "SA30");
    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Set.of(AVR5_THING_TYPE_UID,
            AVR10_THING_TYPE_UID, AVR20_THING_TYPE_UID, AVR30_THING_TYPE_UID, AVR40_THING_TYPE_UID, SA10_THING_TYPE_UID,
            SA20_THING_TYPE_UID, SA30_THING_TYPE_UID);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(SUPPORTED_KNOWN_THING_TYPES_UIDS);

    // List of all Channel ids
    // Generic
    public static final String CHANNEL_DAC_FILTER = "masterZone#dacFilter";
    public static final String CHANNEL_DC_OFFSET = "masterZone#dcOffset";
    public static final String CHANNEL_DISPLAY_BRIGHTNESS = "masterZone#displaybrightness";
    public static final String CHANNEL_HEADPHONES = "masterZone#headphones";
    public static final String CHANNEL_INCOMING_SAMPLE_RATE = "masterZone#incomingSampleRate";
    public static final String CHANNEL_LIFTER_TEMPERATURE = "masterZone#lifterTemperature";
    public static final String CHANNEL_OUTPUT_TEMPERATURE = "masterZone#outputTemperature";
    public static final String CHANNEL_REBOOT = "masterZone#reboot";
    public static final String CHANNEL_SOFTWARE_VERSION = "masterZone#softwareVersion";
    public static final String CHANNEL_TIMEOUT_COUNTER = "masterZone#timeoutCounter";

    // Master zone
    public static final String CHANNEL_MASTER_BALANCE = "masterZone#balance";
    public static final String CHANNEL_MASTER_DIRECT_MODE = "masterZone#directMode";
    public static final String CHANNEL_MASTER_INPUT = "masterZone#input";
    public static final String CHANNEL_MASTER_INPUT_DETECT = "masterZone#inputDetect";
    public static final String CHANNEL_MASTER_MUTE = "masterZone#mute";
    public static final String CHANNEL_MASTER_NOW_PLAYING_TITLE = "masterZone#nowPlayingTitle";
    public static final String CHANNEL_MASTER_NOW_PLAYING_ARTIST = "masterZone#nowPlayingArtist";
    public static final String CHANNEL_MASTER_NOW_PLAYING_ALBUM = "masterZone#nowPlayingAlbum";
    public static final String CHANNEL_MASTER_NOW_PLAYING_APPLICATION = "masterZone#nowPlayingApplication";
    public static final String CHANNEL_MASTER_NOW_PLAYING_SAMPLE_RATE = "masterZone#nowPlayingSampleRate";
    public static final String CHANNEL_MASTER_NOW_PLAYING_AUDIO_ENCODER = "masterZone#nowPlayingAudioEncoder";
    public static final String CHANNEL_MASTER_POWER = "masterZone#power";
    public static final String CHANNEL_MASTER_ROOM_EQUALISATION = "masterZone#roomEqualisation";
    public static final String CHANNEL_MASTER_SHORT_CIRCUIT = "masterZone#shortCircuit";
    public static final String CHANNEL_MASTER_VOLUME = "masterZone#volume";

    // Zone 2
    public static final String CHANNEL_ZONE2_BALANCE = "zone2#balance";
    public static final String CHANNEL_ZONE2_DIRECT_MODE = "zone2#directMode";
    public static final String CHANNEL_ZONE2_INPUT = "zone2#input";
    public static final String CHANNEL_ZONE2_MUTE = "zone2#mute";
    public static final String CHANNEL_ZONE2_POWER = "zone2#power";
    public static final String CHANNEL_ZONE2_ROOM_EQUALISATION = "zone2#roomEqualisation";
    public static final String CHANNEL_ZONE2_VOLUME = "zone2#volume";
}
