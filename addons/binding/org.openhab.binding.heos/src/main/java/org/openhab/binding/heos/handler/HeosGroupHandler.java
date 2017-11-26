/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.handler;

import static org.openhab.binding.heos.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.api.HeosAPI;
import org.openhab.binding.heos.api.HeosSystem;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosGroupHandler} handles the actions for a HEOS group.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosGroupHandler extends BaseThingHandler implements HeosEventListener {

    private HeosAPI api;
    private HeosSystem heos;
    private String gid;
    private HeosGroup heosGroup;
    private HeosBridgeHandler bridge;

    private Logger logger = LoggerFactory.getLogger(HeosGroupHandler.class);

    public HeosGroupHandler(Thing thing, HeosSystem heos, HeosAPI api) {
        super(thing);
        this.heos = heos;
        this.api = api;
        gid = thing.getConfiguration().get(GID).toString();

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command.toString().equals("REFRESH")) {
            return;
        }

        if (channelUID.getId().equals(CH_ID_CONTROL)) {

            String com = command.toString();

            switch (com) {

                case "PLAY":
                    api.play(gid);
                    break;
                case "PAUSE":
                    api.pause(gid);
                    break;
                case "NEXT":
                    api.next(gid);
                    break;
                case "PREVIOUS":
                    api.prevoious(gid);
                    break;
                case "ON":
                    api.play(gid);
                    break;
                case "OFF":
                    api.pause(gid);
                    break;

            }
        } else if (channelUID.getId().equals(CH_ID_VOLUME)) {

            api.volumeGroup(command.toString(), gid);

        } else if (channelUID.getId().equals(CH_ID_MUTE)) {

            if (command.toString().equals("ON")) {

                api.muteGroupON(gid);
            } else {
                api.muteGroupOFF(gid);
            }
        } else if (channelUID.getId().equals(CH_ID_UNGROUP)) {

            if (command.toString().equals("OFF")) {
                api.ungroupGroup(gid);
            } else if (command.toString().equals("ON")) {
                String[] playerArray = heosGroup.getGroupMemberPidList().toArray(new String[0]);
                api.groupPlayer(playerArray);
            }

        } else if (channelUID.getId().equals(CH_ID_INPUTS)) { // See player handler for description

            if (bridge.getSelectedPlayer().isEmpty()) {
                api.playInputSource(gid, null, command.toString());
            } else if (bridge.getSelectedPlayer().size() > 1) {
                logger.warn("Only one source can be selected for HEOS Input. Selected amount of sources: {} ",
                        bridge.getSelectedPlayer().size());
                bridge.getSelectedPlayer().clear();
            } else {
                for (String source_pid : bridge.getSelectedPlayer().keySet()) {
                    api.playInputSource(gid, source_pid, command.toString());
                    bridge.getSelectedPlayer().clear();
                }
            }
        } else if (channelUID.getId().equals(CH_ID_PLAY_URL)) {
            api.playURL(gid, command.toString());
        }
    }

    /**
     * Init the HEOS group. Starts an extra thread to avoid blocking
     * during start up phase. Gathering all information can take longer
     * than 5 seconds which can throw an error within the openhab system.
     */
    @Override
    public void initialize() {

        this.gid = this.thing.getConfiguration().get(GID).toString();
        api.registerforChangeEvents(this);
        ScheduledExecutorService executerPool = Executors.newScheduledThreadPool(1);
        executerPool.schedule(new InitializationRunnable(), 4, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
        updateState(CH_ID_STATUS, StringType.valueOf(ONLINE));
        super.initialize();

    }

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
    public void playURL(String url) {
        api.playURL(gid, url);

    }

    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(heosGroup.getLevel());
    }

    public void setNotificationSoundVolume(PercentType volume) {
        api.volume(volume.toString(), gid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {

        if (this.getThing().getStatus().toString().equals(ThingStatus.UNINITIALIZED)) {
            logger.info("Can't Handle Event. Group {} not initialized. Status is: {}", this.getConfig().get(NAME),
                    this.getThing().getStatus().toString());
            return;

        }

        if (pid.equals(this.gid)) {
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

                switch (command) {
                    case ON:
                        this.updateState(CH_ID_MUTE, OnOffType.ON);
                        break;
                    case OFF:
                        updateState(CH_ID_MUTE, OnOffType.OFF);
                        break;
                }

            }
            if (event.equals(CUR_POS)) {
                this.updateState(CH_ID_CUR_POS, StringType.valueOf(command));
            }
            if (event.equals(DURATION)) {
                this.updateState(CH_ID_DURATION, StringType.valueOf(command));
            }

        }

    }

    @Override
    public void playerMediaChangeEvent(String pid, HashMap<String, String> info) {
        if (pid.equals(this.gid)) {

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
                            String inputName = info.get(MID).substring(info.get(MID).indexOf("/") + 1);
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
        // TODO Auto-generated method stub

    }

    public void setStatusOffline() {
        api.unregisterforChangeEvents(this);
        updateState(CH_ID_STATUS, StringType.valueOf(OFFLINE));
        updateState(CH_ID_UNGROUP, OnOffType.OFF);
        updateStatus(ThingStatus.OFFLINE);
    }

    public class InitializationRunnable implements Runnable {

        @Override
        public void run() {

            bridge = (HeosBridgeHandler) getBridge().getHandler();

            heosGroup = heos.getGroupState(gid);

            if (heosGroup.isOnline()
                    || !thing.getConfiguration().get(GROUP_MEMBER_HASH).equals(heosGroup.getGroupMemberHash())) {

                setStatusOffline();
                bridge.thingStatusOffline(thing.getUID());
                return;
            }

            bridge.thingStatusOnline(thing.getUID()); // informs the System about the existing group

            HashMap<String, HeosGroup> usedToFillOldGroupMap = new HashMap<>();
            usedToFillOldGroupMap.put(heosGroup.getNameHash(), heosGroup);
            heos.addHeosGroupToOldGroupMap(usedToFillOldGroupMap);

            updateState(CH_ID_UNGROUP, OnOffType.ON);
            updateState(CH_ID_VOLUME, PercentType.valueOf(heosGroup.getLevel()));

            if (heosGroup.getMute().equals(ON)) {
                updateState(CH_ID_MUTE, OnOffType.ON);
            } else {
                updateState(CH_ID_MUTE, OnOffType.OFF);
            }

            if (heosGroup.getState().equals(PLAY)) {
                updateState(CH_ID_CONTROL, PlayPauseType.PLAY);
            }
            if (heosGroup.getState().equals(PAUSE) || heosGroup.getState().equals(STOP)) {
                updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
            }
            updateState(CH_ID_SONG, StringType.valueOf(heosGroup.getSong()));
            updateState(CH_ID_ARTIST, StringType.valueOf(heosGroup.getArtist()));
            updateState(CH_ID_ALBUM, StringType.valueOf(heosGroup.getAlbum()));
            updateState(CH_ID_IMAGE_URL, StringType.valueOf(heosGroup.getImage_url()));
            updateState(CH_ID_STATION, StringType.valueOf(heosGroup.getStation()));
            updateState(CH_ID_TYPE, StringType.valueOf(heosGroup.getType()));
            updateState(CH_ID_CUR_POS, StringType.valueOf("0"));
            updateState(CH_ID_DURATION, StringType.valueOf("0"));
            updateState(CH_ID_STATUS, StringType.valueOf(ONLINE));

        }

    }

}
