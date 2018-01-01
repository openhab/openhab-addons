/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosPlayerHandler extends BaseThingHandler implements HeosEventListener {
    private HeosFacade api;
    private HeosSystem heos;
    private String pid;
    private HashMap<String, HeosPlayer> playerMap;
    private HeosPlayer player = new HeosPlayer();
    private HeosBridgeHandler bridge;

    private Logger logger = LoggerFactory.getLogger(HeosPlayerHandler.class);

    public HeosPlayerHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing);
        this.heos = heos;
        this.api = api;
        pid = thing.getConfiguration().get(PID).toString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (channelUID.getId().equals(CH_ID_CONTROL)) {
            String com = command.toString();

            switch (com) {
                case "PLAY":
                    api.play(pid);
                    break;
                case "PAUSE":
                    api.pause(pid);
                    break;
                case "NEXT":
                    api.next(pid);
                    break;
                case "PREVIOUS":
                    api.previous(pid);
                    break;
                case "ON":
                    api.play(pid);
                    break;
                case "OFF":
                    api.pause(pid);
                    break;
            }
        } else if (channelUID.getId().equals(CH_ID_VOLUME)) {
            if (command instanceof IncreaseDecreaseType) {
                if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    api.increaseVolume(pid);
                } else {
                    api.decreaseVolume(pid);
                }
            } else {
                api.setVolume(command.toString(), pid);
            }
        } else if (channelUID.getId().equals(CH_ID_MUTE)) {
            if (command.toString().equals("ON")) {
                api.muteON(pid);
            } else {
                api.muteOFF(pid);
            }
            // Allows playing external sources like Aux In
            // If no player on bridge is selected play input from this this player
            // Only one source can be selected
        } else if (channelUID.getId().equals(CH_ID_INPUTS)) {
            if (bridge.getSelectedPlayer().isEmpty()) {
                api.playInputSource(pid, command.toString());
            } else if (bridge.getSelectedPlayer().size() > 1) {
                logger.warn("Only one source can be selected for HEOS Input. Selected amount of sources: {} ",
                        bridge.getSelectedPlayer().size());
                bridge.getSelectedPlayer().clear();
            } else {
                for (String sourcePid : bridge.getSelectedPlayer().keySet()) {
                    api.playInputSource(pid, sourcePid, command.toString());
                    bridge.getSelectedPlayer().clear();
                }
            }
        } else if (channelUID.getId().equals(CH_ID_PLAY_URL)) {
            try {
                URL url = new URL(command.toString());
                api.playURL(pid, url);
            } catch (MalformedURLException e) {
                logger.debug("Command '{}' is not a propper URL. Error: {}", command.toString(), e.getMessage());
            }

        }
    }

    @Override
    public void initialize() {
        api.registerforChangeEvents(this);
        ScheduledExecutorService executerPool = Executors.newScheduledThreadPool(1);
        executerPool.schedule(new InitializationRunnable(), 3, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
        api.unregisterforChangeEvents(this);
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
            api.playURL(pid, url);
        } catch (MalformedURLException e) {
            logger.debug("Command '{}' is not a propper URL. Error: {}", urlStr, e.getMessage());
        }
    }

    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(player.getLevel());
    }

    public void setNotificationSoundVolume(PercentType volume) {
        api.setVolume(volume.toString(), pid);
    }

    @SuppressWarnings("null")
    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (pid.equals(this.pid)) {
            if (event.equals(STATE)) {
                switch (command) {
                    case PLAY:
                        updateState(CH_ID_CONTROL, PlayPauseType.PLAY);
                        player.setState(PLAY);
                        break;
                    case PAUSE:
                        updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
                        player.setState(PAUSE);
                        break;
                    case STOP:
                        updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
                        player.setState(PAUSE);
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
                this.updateState(CH_ID_CUR_POS, StringType.valueOf(command));
            }
            if (event.equals(DURATION)) {
                this.updateState(CH_ID_DURATION, StringType.valueOf(command));
                player.setDuration(command);
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    public void playerMediaChangeEvent(String pid, HashMap<String, String> info) {
        this.player.updateMediaInfo(info);

        if (pid.equals(this.pid)) {
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

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        // Do nothing
    }

    @SuppressWarnings("null")
    public void setStatusOffline() {
        api.unregisterforChangeEvents(this);
        updateState(CH_ID_STATUS, StringType.valueOf(OFFLINE));
        updateStatus(ThingStatus.OFFLINE);
    }

    public class InitializationRunnable implements Runnable {
        @SuppressWarnings("null")
        @Override
        public void run() {
            bridge = (HeosBridgeHandler) getBridge().getHandler();

            player = heos.getPlayerState(pid);

            if (!player.isOnline()) {
                setStatusOffline();
                bridge.thingStatusOffline(thing.getUID());
                return;
            }
            bridge.thingStatusOnline(thing.getUID());

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
    }
}
