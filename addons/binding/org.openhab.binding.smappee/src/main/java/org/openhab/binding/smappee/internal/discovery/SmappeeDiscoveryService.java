/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.smappee.SmappeeBindingConstants;
import org.openhab.binding.smappee.handler.SmappeeHandler;
import org.openhab.binding.smappee.internal.SmappeeService;
import org.openhab.binding.smappee.internal.SmappeeServiceLocationInfo;
import org.openhab.binding.smappee.internal.SmappeeServiceLocationInfoActuator;
import org.openhab.binding.smappee.internal.SmappeeServiceLocationInfoAppliance;
import org.openhab.binding.smappee.internal.SmappeeServiceLocationInfoSensor;
import org.openhab.binding.smappee.internal.SmappeeServiceLocationInfoSensorChannel;
import org.openhab.binding.smappee.internal.ThingProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery class for the Smappee.
 * This will detect the switches in your home.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME = 60;

    private final Logger logger = LoggerFactory.getLogger(SmappeeDiscoveryService.class);
    private SmappeeHandler smappeeHandler;

    private ScheduledFuture<?> scheduledJob;

    /**
     * Whether we are currently scanning or not
     */
    private boolean isScanning;

    /**
     * Constructs the discovery class using the thing IDs that smappee can discover
     * - actuators (plugs)
     * - detected appliances
     */
    public SmappeeDiscoveryService(SmappeeHandler smappeeHandler) {
        super(Stream.of(SmappeeBindingConstants.THING_TYPE_ACTUATOR, SmappeeBindingConstants.THING_TYPE_APPLIANCE)
                .collect(Collectors.toSet()), SEARCH_TIME, false);

        this.smappeeHandler = smappeeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SmappeeBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Activate the discovery service.
     */
    public void activate() {
        super.activate(null);
        startScan();
    }

    /**
     * Deactivate the discovery service.
     */
    @Override
    public void deactivate() {
        super.deactivate();
        stopScan();
    }

    /**
     * Starts the scan. This discovery will:
     * <ul>
     * <li>Call 'get service location info'
     * (https://smappee.atlassian.net/wiki/spaces/DEVAPI/pages/8552482/Get+Servicelocation+Info)</li>
     * <li>For each actuator, add a thing in the inbox (if it doesn't exist already)</li>
     * <li>For each appliance, add a thing in the inbox (if it doesn't exist already)</li>
     * </ul>
     * The process will continue until {@link #stopScan()} is called.
     */
    @Override
    protected void startScan() {
        if (isScanning) {
            stopScan();
        }

        // this should be done by base class ???
        // somehow this is not working, so starting a scheduler instead
        startAutomaticRefresh();

        isScanning = true;
    }

    public void startAutomaticRefresh() {
        scheduledJob = scheduler.scheduleWithFixedDelay(this::scanForNewDevices, 0, 5, TimeUnit.MINUTES);
    }

    private void scanForNewDevices() {
        SmappeeService smappeeService = smappeeHandler.getSmappeeService();

        if (smappeeService == null || !smappeeService.isInitialized()) {
            logger.debug("skipping discovery because smappee service is not up yet (config error ?)");
            return;
        }

        logger.debug("Starting Discovery");

        SmappeeServiceLocationInfo serviceLocationInfo = smappeeService.getServiceLocationInfo();

        if (serviceLocationInfo == null) {
            logger.debug("failed to scan for new smappee devices");
            return;
        }

        for (SmappeeServiceLocationInfoActuator actuator : serviceLocationInfo.actuators) {
            ThingTypeUID typeId = SmappeeBindingConstants.THING_TYPE_ACTUATOR;

            addNewDiscoveredThing(actuator.id, actuator.name, typeId);
        }

        for (SmappeeServiceLocationInfoAppliance appliance : serviceLocationInfo.appliances) {
            if ("Find me".equals(appliance.type)) {
                continue; // skip
            }

            ThingTypeUID typeId = SmappeeBindingConstants.THING_TYPE_APPLIANCE;
            ThingProperty[] properties = new ThingProperty[] { new ThingProperty("type", appliance.type) };

            addNewDiscoveredThing(appliance.id, appliance.name, typeId, properties);
        }

        for (SmappeeServiceLocationInfoSensor sensor : serviceLocationInfo.sensors) {
            for (SmappeeServiceLocationInfoSensorChannel channel : sensor.channels) {
                String id = sensor.id + "-" + channel.channel;
                ThingTypeUID typeId = SmappeeBindingConstants.THING_TYPE_SENSOR;
                ThingProperty[] properties = new ThingProperty[] { new ThingProperty("type", channel.type) };

                addNewDiscoveredThing(id, channel.type + " sensor", typeId, properties);
            }
        }
    }

    private void addNewDiscoveredThing(String id, String label, ThingTypeUID typeId) {
        addNewDiscoveredThing(id, label, typeId, null);
    }

    private void addNewDiscoveredThing(String id, String label, ThingTypeUID typeId, ThingProperty[] properties) {
        logger.debug("Creating new {} thing with id {}", typeId, id);
        ThingUID newthing = new ThingUID(typeId, id);

        Map<String, Object> thingProperties = new HashMap<>(1);
        thingProperties.put("id", id);

        if (properties != null) {
            for (ThingProperty property : properties) {
                thingProperties.put(property.key, property.value);
            }
        }

        ThingUID bridgeUID = smappeeHandler.getThing().getUID();
        DiscoveryResult result = DiscoveryResultBuilder.create(newthing).withBridge(bridgeUID)
                .withProperties(thingProperties).withLabel(label).build();
        thingDiscovered(result);
    }

    /**
     * Stops the discovery scan.
     */
    @Override
    public synchronized void stopScan() {
        super.stopScan();

        if (scheduledJob != null) {
            scheduledJob.cancel(true);
        }

        isScanning = false;
    }
}
