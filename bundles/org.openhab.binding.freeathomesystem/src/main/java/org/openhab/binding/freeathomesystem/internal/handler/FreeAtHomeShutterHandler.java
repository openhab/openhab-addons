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
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeShutterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */

@NonNullByDefault
public class FreeAtHomeShutterHandler extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String devicePosOdp;
    private @Nullable String devicePosIdp;
    private @Nullable String deviceStepIdp;
    private @Nullable String deviceStopIdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeShutterHandler.class);

    public FreeAtHomeShutterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        // Initialize the communication device channel properties
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");
        devicePosOdp = properties.get("devicePosOdp");
        deviceStepIdp = properties.get("deviceStepIdp");
        deviceStopIdp = properties.get("deviceStopIdp");
        devicePosIdp = properties.get("devicePosIdp");

        if ((null == deviceID) && (null == deviceChannel) && (null == devicePosOdp) && (null == deviceStepIdp)
                && (null == deviceStopIdp) && (null == devicePosIdp)) {
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

                logger.debug("Initialize switch - {}", deviceID);

                // Register device and specific channel for event based state updated
                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.registerChannel(deviceID, deviceChannel, devicePosOdp, this,
                        new ChannelUID(this.getThing().getUID(),
                                FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID),
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

        if (command instanceof RefreshType) {

            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID)) {
                String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, devicePosOdp);

                PercentType pos = new PercentType(valueString);
                updateState(FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID, pos);
            }

            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.SHUTTER_TRIGGER_CHANNEL_ID)) {
                updateState(FreeAtHomeSystemBindingConstants.SHUTTER_TRIGGER_CHANNEL_ID, UnDefType.UNDEF);
            }
        }

        if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.SHUTTER_TRIGGER_CHANNEL_ID)) {
            if (command instanceof UpDownType) {
                UpDownType locCommand = (UpDownType) command;

                if (locCommand.equals(UpDownType.UP)) {
                    freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStepIdp, "0");
                    updateState(channelUID, UpDownType.UP);
                }

                if (locCommand.equals(UpDownType.DOWN)) {
                    freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStepIdp, "1");
                    updateState(channelUID, UpDownType.DOWN);
                }
            }

            if (command instanceof StopMoveType) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceStopIdp, "0");
                StringType triggerStoptValue = new StringType("STOP");
                updateState(FreeAtHomeSystemBindingConstants.SHUTTER_TRIGGER_CHANNEL_ID, triggerStoptValue);
            }
        }

        if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.SHUTTER_POS_CHANNEL_ID)) {
            if (command instanceof PercentType) {
                PercentType locCommand = (PercentType) command;

                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, devicePosIdp, locCommand.toString());
                updateState(channelUID, locCommand);
            }
        }

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
