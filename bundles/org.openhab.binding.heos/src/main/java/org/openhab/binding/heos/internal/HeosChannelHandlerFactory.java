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

import static org.openhab.binding.heos.HeosBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.handler.HeosChannelHandler;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerAlbum;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerArtist;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerBuildGroup;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerControl;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerCover;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerCurrentPosition;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerDuration;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerFavoriteSelect;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerGrouping;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerInputs;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerMute;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerPlayURL;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerPlayerSelect;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerPlaylist;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerRawCommand;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerReboot;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerRepeatMode;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerShuffleMode;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerStation;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerTitle;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerType;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerVolume;

/**
 * The {@link HeosChannelHandlerFactory} is responsible for creating and returning
 * of the single handler for each channel of the single things.
 * It also stores already created handler for further use.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public class HeosChannelHandlerFactory {

    private HeosBridgeHandler bridge;
    private HeosFacade api;
    private Map<ChannelUID, HeosChannelHandler> handlerStorageMap = new HashMap<>();

    public HeosChannelHandlerFactory(HeosBridgeHandler bridge, HeosFacade api) {
        this.bridge = bridge;
        this.api = api;
    }

    public HeosChannelHandler getChannelHandler(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
        if (handlerStorageMap.containsKey(channelUID)) {
            return handlerStorageMap.get(channelUID);
        } else {
            HeosChannelHandler createdChannelHandler = createNewChannelHandler(channelUID, channelTypeUID);
            handlerStorageMap.put(channelUID, createdChannelHandler);
            return createdChannelHandler;
        }
    }

    private HeosChannelHandler createNewChannelHandler(ChannelUID channelUID, ChannelTypeUID channelTypeUID) {
        switch (channelUID.getId()) {
            case CH_ID_CONTROL:
                return new HeosChannelHandlerControl(bridge, api);
            case CH_ID_VOLUME:
                return new HeosChannelHandlerVolume(bridge, api);
            case CH_ID_MUTE:
                return new HeosChannelHandlerMute(bridge, api);
            case CH_ID_PLAY_URL:
                return new HeosChannelHandlerPlayURL(bridge, api);
            case CH_ID_INPUTS:
                return new HeosChannelHandlerInputs(bridge, api);
            case CH_ID_UNGROUP:
                return new HeosChannelHandlerGrouping(bridge, api);
            case CH_ID_RAW_COMMAND:
                return new HeosChannelHandlerRawCommand(bridge, api);
            case CH_ID_REBOOT:
                return new HeosChannelHandlerReboot(bridge, api);
            case CH_ID_BUILDGROUP:
                return new HeosChannelHandlerBuildGroup(bridge, api);
            case CH_ID_PLAYLISTS:
                return new HeosChannelHandlerPlaylist(bridge, api);
            case CH_ID_REPEAT_MODE:
                return new HeosChannelHandlerRepeatMode(bridge, api);
            case CH_ID_SHUFFLE_MODE:
                return new HeosChannelHandlerShuffleMode(bridge, api);
            case CH_ID_ALBUM:
                return new HeosChannelHandlerAlbum(bridge, api);
            case CH_ID_SONG:
                return new HeosChannelHandlerTitle(bridge, api);
            case CH_ID_ARTIST:
                return new HeosChannelHandlerArtist(bridge, api);
            case CH_ID_COVER:
                return new HeosChannelHandlerCover(bridge, api);
            case CH_ID_CUR_POS:
                return new HeosChannelHandlerCurrentPosition(bridge, api);
            case CH_ID_DURATION:
                return new HeosChannelHandlerDuration(bridge, api);
            case CH_ID_TYPE:
                return new HeosChannelHandlerType(bridge, api);
            case CH_ID_STATION:
                return new HeosChannelHandlerStation(bridge, api);
        }
        if (channelTypeUID != null) {
            if (CH_TYPE_FAVORITE.equals(channelTypeUID)) {
                return new HeosChannelHandlerFavoriteSelect(bridge, api);
            }
            if (CH_TYPE_PLAYER.equals(channelTypeUID)) {
                return new HeosChannelHandlerPlayerSelect(bridge, api);
            }
        }
        return null;
    }
}
