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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private final Logger logger = LoggerFactory.getLogger(GrowattInverterHandler.class);

    private String deviceId = "unknown";

    public GrowattInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // everything is read only so do nothing
    }

    /**
     * Receives a GrottDevice object containing data for this thing. Process the respective data values and update the
     * channels accordingly.
     *
     * @param grottDevice a GrottDevice object containing the new status values.
     */
    public void handleGrottDevice(GrottDevice grottDevice) {
        GrottValues grottValues = grottDevice.getValues();
        if (grottValues == null) {
            logger.warn("handleGrottDevice() device '{}' contains no values", grottDevice.getDeviceId());
            return;
        }

        // get channel states
        Map<String, QuantityType<?>> channelStates;
        try {
            channelStates = grottValues.getChannelStates();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
            // should never happen since previously tested in JUnit tests
            logger.warn("handleGrottDevice() unexpected exception:{}, message:{}", e.getClass().getName(),
                    e.getMessage(), e);
            return;
        }

        logger.debug("handleGrottDevice() channelStates size:{}", channelStates.size());

        // find unused channels
        List<Channel> actualChannels = thing.getChannels();
        List<Channel> unusedChannels = actualChannels.stream()
                .filter(channel -> !channelStates.containsKey(channel.getUID().getId())).collect(Collectors.toList());

        // remove unused channels
        if (!unusedChannels.isEmpty()) {
            updateThing(editThing().withoutChannels(unusedChannels).build());
            logger.debug("handleGrottDevice() channel count {} reduced by {} to {}", actualChannels.size(),
                    unusedChannels.size(), thing.getChannels().size());
        }

        List<String> thingChannelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());

        // update channel states
        channelStates.forEach((channelId, state) -> {
            if (thingChannelIds.contains(channelId)) {
                updateState(channelId, state);
            } else {
                logger.debug("handleGrottDevice() channel '{}' not found in thing", channelId);
            }
        });
    }

    /**
     * Receives a list of GrottDevice objects containing potential data for this thing. If the list contains any entry
     * matching the things's deviceId then process it further. Otherwise go offline with a configuration error.
     *
     * @param grottDevices list of GrottDevice objects.
     */
    public void handleGrottDevices(List<GrottDevice> grottDevices) {
        grottDevices.stream().filter(grottDevice -> deviceId.equals(grottDevice.getDeviceId())).findAny()
                .ifPresentOrElse(grottDevice -> {
                    updateStatus(ThingStatus.ONLINE);
                    handleGrottDevice(grottDevice);
                }, () -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                });
    }

    @Override
    public void initialize() {
        GrowattInverterConfiguration config = getConfigAs(GrowattInverterConfiguration.class);
        deviceId = config.deviceId;
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("initialize() thing has {} channels", thing.getChannels().size());
    }
}
