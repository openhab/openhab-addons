/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.handler.ZoneMinderServerBridgeHandler;
import org.openhab.binding.zoneminder.handler.ZoneMinderThingMonitorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.eskildsen.zoneminder.IZoneMinderMonitorData;
import name.eskildsen.zoneminder.ZoneMinderFactory;

/**
 * When a {@link ZoneMinderMonitorDiscoveryService} finds a new Monitor we will
 * add it to the system.
 *
 * @author Martin S. Eskildsen - Highly inspired by Dan Cunningham's SqueezeBox binding
 *
 */
public class ZoneMinderMonitorDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(ZoneMinderMonitorDiscoveryService.class);

    private final static int TIMEOUT = 60;

    private ZoneMinderServerBridgeHandler zoneMinderServerHandler;
    private ScheduledFuture<?> requestMonitorJob;

    /**
     * Discovers ZoneMinder Monitors attached to a ZoneMinder Server
     *
     * @param soneMinderServerHandler
     */
    public ZoneMinderMonitorDiscoveryService(ZoneMinderServerBridgeHandler zoneMinderServerHandler) {
        super(ZoneMinderThingMonitorHandler.SUPPORTED_THING_TYPES, TIMEOUT, true);
        this.zoneMinderServerHandler = zoneMinderServerHandler;

        setupRequestMonitorJob();
    }

    @Override
    protected void startScan() {
        discoverZoneMinderMonitors();
    }

    /*
     * Allows request player job to be canceled when server handler is removed
     */
    public void cancelRequestMonitorJob() {
        logger.debug("canceling RequestMonitorJob");
        if (requestMonitorJob != null) {
            requestMonitorJob.cancel(true);
            requestMonitorJob = null;
        }
    }

    public static String BuildMonitorLabel(String id, String name) {
        return String.format("%s [%s]", ZoneMinderConstants.ZONEMINDER_MONITOR_NAME, name);
    }

    protected synchronized void discoverZoneMinderMonitors() {
        if (ZoneMinderFactory.getSessionManager().isConnected()) {
            ArrayList<IZoneMinderMonitorData> monitors = ZoneMinderFactory.getServerProxy().getMonitors();

            for (IZoneMinderMonitorData monitor : monitors) {
                monitorAdded(monitor);
            }
        }
    }

    protected void monitorAdded(IZoneMinderMonitorData monitor) {

        try {
            ThingUID bridgeUID = zoneMinderServerHandler.getThing().getUID();
            String monitorUID = String.format("%s-%s", ZoneMinderConstants.THING_ZONEMINDER_MONITOR, monitor.getId());
            ThingUID thingUID = new ThingUID(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR, bridgeUID,
                    monitorUID);

            // Does Monitor exist?
            if (!monitorThingExists(thingUID)) {
                logger.info("[DISCOVERY] Monitor added '{}':'{}'", monitor.getId(), monitor.getName());
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
            logger.error("[DISCOVERY] Error occurred when calling 'monitorAdded' from Discovery. Exception={}",
                    ex.getMessage());
        }
    }

    private boolean monitorThingExists(ThingUID newThingUID) {
        return zoneMinderServerHandler.getThingByUID(newThingUID) != null ? true : false;
    }

    /**
     * Tells the bridge to request a list of Monitors
     */
    private void setupRequestMonitorJob() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                discoverZoneMinderMonitors();
            }

        };

        logger.debug("[DISCOVERY] - request monitor discovery job scheduled to run every {} seconds", TIMEOUT);
        requestMonitorJob = scheduler.scheduleWithFixedDelay(runnable, 0, TIMEOUT, TimeUnit.SECONDS);
    }

}
