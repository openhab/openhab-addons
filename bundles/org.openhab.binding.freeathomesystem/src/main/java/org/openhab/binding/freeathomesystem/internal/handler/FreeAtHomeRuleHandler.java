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
import org.openhab.core.library.types.OnOffType;
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
 * The {@link FreeAtHomeRuleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeRuleHandler extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String deviceIdp;
    private @Nullable String deviceOdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeRuleHandler.class);

    public FreeAtHomeRuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        deviceID = properties.get("deviceId");

        deviceChannel = properties.get("channelId");

        // Initialize the communication device channel properties
        deviceIdp = properties.get("deviceIdp");
        deviceOdp = properties.get("deviceOdp");
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");

        if ((null == deviceOdp) && (null == deviceOdp) && (null == deviceID) && (null == deviceChannel)) {
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

                logger.debug("Initialize rule handler - {}", deviceID);

                // Register device and specific channel for event based state updated
                ChannelUpdateHandler updateHandler = freeAtHomeBridge.channelUpdateHandler;

                updateHandler.registerChannel(deviceID, deviceChannel, deviceOdp, this,
                        new ChannelUID(this.getThing().getUID(), FreeAtHomeSystemBindingConstants.SWITCH_CHANNEL_ID),
                        new BooleanValueStateConverter());

                logger.debug("Device - online: {}", deviceID);

                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No online updates are possible");
            }
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
            String valueString = freeAtHomeBridge.getDatapoint(deviceID, deviceChannel, deviceIdp);

            int value;

            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                value = 0;
            }

            if (1 == value) {
                updateState(channelUID, OnOffType.ON);
            } else {
                updateState(channelUID, OnOffType.OFF);
            }
        }

        if (command instanceof OnOffType) {
            OnOffType locCommand = (OnOffType) command;

            if (locCommand.equals(OnOffType.ON)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceIdp, "1");
                updateState(channelUID, OnOffType.ON);
            }

            if (locCommand.equals(OnOffType.OFF)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceIdp, "0");
                updateState(channelUID, OnOffType.OFF);
            }
        }

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
