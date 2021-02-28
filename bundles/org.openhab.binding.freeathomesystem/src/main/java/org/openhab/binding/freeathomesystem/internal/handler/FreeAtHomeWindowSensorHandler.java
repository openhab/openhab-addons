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
import org.openhab.binding.freeathomesystem.internal.valuestateconverters.DecimalValueStateConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeActuatorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * deviceStateOdp, window door open 1 close 0
 * devicePosOdp, Delivers position for Window/Door (Open / Tilted / Closed)
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeWindowSensorHandler extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String devicePosOdp;
    private @Nullable String deviceStateOdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeWindowSensorHandler.class);

    public FreeAtHomeWindowSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        // Initialize the communication device channel properties
        deviceStateOdp = properties.get("deviceStateOdp");
        devicePosOdp = properties.get("devicePosOdp");
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");

        if ((null == deviceStateOdp) && (null == devicePosOdp) && (null == deviceID) && (null == deviceChannel)) {
            // Device properties are found
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device properties are found");
            return;
        }

        Bridge bridge = this.getBridge();

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {

                FreeAtHomeBridgeHandler freeAtHomeBridge;
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;

                logger.debug("Initialize window sensor - {}", deviceID);

                // Register device and specific channel for event based state updated
                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.registerChannel(deviceID, deviceChannel, deviceStateOdp, this,
                        new ChannelUID(this.getThing().getUID(),
                                FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_STATE_ID),
                        new BooleanValueStateConverter());

                updateHandler.registerChannel(deviceID, deviceChannel, devicePosOdp, this,
                        new ChannelUID(this.getThing().getUID(),
                                FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_POS_ID),
                        new DecimalValueStateConverter());

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

                updateHandler.unregisterChannel(deviceID, deviceChannel, deviceStateOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, devicePosOdp);
            }
        }

        logger.debug("Device removed {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;
            }
        }

        if (null != freeAtHomeBridge) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        // Init here but other update only via Websocket
        if (command instanceof RefreshType) {
            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_STATE_ID)) {
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, deviceStateOdp);
                DecimalType dec = new DecimalType(value);

                updateState(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_STATE_ID, dec);
            }

            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_POS_ID)) {
                String value = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, devicePosOdp);
                DecimalType dec = new DecimalType(value);

                updateState(FreeAtHomeSystemBindingConstants.WINDOWSENSOR_CHANNEL_POS_ID, dec);
            }
        }

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
