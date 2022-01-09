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
package org.openhab.binding.upnpcontrol.internal;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

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
    LF_VOLUME("LFvolume", "Left Front Volume", "Left front volume, will be left volume with stereo sound",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    RF_VOLUME("RFvolume", "Right Front Volume", "Right front volume, will be left volume with stereo sound",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    CF_VOLUME("CFvolume", "Center Front Volume", "Center front volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    LFE_VOLUME("LFEvolume", "Frequency Enhancement Volume", "Low frequency enhancement volume (subwoofer)",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    LS_VOLUME("LSvolume", "Left Surround Volume", "Left surround volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    RS_VOLUME("RSvolume", "Right Surround Volume", "Right surround volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    LFC_VOLUME("LFCvolume", "Left of Center Volume", "Left of center (in front) volume", ITEM_TYPE_VOLUME,
            CHANNEL_TYPE_VOLUME),
    RFC_VOLUME("RFCvolume", "Right of Center Volume", "Right of center (in front) volume", ITEM_TYPE_VOLUME,
            CHANNEL_TYPE_VOLUME),
    SD_VOLUME("SDvolume", "Surround Volume", "Surround (rear) volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    SL_VOLUME("SLvolume", "Side Left Volume", "Side left (left wall) volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    SR_VOLUME("SRvolume", "Side Right Volume", "Side right (right wall) volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    T_VOLUME("Tvolume", "Top Volume", "Top (overhead) volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    B_VOLUME("Bvolume", "Bottom Volume", "Bottom volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    BC_VOLUME("BCvolume", "Back Center Volume", "Back center volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    BL_VOLUME("BLvolume", "Back Left Volume", "Back Left Volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    BR_VOLUME("BRvolume", "Back Right Volume", "Back right volume", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),

    // Mute channels
    LF_MUTE("LFmute", "Left Front Mute", "Left front mute, will be left mute with stereo sound", ITEM_TYPE_MUTE,
            CHANNEL_TYPE_MUTE),
    RF_MUTE("RFmute", "Right Front Mute", "Right front mute, will be left mute with stereo sound", ITEM_TYPE_MUTE,
            CHANNEL_TYPE_MUTE),
    CF_MUTE("CFmute", "Center Front Mute", "Center front mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    LFE_MUTE("LFEmute", "Frequency Enhancement Mute", "Low frequency enhancement mute (subwoofer)", ITEM_TYPE_MUTE,
            CHANNEL_TYPE_MUTE),
    LS_MUTE("LSmute", "Left Surround Mute", "Left surround mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    RS_MUTE("RSmute", "Right Surround Mute", "Right surround mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    LFC_MUTE("LFCmute", "Left of Center Mute", "Left of center (in front) mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    RFC_MUTE("RFCmute", "Right of Center Mute", "Right of center (in front) mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    SD_MUTE("SDmute", "Surround Mute", "Surround (rear) mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    SL_MUTE("SLmute", "Side Left Mute", "Side left (left wall) mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    SR_MUTE("SRmute", "Side Right Mute", "Side right (right wall) mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    T_MUTE("Tmute", "Top Mute", "Top (overhead) mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    B_MUTE("Bmute", "Bottom Mute", "Bottom mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    BC_MUTE("BCmute", "Back Center Mute", "Back center mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    BL_MUTE("BLmute", "Back Left Mute", "Back Left Mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    BR_MUTE("BRmute", "Back Right Mute", "Back right mute", ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),

    // Loudness channels
    LOUDNESS("loudness", "Loudness", "Master loudness", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    LF_LOUDNESS("LFloudness", "Left Front Loudness", "Left front loudness", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    RF_LOUDNESS("RFloudness", "Right Front Loudness", "Right front loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    CF_LOUDNESS("CFloudness", "Center Front Loudness", "Center front loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    LFE_LOUDNESS("LFEloudness", "Frequency Enhancement Loudness", "Low frequency enhancement loudness (subwoofer)",
            ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    LS_LOUDNESS("LSloudness", "Left Surround Loudness", "Left surround loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    RS_LOUDNESS("RSloudness", "Right Surround Loudness", "Right surround loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    LFC_LOUDNESS("LFCloudness", "Left of Center Loudness", "Left of center (in front) loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    RFC_LOUDNESS("RFCloudness", "Right of Center Loudness", "Right of center (in front) loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    SD_LOUDNESS("SDloudness", "Surround Loudness", "Surround (rear) loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    SL_LOUDNESS("SLloudness", "Side Left Loudness", "Side left (left wall) loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    SR_LOUDNESS("SRloudness", "Side Right Loudness", "Side right (right wall) loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    T_LOUDNESS("Tloudness", "Top Loudness", "Top (overhead) loudness", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    B_LOUDNESS("Bloudness", "Bottom Loudness", "Bottom loudness", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    BC_LOUDNESS("BCloudness", "Back Center Loudness", "Back center loudness", ITEM_TYPE_LOUDNESS,
            CHANNEL_TYPE_LOUDNESS),
    BL_LOUDNESS("BLloudness", "Back Left Loudness", "Back Left Loudness", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    BR_LOUDNESS("BRloudness", "Back Right Loudness", "Back right loudness", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS);

    private static final Map<String, UpnpChannelName> UPNP_CHANNEL_NAME_MAP = Stream.of(UpnpChannelName.values())
            .collect(Collectors.toMap(UpnpChannelName::getChannelId, Function.identity()));

    private final String channelId;
    private final String label;
    private final String description;
    private final String itemType;
    private final String channelType;

    UpnpChannelName(final String channelId, final String label, final String description, final String itemType,
            final String channelType) {
        this.channelId = channelId;
        this.label = label;
        this.description = description;
        this.itemType = itemType;
        this.channelType = channelType;
    }

    /**
     * @return The name of the Channel
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * @return The label for the Channel
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return The description for the Channel
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
     * @return The channel type for the Channel
     */
    public String getChannelType() {
        return channelType;
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
