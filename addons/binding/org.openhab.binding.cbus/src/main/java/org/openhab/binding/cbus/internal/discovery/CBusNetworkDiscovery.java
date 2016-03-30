/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusCGateHandler;
import org.openhab.binding.cbus.internal.cgate.CGateException;
import org.openhab.binding.cbus.internal.cgate.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CBusNetworkDiscovery} class is used to discover CBus
 * networks that are in the CBus Project
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusNetworkDiscovery extends AbstractDiscoveryService {

    private final static Logger logger = LoggerFactory.getLogger(CBusNetworkDiscovery.class);

    private CBusCGateHandler cBusCGateHandler;

    public CBusNetworkDiscovery(CBusCGateHandler cBusCGateHandler) {
        super(CBusBindingConstants.NETWORK_DISCOVERY_THING_TYPES_UIDS, 60, false);
        this.cBusCGateHandler = cBusCGateHandler;
    }

    @Override
    protected void startScan() {
        if (cBusCGateHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            try {
                ArrayList<Network> networks = Network.listAll(cBusCGateHandler.getCGateSession(), false);
                for (Network network : networks) {
                    logger.debug("Found Network: {} {}", network.getNetworkID(), network.getName());
                    Map<String, Object> properties = new HashMap<>(2);
                    properties.put(CBusBindingConstants.PROPERTY_ID, network.getNetworkID());
                    properties.put(CBusBindingConstants.PROPERTY_NAME, network.getName());
                    properties.put(CBusBindingConstants.PROPERTY_PROJECT, network.getProjectName());
                    ThingUID uid = new ThingUID(CBusBindingConstants.BRIDGE_TYPE_NETWORK,
                            network.getProjectName().toLowerCase().replace(" ", "_")
                                    + Integer.toString(network.getNetworkID()),
                            cBusCGateHandler.getThing().getUID().getId());
                    if (uid != null) {
                        DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                                .withProperties(properties).withLabel(network.getProjectName() + "/"
                                        + network.getNetworkID() + " - " + network.getName())
                                .withBridge(cBusCGateHandler.getThing().getUID()).build();
                        thingDiscovered(result);
                    }
                }
            } catch (CGateException e) {
                logger.error("Failed to discover networks", e);
            }

        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        // TODO Auto-generated method stub
        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        // TODO Auto-generated method stub
        super.stopBackgroundDiscovery();
    }

    public void activate() {
        // cgateHandler.registerDiscoveryService(this);
    }

    @Override
    public void deactivate() {
        // cgateHandler.unregisterDiscoveryService();
    }
}
