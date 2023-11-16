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
package org.openhab.binding.plugwiseha.internal.discovery;

import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.dto.DomainObjects;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Location;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHABridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHADiscoveryService} class is capable of discovering the
 * available data from the Plugwise Home Automation gateway
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 */
@NonNullByDefault
public class PlugwiseHADiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseHADiscoveryService.class);
    private static final int TIMEOUT_SECONDS = 5;
    private static final int REFRESH_SECONDS = 600;
    private @Nullable PlugwiseHABridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> discoveryFuture;

    public PlugwiseHADiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT_SECONDS, true);
    }

    @Override
    protected synchronized void startScan() {
        try {
            discoverDomainObjects();
        } catch (PlugwiseHAException e) {
            // Ignore silently
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Plugwise Home Automation background discovery");

        ScheduledFuture<?> localDiscoveryFuture = discoveryFuture;
        if (localDiscoveryFuture == null || localDiscoveryFuture.isCancelled()) {
            discoveryFuture = scheduler.scheduleWithFixedDelay(this::startScan, 30, REFRESH_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping Plugwise Home Automation background discovery");

        ScheduledFuture<?> localDiscoveryFuture = discoveryFuture;
        if (localDiscoveryFuture != null) {
            if (!localDiscoveryFuture.isCancelled()) {
                localDiscoveryFuture.cancel(true);
                localDiscoveryFuture = null;
            }
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof PlugwiseHABridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    private void discoverDomainObjects() throws PlugwiseHAException {
        PlugwiseHAController controller = null;
        PlugwiseHABridgeHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler != null) {
            controller = localBridgeHandler.getController();
        }

        if (controller != null) {
            DomainObjects domainObjects = controller.getDomainObjects();

            if (domainObjects != null) {
                for (Location location : domainObjects.getLocations().values()) {
                    // Only add locations with at least 1 appliance (this ignores the 'root' (home)
                    // location which is the parent of all other locations.)
                    if (location.applianceCount() > 0) {
                        locationDiscovery(location);
                    }
                }

                for (Appliance appliance : domainObjects.getAppliances().values()) {
                    // Only add appliances that are required/supported for this binding
                    if (PlugwiseHABindingConstants.SUPPORTED_APPLIANCE_TYPES.contains(appliance.getType())) {
                        applianceDiscovery(appliance);
                    }
                }
            }
        }
    }

    private void applianceDiscovery(Appliance appliance) {
        String applianceId = appliance.getId();
        String applianceName = appliance.getName();
        String applianceType = appliance.getType();

        PlugwiseHABridgeHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID bridgeUID = localBridgeHandler.getThing().getUID();

            ThingUID uid;

            Map<String, Object> configProperties = new HashMap<>();

            configProperties.put(APPLIANCE_CONFIG_ID, applianceId);

            switch (applianceType) {
                case "thermostatic_radiator_valve":
                    uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_APPLIANCE_VALVE, bridgeUID, applianceId);
                    configProperties.put(APPLIANCE_CONFIG_LOWBATTERY, 15);
                    break;
                case "central_heating_pump":
                    uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_APPLIANCE_PUMP, bridgeUID, applianceId);
                    break;
                case "heater_central":
                    uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_APPLIANCE_BOILER, bridgeUID, applianceId);
                    break;
                case "zone_thermostat":
                    uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_APPLIANCE_THERMOSTAT, bridgeUID,
                            applianceId);
                    configProperties.put(APPLIANCE_CONFIG_LOWBATTERY, 15);
                    break;
                default:
                    return;
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                    .withLabel(applianceName).withProperties(configProperties)
                    .withRepresentationProperty(APPLIANCE_CONFIG_ID).build();

            thingDiscovered(discoveryResult);

            logger.debug("Discovered plugwise appliance type '{}' with name '{}' with id {} ({})", applianceType,
                    applianceName, applianceId, uid);
        }
    }

    private void locationDiscovery(Location location) {
        String locationId = location.getId();
        String locationName = location.getName();

        PlugwiseHABridgeHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler != null) {
            ThingUID bridgeUID = localBridgeHandler.getThing().getUID();
            ThingUID uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_ZONE, bridgeUID, locationId);

            Map<String, Object> configProperties = new HashMap<>();

            configProperties.put(ZONE_CONFIG_ID, locationId);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                    .withLabel(locationName).withRepresentationProperty(ZONE_CONFIG_ID).withProperties(configProperties)
                    .build();

            thingDiscovered(discoveryResult);

            logger.debug("Discovered plugwise zone '{}' with id {} ({})", locationName, locationId, uid);
        }
    }
}
