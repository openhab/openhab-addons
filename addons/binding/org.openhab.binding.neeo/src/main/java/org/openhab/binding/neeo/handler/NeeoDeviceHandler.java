/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.NeeoUtil;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoDeviceConfig;
import org.openhab.binding.neeo.internal.NeeoDeviceProtocol;
import org.openhab.binding.neeo.internal.NeeoHandlerCallback;
import org.openhab.binding.neeo.internal.NeeoRoomConfig;
import org.openhab.binding.neeo.internal.StatefulHandlerCallback;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoDeviceDetails;
import org.openhab.binding.neeo.internal.models.NeeoDeviceDetailsTiming;
import org.openhab.binding.neeo.internal.models.NeeoMacro;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@link AbstractThingHandler} that is responsible for handling commands for a room
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDeviceHandler extends AbstractThingHandler {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceHandler.class);

    /**
     * The initialization task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<Future<?>> initializationTask = new AtomicReference<>(null);

    /**
     * The refresh task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<ScheduledFuture<?>> refreshTask = new AtomicReference<>(null);

    /** The {@link NeeoDeviceProtocol} (null until set by {@link #initializationTask}) */
    private final AtomicReference<NeeoDeviceProtocol> deviceProtocol = new AtomicReference<>();

    /**
     * Instantiates a new neeo room handler.
     *
     * @param typeGenerator the non-null type generator
     */
    NeeoDeviceHandler(Thing thing) {
        super(thing);
        Objects.requireNonNull(thing, "thing cannot be null");
    }

    /**
     * Handles commands sent to the room
     *
     * @see
     *      org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     *      org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        final NeeoDeviceProtocol protocol = deviceProtocol.get();
        if (protocol == null) {
            logger.debug("Protocol is null - ignoring update: {}", channelUID);
            return;
        }

        final String groupId = channelUID.getGroupId();
        final String channelId = channelUID.getIdWithoutGroup();

        ((StatefulHandlerCallback) protocol.getCallback()).removeState(channelUID.getId());

        if (command instanceof RefreshType) {
            refreshChannel(protocol, groupId, channelId);
        } else {
            switch (groupId) {
                case NeeoConstants.DEVICE_GROUP_MACROSID:
                    if (command instanceof OnOffType) {
                        protocol.setMacroStatus(channelId, command == OnOffType.ON);
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
     * @param groupId the non-empty groupId
     * @param channelId the non-empty channel id
     */
    private void refreshChannel(NeeoDeviceProtocol protocol, String groupId, String channelId) {
        Objects.requireNonNull(protocol, "protocol cannot be null");
        NeeoUtil.requireNotEmpty(groupId, "groupId must not be empty");
        NeeoUtil.requireNotEmpty(channelId, "channelId must not be empty");

        switch (groupId) {
            case NeeoConstants.DEVICE_GROUP_MACROSID:
                protocol.refreshMacroStatus(channelId);
                break;
        }
    }

    /**
     * Simply cancels any existing initialization tasks and schedules a new task
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
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
        if (StringUtils.isEmpty(roomKey)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Room key (from the parent room bridge) was not found");
            return;
        }

        try {
            NeeoUtil.checkInterrupt();
            final NeeoBrainApi brainApi = getNeeoBrainApi();
            if (brainApi == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }

            final NeeoRoom room = brainApi.getRoom(roomKey);
            if (room == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Room (" + roomKey + ") was not found");
                return;
            }

            final NeeoDevice device = room.getDevices().getDevice(config.getDeviceKey());
            if (device == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device (" + config.getDeviceKey() + ") was not found in room (" + roomKey + ")");
                return;
            }

            final ThingUID thingUid = getThing().getUID();
            final List<Channel> channels = new ArrayList<>();
            for (NeeoMacro macro : device.getMacros().getMacros()) {
                final Channel channel = ChannelBuilder
                        .create(new ChannelUID(thingUid, NeeoConstants.DEVICE_GROUP_MACROSID, macro.getKey()), "Switch")
                        .withType(UidUtils.createChannelType(macro)).build();

                channels.add(channel);
            }

            final ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channels);
            updateThing(thingBuilder.build());

            final Map<String, String> properties = new HashMap<>();
            final NeeoDeviceDetails details = device.getDetails();
            if (details != null) {
                /** The following properties have matches in neeo.io: OpenHabToDeviceConverter.java */
                properties.put("Source Name", details.getSourceName());
                properties.put("Adapter Name", details.getAdapterName());
                properties.put("Type", details.getType());
                properties.put("Manufacturer", details.getManufacturer());
                properties.put("Name", details.getName());

                final NeeoDeviceDetailsTiming timing = details.getTiming();
                if (timing != null) {
                    properties.put("Standby Command Delay", NeeoUtil.toString(timing.getStandbyCommandDelay()));
                    properties.put("Source Switch Delay", NeeoUtil.toString(timing.getSourceSwitchDelay()));
                    properties.put("Shutdown Delay", NeeoUtil.toString(timing.getShutdownDelay()));
                }

                properties.put("Device Capabilities", StringUtils.join(details.getDeviceCapabilities(), ','));
            }
            updateProperties(properties);

            NeeoUtil.checkInterrupt();
            final NeeoDeviceProtocol protocol = new NeeoDeviceProtocol(
                    new StatefulHandlerCallback(new NeeoHandlerCallback() {

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

                        @Override
                        public NeeoBrainApi getApi() {
                            return getNeeoBrainApi();
                        }
                    }), roomKey, config.getDeviceKey());
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
     * Helper method to get the room key from the parent bridge (which should be a room)
     *
     * @return a non-null, non-empty room key if found, null if not found
     */
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
     * Cancels/removes the initialization and refresh task, closes/removes the room protocol
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        NeeoUtil.cancel(initializationTask.getAndSet(null));
        NeeoUtil.cancel(refreshTask.getAndSet(null));
        deviceProtocol.getAndSet(null);
    }
}
