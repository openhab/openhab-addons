/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;
import org.openhab.binding.isy.internal.InsteonAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IsyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Craig Hamilton - Initial contribution
 */
public class IsyHandler extends BaseThingHandler {

    protected Logger logger = LoggerFactory.getLogger(IsyHandler.class);

    protected Map<Integer, String> mDeviceidToChannelMap = new HashMap<Integer, String>();

    // most devices only have one device id, which is 1. so if map is empty, we'll return 1
    protected int getDeviceIdForChannel(String channel) {
        if (mDeviceidToChannelMap.size() == 0) {
            return 1;
        }
        for (int id : mDeviceidToChannelMap.keySet()) {
            if (mDeviceidToChannelMap.get(id).equals(channel)) {
                return id;
            }
        }
        throw new IllegalArgumentException("Could not find device id for channel: '" + channel + "'");
    }

    public void handleUpdate(Object... parameters) {
        InsteonAddress insteonAddress = new InsteonAddress((String) parameters[2]);
        int deviceId = insteonAddress.getDeviceId();
        if ("ST".equals(parameters[0])) {
            updateState(mDeviceidToChannelMap.get(deviceId),
                    IsyHandler.statusValuetoState(Integer.parseInt((String) parameters[1])));
        }
    }

    protected IsyBridgeHandler getBridgeHandler() {
        return (IsyBridgeHandler) getBridge().getHandler();
    }

    protected IsyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IsyBridgeHandler bridgeHandler = getBridgeHandler();
        IsyInsteonDeviceConfiguration test = getThing().getConfiguration().as(IsyInsteonDeviceConfiguration.class);
        // isy needs device id appended to address
        String isyAddress = new InsteonAddress(test.address, getDeviceIdForChannel(channelUID.getId())).toString();
        logger.debug("insteon address for command is: " + isyAddress);
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                boolean result = bridgeHandler.getInsteonClient().changeNodeState("DON", "0", isyAddress);
                logger.debug("result: " + result);
            } else if (command.equals(OnOffType.OFF)) {
                bridgeHandler.getInsteonClient().changeNodeState("DOF", "0", isyAddress);
            } else if (command.equals(RefreshType.REFRESH)) {
                logger.debug("should retrieve state");
            }
        } else if (command instanceof PercentType) {
            int commandValue = ((PercentType) command).intValue() * 255 / 100;
            if (commandValue == 0) {
                bridgeHandler.getInsteonClient().changeNodeState("DOF", Integer.toString(0), isyAddress);
            } else {
                bridgeHandler.getInsteonClient().changeNodeState("DON", Integer.toString(commandValue), isyAddress);
            }
        } else {
            logger.error("unhandled Command");
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public static State statusValuetoState(int updateValue) {
        State returnValue;
        if (updateValue > 0) {
            returnValue = new PercentType(updateValue * 100 / 255);
        } else {
            returnValue = OnOffType.OFF;
        }
        return returnValue;

    }

    protected void addChannelToDevice(String channel, int deviceId) {
        this.mDeviceidToChannelMap.put(deviceId, channel);
    }

}
