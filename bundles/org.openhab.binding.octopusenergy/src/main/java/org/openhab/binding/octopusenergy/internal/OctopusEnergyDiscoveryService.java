/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal;

import static org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.dto.ElectricityMeterPoint;
import org.openhab.binding.octopusenergy.internal.dto.GasMeterPoint;
import org.openhab.binding.octopusenergy.internal.handler.OctopusEnergyBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyDiscoveryService} is an implementation of a discovery service for Octopus Energy meter
 * points.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class OctopusEnergyDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int DISCOVERY_SCAN_DELAY_MINUTES = 1;
    private static final int DISCOVERY_REFRESH_INTERVAL_HOURS = 12;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private @NonNullByDefault({}) OctopusEnergyBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a OctopusEnergyDiscoveryService with enabled autostart.
     */
    public OctopusEnergyDiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void activate() {
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
        if (handler instanceof OctopusEnergyBridgeHandler) {
            bridgeHandler = (OctopusEnergyBridgeHandler) handler;
            bridgeUID = bridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Octopus Energy account discovery");
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
            logger.debug("Stopped Octopus Energy background discovery");
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Octopus Energy account discovery scan");
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            logger.debug("Starting device discovery for bridge {}", bridgeUID);
            bridgeHandler.listElectricityMeterPoints().forEach(this::electricityMeterPointDiscovered);
            bridgeHandler.listGasMeterPoints().forEach(this::gasMeterPointDiscovered);
        }
    }

    private void electricityMeterPointDiscovered(ElectricityMeterPoint meterPoint) {
        logger.debug("Discovered meter point: {}", meterPoint.mpan);
        ThingUID thingsUID = new ThingUID(THING_TYPE_ELECTRICITY_METER_POINT, bridgeUID, meterPoint.mpan);
        Map<String, Object> properties = new HashMap<String, Object>(meterPoint.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID)
                .withLabel("Octopus Energy Electricity Meter Point " + meterPoint.mpan).withProperties(properties)
                .withRepresentationProperty(PROPERTY_NAME_MPAN).withBridge(bridgeUID).build());
    }

    private void gasMeterPointDiscovered(GasMeterPoint meterPoint) {
        logger.debug("Discovered meter point: {}", meterPoint.mprn);
        ThingUID thingsUID = new ThingUID(THING_TYPE_GAS_METER_POINT, bridgeUID, meterPoint.mprn);
        Map<String, Object> properties = new HashMap<String, Object>(meterPoint.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID)
                .withLabel("Octopus Gas Electricity Meter Point " + meterPoint.mprn).withProperties(properties)
                .withRepresentationProperty(PROPERTY_NAME_MPRN).withBridge(bridgeUID).build());
    }
}
