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
package org.openhab.binding.growatt.internal.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.config.GrowattInverterConfiguration;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GrowattInverterHandler} is a thing handler for Growatt inverters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattInverterHandler extends BaseThingHandler {

    // data-logger sends packets each 5 minutes; timeout means 2 packets missed
    private static final int AWAITING_DATA_TIMEOUT_MINUTES = 11;

    private final Logger logger = LoggerFactory.getLogger(GrowattInverterHandler.class);

    private String deviceId = "unknown";

    private @Nullable ScheduledFuture<?> awaitingDataTimeoutTask;

    public GrowattInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> task = awaitingDataTimeoutTask;
        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // everything is read only so do nothing
    }

    @Override
    public void initialize() {
        GrowattInverterConfiguration config = getConfigAs(GrowattInverterConfiguration.class);
        deviceId = config.deviceId;
        thing.setProperty(GrowattInverterConfiguration.DEVICE_ID, deviceId);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.awaiting-data");
        scheduleAwaitingDataTimeoutTask();
        logger.debug("initialize() thing has {} channels", thing.getChannels().size());
    }

    private void scheduleAwaitingDataTimeoutTask() {
        ScheduledFuture<?> task = awaitingDataTimeoutTask;
        if (task != null) {
            task.cancel(true);
        }
        awaitingDataTimeoutTask = scheduler.schedule(() -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.awaiting-data-timeout");
        }, AWAITING_DATA_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Receives a collection of GrottDevice inverter objects containing potential data for this thing. If the collection
     * contains an entry matching the things's deviceId, and it contains GrottValues, then process it further. Otherwise
     * go offline with a configuration error.
     *
     * @param inverters collection of GrottDevice objects.
     */
    public void updateInverters(Collection<GrottDevice> inverters) {
        inverters.stream().filter(inverter -> deviceId.equals(inverter.getDeviceId()))
                .map(inverter -> inverter.getValues()).filter(values -> values != null).findAny()
                .ifPresentOrElse(values -> {
                    updateStatus(ThingStatus.ONLINE);
                    scheduleAwaitingDataTimeoutTask();
                    updateInverterValues(values);
                }, () -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                });
    }

    /**
     * Receives a GrottValues object containing state values for this thing. Process the respective values and update
     * the channels accordingly.
     *
     * @param inverter a GrottDevice object containing the new status values.
     */
    public void updateInverterValues(GrottValues inverterValues) {
        // get channel states
        Map<String, QuantityType<?>> channelStates;
        try {
            channelStates = inverterValues.getChannelStates();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
            logger.warn("updateInverterValues() unexpected exception:{}, message:{}", e.getClass().getName(),
                    e.getMessage(), e);
            return;
        }

        // find unused channels
        List<Channel> actualChannels = thing.getChannels();
        List<Channel> unusedChannels = actualChannels.stream()
                .filter(channel -> !channelStates.containsKey(channel.getUID().getId())).collect(Collectors.toList());

        // remove unused channels
        if (!unusedChannels.isEmpty()) {
            updateThing(editThing().withoutChannels(unusedChannels).build());
            logger.debug("updateInverterValues() channel count {} reduced by {} to {}", actualChannels.size(),
                    unusedChannels.size(), thing.getChannels().size());
        }

        List<String> thingChannelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());

        // update channel states
        channelStates.forEach((channelId, state) -> {
            if (thingChannelIds.contains(channelId)) {
                updateState(channelId, state);
            } else {
                logger.warn("updateInverterValues() channel '{}' not found; try re-creating the thing", channelId);
            }
        });
    }
}
