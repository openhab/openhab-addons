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
package org.openhab.binding.wolfsmartset.internal.discovery;

import static org.openhab.binding.wolfsmartset.internal.WolfSmartsetBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wolfsmartset.internal.dto.GetSystemListDTO;
import org.openhab.binding.wolfsmartset.internal.handler.WolfSmartsetAccountBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WolfSmartsetAccountDiscoveryService} is responsible for discovering the WolfSmartset
 * systems that are associated with the WolfSmartset Account
 *
 * @author Bo Biene - Initial contribution
 */
@NonNullByDefault
public class WolfSmartsetAccountDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(WolfSmartsetAccountDiscoveryService.class);

    private @NonNullByDefault({}) WolfSmartsetAccountBridgeHandler bridgeHandler;

    private @Nullable Future<?> discoveryJob;

    public WolfSmartsetAccountDiscoveryService() {
        super(SUPPORTED_SYSTEM_AND_UNIT_THING_TYPES_UIDS, 8, true);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof WolfSmartsetAccountBridgeHandler) {
            this.bridgeHandler = (WolfSmartsetAccountBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_SYSTEM_AND_UNIT_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("WolfSmartsetDiscovery: Starting background discovery job");

        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::backgroundDiscover, DISCOVERY_INITIAL_DELAY_SECONDS,
                    DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("WolfSmartsetDiscovery: Stopping background discovery job");
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        logger.debug("WolfSmartsetDiscovery: Starting discovery scan");
        discover();
    }

    private void backgroundDiscover() {
        if (!bridgeHandler.isBackgroundDiscoveryEnabled()) {
            return;
        }
        discover();
    }

    private void discover() {
        if (bridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("WolfSmartsetDiscovery: Skipping discovery because Account Bridge thing is not ONLINE");
            return;
        }
        logger.debug("WolfSmartsetDiscovery: Discovering WolfSmartset devices");
        discoverSystems();
    }

    private synchronized void discoverSystems() {
        logger.debug("WolfSmartsetDiscovery: Discovering systems");
        var registeredSytems = bridgeHandler.getRegisteredSystems();
        if (registeredSytems != null) {
            for (GetSystemListDTO system : registeredSytems) {
                String name = system.getName();
                String identifier = null;
                if (system.getId() != null) {
                    identifier = system.getId().toString();
                }
                if (identifier != null && name != null) {
                    ThingUID thingUID = new ThingUID(UID_SYSTEM_BRIDGE, bridgeHandler.getThing().getUID(), identifier);
                    thingDiscovered(createSystemDiscoveryResult(thingUID, identifier, name));
                    logger.debug("WolfSmartsetDiscovery: System '{}' and name '{}' added with UID '{}'", identifier,
                            name, thingUID);
                }
            }
        }
    }

    private DiscoveryResult createSystemDiscoveryResult(ThingUID systemUID, String identifier, String name) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_SYSTEM_ID, identifier);
        return DiscoveryResultBuilder.create(systemUID).withProperties(properties)
                .withRepresentationProperty(CONFIG_SYSTEM_ID).withBridge(bridgeHandler.getThing().getUID())
                .withLabel(String.format("WolfSmartset System %s", name)).build();
    }
}
