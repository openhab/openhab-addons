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
package org.openhab.binding.neeo.internal.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.NeeoHandlerCallback;
import org.openhab.binding.neeo.internal.NeeoRoomConfig;
import org.openhab.binding.neeo.internal.NeeoRoomProtocol;
import org.openhab.binding.neeo.internal.NeeoUtil;
import org.openhab.binding.neeo.internal.UidUtils;
import org.openhab.binding.neeo.internal.models.NeeoAction;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subclass of {@link BaseBridgeHandler} that is responsible for handling commands for a room
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoRoomHandler extends BaseBridgeHandler {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoRoomHandler.class);

    /**
     * The initialization task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<@Nullable Future<?>> initializationTask = new AtomicReference<>();

    /**
     * The refresh task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<@Nullable Future<?>> refreshTask = new AtomicReference<>();

    /** The {@link NeeoRoomProtocol} (null until set by {@link #initializationTask}) */
    private final AtomicReference<@Nullable NeeoRoomProtocol> roomProtocol = new AtomicReference<>();

    /**
     * Instantiates a new neeo room handler.
     *
     * @param bridge the non-null bridge
     */
    NeeoRoomHandler(Bridge bridge) {
        super(bridge);
        Objects.requireNonNull(bridge, "bridge cannot be null");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final NeeoRoomProtocol protocol = roomProtocol.get();
        if (protocol == null) {
            logger.debug("Protocol is null - ignoring update: {}", channelUID);
            return;
        }

        final String[] channelIds = UidUtils.parseChannelId(channelUID);
        if (channelIds.length == 0) {
            logger.debug("Bad group declaration: {}", channelUID);
            return;
        }

        final String localGroupId = channelUID.getGroupId();
        final String groupId = localGroupId == null || localGroupId.isEmpty() ? "" : localGroupId;
        final String channelId = channelIds[0];
        final String channelKey = channelIds.length > 1 ? channelIds[1] : "";

        if (command instanceof RefreshType) {
            refreshChannel(protocol, groupId, channelKey, channelId);
        } else {
            switch (groupId) {
                case NeeoConstants.ROOM_GROUP_RECIPE_ID:
                    switch (channelId) {
                        case NeeoConstants.ROOM_CHANNEL_STATUS:
                            // Ignore OFF status updates
                            if (command == OnOffType.ON) {
                                protocol.startRecipe(channelKey);
                            }
                            break;
                    }
                    break;
                case NeeoConstants.ROOM_GROUP_SCENARIO_ID:
                    switch (channelId) {
                        case NeeoConstants.ROOM_CHANNEL_STATUS:
                            if (command instanceof OnOffType) {
                                protocol.setScenarioStatus(channelKey, command == OnOffType.ON);
                            }
                            break;
                    }
                    break;
                default:
                    logger.debug("Unknown channel to set: {}", channelUID);
                    break;
            }
        }
    }

    /**
     * Refresh the specified channel section, key and id using the specified protocol
     *
     * @param protocol a non-null protocol to use
     * @param groupId the non-empty channel section
     * @param channelKey the non-empty channel key
     * @param channelId the non-empty channel id
     */
    private void refreshChannel(NeeoRoomProtocol protocol, String groupId, String channelKey, String channelId) {
        Objects.requireNonNull(protocol, "protocol cannot be null");
        NeeoUtil.requireNotEmpty(groupId, "groupId must not be empty");
        NeeoUtil.requireNotEmpty(channelId, "channelId must not be empty");

        switch (groupId) {
            case NeeoConstants.ROOM_GROUP_RECIPE_ID:
                NeeoUtil.requireNotEmpty(channelKey, "channelKey must not be empty");
                switch (channelId) {
                    case NeeoConstants.ROOM_CHANNEL_NAME:
                        protocol.refreshRecipeName(channelKey);
                        break;
                    case NeeoConstants.ROOM_CHANNEL_TYPE:
                        protocol.refreshRecipeType(channelKey);
                        break;
                    case NeeoConstants.ROOM_CHANNEL_ENABLED:
                        protocol.refreshRecipeEnabled(channelKey);
                        break;
                    case NeeoConstants.ROOM_CHANNEL_STATUS:
                        protocol.refreshRecipeStatus(channelKey);
                        break;
                }
                break;
            case NeeoConstants.ROOM_GROUP_SCENARIO_ID:
                NeeoUtil.requireNotEmpty(channelKey, "channelKey must not be empty");
                switch (channelId) {
                    case NeeoConstants.ROOM_CHANNEL_NAME:
                        protocol.refreshScenarioName(channelKey);
                        break;
                    case NeeoConstants.ROOM_CHANNEL_CONFIGURED:
                        protocol.refreshScenarioConfigured(channelKey);
                        break;
                    case NeeoConstants.ROOM_CHANNEL_STATUS:
                        protocol.refreshScenarioStatus(channelKey);
                        break;
                }
                break;
        }
    }

    @Override
    public void initialize() {
        NeeoUtil.cancel(initializationTask.getAndSet(scheduler.submit(() -> {
            initializeTask();
        })));
    }

    /**
     * Initializes the task be creating the {@link NeeoRoomProtocol}, going online and then scheduling the refresh task.
     */
    private void initializeTask() {
        final NeeoRoomConfig config = getConfigAs(NeeoRoomConfig.class);

        final String roomKey = config.getRoomKey();
        if (roomKey == null || roomKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Room key (from the parent room bridge) was not found");
            return;
        }

        try {
            NeeoUtil.checkInterrupt();
            final NeeoBrainApi brainApi = getNeeoBrainApi();
            if (brainApi == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Cannot find the NEEO Brain API");
                return;
            }

            final NeeoRoom room = brainApi.getRoom(roomKey);

            final ThingUID thingUid = getThing().getUID();

            final Map<String, String> properties = new HashMap<>();
            properties.put("Key", roomKey);

            final ThingBuilder thingBuilder = editThing();
            thingBuilder.withLabel(room.getName() + " (NEEO " + brainApi.getBrain().getKey() + ")")
                    .withProperties(properties).withChannels(ChannelUtils.generateChannels(thingUid, room));
            updateThing(thingBuilder.build());

            NeeoUtil.checkInterrupt();
            final NeeoRoomProtocol protocol = new NeeoRoomProtocol(new NeeoHandlerCallback() {

                @Override
                public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
                    updateStatus(status, detail, msg);
                }

                @Override
                public void stateChanged(String channelId, State state) {
                    updateState(channelId, state);
                }

                @Override
                public void setProperty(String propertyName, String propertyValue) {
                    getThing().setProperty(propertyName, propertyValue);
                }

                @Override
                public void scheduleTask(Runnable task, long milliSeconds) {
                    scheduler.schedule(task, milliSeconds, TimeUnit.MILLISECONDS);
                }

                @Override
                public void triggerEvent(String channelID, String event) {
                    triggerChannel(channelID, event);
                }

                @Nullable
                @Override
                public NeeoBrainApi getApi() {
                    return getNeeoBrainApi();
                }
            }, roomKey);
            roomProtocol.getAndSet(protocol);

            NeeoUtil.checkInterrupt();
            updateStatus(ThingStatus.ONLINE);

            if (config.getRefreshPolling() > 0) {
                NeeoUtil.checkInterrupt();
                NeeoUtil.cancel(refreshTask.getAndSet(scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        refreshState();
                    } catch (InterruptedException e) {
                        logger.debug("Refresh State was interrupted", e);
                    }
                }, 0, config.getRefreshPolling(), TimeUnit.SECONDS)));
            }
        } catch (IOException e) {
            logger.debug("IOException during initialization", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Room " + config.getRoomKey() + " couldn't be found");
        } catch (InterruptedException e) {
            logger.debug("Initialization was interrupted", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Initialization was interrupted");
        }
    }

    /**
     * Processes the action if it applies to this room
     *
     * @param action a non-null action to process
     */
    void processAction(NeeoAction action) {
        Objects.requireNonNull(action, "action cannot be null");
        final NeeoRoomProtocol protocol = roomProtocol.get();
        if (protocol != null) {
            protocol.processAction(action);
        }
    }

    /**
     * Refreshes the state of the room by calling {@link NeeoRoomProtocol#refreshState()}
     *
     * @throws InterruptedException if the call is interrupted
     */
    private void refreshState() throws InterruptedException {
        NeeoUtil.checkInterrupt();
        final NeeoRoomProtocol protocol = roomProtocol.get();
        if (protocol != null) {
            NeeoUtil.checkInterrupt();
            protocol.refreshState();
        }
    }

    /**
     * Helper method to return the {@link NeeoBrainHandler} associated with this handler
     *
     * @return a possibly null {@link NeeoBrainHandler}
     */
    @Nullable
    private NeeoBrainHandler getBrainHandler() {
        final Bridge parent = getBridge();
        if (parent != null) {
            final BridgeHandler handler = parent.getHandler();
            if (handler instanceof NeeoBrainHandler) {
                return ((NeeoBrainHandler) handler);
            }
        }
        return null;
    }

    /**
     * Returns the {@link NeeoBrainApi} associated with this handler.
     *
     * @return the {@link NeeoBrainApi} or null if not found
     */
    @Nullable
    public NeeoBrainApi getNeeoBrainApi() {
        final NeeoBrainHandler handler = getBrainHandler();
        return handler == null ? null : handler.getNeeoBrainApi();
    }

    /**
     * Returns the brain ID associated with this handler.
     *
     * @return the brain ID or null if not found
     */
    @Nullable
    public String getNeeoBrainId() {
        final NeeoBrainHandler handler = getBrainHandler();
        return handler == null ? null : handler.getNeeoBrainId();
    }

    @Override
    public void dispose() {
        NeeoUtil.cancel(initializationTask.getAndSet(null));
        NeeoUtil.cancel(refreshTask.getAndSet(null));
        roomProtocol.getAndSet(null);
    }
}
