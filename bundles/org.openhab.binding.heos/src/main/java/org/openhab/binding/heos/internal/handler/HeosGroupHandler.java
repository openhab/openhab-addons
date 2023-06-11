/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.handler.FutureUtil.cancel;
import static org.openhab.binding.heos.internal.json.dto.HeosEvent.PLAYER_VOLUME_CHANGED;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.configuration.GroupConfiguration;
import org.openhab.binding.heos.internal.exception.HeosFunctionalException;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosGroupHandler} handles the actions for a HEOS group.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosGroupHandler extends HeosThingBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(HeosGroupHandler.class);

    private @NonNullByDefault({}) GroupConfiguration configuration;
    private @Nullable String gid;

    private boolean blockInitialization;
    private @Nullable Future<?> scheduledStartupFuture;

    public HeosGroupHandler(Thing thing, HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        super(thing, heosDynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The GID is null if there is no group online with the groupMemberHash
        // Only commands from the UNGROUP channel are passed through
        // to activate the group if it is offline
        if (gid != null || CH_ID_UNGROUP.equals(channelUID.getId())) {
            @Nullable
            HeosChannelHandler channelHandler = getHeosChannelHandler(channelUID);
            if (channelHandler != null) {
                try {
                    @Nullable
                    String id = getMaybeId(channelUID, command);
                    channelHandler.handleGroupCommand(command, id, thing.getUID(), this);
                    handleSuccess();
                } catch (IOException | ReadException e) {
                    handleError(e);
                }
            }
        }
    }

    @Nullable
    private String getMaybeId(ChannelUID channelUID, Command command) throws HeosNotFoundException {
        if (isCreateGroupRequest(channelUID, command)) {
            return null;
        } else {
            return getId();
        }
    }

    private boolean isCreateGroupRequest(ChannelUID channelUID, Command command) {
        return CH_ID_UNGROUP.equals(channelUID.getId()) && OnOffType.ON == command;
    }

    /**
     * Initialize the HEOS group. Starts an extra thread to avoid blocking
     * during start up phase. Gathering all information can take longer
     * than 5 seconds which can throw an error within the openHAB system.
     */
    @Override
    public synchronized void initialize() {
        super.initialize();

        configuration = thing.getConfiguration().as(GroupConfiguration.class);

        // Prevents that initialize() is called multiple times if group goes online
        blockInitialization = true;

        scheduledStartUp();
    }

    @Override
    public void dispose() {
        cancel(scheduledStartupFuture);
        super.dispose();
    }

    @Override
    public String getId() throws HeosNotFoundException {
        @Nullable
        String localGroupId = this.gid;
        if (localGroupId == null) {
            throw new HeosNotFoundException();
        }
        return localGroupId;
    }

    public String getGroupMemberHash() {
        return HeosGroup.calculateGroupMemberHash(configuration.members);
    }

    public String[] getGroupMemberPidList() {
        return configuration.members.split(";");
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        super.setNotificationSoundVolume(volume);
        try {
            getApiConnection().volumeGroup(volume.toString(), getId());
        } catch (IOException | ReadException e) {
            logger.warn("Failed to set notification volume", e);
        }
    }

    @Override
    public void playerStateChangeEvent(HeosEventObject eventObject) {
        if (ThingStatus.UNINITIALIZED == getThing().getStatus()) {
            logger.debug("Can't Handle Event. Group {} not initialized. Status is: {}", getConfig().get(PROP_NAME),
                    getThing().getStatus());
            return;
        }

        @Nullable
        String localGid = this.gid;
        @Nullable
        String eventGroupId = eventObject.getAttribute(HeosCommunicationAttribute.GROUP_ID);
        @Nullable
        String eventPlayerId = eventObject.getAttribute(HeosCommunicationAttribute.PLAYER_ID);
        if (localGid == null || !(localGid.equals(eventGroupId) || localGid.equals(eventPlayerId))) {
            return;
        }

        if (PLAYER_VOLUME_CHANGED.equals(eventObject.command)) {
            logger.debug("Ignoring player-volume changes for groups");
            return;
        }

        handleThingStateUpdate(eventObject);
    }

    @Override
    public void playerStateChangeEvent(HeosResponseObject<?> responseObject) throws HeosFunctionalException {
        if (ThingStatus.UNINITIALIZED == getThing().getStatus()) {
            logger.debug("Can't Handle Event. Group {} not initialized. Status is: {}", getConfig().get(PROP_NAME),
                    getThing().getStatus());
            return;
        }

        @Nullable
        String localGid = this.gid;
        if (localGid == null || !localGid.equals(responseObject.getAttribute(HeosCommunicationAttribute.GROUP_ID))) {
            return;
        }

        handleThingStateUpdate(responseObject);
    }

    @Override
    public void playerMediaChangeEvent(String pid, Media media) {
        if (!pid.equals(gid)) {
            return;
        }

        handleThingMediaUpdate(media);
    }

    /**
     * Sets the status of the HEOS group to OFFLINE.
     * Also sets the UNGROUP channel to OFF and the CONTROL
     * channel to PAUSE
     */
    @Override
    public void setStatusOffline() {
        logger.debug("Status was set offline");
        try {
            getApiConnection().unregisterForChangeEvents(this);
        } catch (HeosNotConnectedException e) {
            logger.debug("Not connected, failed to unregister");
        }
        updateState(CH_ID_UNGROUP, OnOffType.OFF);
        updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED, "Group is not available on HEOS system");
    }

    @Override
    public void setStatusOnline() {
        if (!blockInitialization) {
            initialize();
        } else {
            logger.debug("Not initializing from setStatusOnline ({}, {})", thing.getStatus(), blockInitialization);
        }
    }

    private void updateConfiguration(String groupId, Group group) {
        Map<String, String> prop = new HashMap<>();
        prop.put(PROP_NAME, group.name);
        prop.put(PROP_GROUP_MEMBERS, group.getGroupMemberIds());
        prop.put(PROP_GROUP_LEADER, group.getLeaderId());
        prop.put(PROP_GROUP_HASH, HeosGroup.calculateGroupMemberHash(group));
        prop.put(PROP_GID, groupId);
        updateProperties(prop);
    }

    private void scheduledStartUp() {
        cancel(scheduledStartupFuture);
        scheduledStartupFuture = scheduler.submit(this::delayedInitialize);
    }

    private void delayedInitialize() {
        @Nullable
        HeosBridgeHandler bridgeHandler = this.bridgeHandler;

        if (bridgeHandler == null) {
            logger.debug("Bridge handler not found, rescheduling");
            scheduledStartUp();
            return;
        }

        if (bridgeHandler.isLoggedIn()) {
            handleDynamicStatesSignedIn();
        }

        bridgeHandler.addGroupHandlerInformation(this);
        // Checks if there is a group online with the same group member hash.
        // If not setting the group offline.
        @Nullable
        String groupId = bridgeHandler.getActualGID(HeosGroup.calculateGroupMemberHash(configuration.members));
        if (groupId == null) {
            blockInitialization = false;
            setStatusOffline();
        } else {
            try {
                refreshPlayState(groupId);

                HeosResponseObject<Group> response = getApiConnection().getGroupInfo(groupId);
                @Nullable
                Group group = response.payload;
                if (group == null) {
                    throw new IllegalStateException("Invalid group response received");
                }

                assertSameGroup(group);

                gid = groupId;
                updateConfiguration(groupId, group);
                updateStatus(ThingStatus.ONLINE);
                updateState(CH_ID_UNGROUP, OnOffType.ON);
                blockInitialization = false;
            } catch (IOException | ReadException | IllegalStateException e) {
                logger.debug("Failed initializing, will retry", e);
                cancel(scheduledStartupFuture, false);
                scheduledStartupFuture = scheduler.schedule(this::delayedInitialize, 30, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Make sure the given group is group which this handler represents
     * 
     * @param group retrieved from HEOS system
     */
    private void assertSameGroup(Group group) {
        String storedGroupHash = HeosGroup.calculateGroupMemberHash(configuration.members);
        String retrievedGroupHash = HeosGroup.calculateGroupMemberHash(group);

        if (!retrievedGroupHash.equals(storedGroupHash)) {
            throw new IllegalStateException("Invalid group received, members / hash do not match.");
        }
    }

    @Override
    void refreshPlayState(String id) throws IOException, ReadException {
        super.refreshPlayState(id);

        handleThingStateUpdate(getApiConnection().getGroupMuteState(id));
        handleThingStateUpdate(getApiConnection().getGroupVolume(id));
    }
}
