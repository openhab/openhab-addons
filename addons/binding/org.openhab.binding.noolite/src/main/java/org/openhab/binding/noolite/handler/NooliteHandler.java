/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.noolite.handler;

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
import org.openhab.binding.noolite.NooliteBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NooliteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Petr Shatsillo - Initial contribution
 */
public class NooliteHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NooliteHandler.class);
    NooliteMTRF64BridgeHandler bridgeMTRF64;

    public NooliteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(NooliteBindingConstants.CHANNEL_SWITCH)) {
            bridgeMTRF64.sendMessage(this, null, command);
        }
        if (channelUID.getId().equals(NooliteBindingConstants.CHANNEL_BINDCHANNEL)) {
            logger.debug("bindchannel {}", command);
            bridgeMTRF64.sendMessage(this, channelUID, command);
        }
    }

    @Override
    public void initialize() {
        bridgeMTRF64 = getBridgeHandler();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING);
        registerMegadThingListener(bridgeMTRF64);
        updateStatus(ThingStatus.ONLINE);
    }

    private NooliteMTRF64BridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.", this.getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "Bridge not defined for device");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized NooliteMTRF64BridgeHandler getBridgeHandler(Bridge bridge) {

        NooliteMTRF64BridgeHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof NooliteMTRF64BridgeHandler) {
            bridgeHandler = (NooliteMTRF64BridgeHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            bridgeHandler = null;
        }
        return bridgeHandler;
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    private void registerMegadThingListener(NooliteMTRF64BridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.registerMegadThingListener(this);
        } else {
            logger.debug("Can't register {} at bridge. BridgeHandler is null.", this.getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Bridge is not selected");
        }
    }

    public void updateValues(byte[] data) {

        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID().getId())) {
                if (channel.getUID().getId().equals(NooliteBindingConstants.CHANNEL_TEMPERATURE)) {

                    int intTemp = ((data[8] & 0x0f) << 8) + (data[7] & 0xff);

                    if (intTemp >= 0x800) {
                        intTemp = intTemp - 0x1000;
                    }

                    double temp = (double) intTemp / 10;
                    try {
                        updateState(channel.getUID().getId(), DecimalType.valueOf(Double.toString(temp)));
                    } catch (Exception ex) {
                        logger.debug("Temperature update error");
                    }
                } else if (channel.getUID().getId().equals(NooliteBindingConstants.CHANNEL_HUMIDITY)) {
                    try {
                        updateState(channel.getUID().getId(), DecimalType.valueOf(Integer.toString(data[9])));
                    } catch (Exception ex) {
                        logger.debug("Humidity update error");
                    }
                } else if (channel.getUID().getId().equals(NooliteBindingConstants.CHANNEL_BATTERY)) {

                    int state = (data[8] >> 7) & 1;
                    try {
                        if (state == 0) {

                            updateState(channel.getUID().getId(), StringType.valueOf("OK"));

                        } else if (state == 1) {

                            updateState(channel.getUID().getId(), StringType.valueOf("BATT LOW"));

                        }
                    } catch (Exception ex) {
                        logger.debug("State update error");
                    }

                } else if (channel.getUID().getId().equals(NooliteBindingConstants.CHANNEL_SENSOR_TYPE)) {
                    int result = (data[8] >> 4) & 7;
                    try {
                        if (result == 1) {
                            updateState(channel.getUID().getId(), StringType.valueOf("PT112"));
                        } else if (result == 2) {
                            updateState(channel.getUID().getId(), StringType.valueOf("PT111"));
                        }
                    } catch (Exception ex) {
                        logger.debug("Type update error");
                    }
                }
            }
        }
    }

}
