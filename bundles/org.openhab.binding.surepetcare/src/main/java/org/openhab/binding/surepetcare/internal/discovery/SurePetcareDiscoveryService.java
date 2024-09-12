/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.discovery;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDevice.ProductType;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareDiscoveryService} is an implementation of a discovery service for Sure Petcare pets and
 * devices.
 *
 * @author Rene Scherer - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SurePetcareDiscoveryService.class)
@NonNullByDefault
public class SurePetcareDiscoveryService extends AbstractThingHandlerDiscoveryService<SurePetcareBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int DISCOVERY_SCAN_DELAY_MINUTES = 1;
    private static final int DISCOVERY_REFRESH_INTERVAL_HOURS = 12;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a SurePetcareDiscoveryService with enabled autostart.
     */
    public SurePetcareDiscoveryService() {
        super(SurePetcareBridgeHandler.class, SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
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

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getUID();
        super.initialize();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Sure Petcare household discovery");
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
            logger.debug("Stopped Sure Petcare device background discovery");
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Sure Petcare discovery scan");
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        if (thingHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            logger.debug("Starting device discovery for bridge {}", bridgeUID);
            thingHandler.listHouseholds().forEach(this::householdDiscovered);
            thingHandler.listPets().forEach(this::petDiscovered);
            thingHandler.listDevices().forEach(this::deviceDiscovered);
        }
    }

    private void householdDiscovered(SurePetcareHousehold household) {
        logger.debug("Discovered household: {}", household.name);
        ThingUID thingsUID = new ThingUID(THING_TYPE_HOUSEHOLD, bridgeUID, household.id.toString());
        Map<String, Object> properties = new HashMap<>(household.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel(household.name).withProperties(properties)
                .withRepresentationProperty(PROPERTY_NAME_ID).withBridge(bridgeUID).build());
    }

    private void petDiscovered(SurePetcarePet pet) {
        logger.debug("Discovered pet: {}", pet.name);
        ThingUID thingsUID = new ThingUID(THING_TYPE_PET, bridgeUID, pet.id.toString());
        Map<String, Object> properties = new HashMap<>(pet.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel(pet.name).withProperties(properties)
                .withRepresentationProperty(PROPERTY_NAME_ID).withBridge(bridgeUID).build());
    }

    private void deviceDiscovered(SurePetcareDevice device) {
        logger.debug("Discovered device: {}", device.name);
        ThingTypeUID typeUID = null;
        switch (ProductType.findByTypeId(device.productId)) {
            case HUB:
                typeUID = THING_TYPE_HUB_DEVICE;
                break;
            case CAT_FLAP:
                typeUID = THING_TYPE_FLAP_DEVICE;
                break;
            case PET_FLAP:
                typeUID = THING_TYPE_FLAP_DEVICE;
                break;
            case PET_FEEDER:
                typeUID = THING_TYPE_FEEDER_DEVICE;
                break;
            case UNKNOWN:
            default:
                return;
        }
        ThingUID thingsUID = new ThingUID(typeUID, bridgeUID, device.id.toString());
        Map<String, Object> properties = new HashMap<>(device.getThingProperties());
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel(device.name).withProperties(properties)
                .withRepresentationProperty(PROPERTY_NAME_ID).withBridge(bridgeUID).build());
    }
}
