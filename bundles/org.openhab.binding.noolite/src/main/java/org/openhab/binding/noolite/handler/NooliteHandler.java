/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.noolite.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.noolite.NooLiteBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NooliteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class NooliteHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NooliteHandler.class);
    @Nullable
    private @Nullable NooliteMTRF64BridgeHandler bridgeMTRF64;

    public NooliteHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(NooLiteBindingConstants.CHANNEL_SWITCH)) {
            bridgeMTRF64.sendMessage(this, null, command);
        }
        if (channelUID.getId().equals(NooLiteBindingConstants.CHANNEL_BINDCHANNEL)) {
            logger.debug("bindchannel {}", command);
            bridgeMTRF64.sendMessage(this, channelUID, command);
        }
    }

    @Override
    public void initialize() {
        if (getBridgeHandler() != null) {
            bridgeMTRF64 = getBridgeHandler();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING);
            registerMegadThingListener(bridgeMTRF64);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private @Nullable NooliteMTRF64BridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.", this.getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "Bridge not defined for device");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized @Nullable NooliteMTRF64BridgeHandler getBridgeHandler(Bridge bridge) {
        NooliteMTRF64BridgeHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof NooliteMTRF64BridgeHandler) {
            bridgeHandler = (NooliteMTRF64BridgeHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
        }
        return bridgeHandler;
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    private void registerMegadThingListener(@Nullable NooliteMTRF64BridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.registerMegadThingListener(this);
        } else {
            logger.debug("Can't register {} at bridge. BridgeHandler is null.", this.getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Bridge is not selected");
        }
    }

    public void updateValues(byte[] data) {
        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID())) {
                if (channel.getUID().getId().equals(NooLiteBindingConstants.CHANNEL_TEMPERATURE)) {
                    int intTemp = ((data[8] & 0x0f) << 8) + (data[7] & 0xff);

                    if (intTemp >= 0x800) {
                        intTemp = intTemp - 0x1000;
                    }

                    double temp = (double) intTemp / 10;
                    try {
                        updateState(channel.getUID(), new DecimalType(temp)));
                    } catch (RuntimeException ex) {
                        logger.debug("Temperature update error");
                    }
                } else if (channel.getUID().getId().equals(NooLiteBindingConstants.CHANNEL_HUMIDITY)) {
                    try {
                        updateState(channel.getUID().getId(), DecimalType.valueOf(Integer.toString(data[9])));
                    } catch (RuntimeException ex) {
                        logger.debug("Humidity update error");
                    }
                } else if (channel.getUID().getId().equals(NooLiteBindingConstants.CHANNEL_BATTERY)) {
                    int state = (data[8] >> 7) & 1;
                    try {
                        if (state == 0) {
                            updateState(channel.getUID().getId(), StringType.valueOf("OK"));
                        } else if (state == 1) {
                            updateState(channel.getUID().getId(), StringType.valueOf("BATT LOW"));
                        }
                    } catch (RuntimeException ex) {
                        logger.debug("State update error");
                    }
                } else if (channel.getUID().getId().equals(NooLiteBindingConstants.CHANNEL_SENSOR_TYPE)) {
                    int result = (data[8] >> 4) & 7;
                    try {
                        if (result == 1) {
                            updateState(channel.getUID().getId(), StringType.valueOf("PT112"));
                        } else if (result == 2) {
                            updateState(channel.getUID().getId(), StringType.valueOf("PT111"));
                        }
                    } catch (RuntimeException ex) {
                        logger.debug("Type update error");
                    }
                }
            }
        }
    }

}
