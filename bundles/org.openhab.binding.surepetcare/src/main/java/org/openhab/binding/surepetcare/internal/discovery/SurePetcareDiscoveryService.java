/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Collections;
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
 * The {@link SurePetcareDiscoveryService} is an implementation of a discovery service for Sure Petcare pets and
 * devices.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;
    private static final int DISCOVERY_SCAN_DELAY_MINUTES = 1;
    private static final int DISCOVERY_REFRESH_INTERVAL_HOURS = 12;

    private @Nullable ScheduledFuture<?> discoveryJob;

    private @NonNullByDefault({}) SurePetcareBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a SurePetcareDiscoveryService with enabled autostart.
     */
    public SurePetcareDiscoveryService() {
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
        if (handler instanceof SurePetcareBridgeHandler) {
            bridgeHandler = (SurePetcareBridgeHandler) handler;
            bridgeUID = bridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
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
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            logger.debug("Starting device discovery for bridge {}", bridgeUID);
            bridgeHandler.listHouseholds().forEach(this::householdDiscovered);
            bridgeHandler.listPets().forEach(this::petDiscovered);
            bridgeHandler.listDevices().forEach(this::deviceDiscovered);
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
