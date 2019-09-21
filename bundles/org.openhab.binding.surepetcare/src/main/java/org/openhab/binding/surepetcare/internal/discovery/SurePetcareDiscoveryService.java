/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice.ProductType;
import org.openhab.binding.surepetcare.internal.data.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.data.SurePetcareTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareDiscoveryService} is an implementation of a discovery service for Sure Petcare pets and
 * devices.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(SurePetcareConstants.THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final int DISCOVERY_REFRESH_INTERVAL = 12 * 3600; // 12 hours

    private @Nullable ScheduledFuture<?> discoveryJob;

    private SurePetcareAPIHelper petcareAPI;

    private final ThingUID bridgeUID;

    /**
     * Creates a SurePetcareDiscoveryService with enabled autostart.
     *
     * @param petcareAPI
     */

    public SurePetcareDiscoveryService(ThingUID bridgeUID, SurePetcareAPIHelper petcareAPI) {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, true);
        logger.debug("Setting BridgeUID to: {}", bridgeUID.getAsString());

        this.bridgeUID = bridgeUID;
        this.petcareAPI = petcareAPI;
    }

    /* We override this method to allow a call from the thing handler factory */
    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    /* We override this method to allow a call from the thing handler factory */
    @Override
    public void deactivate() {
        logger.debug("deactivate SurePetcareDiscoveryService");
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Sure Petcare discovery scan");
        retrieveTopologyFromSurePetcare();
    }

    @SuppressWarnings("null")
    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting Sure Petcare household discovery");
        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> {
                retrieveTopologyFromSurePetcare();
            }, 0, DISCOVERY_REFRESH_INTERVAL, TimeUnit.SECONDS);
            logger.debug("Scheduled topology-changed job every {} seconds", DISCOVERY_REFRESH_INTERVAL);
        }
    }

    @SuppressWarnings("null")
    @Override
    protected void stopBackgroundDiscovery() {
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
            logger.debug("Stopped Sure Petcare device background discovery");
        }
    }

    private void retrieveTopologyFromSurePetcare() {
        int retryCount = 5;
        while (retryCount-- > 0) {
            if (petcareAPI.isOnline()) {
                logger.debug("I'm discovering the Sure Petcare topology ...");
                SurePetcareTopology topology = petcareAPI.retrieveTopology();
                for (SurePetcareHousehold household : topology.getHouseholds()) {
                    logger.debug("Creating new thing for household: {}", household.toString());
                    createHouseholdThing(household);
                }
                for (SurePetcareDevice device : topology.getDevices()) {
                    logger.debug("Creating new thing for device: {}", device.toString());
                    createDeviceThing(device);
                }
                for (SurePetcarePet pet : topology.getPets()) {
                    logger.debug("Creating new thing for pet: {}", pet.toString());
                    createPetThing(pet);
                }
                retryCount = 0;
            } else {
                logger.debug("API is not yet online, we'll wait 30s and retry");
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    retryCount = 0;
                }
            }
        }
    }

    private void createHouseholdThing(SurePetcareHousehold household) {
        ThingUID thingsUID = new ThingUID(SurePetcareConstants.THING_TYPE_HOUSEHOLD, bridgeUID,
                household.getId().toString());
        Map<String, Object> properties = household.getThingProperties();
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel(household.getName())
                .withProperties(properties).withBridge(bridgeUID).build());
    }

    private void createPetThing(SurePetcarePet pet) {
        ThingUID thingsUID = new ThingUID(SurePetcareConstants.THING_TYPE_PET, bridgeUID, pet.getId().toString());
        Map<String, Object> properties = pet.getThingProperties();
        thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel(pet.getName()).withProperties(properties)
                .withBridge(bridgeUID).build());
    }

    private void createDeviceThing(SurePetcareDevice device) {
        ThingTypeUID typeUID = null;
        switch (ProductType.findByTypeId(device.getProductId())) {
            case HUB:
                typeUID = SurePetcareConstants.THING_TYPE_HUB_DEVICE;
                break;
            case CAT_FLAP:
                typeUID = SurePetcareConstants.THING_TYPE_FLAP_DEVICE;
                break;
            case PET_FLAP:
                typeUID = SurePetcareConstants.THING_TYPE_FLAP_DEVICE;
                break;
            case UNKNOWN:
            default:
                break;
        }
        if (typeUID != null) {
            ThingUID thingsUID = new ThingUID(typeUID, bridgeUID, device.getId().toString());
            logger.debug("BridgeUID: {}, ThingsUID: {}", bridgeUID.getAsString(), thingsUID.getAsString());
            Map<String, Object> properties = device.getThingProperties();
            thingDiscovered(DiscoveryResultBuilder.create(thingsUID).withLabel(device.getName())
                    .withProperties(properties).withBridge(bridgeUID).build());
        }
    }

}
