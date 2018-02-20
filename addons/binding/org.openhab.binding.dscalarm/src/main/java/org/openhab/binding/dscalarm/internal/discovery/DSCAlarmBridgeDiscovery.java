/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.dscalarm.DSCAlarmBindingConstants;
import org.openhab.binding.dscalarm.internal.config.EnvisalinkBridgeConfiguration;
import org.openhab.binding.dscalarm.internal.config.IT100BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the EyezOn Envisalink 3/2DS Ethernet interface.
 *
 * @author Russell Stephens - Initial Contribution
 *
 */
public class DSCAlarmBridgeDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(DSCAlarmBridgeDiscovery.class);

    private EnvisalinkBridgeDiscovery envisalinkBridgeDiscovery = new EnvisalinkBridgeDiscovery(this);
    private IT100BridgeDiscovery it100BridgeDiscovery = new IT100BridgeDiscovery(this);

    /**
     * Constructor.
     */
    public DSCAlarmBridgeDiscovery() {
        super(DSCAlarmBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15, true);
    }

    @Override
    protected void startScan() {
        logger.trace("Start DSC Alarm Bridge discovery.");
        scheduler.execute(envisalinkBridgeDiscoveryRunnable);
        scheduler.execute(it100BridgeDiscoveryRunnable);
    }

    private Runnable envisalinkBridgeDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            envisalinkBridgeDiscovery.discoverBridge();
        }
    };

    private Runnable it100BridgeDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            it100BridgeDiscovery.discoverBridge();
        }
    };

    /**
     * Method to add an Envisalink Bridge to the Smarthome Inbox.
     *
     * @param ipAddress
     */
    public void addEnvisalinkBridge(String ipAddress) {
        logger.trace("addBridge(): Adding new Envisalink Bridge on {} to Smarthome inbox", ipAddress);

        String bridgeID = ipAddress.replace('.', '_');
        Map<String, Object> properties = new HashMap<>(0);
        properties.put(EnvisalinkBridgeConfiguration.IP_ADDRESS, ipAddress);

        try {
            ThingUID thingUID = new ThingUID(DSCAlarmBindingConstants.ENVISALINKBRIDGE_THING_TYPE, bridgeID);

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("EyezOn Envisalink Bridge - " + ipAddress).build());

            logger.trace("addBridge(): '{}' was added to Smarthome inbox.", thingUID);
        } catch (Exception e) {
            logger.error("addBridge(): Error: {}", e);
        }
    }

    /**
     * Method to add an IT-100 Bridge to the Smarthome Inbox.
     *
     * @param port
     */
    public void addIT100Bridge(String port) {
        logger.trace("addBridge(): Adding new IT-100 Bridge on {} to Smarthome inbox", port);

        String bridgeID = "";
        boolean containsChar = port.contains("/");

        if (containsChar) {
            String[] parts = port.split("/");
            String id = parts[parts.length - 1].toUpperCase();
            bridgeID = id.replaceAll("\\W", "_");

        } else {
            String id = port.toUpperCase();
            bridgeID = id.replaceAll("\\W", "_");
        }

        Map<String, Object> properties = new HashMap<>(0);
        properties.put(IT100BridgeConfiguration.SERIAL_PORT, port);

        try {
            ThingUID thingUID = new ThingUID(DSCAlarmBindingConstants.IT100BRIDGE_THING_TYPE, bridgeID);

            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withLabel("DSC IT-100 Bridge - " + port).build());

            logger.trace("addBridge(): '{}' was added to Smarthome inbox.", thingUID);
        } catch (Exception e) {
            logger.error("addBridge(): Error: {}", e);
        }
    }
}
