/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This enum contains default openHAB channel configurations for optional channels as defined in the UPnP standard.
 * Vendor specific channels are not part of this.
 *
 * @author Mark Herwege - Initial contribution
 *
 */
@NonNullByDefault
public enum UpnpChannelName {

    // Volume channels
    LF_VOLUME("LFvolume", "Left Front Volume", "Left front volume, will be left volume with stereo sound", "Dimmer",
            "SoundVolume", false),
    RF_VOLUME("RFvolume", "Right Front Volume", "Right front volume, will be left volume with stereo sound", "Dimmer",
            "SoundVolume", false),
    CF_VOLUME("CFvolume", "Center Front Volume", "Center front volume", "Dimmer", "SoundVolume", false),
    LFE_VOLUME("LFEvolume", "Frequency Enhancement Volume", "Low frequency enhancement volume (subwoofer)", "Dimmer",
            "SoundVolume", false),
    LS_VOLUME("LSvolume", "Left Surround Volume", "Left surround volume", "Dimmer", "SoundVolume", false),
    RS_VOLUME("RSvolume", "Right Surround Volume", "Right surround volume", "Dimmer", "SoundVolume", false),
    LFC_VOLUME("LFCvolume", "Left of Center Volume", "Left of center (in front) volume", "Dimmer", "SoundVolume",
            false),
    RFC_VOLUME("RFCvolume", "Right of Center Volume", "Right of center (in front) volume", "Dimmer", "SoundVolume",
            false),
    SD_VOLUME("SDvolume", "Surround Volume", "Surround (rear) volume", "Dimmer", "SoundVolume", false),
    SL_VOLUME("SLvolume", "Side Left Volume", "Side left (left wall) volume", "Dimmer", "SoundVolume", false),
    SR_VOLUME("SRvolume", "Side Right Volume", "Side right (right wall) volume", "Dimmer", "SoundVolume", false),
    T_VOLUME("Tvolume", "Top Volume", "Top (overhead) volume", "Dimmer", "SoundVolume", false),
    B_VOLUME("Bvolume", "Bottom Volume", "Bottom volume", "Dimmer", "SoundVolume", false),
    BC_VOLUME("BCvolume", "Back Center Volume", "Back center volume", "Dimmer", "SoundVolume", false),
    BL_VOLUME("BLvolume", "Back Left Volume", "Back Left Volume", "Dimmer", "SoundVolume", false),
    BR_VOLUME("BRvolume", "Back Right Volume", "Back right volume", "Dimmer", "SoundVolume", false),

    // Mute channels
    LF_MUTE("LFmute", "Left Front Mute", "Left front mute, will be left mute with stereo sound", "Switch",
            "SoundVolume", false),
    RF_MUTE("RFmute", "Right Front Mute", "Right front mute, will be left mute with stereo sound", "Switch",
            "SoundVolume", false),
    CF_MUTE("CFmute", "Center Front Mute", "Center front mute", "Switch", "SoundVolume", false),
    LFE_MUTE("LFEmute", "Frequency Enhancement Mute", "Low frequency enhancement mute (subwoofer)", "Switch",
            "SoundVolume", false),
    LS_MUTE("LSmute", "Left Surround Mute", "Left surround mute", "Switch", "SoundVolume", false),
    RS_MUTE("RSmute", "Right Surround Mute", "Right surround mute", "Switch", "SoundVolume", false),
    LFC_MUTE("LFCmute", "Left of Center Mute", "Left of center (in front) mute", "Switch", "SoundVolume", false),
    RFC_MUTE("RFCmute", "Right of Center Mute", "Right of center (in front) mute", "Switch", "SoundVolume", false),
    SD_MUTE("SDmute", "Surround Mute", "Surround (rear) mute", "Switch", "SoundVolume", false),
    SL_MUTE("SLmute", "Side Left Mute", "Side left (left wall) mute", "Switch", "SoundVolume", false),
    SR_MUTE("SRmute", "Side Right Mute", "Side right (right wall) mute", "Switch", "SoundVolume", false),
    T_MUTE("Tmute", "Top Mute", "Top (overhead) mute", "Switch", "SoundVolume", false),
    B_MUTE("Bmute", "Bottom Mute", "Bottom mute", "Switch", "SoundVolume", false),
    BC_MUTE("BCmute", "Back Center Mute", "Back center mute", "Switch", "SoundVolume", false),
    BL_MUTE("BLmute", "Back Left Mute", "Back Left Mute", "Switch", "SoundVolume", false),
    BR_MUTE("BRmute", "Back Right Mute", "Back right mute", "Switch", "SoundVolume", false),

