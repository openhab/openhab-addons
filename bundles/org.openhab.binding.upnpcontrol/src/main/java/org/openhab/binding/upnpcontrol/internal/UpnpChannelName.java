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
    LF_VOLUME("LFvolume", "@text/channel.upnpcontrol.lfvolume.label", "@text/channel.upnpcontrol.lfvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    RF_VOLUME("RFvolume", "@text/channel.upnpcontrol.rfvolume.label", "@text/channel.upnpcontrol.rfvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    CF_VOLUME("CFvolume", "@text/channel.upnpcontrol.cfvolume.label", "@text/channel.upnpcontrol.cfvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    LFE_VOLUME("LFEvolume", "@text/channel.upnpcontrol.lfevolume.label",
            "@text/channel.upnpcontrol.lfevolume.description", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    LS_VOLUME("LSvolume", "@text/channel.upnpcontrol.lsvolume.label", "@text/channel.upnpcontrol.lsvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    RS_VOLUME("RSvolume", "@text/channel.upnpcontrol.rsvolume.label", "@text/channel.upnpcontrol.rsvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    LFC_VOLUME("LFCvolume", "@text/channel.upnpcontrol.lfcvolume.label",
            "@text/channel.upnpcontrol.lfcvolume.description", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    RFC_VOLUME("RFCvolume", "@text/channel.upnpcontrol.rfcvolume.label",
            "@text/channel.upnpcontrol.rfcvolume.description", ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    SD_VOLUME("SDvolume", "@text/channel.upnpcontrol.sdvolume.label", "@text/channel.upnpcontrol.sdvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    SL_VOLUME("SLvolume", "@text/channel.upnpcontrol.slvolume.label", "@text/channel.upnpcontrol.slvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    SR_VOLUME("SRvolume", "@text/channel.upnpcontrol.srvolume.label", "@text/channel.upnpcontrol.srvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    T_VOLUME("Tvolume", "@text/channel.upnpcontrol.tvolume.label", "@text/channel.upnpcontrol.tvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    B_VOLUME("Bvolume", "@text/channel.upnpcontrol.bvolume.label", "@text/channel.upnpcontrol.bvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    BC_VOLUME("BCvolume", "@text/channel.upnpcontrol.bcvolume.label", "@text/channel.upnpcontrol.bcvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    BL_VOLUME("BLvolume", "@text/channel.upnpcontrol.blvolume.label", "@text/channel.upnpcontrol.blvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),
    BR_VOLUME("BRvolume", "@text/channel.upnpcontrol.brvolume.label", "@text/channel.upnpcontrol.brvolume.description",
            ITEM_TYPE_VOLUME, CHANNEL_TYPE_VOLUME),

    // Mute channels
    LF_MUTE("LFmute", "@text/channel.upnpcontrol.lfmute.label", "@text/channel.upnpcontrol.lfmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    RF_MUTE("RFmute", "@text/channel.upnpcontrol.rfmute.label", "@text/channel.upnpcontrol.rfmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    CF_MUTE("CFmute", "@text/channel.upnpcontrol.cfmute.label", "@text/channel.upnpcontrol.cfmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    LFE_MUTE("LFEmute", "@text/channel.upnpcontrol.lfemute.label", "@text/channel.upnpcontrol.lfemute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    LS_MUTE("LSmute", "@text/channel.upnpcontrol.lsmute.label", "@text/channel.upnpcontrol.lsmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    RS_MUTE("RSmute", "@text/channel.upnpcontrol.rsmute.label", "@text/channel.upnpcontrol.rsmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    LFC_MUTE("LFCmute", "@text/channel.upnpcontrol.lfcmute.label", "@text/channel.upnpcontrol.lfcmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    RFC_MUTE("RFCmute", "@text/channel.upnpcontrol.rfcmute.label", "@text/channel.upnpcontrol.rfcmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    SD_MUTE("SDmute", "@text/channel.upnpcontrol.sdmute.label", "@text/channel.upnpcontrol.sdmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    SL_MUTE("SLmute", "@text/channel.upnpcontrol.slmute.label", "@text/channel.upnpcontrol.slmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    SR_MUTE("SRmute", "@text/channel.upnpcontrol.srmute.label", "@text/channel.upnpcontrol.srmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    T_MUTE("Tmute", "@text/channel.upnpcontrol.tmute.label", "@text/channel.upnpcontrol.tmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    B_MUTE("Bmute", "@text/channel.upnpcontrol.bmute.label", "@text/channel.upnpcontrol.bmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    BC_MUTE("BCmute", "@text/channel.upnpcontrol.bcmute.label", "@text/channel.upnpcontrol.bcmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    BL_MUTE("BLmute", "@text/channel.upnpcontrol.blmute.label", "@text/channel.upnpcontrol.blmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),
    BR_MUTE("BRmute", "@text/channel.upnpcontrol.brmute.label", "@text/channel.upnpcontrol.brmute.description",
            ITEM_TYPE_MUTE, CHANNEL_TYPE_MUTE),

    // Loudness channels
    LF_LOUDNESS("LFloudness", "@text/channel.upnpcontrol.lfloudness.label",
            "@text/channel.upnpcontrol.lfloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    RF_LOUDNESS("RFloudness", "@text/channel.upnpcontrol.rfloudness.label",
            "@text/channel.upnpcontrol.rfloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    CF_LOUDNESS("CFloudness", "@text/channel.upnpcontrol.cfloudness.label",
            "@text/channel.upnpcontrol.cfloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    LFE_LOUDNESS("LFEloudness", "@text/channel.upnpcontrol.lfeloudness.label",
            "@text/channel.upnpcontrol.lfeloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    LS_LOUDNESS("LSloudness", "@text/channel.upnpcontrol.lsloudness.label",
            "@text/channel.upnpcontrol.lsloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    RS_LOUDNESS("RSloudness", "@text/channel.upnpcontrol.rsloudness.label",
            "@text/channel.upnpcontrol.rsloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    LFC_LOUDNESS("LFCloudness", "@text/channel.upnpcontrol.lfcloudness.label",
            "@text/channel.upnpcontrol.lfcloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    RFC_LOUDNESS("RFCloudness", "@text/channel.upnpcontrol.rfcloudness.label",
            "@text/channel.upnpcontrol.rfcloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    SD_LOUDNESS("SDloudness", "@text/channel.upnpcontrol.sdloudness.label",
            "@text/channel.upnpcontrol.sdloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    SL_LOUDNESS("SLloudness", "@text/channel.upnpcontrol.slloudness.label",
            "@text/channel.upnpcontrol.slloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    SR_LOUDNESS("SRloudness", "@text/channel.upnpcontrol.srloudness.label",
            "@text/channel.upnpcontrol.srloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    T_LOUDNESS("Tloudness", "@text/channel.upnpcontrol.tloudness.label",
            "@text/channel.upnpcontrol.tloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    B_LOUDNESS("Bloudness", "@text/channel.upnpcontrol.bloudness.label",
            "@text/channel.upnpcontrol.bloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    BC_LOUDNESS("BCloudness", "@text/channel.upnpcontrol.bcloudness.label",
            "@text/channel.upnpcontrol.bcloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    BL_LOUDNESS("BLloudness", "@text/channel.upnpcontrol.blloudness.label",
            "@text/channel.upnpcontrol.blloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS),
    BR_LOUDNESS("BRloudness", "@text/channel.upnpcontrol.brloudness.label",
            "@text/channel.upnpcontrol.brloudness.description", ITEM_TYPE_LOUDNESS, CHANNEL_TYPE_LOUDNESS);

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
