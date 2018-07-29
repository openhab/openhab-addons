/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal;

import static org.openhab.binding.heos.HeosBindingConstants.*;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.handler.HeosChannelHandler;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerBuildGroup;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerControl;
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerDynGroupHandling;
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
import org.openhab.binding.heos.internal.handler.HeosChannelHandlerVolume;

/**
 * The {@link HeosChannelHandlerFactory} is responsible for creating the single handler
 * for the channel of the single things.
 * 
 * @author Johannes Einig - Initial contribution
 *
 */

public class HeosChannelHandlerFactory {

    private HeosBridgeHandler bridge;
    private HeosFacade api;

    public HeosChannelHandlerFactory(HeosBridgeHandler bridge, HeosFacade api) {
        this.bridge = bridge;
        this.api = api;
    }

    public HeosChannelHandler getChannelHandler(ChannelUID channelUID) {
        ChannelTypeUID channelTypeUID;
        Channel channel = bridge.getThing().getChannel(channelUID.getId());
        if (channel == null) {
            channelTypeUID = null;
        } else {
            channelTypeUID = channel.getChannelTypeUID();
        }

        if (channelUID.getId().equals(CH_ID_CONTROL)) {
            return new HeosChannelHandlerControl(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_VOLUME)) {
            return new HeosChannelHandlerVolume(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_MUTE)) {
            return new HeosChannelHandlerMute(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_PLAY_URL)) {
            return new HeosChannelHandlerPlayURL(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_INPUTS)) {
            return new HeosChannelHandlerInputs(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_UNGROUP)) {
            return new HeosChannelHandlerGrouping(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_RAW_COMMAND)) {
            return new HeosChannelHandlerRawCommand(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_REBOOT)) {
            return new HeosChannelHandlerReboot(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_DYNGROUPSHAND)) {
            return new HeosChannelHandlerDynGroupHandling(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_BUILDGROUP)) {
            return new HeosChannelHandlerBuildGroup(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_PLAYLISTS)) {
            return new HeosChannelHandlerPlaylist(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_REPEAT_MODE)) {
            return new HeosChannelHandlerRepeatMode(bridge, api);
        }
        if (channelUID.getId().equals(CH_ID_SHUFFLE_MODE)) {
            return new HeosChannelHandlerShuffleMode(bridge, api);
        }
        if (channelTypeUID != null) {
            if (channelTypeUID.equals(CH_TYPE_FAVORITE)) {
                return new HeosChannelHandlerFavoriteSelect(bridge, api);
            }
            if (channelTypeUID.equals(CH_TYPE_PLAYER)) {
                return new HeosChannelHandlerPlayerSelect(bridge, api);
            }
        }
        return null;
    }
}