    // Loudness channels
    LOUDNESS("loudness", "Loudness", "Master loudness", "Switch", "SoundVolume", false),
    LF_LOUDNESS("LFloudness", "Left Front Loudness", "Left front loudness, will be left loudness with stereo sound",
            "Switch", "SoundVolume", false),
    RF_LOUDNESS("RFloudness", "Right Front Loudness", "Right front loudness, will be left loudness with stereo sound",
            "Switch", "SoundVolume", false),
    CF_LOUDNESS("CFloudness", "Center Front Loudness", "Center front loudness", "Switch", "SoundVolume", false),
    LFE_LOUDNESS("LFEloudness", "Frequency Enhancement Loudness", "Low frequency enhancement loudness (subwoofer)",
            "Switch", "SoundVolume", false),
    LS_LOUDNESS("LSloudness", "Left Surround Loudness", "Left surround loudness", "Switch", "SoundVolume", false),
    RS_LOUDNESS("RSloudness", "Right Surround Loudness", "Right surround loudness", "Switch", "SoundVolume", false),
    LFC_LOUDNESS("LFCloudness", "Left of Center Loudness", "Left of center (in front) loudness", "Switch",
            "SoundVolume", false),
    RFC_LOUDNESS("RFCloudness", "Right of Center Loudness", "Right of center (in front) loudness", "Switch",
            "SoundVolume", false),
    SD_LOUDNESS("SDloudness", "Surround Loudness", "Surround (rear) loudness", "Switch", "SoundVolume", false),
    SL_LOUDNESS("SLloudness", "Side Left Loudness", "Side left (left wall) loudness", "Switch", "SoundVolume", false),
    SR_LOUDNESS("SRloudness", "Side Right Loudness", "Side right (right wall) loudness", "Switch", "SoundVolume",
            false),
    T_LOUDNESS("Tloudness", "Top Loudness", "Top (overhead) loudness", "Switch", "SoundVolume", false),
    B_LOUDNESS("Bloudness", "Bottom Loudness", "Bottom loudness", "Switch", "SoundVolume", false),
    BC_LOUDNESS("BCloudness", "Back Center Loudness", "Back center loudness", "Switch", "SoundVolume", false),
    BL_LOUDNESS("BLloudness", "Back Left Loudness", "Back Left Loudness", "Switch", "SoundVolume", false),
    BR_LOUDNESS("BRloudness", "Back Right Loudness", "Back right loudness", "Switch", "SoundVolume", false);

    private static final Map<String, UpnpChannelName> UPNP_CHANNEL_NAME_MAP = Stream.of(UpnpChannelName.values())
            .collect(Collectors.toMap(UpnpChannelName::getChannelId, Function.identity()));

    private final String channelId;
    private final String label;
    private final String description;
    private final String itemType;
    private final String category;
    private final boolean advanced;

    UpnpChannelName(final String channelId, final String label, final String description, final String itemType,
            final String category, final boolean advanced) {
        this.channelId = channelId;
        this.label = label;
        this.description = description;
        this.itemType = itemType;
        this.category = category;
        this.advanced = advanced;
    }

    /**
     * @return The name of the Channel
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * @return The label for the Channel Type
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return The description for the Channel Type
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The item type for the Channel
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * @return The category for the Channel Type
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return If the Channel Type is advanced
     */
    public boolean isAdvanced() {
        return advanced;
    }

    /**
     * Returns the UPnP Channel enum for the given channel id or null if there is no enum available for the given
     * channel.
     *
     * @param channelId Channel to find
     * @return The UPnP Channel enum or null if there is none.
     */
    public static @Nullable UpnpChannelName channelIdToUpnpChannelName(final String channelId) {
        return UPNP_CHANNEL_NAME_MAP.get(channelId);
    }
}
