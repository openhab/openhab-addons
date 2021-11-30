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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
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
 * The {@link FreeAtHomeDimmingHandler} is responsible for handling switch and dimming commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDimmingHandler extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String deviceDimOdp;
    private @Nullable String deviceDimIdp;
    private @Nullable String deviceSwitchOdp;
    private @Nullable String deviceSwitchIdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDimmingHandler.class);

    public FreeAtHomeDimmingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        // Initialize the communication device channel properties
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");
        deviceDimOdp = properties.get("deviceDimOdp");
        deviceDimIdp = properties.get("deviceDimIdp");
        deviceSwitchOdp = properties.get("deviceSwitchOdp");
        deviceSwitchIdp = properties.get("deviceSwitchIdp");

        if ((null == deviceID) && (null == deviceChannel) && (null == deviceDimOdp) && (null == deviceDimIdp)
                && (null == deviceSwitchOdp) && (null == deviceSwitchIdp)) {
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

                // Get initial state of the switch directly from the free@home switch
                String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, deviceDimOdp);
                DecimalType dec = new DecimalType(valueString);
                updateState(FreeAtHomeSystemBindingConstants.DIMMING_VALUE_CHANNEL_ID, dec);

                // Register device and specific channel for event based state updated
                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.registerChannel(deviceID, deviceChannel, deviceDimOdp, this,
                        new ChannelUID(this.getThing().getUID(),
                                FreeAtHomeSystemBindingConstants.DIMMING_SWITCH_CHANNEL_ID),
                        new BooleanValueStateConverter());

                updateHandler.registerChannel(deviceID, deviceChannel, deviceSwitchOdp, this,
                        new ChannelUID(this.getThing().getUID(),
                                FreeAtHomeSystemBindingConstants.DIMMING_VALUE_CHANNEL_ID),
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

                updateHandler.unregisterChannel(deviceID, deviceChannel, deviceSwitchOdp);
                updateHandler.unregisterChannel(deviceID, deviceChannel, deviceDimOdp);
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
            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.DIMMING_VALUE_CHANNEL_ID)) {
                String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, deviceDimOdp);
                DecimalType dec = new DecimalType(valueString);
                updateState(channelUID, dec);
            }

            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.DIMMING_SWITCH_CHANNEL_ID)) {
                int value;

                String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, deviceSwitchOdp);

                try {
                    value = Integer.parseInt(valueString);
                } catch (NumberFormatException e) {
                    value = 0;
                }

                // set the initial state
                if (1 == value) {
                    updateState(FreeAtHomeSystemBindingConstants.DIMMING_SWITCH_CHANNEL_ID, OnOffType.ON);
                } else {
                    updateState(FreeAtHomeSystemBindingConstants.DIMMING_SWITCH_CHANNEL_ID, OnOffType.OFF);
                }
            }
        }

        if (command instanceof OnOffType) {
            OnOffType locCommand = (OnOffType) command;

            if (locCommand.equals(OnOffType.ON)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceSwitchIdp, "1");
                updateState(channelUID, OnOffType.ON);
            }

            if (locCommand.equals(OnOffType.OFF)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceSwitchIdp, "0");
                updateState(channelUID, OnOffType.OFF);
            }
        }

        if (command instanceof PercentType) {
            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.DIMMING_VALUE_CHANNEL_ID)) {

                String valueString = ((PercentType) command).toString();

                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceDimIdp, valueString);

                updateState(channelUID, ((DecimalType) command));
            }
        }

        if (command instanceof DecimalType) {
            if (channelUID.getId().equalsIgnoreCase(FreeAtHomeSystemBindingConstants.DIMMING_VALUE_CHANNEL_ID)) {

                String valueString = ((DecimalType) command).toString();

                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceDimIdp, valueString);

                updateState(channelUID, ((DecimalType) command));
            }
        }

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
