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
import static org.openhab.binding.heos.internal.resources.HeosConstants.PID;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.resources.HeosPlayer;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosPlayerHandler extends HeosThingBaseHandler {

    private String pid;
    private HeosPlayer player = new HeosPlayer();

    public HeosPlayerHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing, heos, api);
        pid = thing.getConfiguration().get(PID).toString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
        api.registerforChangeEvents(this);
        ScheduledExecutorService executerPool = Executors.newScheduledThreadPool(1);
        executerPool.schedule(new InitializationRunnable(), 3, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(player.getLevel());
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        api.setVolume(volume.toString(), pid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (pid.equals(this.pid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, HashMap<String, String> info) {
        this.player.updateMediaInfo(info);
        if (pid.equals(this.pid)) {
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        // Do nothing
    }

    @Override
    @SuppressWarnings("null")
    public void setStatusOffline() {
        api.unregisterforChangeEvents(this);
        updateState(CH_ID_STATUS, StringType.valueOf(OFFLINE));
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    protected void updateHeosThingState() {
        player = heos.getPlayerState(pid);
    }

    @Override
    protected void refreshChannels() {
        if (player.getLevel() != null) {
            updateState(CH_ID_VOLUME, PercentType.valueOf(player.getLevel()));
        }

        if (player.getMute().equals(ON)) {
            updateState(CH_ID_MUTE, OnOffType.ON);
        } else {
            updateState(CH_ID_MUTE, OnOffType.OFF);
        }

        if (player.getState().equals(PLAY)) {
            updateState(CH_ID_CONTROL, PlayPauseType.PLAY);
        }
        if (player.getState().equals(PAUSE) || player.getState().equals(STOP)) {
            updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
        }
        updateState(CH_ID_SONG, StringType.valueOf(player.getSong()));
        updateState(CH_ID_ARTIST, StringType.valueOf(player.getArtist()));
        updateState(CH_ID_ALBUM, StringType.valueOf(player.getAlbum()));
        updateState(CH_ID_IMAGE_URL, StringType.valueOf(player.getImageUrl()));
        updateState(CH_ID_STATUS, StringType.valueOf(ONLINE));
        updateState(CH_ID_STATION, StringType.valueOf(player.getStation()));
        updateState(CH_ID_TYPE, StringType.valueOf(player.getType()));
        updateState(CH_ID_CUR_POS, StringType.valueOf("0"));
        updateState(CH_ID_DURATION, StringType.valueOf("0"));
        updateState(CH_ID_INPUTS, StringType.valueOf("NULL"));
    }

    public class InitializationRunnable implements Runnable {
        @SuppressWarnings("null")
        @Override
        public void run() {
            initChannelHandlerFatory();
            player = heos.getPlayerState(pid);

            if (!player.isOnline()) {
                setStatusOffline();
                bridge.setThingStatusOffline(thing.getUID());
                return;
            }
            bridge.setThingStatusOnline(thing.getUID());
            refreshChannels();
        }
    }
}
