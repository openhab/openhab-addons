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
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.*;
//import static org.openhab.binding.heos.internal.resources.HeosConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
import org.openhab.binding.heos.internal.HeosChannelManager;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.handler.HeosChannelHandler;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosThingBaseHandler} class is the base Class all HEOS handler have to extend.
 * It provides basic command handling and common needed methods.
 *
 * @author Johannes Einig - Initial contribution
 *
 */
public abstract class HeosThingBaseHandler extends BaseThingHandler implements HeosEventListener {

    private final Logger logger = LoggerFactory.getLogger(HeosThingBaseHandler.class);

    protected String id;
    protected HeosSystem heos;
    protected HeosFacade api;
    protected HeosChannelHandlerFactory channelHandlerFactory;
    protected HeosBridgeHandler bridge;
    protected HeosChannelManager channelManager = new HeosChannelManager(this);

    public HeosThingBaseHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing);
        this.heos = heos;
        this.api = api;
        setId();
    }

    public abstract void setStatusOffline();

    public abstract void setStatusOnline();

    public abstract PercentType getNotificationSoundVolume();

    public abstract void setNotificationSoundVolume(PercentType volume);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ChannelTypeUID channelTypeUID = null; // Needed to detect the favorite channels
        Channel channel = this.getThing().getChannel(channelUID.getId());
        if (channel != null) {
            channelTypeUID = channel.getChannelTypeUID();
        } else {
            logger.debug("No valid channel found");
            return;
        }
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID, channelTypeUID);
        if (channelHandler != null) {
            channelHandler.handleCommand(command, id, this, channelUID);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.ONLINE);
        } else if (ThingStatus.UNINITIALIZED.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void setId() {
        if (thing.getConfiguration().containsKey(GID)) {
            id = thing.getConfiguration().get(GID).toString();
        }
        if (thing.getConfiguration().containsKey(PROP_PID)) {
            id = thing.getConfiguration().get(PROP_PID).toString();
        }
    }

    protected void updateThingChannels(List<Channel> channelList) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    /**
     * Has to be called by the player or group handler to initialize
     * {@link HeosChannelHandlerFactory}
     */
    @SuppressWarnings("null")
    protected void initChannelHandlerFactory() {
        if (getBridge() != null) {
            bridge = (HeosBridgeHandler) getBridge().getHandler();
            channelHandlerFactory = bridge.getChannelHandlerFactory();
        } else {
            logger.warn("No Bridge set within child handler");
        }
    }

    /**
     * Dispose the handler and unregister the handler
     * form Change Events
     */
    @Override
    public void dispose() {
        api.unregisterforChangeEvents(this);
        super.dispose();
    }

    /**
     * Plays a media file from an external source. Can be
     * used for audio sink function
     *
     * @param urlStr The external URL where the file is located
     */
    public void playURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            api.playURL(id, url);
        } catch (MalformedURLException e) {
            logger.debug("Command '{}' is not a propper URL. Error: {}", urlStr, e.getMessage());
        }
    }

    /**
     * Handles the updates send from the HEOS system to
     * the binding. To receive updates the handler has
     * to register itself via {@link HeosFacade} via the method:
     * {@link HeosFacade#registerforChangeEvents(HeosEventListener)}
     *
     * @param event
     * @param command
     */
    @SuppressWarnings("null")
    protected void handleThingStateUpdate(String event, String command) {
        if (event.equals(STATE)) {
            switch (command) {
                case PLAY:
                    updateState(CH_ID_CONTROL, PlayPauseType.PLAY);
                    break;
                case PAUSE:
                    updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
                    break;
                case STOP:
                    updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
                    break;
            }
        }
        if (event.equals(VOLUME)) {
            updateState(CH_ID_VOLUME, PercentType.valueOf(command));
        }
        if (event.equals(MUTE)) {
            if (command != null) {
                switch (command) {
                    case ON:
                        updateState(CH_ID_MUTE, OnOffType.ON);
                        break;
                    case OFF:
                        updateState(CH_ID_MUTE, OnOffType.OFF);
                        break;
                }
            }
        }
        if (event.equals(CUR_POS)) {
            updateState(CH_ID_CUR_POS, DecimalType.valueOf(command.toString()));
        }
        if (event.equals(DURATION)) {
            updateState(CH_ID_DURATION, DecimalType.valueOf(command.toString()));
        }
        if (event.equals(SHUFFLE_MODE_CHANGED)) {
            if (ON.equals(command)) {
                updateState(CH_ID_SHUFFLE_MODE, OnOffType.ON);
            } else {
                updateState(CH_ID_SHUFFLE_MODE, OnOffType.OFF);
            }
        }
        if (event.equals(REPEAT_MODE_CHANGED)) {
            if (REPEAT_ALL.equals(command)) {
                updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_ALL));
            } else if (REPEAT_MODE.equals(command)) {
                updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_ONE));
            } else if (OFF.equals(command)) {
                updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_OFF));
            }
        }
    }

    protected void handleThingMediaUpdate(Map<String, String> info) {
        for (String key : info.keySet()) {
            switch (key) {
                case SONG:
                    updateState(CH_ID_SONG, StringType.valueOf(info.get(key)));
                    break;
                case ARTIST:
                    updateState(CH_ID_ARTIST, StringType.valueOf(info.get(key)));
                    break;
                case ALBUM:
                    updateState(CH_ID_ALBUM, StringType.valueOf(info.get(key)));
                    break;
                case IMAGE_URL:
                    try {
                        URL url = new URL(info.get(key)); // checks if String is proper URL
                        RawType cover = HttpUtil.downloadImage(url.toString());
                        if (cover != null) {
                            updateState(CH_ID_COVER, cover);
                        }
                        break;
                    } catch (MalformedURLException e) {
                        logger.debug("Cover can't be loaded. No propper URL: {}", info.get(key));
                        break;
                    }
                case STATION:
                    updateState(CH_ID_STATION, StringType.valueOf(info.get(key)));
                    if (info.get(SID).equals(INPUT_SID)) {
                        // removes the "input/" part before the input name
                        String inputName = info.get(MID).substring(info.get(MID).indexOf("/") + 1);
                        updateState(CH_ID_INPUTS, StringType.valueOf(inputName));
                    }
                    break;
                case TYPE:
                    if (INPUT_SID.equals(info.get(SID))) {
                        updateState(CH_ID_TYPE, StringType.valueOf(info.get(STATION)));
                    } else {
                        updateState(CH_ID_TYPE, StringType.valueOf(info.get(key)));
                        updateState(CH_ID_INPUTS, StringType.valueOf(""));
                    }
                    if (!STATION.equals(info.get(key))) {
                        updateState(CH_ID_STATION, StringType.valueOf(""));
                    }
                    break;
            }
        }
    }
}
