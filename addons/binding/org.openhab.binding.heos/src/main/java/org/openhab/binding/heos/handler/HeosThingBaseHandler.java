/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
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

    protected String id;
    protected HeosSystem heos;
    protected HeosFacade api;
    protected HeosChannelHandlerFactory channelHandlerFactory;
    protected HeosBridgeHandler bridge;

    private long refreshStartTime = 0;
    private long refreshRequestTime = 0;
    private final int REFRESH_BLOCK_TIME = 5000;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public HeosThingBaseHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing);
        this.heos = heos;
        this.api = api;
        setId();
    }

    /**
     * To be implemented by extending class by the command
     * for updating the HEOS thing via the {@link HeosSystem}. As example
     * for a player the command would be {@link HeosSystem#getPlayerState(String)}
     */

    protected abstract void updateHeosThingState();

    /**
     * The channels which has to be updated during the refresh command
     * For more information about refreshing the channels see
     * {link {@link HeosThingBaseHandler#handleRefresh()}
     */

    protected abstract void refreshChannels();

    public abstract void setStatusOffline();

    public abstract PercentType getNotificationSoundVolume();

    public abstract void setNotificationSoundVolume(PercentType volume);

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (command instanceof RefreshType) {
            if (this.getThing().getStatus().toString().equals(ONLINE)) {
                handleRefresh();
            }
            return;
        }
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID);
        if (channelHandler != null) {
            channelHandler.handleCommand(command, id, this, channelUID);
        }
    }

    /**
     * Handles the Refresh command.
     * Because refreshing the channels requires a request of the current state
     * via the Telnet connection, updating all channels after each other would
     * lead to a high amount of network traffic. So the handleRefresh() blocks
     * the update request of {@link HeosThingBaseHandler#updateHeosThingState()}.
     */

    private synchronized void handleRefresh() {
        refreshRequestTime = System.currentTimeMillis();
        if (refreshRequestTime - refreshStartTime > REFRESH_BLOCK_TIME) {
            updateHeosThingState();
            refreshChannels();
            refreshStartTime = System.currentTimeMillis();
        }
    }

    private void setId() {
        if (thing.getConfiguration().containsKey(GID)) {
            this.id = thing.getConfiguration().get(GID).toString();
        }
        if (thing.getConfiguration().containsKey(PID)) {
            this.id = thing.getConfiguration().get(PID).toString();
        }
    }

    /**
     * Has to be called by the player or group handler to initialize
     * {@link HeosChannelHandlerFactory} and bridge
     */

    protected void initChannelHandlerFatory() {
        if (getBridge() != null) {
            this.bridge = (HeosBridgeHandler) getBridge().getHandler();
            this.channelHandlerFactory = bridge.getChannelHandlerFactory();
        } else {
            logger.warn("No Bridge set within handler");
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
     * @param url The external URL where the file is located
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

    protected void handleThingStateUpdate(@NonNull String event, @NonNull String command) {
        if (event.equals(HEOS_STATE)) {
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
        if (event.equals(HEOS_VOLUME)) {
            updateState(CH_ID_VOLUME, PercentType.valueOf(command));
        }
        if (event.equals(HEOS_MUTE)) {
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
        if (event.equals(HEOS_CUR_POS)) {
            this.updateState(CH_ID_CUR_POS, StringType.valueOf(command));
        }
        if (event.equals(HEOS_DURATION)) {
            this.updateState(CH_ID_DURATION, StringType.valueOf(command));
        }
        if (event.equals(SHUFFLE_MODE_CHANGED)) {
            if (command.equals(HEOS_ON)) {
                this.updateState(CH_ID_SHUFFLE_MODE, OnOffType.ON);
            } else {
                this.updateState(CH_ID_SHUFFLE_MODE, OnOffType.OFF);
            }
        }
        if (event.equals(REPEAT_MODE_CHANGED)) {
            if (command.toString().equals(HEOS_REPEAT_ALL)) {
                this.updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_ALL));
            } else if (command.toString().equals(HEOS_REPEAT_ONE)) {
                this.updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_ONE));
            } else if (command.toString().equals(HEOS_OFF)) {
                this.updateState(CH_ID_REPEAT_MODE, StringType.valueOf(HEOS_UI_OFF));
            }
        }
    }

    protected void handleThingMediaUpdate(HashMap<String, String> info) {
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
                    updateState(CH_ID_IMAGE_URL, StringType.valueOf(info.get(key)));
                    break;
                case STATION:
                    if (info.get(SID).equals(INPUT_SID)) {
                        String inputName = info.get(MID).substring(info.get(MID).indexOf("/") + 1); // removes the
                                                                                                    // "input/" part
                                                                                                    // before the
                                                                                                    // input name
                        updateState(CH_ID_INPUTS, StringType.valueOf(inputName));
                    }
                    updateState(CH_ID_STATION, StringType.valueOf(info.get(key)));
                    break;
                case TYPE:
                    updateState(CH_ID_TYPE, StringType.valueOf(info.get(key)));
                    if (info.get(key).equals(STATION)) {
                        updateState(CH_ID_STATION, StringType.valueOf(info.get(STATION)));
                    } else {
                        updateState(CH_ID_STATION, StringType.valueOf("No Station"));
                    }
                    break;
            }
        }
    }
}
