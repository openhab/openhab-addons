/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;
import static org.openhab.binding.ring.internal.ApiConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.api.RingDeviceTO;
import org.openhab.binding.ring.internal.device.OtherDevice;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.Gson;

/**
 * The handler for a Ring Other Device.
 *
 * @author Ben Rosenblum - Initial Contribution
 *
 */

@NonNullByDefault
public class OtherDeviceHandler extends RingDeviceHandler {
    private int lastBattery = -1;
    private boolean batterySupport = false;
    private boolean openDoorSupport = false;

    private final Gson gson = new Gson();

    public OtherDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Other Device handler");
        super.initialize(OtherDevice.class);
        String kind = thing.getProperties().get(THING_PROPERTY_KIND);
        if (kind != null && !kind.isEmpty()) {
            if (BATTERY_KINDS.contains(kind)) {
                batterySupport = true;
            }
            if (INTERCOM_KINDS.contains(kind)) {
                openDoorSupport = true;
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), CHANNEL_CONTROL_OPENDOOR);
                Channel channel = thing.getChannel(channelUID);
                if (channel == null) {
                    logger.debug("Adding channel for opendoor, on device {}", getThing().getUID());
                    ThingBuilder thingBuilder = editThing();
                    channel = ChannelBuilder.create(channelUID, CoreItemFactory.SWITCH).withLabel("Open Door")
                            .withType(new ChannelTypeUID(BINDING_ID, "opendoor")).build();
                    thingBuilder.withChannel(channel);
                    updateThing(thingBuilder.build());
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            return;
        }
        if (openDoorSupport) {
            if (channelUID.getId().equals(CHANNEL_CONTROL_OPENDOOR)) {
                if (command instanceof OnOffType onOffCommand) {
                    if (onOffCommand == OnOffType.ON) {
                        openDoorCommand();
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
            }
        }
    }

    @Override
    protected void refreshState() {
        // Do Nothing
    }

    @Override
    protected void minuteTick() {
        logger.debug("OtherDeviceHandler - minuteTick - device {}", getThing().getUID().getId());
        if (device == null) {
            initialize();
        }

        if (batterySupport) {
            RingDeviceTO deviceTO = device.getDeviceStatus();
            if (deviceTO.health.batteryPercentage != lastBattery) {
                logger.debug("Battery Level: {}", deviceTO.health.batteryPercentage);
                ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_STATUS_BATTERY);
                updateState(channelUID, new DecimalType(deviceTO.health.batteryPercentage));
                lastBattery = deviceTO.health.batteryPercentage;
            } else {
                logger.debug("Battery Level Unchanged for {} - {} vs {}", getThing().getUID().getId(),
                        deviceTO.health.batteryPercentage, lastBattery);

            }
        }
    }

    protected void openDoorCommand() {
        String command = "/device_rpc";
        Map<String, Object> params = new HashMap<>();
        params.put("door_id", 0);
        params.put("user_id", 0);

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "unlock_door");
        request.put("params", params);

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("request", request);
        payloadMap.put("command_name", "device_rpc");

        String payload = gson.toJson(payloadMap);
        logger.debug("payload = {}", payload);
        sendCommand(URL_INTERCOM_COMMAND, command, payload);
    }
}
