/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;

/**
 * This implements the discovery service for detecting new DSMR Meters
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class DSMRBridgeDiscoveryService extends AbstractDiscoveryService implements DSMRBridgeDiscoveryListener {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(DSMRBridgeDiscoveryService.class);

    /**
     * Constructs a new DSMRBridgeDiscoveryService
     */
    public DSMRBridgeDiscoveryService() {
        super(Collections.singleton(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE),
                DSMRBindingConstants.DSMR_DISCOVERY_TIMEOUT, false);
    }

    /**
     * Starts a new discovery scan.
     *
     * All available Serial Ports are scanned for P1 telegrams.
     */
    @Override
    protected void startScan() {
        logger.debug("Started Discovery Scan");

        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

        // Traverse each available serial port
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();

            // Check only available SERIAL ports
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL && !portIdentifier.isCurrentlyOwned()) {
                logger.debug("Start discovery for serial port: {}", portIdentifier.getName());
                DSMRBridgeDiscoveryHelper discoveryHelper = new DSMRBridgeDiscoveryHelper(portIdentifier.getName(),
                        this);
                discoveryHelper.startDiscovery();
            }
        }
    }

    /**
     * Callback when a new bridge is discovered.
     * At this moment there is no reason why a bridge is not accepted.
     *
     * Therefore this method will always return true
     *
     * @param serialPort the serialPort name of the new discovered DSMRBridge Thing
     * @return true if bridge is accepted, false otherwise
     */
    @Override
    public boolean bridgeDiscovered(String serialPort) {
        ThingUID thingUID = new ThingUID(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE,
                Integer.toHexString(serialPort.hashCode()));

        // Construct the configuration for this meter
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("serialPort", serialPort);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE).withProperties(properties)
                .withLabel("DSMR bridge on " + serialPort).build();

        logger.debug("{} for serialPort {}", discoveryResult, serialPort);

        thingDiscovered(discoveryResult);

        return true;
    }
}