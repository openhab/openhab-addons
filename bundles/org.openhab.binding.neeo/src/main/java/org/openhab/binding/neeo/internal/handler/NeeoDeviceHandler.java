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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.NeeoDeviceConfig;
import org.openhab.binding.neeo.internal.NeeoDeviceProtocol;
import org.openhab.binding.neeo.internal.NeeoHandlerCallback;
import org.openhab.binding.neeo.internal.NeeoRoomConfig;
import org.openhab.binding.neeo.internal.NeeoUtil;
import org.openhab.binding.neeo.internal.UidUtils;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoDeviceDetails;
import org.openhab.binding.neeo.internal.models.NeeoDeviceDetailsTiming;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@link BaseThingHandler} that is responsible for handling commands for a device
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDeviceHandler extends BaseThingHandler {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceHandler.class);

    /**
     * The initialization task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<@Nullable Future<?>> initializationTask = new AtomicReference<>(null);

    /**
     * The refresh task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<@Nullable ScheduledFuture<?>> refreshTask = new AtomicReference<>(null);

    /** The {@link NeeoDeviceProtocol} (null until set by {@link #initializationTask}) */
    private final AtomicReference<@Nullable NeeoDeviceProtocol> deviceProtocol = new AtomicReference<>();

    /**
     * Instantiates a new neeo device handler.
     *
     * @param thing the non-null thing
     */
    NeeoDeviceHandler(Thing thing) {
        super(thing);
        Objects.requireNonNull(thing, "thing cannot be null");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final NeeoDeviceProtocol protocol = deviceProtocol.get();
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

        if (groupId.isEmpty()) {
            logger.debug("GroupID for channel is null - ignoring command: {}", channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            refreshChannel(protocol, groupId, channelId, channelKey);
        } else {
            switch (groupId) {
                case NeeoConstants.DEVICE_GROUP_MACROS_ID:
                    switch (channelId) {
                        case NeeoConstants.DEVICE_CHANNEL_STATUS:
                            if (command instanceof OnOffType) {
                                protocol.setMacroStatus(channelKey, command == OnOffType.ON);
                            }
                            break;
                        default:
                            logger.debug("Unknown channel to set: {}", channelUID);
                            break;
                    }
                    break;
                default:
                    logger.debug("Unknown group to set: {}", channelUID);
                    break;
            }
        }
    }

    /**
     * Refresh the specified channel section, key and id using the specified protocol
     *
     * @param protocol a non-null protocol to use
     * @param groupId the non-empty groupId
     * @param channelId the non-empty channel id
     * @param channelKey the non-empty channel key
     */
    private void refreshChannel(NeeoDeviceProtocol protocol, String groupId, String channelId, String channelKey) {
        Objects.requireNonNull(protocol, "protocol cannot be null");
        NeeoUtil.requireNotEmpty(groupId, "groupId must not be empty");
        NeeoUtil.requireNotEmpty(channelId, "channelId must not be empty");
        NeeoUtil.requireNotEmpty(channelKey, "channelKey must not be empty");

        switch (groupId) {
            case NeeoConstants.DEVICE_GROUP_MACROS_ID:
                switch (channelId) {
                    case NeeoConstants.DEVICE_CHANNEL_STATUS:
                        protocol.refreshMacroStatus(channelKey);
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
     * Initializes the task be creating the {@link NeeoDeviceProtocol}, going online and then scheduling the refresh
     * task.
     */
    private void initializeTask() {
        final NeeoDeviceConfig config = getConfigAs(NeeoDeviceConfig.class);

        final String roomKey = getRoomKey();
        if (roomKey == null || roomKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Room key (from the parent room bridge) was not found");
            return;
        }

        final String deviceKey = config.getDeviceKey();
        if (deviceKey == null || deviceKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device key was not found or empty");
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

            final NeeoDevice device = room.getDevices().getDevice(deviceKey);
            if (device == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device (" + config.getDeviceKey() + ") was not found in room (" + roomKey + ")");
                return;
            }

            final ThingUID thingUid = getThing().getUID();

            final Map<String, String> properties = new HashMap<>();
            final NeeoDeviceDetails details = device.getDetails();
            if (details != null) {
                /** The following properties have matches in org.openhab.io.neeo.OpenHabToDeviceConverter.java */
                addProperty(properties, "Source Name", details.getSourceName());
                addProperty(properties, "Adapter Name", details.getAdapterName());
                addProperty(properties, "Type", details.getType());
                addProperty(properties, "Manufacturer", details.getManufacturer());
                addProperty(properties, "Name", details.getName());

                final NeeoDeviceDetailsTiming timing = details.getTiming();
                if (timing != null) {
                    properties.put("Standby Command Delay", toString(timing.getStandbyCommandDelay()));
                    properties.put("Source Switch Delay", toString(timing.getSourceSwitchDelay()));
                    properties.put("Shutdown Delay", toString(timing.getShutdownDelay()));
                }

                properties.put("Device Capabilities",
                        Arrays.stream(details.getDeviceCapabilities()).collect(Collectors.joining(",")));
            }

            final ThingBuilder thingBuilder = editThing();
            thingBuilder.withLabel(device.getName() + " (NEEO " + brainApi.getBrain().getKey() + ")")
                    .withProperties(properties).withChannels(ChannelUtils.generateChannels(thingUid, device));
            updateThing(thingBuilder.build());

            NeeoUtil.checkInterrupt();
            final NeeoDeviceProtocol protocol = new NeeoDeviceProtocol(new NeeoHandlerCallback() {

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
            }, roomKey, deviceKey);
            deviceProtocol.getAndSet(protocol);

            NeeoUtil.checkInterrupt();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.debug("IOException during initialization", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Room " + roomKey + " couldn't be found");
        } catch (InterruptedException e) {
            logger.debug("Initialization was interrupted", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Initialization was interrupted");
        }
    }

    /**
     * Helper method to add a property to the properties map if the value is not null
     *
     * @param properties a non-null properties map
     * @param key a non-null, non-empty key
     * @param value a possibly null, possibly empty key
     */
    private void addProperty(Map<String, String> properties, String key, @Nullable String value) {
        Objects.requireNonNull(properties, "properties cannot be null");
        NeeoUtil.requireNotEmpty(key, "key cannot be empty");
        if (value != null && !value.isEmpty()) {
            properties.put(key, value);
        }
    }

    /**
     * Helper method to get the room key from the parent bridge (which should be a room)
     *
     * @return a non-null, non-empty room key if found, null if not found
     */
    @Nullable
    private String getRoomKey() {
        final Bridge bridge = getBridge();
        if (bridge != null) {
            final BridgeHandler handler = bridge.getHandler();
            if (handler instanceof NeeoRoomHandler) {
                return handler.getThing().getConfiguration().as(NeeoRoomConfig.class).getRoomKey();
            }
        }
        return null;
    }

    /**
     * Helper method to simply create a string from an integer
     *
     * @param i the integer
     * @return the resulting string representation
     */
    private static String toString(@Nullable Integer i) {
        if (i == null) {
            return "";
        }
        return i.toString();
    }

    /**
     * Helper method to return the {@link NeeoRoomHandler} associated with this handler
     *
     * @return a possibly null {@link NeeoRoomHandler}
     */
    @Nullable
    private NeeoRoomHandler getRoomHandler() {
        final Bridge parent = getBridge();
        if (parent != null) {
            final BridgeHandler handler = parent.getHandler();
            if (handler instanceof NeeoRoomHandler roomHandler) {
                return roomHandler;
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
    private NeeoBrainApi getNeeoBrainApi() {
        final NeeoRoomHandler handler = getRoomHandler();
        return handler == null ? null : handler.getNeeoBrainApi();
    }

    /**
     * Returns the brain ID associated with this handler.
     *
     * @return the brain ID or null if not found
     */
    @Nullable
    public String getNeeoBrainId() {
        final NeeoRoomHandler handler = getRoomHandler();
        return handler == null ? null : handler.getNeeoBrainId();
    }

    @Override
    public void dispose() {
        NeeoUtil.cancel(initializationTask.getAndSet(null));
        NeeoUtil.cancel(refreshTask.getAndSet(null));
        deviceProtocol.getAndSet(null);
    }
}
