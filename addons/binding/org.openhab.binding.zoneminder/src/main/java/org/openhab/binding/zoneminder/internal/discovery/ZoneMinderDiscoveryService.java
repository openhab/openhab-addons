/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.handler.ZoneMinderServerBridgeHandler;
import org.openhab.binding.zoneminder.handler.ZoneMinderThingMonitorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.eskildsen.zoneminder.IZoneMinderMonitorData;

/**
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ZoneMinderDiscoveryService.class);

    private ZoneMinderServerBridgeHandler serverHandler;
    private DiscoveryServiceCallback discoveryServiceCallback;

    public ZoneMinderDiscoveryService(ZoneMinderServerBridgeHandler coordinatorHandler, int searchTime) {
        super(searchTime);
        this.serverHandler = coordinatorHandler;
    }

    public void activate() {
        logger.debug("[DISCOVERY]: Activating ZoneMinder discovery service for {}", serverHandler.getThing().getUID());
    }

    @Override
    public void deactivate() {
        logger.debug("[DISCOVERY]: Deactivating ZoneMinder discovery service for {}",
                serverHandler.getThing().getUID());
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ZoneMinderThingMonitorHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.debug("[DISCOVERY]: Performing background discovery scan for {}", serverHandler.getThing().getUID());
        discoverMonitors();
    }

    @Override
    public void startScan() {
        logger.debug("[DISCOVERY]: Starting discovery scan for {}", serverHandler.getThing().getUID());
        discoverMonitors();
    }

    @Override
    public synchronized void abortScan() {
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    protected String BuildMonitorLabel(String id, String name) {
        return String.format("%s [%s]", ZoneMinderConstants.ZONEMINDER_MONITOR_NAME, name);
    }

    protected synchronized void discoverMonitors() {
        // Add all existing devices
        for (IZoneMinderMonitorData monitor : serverHandler.getMonitors()) {
            deviceAdded(monitor);
        }
    }

    private boolean monitorThingExists(ThingUID newThingUID) {
        return serverHandler.getThingByUID(newThingUID) != null ? true : false;
    }

    /**
     * This is called once the node is fully discovered. At this point we know most of the information about
     * the device including manufacturer information.
     *
     * @param node the node to be added
     */

    public void deviceAdded(IZoneMinderMonitorData monitor) {
        try {
            ThingUID bridgeUID = serverHandler.getThing().getUID();
            String monitorUID = String.format("%s-%s", ZoneMinderConstants.THING_ZONEMINDER_MONITOR, monitor.getId());
            ThingUID thingUID = new ThingUID(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR, bridgeUID,
                    monitorUID);

            // Does Monitor exist?
            if (!monitorThingExists(thingUID)) {
                logger.info("[DISCOVERY]: Monitor with Id='{}' and Name='{}' added", monitor.getId(),
                        monitor.getName());
                Map<String, Object> properties = new HashMap<>(0);
                properties.put(ZoneMinderConstants.PARAMETER_MONITOR_ID, Integer.valueOf(monitor.getId()));
                properties.put(ZoneMinderConstants.PARAMETER_MONITOR_TRIGGER_TIMEOUT,
                        ZoneMinderConstants.PARAMETER_MONITOR_TRIGGER_TIMEOUT_DEFAULTVALUE);
                properties.put(ZoneMinderConstants.PARAMETER_MONITOR_EVENTTEXT,
                        ZoneMinderConstants.MONITOR_EVENT_OPENHAB);

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(BuildMonitorLabel(monitor.getId(), monitor.getName())).build();

                thingDiscovered(discoveryResult);
            }
        } catch (Exception ex) {
            logger.error("[DISCOVERY]: Error occurred when calling 'monitorAdded' from Discovery. Exception={}",
                    ex.getMessage());
        }

    }
}
