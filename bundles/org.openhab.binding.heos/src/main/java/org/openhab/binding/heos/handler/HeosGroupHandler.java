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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
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

    private final Logger logger = LoggerFactory.getLogger(HeosGroupHandler.class);

    private String gid;
    private HeosGroup heosGroup;

    private boolean blockInitialization;

    public HeosGroupHandler(Thing thing, HeosSystem heos, HeosFacade api) {
        super(thing, heos, api);
        this.heosGroup = new HeosGroup();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The GID is null if there is no group online with the groubMemberHash
        // Only commands from the UNGROUP channel are passed through
        // to activate the group if it is offline
        if (gid != null || CH_ID_UNGROUP.equals(channelUID.getId())) {
            super.handleCommand(channelUID, command);
        }
    }

    /**
     * Initialize the HEOS group. Starts an extra thread to avoid blocking
     * during start up phase. Gathering all information can take longer
     * than 5 seconds which can throw an error within the OpenHab system.
     */
    @Override
    public synchronized void initialize() {
        // Prevents that initialize() is called multiple times if group goes online
        blockInitialization = true;
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            return;
        }
        // Generates the groupMember from the properties. Is needed to generate group after restart of OpenHab.
        heosGroup.updateGroupPlayers(thing.getConfiguration().get(PROP_GROUP_MEMBERS).toString());
        api.registerforChangeEvents(this);
        scheduledStartUp();
    }

    public String getGroupMemberHash() {
        return heosGroup.getGroupMemberHash();
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
        if (getThing().getStatus().equals(ThingStatus.UNINITIALIZED)) {
            logger.debug("Can't Handle Event. Group {} not initialized. Status is: {}", getConfig().get(PROP_NAME),
                    getThing().getStatus().toString());
            return;
        }
        if (pid.equals(gid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        if (pid.equals(gid)) {
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (HeosConstants.USER_CHANGED.equals(command)) {
            updateThingChannels(channelManager.addFavoriteChannels(heos.getFavorites()));
        }
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
        if (thing.getStatus().equals(ThingStatus.OFFLINE) && !blockInitialization) {
            initialize();
        }
    }

    public HeosGroup getHeosGroup() {
        return heosGroup;
    }

    public String getGroupID() {
        return gid;
    }

    private void updateConfiguration() {
        Map<String, Object> prop = new HashMap<>();
        prop.put(PROP_NAME, heosGroup.getName());
        prop.put(PROP_GROUP_MEMBERS, heosGroup.getGroupMembersAsString());
        prop.put(PROP_GROUP_LEADER, heosGroup.getLeader());
        prop.put(PROP_GROUP_HASH, heosGroup.getGroupMemberHash());
        prop.put(PROP_GID, gid);
        Configuration conf = editConfiguration();
        conf.setProperties(prop);
        updateConfiguration(conf);
    }

    private void scheduledStartUp() {
        scheduler.schedule(() -> {
            initChannelHandlerFactory();
            bridge.addGroupHandlerInformation(this);
            // Checks if there is a group online with the same group member hash.
            // If not setting the group offline.
            id = gid = bridge.getActualGID(heosGroup.getGroupMemberHash());
            if (gid == null) {
                blockInitialization = false;
                setStatusOffline();
            } else {
                heosGroup.setGid(gid);
                heosGroup = heos.getGroupState(heosGroup);
                heos.addHeosGroupToOldGroupMap(heosGroup.getGroupMemberHash(), heosGroup);
                if (bridge.isLoggedin()) {
                    updateThingChannels(channelManager.addFavoriteChannels(heos.getFavorites()));
                }
                updateConfiguration();
                updateStatus(ThingStatus.ONLINE);
                updateState(CH_ID_UNGROUP, OnOffType.ON);
                blockInitialization = false;
            }
        }, 4, TimeUnit.SECONDS);
    }
}
