/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.dscalarm.DSCAlarmBindingConstants;
import org.openhab.binding.dscalarm.config.EnvisalinkBridgeConfiguration;
import org.openhab.binding.dscalarm.config.IT100BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the EyezOn Envisalink 3/2DS Ethernet interface.
 *
 * @author Russell Stephens - Initial Contribution
 *
 */
public class DSCAlarmBridgeDiscovery extends AbstractDiscoveryService {
    private final static Logger logger = LoggerFactory.getLogger(DSCAlarmBridgeDiscovery.class);

    private long refreshInterval = 600;
    private ScheduledFuture<?> envisalinkBridgeDiscoveryJob;
    private ScheduledFuture<?> it100BridgeDiscoveryJob;
    private EnvisalinkBridgeDiscovery envisalinkBridgeDiscovery = new EnvisalinkBridgeDiscovery(this);
    private IT100BridgeDiscovery it100BridgeDiscovery = new IT100BridgeDiscovery(this);

    /**
     * Constructor.
     */
    public DSCAlarmBridgeDiscovery() {
        super(DSCAlarmBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return DSCAlarmBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.trace("Start DSC Alarm Bridge discovery.");
        scheduler.execute(envisalinkBridgeDiscoveryRunnable);
        scheduler.execute(it100BridgeDiscoveryRunnable);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start DSC Alarm Bridge background discovery");
        if (envisalinkBridgeDiscoveryJob == null || envisalinkBridgeDiscoveryJob.isCancelled()) {
            envisalinkBridgeDiscoveryJob = scheduler.scheduleAtFixedRate(envisalinkBridgeDiscoveryRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
        if (it100BridgeDiscoveryJob == null || it100BridgeDiscoveryJob.isCancelled()) {
            it100BridgeDiscoveryJob = scheduler.scheduleAtFixedRate(it100BridgeDiscoveryRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop DSC Alarm Bridge background discovery");
        if (envisalinkBridgeDiscoveryJob != null && !envisalinkBridgeDiscoveryJob.isCancelled()) {
            envisalinkBridgeDiscoveryJob.cancel(true);
            envisalinkBridgeDiscoveryJob = null;
        }

        if (it100BridgeDiscoveryJob != null && !it100BridgeDiscoveryJob.isCancelled()) {
            it100BridgeDiscoveryJob.cancel(true);
            it100BridgeDiscoveryJob = null;
        }
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

            if (thingUID != null) {

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties) // .withProperty(EnvisalinkBridgeConfiguration.IP_ADDRESS,
                                                                                                            // ipAddress)
                        .withLabel("EyezOn Envisalink Bridge - " + ipAddress).build();
                thingDiscovered(result);

                logger.trace("addBridge(): '{}' was added to Smarthome inbox.", result.getThingUID());
            }
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

            if (thingUID != null) {

                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel("DSC IT-100 Bridge - " + port).build();
                thingDiscovered(result);

                logger.trace("addBridge(): '{}' was added to Smarthome inbox.", result.getThingUID());
            }
        } catch (Exception e) {
            logger.error("addBridge(): Error: {}", e);
        }
    }
}
