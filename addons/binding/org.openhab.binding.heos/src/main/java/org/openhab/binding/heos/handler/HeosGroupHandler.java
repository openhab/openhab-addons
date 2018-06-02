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
import static org.openhab.binding.heos.internal.resources.HeosConstants.GID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
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
import org.openhab.binding.heos.internal.resources.HeosConstants;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosGroupHandler} handles the actions for a HEOS group.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosGroupHandler extends HeosThingBaseHandler {

    private String gid;
    private HeosGroup heosGroup;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public HeosGroupHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing, heos, api);
        gid = thing.getConfiguration().get(GID).toString();
        this.heosGroup = new HeosGroup();
        this.heosGroup.setGid(gid);
        this.heosGroup.setGroupMemberHash(thing.getConfiguration().get(GROUP_MEMBER_HASH).toString());
        setGroupMemberPidList();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, @NonNull Command command) {
        super.handleCommand(channelUID, command);
    }

    /**
     * Init the HEOS group. Starts an extra thread to avoid blocking
     * during start up phase. Gathering all information can take longer
     * than 5 seconds which can throw an error within the openhab system.
     */
    @Override
    public void initialize() {
        this.gid = this.thing.getConfiguration().get(GID).toString();
        this.heosGroup.setGid(gid);
        api.registerforChangeEvents(this);
        ScheduledExecutorService executerPool = Executors.newScheduledThreadPool(1);
        executerPool.schedule(new InitializationRunnable(), 4, TimeUnit.SECONDS);
    }

    @Override
    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(heosGroup.getLevel());
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        api.volumeGroup(volume.toString(), gid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (this.getThing().getStatus().equals(ThingStatus.UNINITIALIZED)) {
            logger.info("Can't Handle Event. Group {} not initialized. Status is: {}", this.getConfig().get(NAME),
                    this.getThing().getStatus().toString());
            return;
        }
        if (pid.equals(this.gid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, HashMap<String, String> info) {
        if (pid.equals(this.gid)) {
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        // Do nothing
    }

    // Generates the groupMember from the properties. Is needed to generate group after restart of OpenHab.

    private void setGroupMemberPidList() {
        String memberListString = thing.getProperties().get(GROUP_MEMBER_PID_LIST);
        memberListString = memberListString.substring(1, memberListString.length() - 1);
        String array[] = memberListString.split(", "); // important: Keep the white space.
        List<String> memberPidList = Arrays.asList(array);
        heosGroup.setGroupMemberPidList(memberPidList);
    }

    /**
     * Sets the status of the HEOS group to OFFLINE.
     * Also sets the UNGROUP channel to OFF and the CONTROL
     * channel to PAUSE
     */
    @Override
    public void setStatusOffline() {
        api.unregisterforChangeEvents(this);
        updateState(CH_ID_UNGROUP, OnOffType.OFF);
        updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     *
     * @return The instance of the HEOS group
     */

    public HeosGroup getHeosGroup() {
        return heosGroup;
    }

    @Override
    protected void updateHeosThingState() {
        heosGroup = heos.getGroupState(heosGroup);
    }

    @Override
    protected void refreshChannels() {
        postCommand(CH_ID_UNGROUP, OnOffType.ON);
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
        if (heosGroup.getShuffle().equals(HeosConstants.HEOS_ON)) {
            updateState(CH_ID_SHUFFLE_MODE, OnOffType.ON);
        }
        if (heosGroup.getShuffle().equals(HeosConstants.HEOS_OFF)) {
            updateState(CH_ID_SHUFFLE_MODE, OnOffType.OFF);
        }
        updateState(CH_ID_SONG, StringType.valueOf(heosGroup.getSong()));
        updateState(CH_ID_ARTIST, StringType.valueOf(heosGroup.getArtist()));
        updateState(CH_ID_ALBUM, StringType.valueOf(heosGroup.getAlbum()));
        updateState(CH_ID_IMAGE_URL, StringType.valueOf(heosGroup.getImageUrl()));
        updateState(CH_ID_STATION, StringType.valueOf(heosGroup.getStation()));
        updateState(CH_ID_TYPE, StringType.valueOf(heosGroup.getType()));
        updateState(CH_ID_CUR_POS, StringType.valueOf("0"));
        updateState(CH_ID_DURATION, StringType.valueOf("0"));
        updateState(CH_ID_REPEAT_MODE, StringType.valueOf(heosGroup.getRepeatMode()));
    }

    public class InitializationRunnable implements Runnable {
        @SuppressWarnings("null")
        @Override
        public void run() {
            initChannelHandlerFatory();
            heosGroup = heos.getGroupState(heosGroup);
            if (!heosGroup.isOnline() || !heosGroup.getGroupMemberHash()
                    .equals(thing.getConfiguration().get(GROUP_MEMBER_HASH).toString())) {
                bridge.setThingStatusOffline(thing.getUID());
                setStatusOffline();
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            bridge.setThingStatusOnline(thing.getUID()); // informs the System about the existing group
            HashMap<String, HeosGroup> usedToFillOldGroupMap = new HashMap<>();
            usedToFillOldGroupMap.put(heosGroup.getNameHash(), heosGroup);
            heos.addHeosGroupToOldGroupMap(usedToFillOldGroupMap);
            id = heosGroup.getGid(); // Updates the id of the group. Needed if group leader has changed
            refreshChannels();
        }
    }
}
