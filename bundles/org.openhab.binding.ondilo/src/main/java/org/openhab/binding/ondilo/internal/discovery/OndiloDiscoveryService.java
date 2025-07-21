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
package org.openhab.binding.ondilo.internal.discovery;

import static org.openhab.binding.ondilo.internal.OndiloBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.OndiloBridgeHandler;
import org.openhab.binding.ondilo.internal.dto.Pool;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OndiloDiscoveryService} is responsible for discovering new devices
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(OndiloDiscoveryService.class);

    private final OndiloBridgeHandler bridgeHandler;
    private @Nullable ScheduledExecutorService scheduler;

    public OndiloDiscoveryService(OndiloBridgeHandler bridgeHandler) {
        super(Set.of(THING_TYPE_ONDILO), 10, true);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.trace("Starting device scan");
        checkForDiscoveredPools();
    }

    public void startBackgroundDiscovery() {
        logger.trace("Starting background discovery scheduler");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // Poll bridge every 60 seconds
        scheduler.scheduleWithFixedDelay(() -> checkForDiscoveredPools(), 10, 60, TimeUnit.SECONDS);
        this.scheduler = scheduler;
    }

    public void stopBackgroundDiscovery() {
        logger.trace("Stopping background discovery scheduler");
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow(); // Stop any running discovery threads
            this.scheduler = null;
        }
    }

    protected void checkForDiscoveredPools() {
        try {
            Optional<List<Pool>> registeredPools = bridgeHandler.getPools();
            if (registeredPools.isPresent()) {
                addDiscoveredPools(registeredPools.get());
            } else {
                logger.trace("No Ondilo ICOs found from bridgeHandler");
            }
        } catch (RuntimeException e) {
            logger.error("Unexpected error in discovery job: {}", e.getMessage(), e);
        }
    }

    protected void addDiscoveredPools(List<Pool> pools) {
        for (Pool pool : pools) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingTypeUID thingTypeUID = THING_TYPE_ONDILO;
            ThingUID poolThingUid = new ThingUID(THING_TYPE_ONDILO, bridgeUID, String.valueOf(pool.id));

            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_ONDILO_ID, pool.id);
            properties.put(PROPERTY_ONDILO_NAME, pool.name);
            properties.put(PROPERTY_ONDILO_TYPE, pool.type);
            properties.put(PROPERTY_ONDILO_VOLUME, pool.getVolume());
            properties.put(PROPERTY_ONDILO_DISINFECTION, pool.getDisinfection());
            properties.put(PROPERTY_ONDILO_ADDRESS, pool.getAddress());
            properties.put(PROPERTY_ONDILO_LOCATION, pool.getLocation());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(poolThingUid).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(PROPERTY_ONDILO_ID)
                    .withLabel("Ondilo ICO: " + pool.name).build();
            thingDiscovered(discoveryResult);
        }
    }
}
