/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal;

import static org.openhab.binding.myenergi.internal.MyenergiBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.dto.EddiSummary;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.handler.MyenergiBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiDiscoveryService} is an implementation of a discovery service for MyEnergi devices.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyenergiDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(MyenergiDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int DISCOVERY_SCAN_DELAY_MINUTES = 1;
    private static final int DISCOVERY_REFRESH_INTERVAL_HOURS = 12;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private @NonNullByDefault({}) MyenergiBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a MyEnergiDiscoveryService with enabled autostart.
     */
    public MyenergiDiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void activate() {
        @Nullable
        Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    /* We override this method to allow a call from the thing handler factory */
    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MyenergiBridgeHandler) {
            bridgeHandler = (MyenergiBridgeHandler) handler;
            bridgeUID = bridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting MyEnergi device discovery");
        stopBackgroundDiscovery();
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, DISCOVERY_SCAN_DELAY_MINUTES,
                DISCOVERY_REFRESH_INTERVAL_HOURS * 60, TimeUnit.MINUTES);
        logger.debug("Scheduled topology-changed job every {} hours", DISCOVERY_REFRESH_INTERVAL_HOURS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> job = discoveryJob;
        if (job != null) {
            job.cancel(true);
            discoveryJob = null;
            logger.debug("Stopped MyEnergi device discovery");
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting MyEnergi discovery scan");
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        logger.debug("Starting device discovery for bridge {}", bridgeUID);
        bridgeHandler.listEddis().forEach(this::eddiDiscovered);
        bridgeHandler.listZappis().forEach(this::zappiDiscovered);
        bridgeHandler.listHarvis().forEach(this::harviDiscovered);
    }

    private void eddiDiscovered(EddiSummary device) {
        logger.debug("Discovered Eddi: {}", device.serialNumber);
        ThingUID thingsUID = new ThingUID(THING_TYPE_EDDI, bridgeUID, device.serialNumber.toString());
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(device.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel("MyEnergi Eddi").withProperties(properties)
                .withBridge(bridgeUID).build());
    }

    private void zappiDiscovered(ZappiSummary device) {
        logger.debug("Discovered Zappi: {}", device.serialNumber);
        ThingUID thingsUID = new ThingUID(THING_TYPE_ZAPPI, bridgeUID, device.serialNumber.toString());
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(device.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel("MyEnergi Zappi").withProperties(properties)
                .withBridge(bridgeUID).build());
    }

    private void harviDiscovered(HarviSummary device) {
        logger.debug("Discovered Harvi: {}", device.serialNumber);
        ThingUID thingsUID = new ThingUID(THING_TYPE_HARVI, bridgeUID, device.serialNumber.toString());
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(device.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel("MyEnergi Harvi").withProperties(properties)
                .withBridge(bridgeUID).build());
    }
}
