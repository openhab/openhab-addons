/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vitotronic.internal.handler;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Andres - Initial contribution
 */

public class VitotronicThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VitotronicThingHandler.class);

    private VitotronicBridgeHandler bridgeHandler;

    public VitotronicThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        bridgeHandler = getBridgeHandler();
        logger.debug("Thing Handler for {} started", getThing().getUID().getId());
        registerVitotronicThingListener(bridgeHandler);
    }

    @Override
    public void dispose() {
        logger.debug("Thing Handler for {} stop", getThing().getUID().getId());
        unregisterVitotronicThingListener(bridgeHandler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handle command for channel '{}' command '{}'", channelUID.getId(), command);
        bridgeHandler.updateChannel(getThing().getUID().getId(), channelUID.getId(), command.toString());
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    private synchronized VitotronicBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.", getThing().getUID());
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized VitotronicBridgeHandler getBridgeHandler(Bridge bridge) {
        VitotronicBridgeHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof VitotronicBridgeHandler vitotronicBridgeHandler) {
            bridgeHandler = vitotronicBridgeHandler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            bridgeHandler = null;
        }
        return bridgeHandler;
    }

    private void registerVitotronicThingListener(VitotronicBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.registerVitotronicThingListener(this);
        } else {
            logger.debug("Can't register {} at bridge bridgeHandler is null.", this.getThing().getUID());
        }
    }

    private void unregisterVitotronicThingListener(VitotronicBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterThingListener(this);
        } else {
            logger.debug("Can't unregister {} at bridge bridgeHandler is null.", this.getThing().getUID());
        }
    }

    public void setChannelValue(String channelId, String value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.trace("Cannel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
            return;
        }

        logger.trace("Set {}:{}:{} = {}", getThing().getUID().getId(), channelId, channel.getAcceptedItemType(), value);
        switch (channel.getAcceptedItemType()) {
            case "Number":
                this.updateState(channelId, new DecimalType(value));
                break;
            case "String":
                this.updateState(channelId, new StringType(value));
                break;
            case "Switch":
                if (value.toUpperCase().contains("ON")) {
                    this.updateState(channelId, OnOffType.ON);
                } else {
                    this.updateState(channelId, OnOffType.OFF);
                }
                break;
            case "DateTime":
                this.updateState(channelId, new DateTimeType(value));
                break;
            default:
                logger.trace("Type '{}' for channel '{}' not implemented", channel.getAcceptedItemType(), channelId);
        }
    }

    public String getActiveChannelListAsString() {
        String channelList = "";
        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID().getId())) {
                if (channelList.length() > 0) {
                    channelList = channelList + "," + channel.getUID().getId();
                } else {
                    channelList = channel.getUID().getId();
                }
            }
        }
        return channelList;
    }
}
