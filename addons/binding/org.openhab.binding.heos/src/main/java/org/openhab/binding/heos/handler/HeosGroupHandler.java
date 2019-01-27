/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.openhab.binding.heos.internal.resources.HeosConstants.GID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        scheduledStartUp();
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
            logger.debug("Can't Handle Event. Group {} not initialized. Status is: {}", this.getConfig().get(NAME),
                    this.getThing().getStatus().toString());
            return;
        }
        if (pid.equals(this.gid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
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

    @Override
    public void setStatusOnline() {
        this.initialize();
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

    private void scheduledStartUp() {
        scheduler.schedule(() -> {
            initChannelHandlerFactory();
            heosGroup = heos.getGroupState(heosGroup);
            if (!heosGroup.isOnline() || !heosGroup.getGroupMemberHash()
                    .equals(thing.getConfiguration().get(GROUP_MEMBER_HASH).toString())) {
                bridge.setThingStatusOffline(thing.getUID());
                setStatusOffline();
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            HashMap<String, HeosGroup> usedToFillOldGroupMap = new HashMap<>();
            usedToFillOldGroupMap.put(heosGroup.getNameHash(), heosGroup);
            heos.addHeosGroupToOldGroupMap(usedToFillOldGroupMap);
            updateState(CH_ID_UNGROUP, OnOffType.ON);
            id = heosGroup.getGid(); // Updates the id of the group. Needed if group leader has changed
        }, 4, TimeUnit.SECONDS);
    }
}
