/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.internal.cgate.CGateException;
import org.openhab.binding.cbus.internal.cgate.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CBusGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
public abstract class CBusGroupHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected CBusNetworkHandler cBusNetworkHandler = null;
    // protected CBusCGateHandler cBusCGateHandler = null;
    // protected CGateCommandSet commandSet = null;
    protected Group group = null;

    public CBusGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public void initialize() {
        cBusNetworkHandler = getCBusNetworkHandler();
        if (!cBusNetworkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        try {
            this.group = getGroup(Integer.parseInt(getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()));
        } catch (Exception e) {
            logger.error("Cannot create group {} ", getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString(),
                    e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }
        updateStatus();
    }

    public void updateStatus() {
        try {
            if (cBusNetworkHandler == null || !cBusNetworkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else if (group == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else if (group.getNetwork().isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Network is not reporting online");
            }
        } catch (CGateException e) {
            logger.error("Problem checking network state for network {}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    protected abstract Group getGroup(int groupID) throws CGateException;

    private synchronized CBusNetworkHandler getCBusNetworkHandler() {
        CBusNetworkHandler bridgeHandler = null;
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.");
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof CBusNetworkHandler) {
            bridgeHandler = (CBusNetworkHandler) handler;
        } else {
            logger.debug("No available bridge handler found for bridge: {} .", bridge.getUID());
            bridgeHandler = null;
        }
        return bridgeHandler;
    }
}
