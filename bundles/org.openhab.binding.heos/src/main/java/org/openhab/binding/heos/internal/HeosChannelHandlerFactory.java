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
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.heos.internal.handler.*;
import org.openhab.binding.heos.internal.resources.HeosEventListener;

/**
 * The {@link HeosChannelHandlerFactory} is responsible for creating and returning
 * of the single handler for each channel of the single things.
 * It also stores already created handler for further use.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerFactory {
    private final HeosBridgeHandler bridge;
    private final HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider;
    private final Map<ChannelUID, HeosChannelHandler> handlerStorageMap = new HashMap<>();

    public HeosChannelHandlerFactory(HeosBridgeHandler bridge,
            HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        this.bridge = bridge;
        this.heosDynamicStateDescriptionProvider = heosDynamicStateDescriptionProvider;
    }

    public @Nullable HeosChannelHandler getChannelHandler(ChannelUID channelUID, HeosEventListener eventListener,
            @Nullable ChannelTypeUID channelTypeUID) {
        if (handlerStorageMap.containsKey(channelUID)) {
            return handlerStorageMap.get(channelUID);
        } else {
            HeosChannelHandler handler = createNewChannelHandler(channelUID, eventListener, channelTypeUID);
            if (handler != null) {
                handlerStorageMap.put(channelUID, handler);
            }
            return handler;
        }
    }

    private @Nullable HeosChannelHandler createNewChannelHandler(ChannelUID channelUID, HeosEventListener eventListener,
            @Nullable ChannelTypeUID channelTypeUID) {
        switch (channelUID.getId()) {
            case CH_ID_CONTROL:
                return new HeosChannelHandlerControl(eventListener, bridge);
            case CH_ID_VOLUME:
                return new HeosChannelHandlerVolume(eventListener, bridge);
            case CH_ID_MUTE:
                return new HeosChannelHandlerMute(eventListener, bridge);
            case CH_ID_INPUTS:
                return new HeosChannelHandlerInputs(eventListener, bridge);
            case CH_ID_REPEAT_MODE:
                return new HeosChannelHandlerRepeatMode(eventListener, bridge);
            case CH_ID_SHUFFLE_MODE:
                return new HeosChannelHandlerShuffleMode(eventListener, bridge);
            case CH_ID_ALBUM:
            case CH_ID_SONG:
            case CH_ID_ARTIST:
            case CH_ID_COVER:
            case CH_ID_TYPE:
            case CH_ID_STATION:
                return new HeosChannelHandlerNowPlaying(eventListener, bridge);
            case CH_ID_QUEUE:
                return new HeosChannelHandlerQueue(heosDynamicStateDescriptionProvider, bridge);
            case CH_ID_CLEAR_QUEUE:
                return new HeosChannelHandlerClearQueue(bridge);

            case CH_ID_PLAY_URL:
                return new HeosChannelHandlerPlayURL(bridge);
            case CH_ID_UNGROUP:
                return new HeosChannelHandlerGrouping(bridge);
            case CH_ID_RAW_COMMAND:
                return new HeosChannelHandlerRawCommand(eventListener, bridge);
            case CH_ID_REBOOT:
                return new HeosChannelHandlerReboot(bridge);
            case CH_ID_BUILDGROUP:
                return new HeosChannelHandlerBuildGroup(channelUID, bridge);
            case CH_ID_PLAYLISTS:
                return new HeosChannelHandlerPlaylist(heosDynamicStateDescriptionProvider, bridge);
            case CH_ID_FAVORITES:
                return new HeosChannelHandlerFavorite(heosDynamicStateDescriptionProvider, bridge);
            case CH_ID_CUR_POS:
            case CH_ID_DURATION:
                // nothing to handle, we receive updates automatically
                return null;
        }

        if (channelTypeUID != null) {
            if (CH_TYPE_PLAYER.equals(channelTypeUID)) {
                return new HeosChannelHandlerPlayerSelect(channelUID, bridge);
            }
        }
        return null;
    }
}
