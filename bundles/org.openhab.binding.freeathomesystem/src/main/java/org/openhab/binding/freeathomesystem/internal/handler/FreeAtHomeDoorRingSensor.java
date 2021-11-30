/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants;
import org.openhab.binding.freeathomesystem.internal.valuestateconverters.BooleanValueStateConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeDoorRingSensor} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

@NonNullByDefault
public class FreeAtHomeDoorRingSensor extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String deviceOdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDoorRingSensor.class);

    public FreeAtHomeDoorRingSensor(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        deviceID = properties.get("deviceId");

        deviceChannel = properties.get("channelId");

        // Initialize the communication device channel properties
        deviceOdp = properties.get("deviceStateOdp");
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");

        if ((null == deviceOdp) && (null == deviceID) && (null == deviceChannel)) {
            // Device properties are found
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device properties are not found");
            return;
        }

        Bridge bridge = this.getBridge();

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {

                FreeAtHomeBridgeHandler freeAtHomeBridge;
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                logger.debug("Initialize doorring sensor - {}", deviceID);

                DecimalType decPos = new DecimalType(0);

                // set the initial state
                updateState(FreeAtHomeSystemBindingConstants.DOORDINGSENSOR_CHANNEL_STATE_ID, decPos);

                // Register device and specific channel for event based state updated
                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.registerChannel(deviceID, deviceChannel, deviceOdp, this,
                        new ChannelUID(this.getThing().getUID(),
                                FreeAtHomeSystemBindingConstants.DOORDINGSENSOR_CHANNEL_STATE_ID),
                        new BooleanValueStateConverter());

                logger.debug("Device - online: {}", deviceID);

                updateStatus(ThingStatus.ONLINE);

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);

                logger.debug("Incorrect bridge class: {}", deviceID);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge available");

            logger.debug("No bridge for device: {}", deviceID);
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = this.getBridge();

        // Unregister device and specific channel for event based state updated
        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                FreeAtHomeBridgeHandler freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.unregisterChannel(deviceID, deviceChannel, deviceOdp);
            }
        }

        logger.debug("Device removed {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        updateStatus(ThingStatus.ONLINE);

        // update only via Websocket
        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
