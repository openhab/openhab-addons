/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.handler;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.internal.cgate.CGateException;
import org.openhab.binding.cbus.internal.cgate.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CBusNetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusNetworkHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CBusCGateHandler bridgeHandler;
    private Network network;

    public CBusNetworkHandler(Bridge thing) {
        super(thing);
        if (thing == null) {
            logger.error("Required bridge not defined for device.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        getCBusCGateHandler();
        if (!bridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        String networkID = getConfig().get(CBusBindingConstants.PROPERTY_ID).toString();
        String project = getConfig().get(CBusBindingConstants.PROPERTY_PROJECT).toString();
        try {
            network = (Network) getCBusCGateHandler().getCGateSession()
                    .getCGateObject("//" + project + "/" + networkID);
        } catch (CGateException e) {
            logger.error("Cannot load C-Bus network {}", networkID, e);
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        updateStatus();
        // now also re-initialize all group handlers
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                handler.initialize();
            }
        }
        scheduler.scheduleAtFixedRate(networkSyncRunnable, (int) (60 * Math.random()),
                Integer.parseInt(getConfig().get(CBusBindingConstants.PROPERTY_NETWORK_SYNC).toString()),
                TimeUnit.SECONDS);
    }

    public void updateStatus() {
        ThingStatus lastStatus = getThing().getStatus();
        try {
            if (!getCBusCGateHandler().getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "CGate connection offline");
            } else if (network == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else if (network.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Network is not reporting online");
            }
        } catch (CGateException e) {
            logger.error("Problem checking network state for network {}",
                    network != null ? network.getNetworkID() : "<unknown>", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        if (!getThing().getStatus().equals(lastStatus)) {
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof CBusGroupHandler) {
                    ((CBusGroupHandler) handler).updateStatus();
                }
            }
        }
    }

    public synchronized CBusCGateHandler getCBusCGateHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.error("Required bridge not defined for device.");
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof CBusCGateHandler) {
                this.bridgeHandler = (CBusCGateHandler) handler;
            } else {
                logger.debug("No available bridge handler found for bridge: {} .", bridge.getUID());
                this.bridgeHandler = null;
            }
        }
        return bridgeHandler;
    }

    public Network getNetwork() {
        return network;
    }

    private Runnable networkSyncRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    logger.info("Starting network sync on network {}", network.getNetworkID());
                    getNetwork().startSync();
                }
            } catch (CGateException e) {
                logger.error("Cannot start network sync on network {} ", network.getNetworkID());
            }
        }
    };
}
