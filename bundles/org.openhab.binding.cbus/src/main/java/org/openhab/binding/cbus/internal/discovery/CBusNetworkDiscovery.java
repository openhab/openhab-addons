/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.cbus.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusCGateHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Network;

/**
 * The {@link CBusNetworkDiscovery} class is used to discover CBus
 * networks that are in the CBus Project
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusNetworkDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(CBusNetworkDiscovery.class);

    private final CBusCGateHandler cBusCGateHandler;

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
                    Map<String, Object> properties = new HashMap<>(3);
                    properties.put(CBusBindingConstants.CONFIG_NETWORK_ID, network.getNetworkID());
                    properties.put(CBusBindingConstants.PROPERTY_NETWORK_NAME, network.getName());
                    properties.put(CBusBindingConstants.CONFIG_NETWORK_PROJECT, network.getProjectName());
                    ThingUID uid = new ThingUID(CBusBindingConstants.BRIDGE_TYPE_NETWORK,
                            network.getProjectName().toLowerCase().replace(" ", "_") + network.getNetworkID(),
                            cBusCGateHandler.getThing().getUID().getId());
                    DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                            .withLabel(
                                    network.getProjectName() + "/" + network.getNetworkID() + " - " + network.getName())
                            .withBridge(cBusCGateHandler.getThing().getUID()).build();
                    thingDiscovered(result);
                }
            } catch (CGateException e) {
                logger.warn("Failed to discover networks", e);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }
}
