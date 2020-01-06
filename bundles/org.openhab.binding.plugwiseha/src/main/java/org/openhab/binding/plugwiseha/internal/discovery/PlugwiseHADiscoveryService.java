/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.object.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.object.Appliances;
import org.openhab.binding.plugwiseha.internal.api.model.object.DomainObjects;
import org.openhab.binding.plugwiseha.internal.api.model.object.Location;
import org.openhab.binding.plugwiseha.internal.api.model.object.Locations;
import org.openhab.binding.plugwiseha.internal.handler.PlugwiseHABridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHADiscoveryService} class is capable of discovering the
 * available data from the Plugwise Home Automation gateway
 *
 * @author Bas van Wetten - Initial contribution
 *
 */
public class PlugwiseHADiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseHADiscoveryService.class);
    private final PlugwiseHABridgeHandler handler;
    private static final int TIMEOUT = 5;
    private static final int REFRESH = 600;

    private ScheduledFuture<?> discoveryFuture;

    public PlugwiseHADiscoveryService(PlugwiseHABridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        this.handler = bridgeHandler;
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
    protected void startBackgroundDiscovery() {
        logger.debug("Start Plugwise Home Automation background discovery");
        if (this.discoveryFuture == null || this.discoveryFuture.isCancelled()) {
            if (this.handler.getThing().getStatus() == ThingStatus.ONLINE) {
                logger.debug("Start Scan");
                this.discoveryFuture = scheduler.scheduleWithFixedDelay(this::startScan, 30, REFRESH, TimeUnit.SECONDS);
            } else {
                stopBackgroundDiscovery();
            }
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (this.discoveryFuture != null && !this.discoveryFuture.isCancelled()) {
            logger.debug("Stop Plugwise Home Automation background discovery");
            this.discoveryFuture.cancel(true);
            this.discoveryFuture = null;
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    // @Override
    // public void accountStatusChanged(ThingStatus status) {
    // if (status == ThingStatus.ONLINE) {
    // discoverLocations();
    // discoverAppliances();
    // }
    // }

    private void discoverDomainObjects() throws PlugwiseHAException {
        PlugwiseHAController controller = this.handler.getController();

        if (controller != null) {
            DomainObjects domainObjects = controller.getDomainObjects();            

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

    private void applianceDiscovery(Appliance appliance) {
        String applianceId = appliance.getId();
        String applianceName = appliance.getName();
        String applianceType = appliance.getType();
        ThingUID bridgeUID = this.handler.getThing().getUID();
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
        case "zone_thermostat":
            uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_APPLIANCE_THERMOSTAT, bridgeUID, applianceId);
            configProperties.put(APPLIANCE_CONFIG_LOWBATTERY, 15);
            break;
        default:
            return;
        }

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(applianceName).withProperties(configProperties).build();

        thingDiscovered(discoveryResult);

        logger.debug("Discovered plugwise appliance type '{}' with name '{}' with id {} ({})", applianceType,
                applianceName, applianceId, uid);
    }

    private void locationDiscovery(Location location) {
        String locationId = location.getId();
        String locationName = location.getName();
        ThingUID bridgeUID = this.handler.getThing().getUID();
        ThingUID uid = new ThingUID(PlugwiseHABindingConstants.THING_TYPE_ZONE, bridgeUID, locationId);

        Map<String, Object> configProperties = new HashMap<>();

        configProperties.put(ZONE_CONFIG_ID, locationId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(locationName).withProperties(configProperties).build();

        thingDiscovered(discoveryResult);

        logger.debug("Discovered plugwise zone '{}' with id {} ({})", locationName, locationId, uid);
    }
}
