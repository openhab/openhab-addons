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
package org.openhab.binding.smarther.internal.discovery;

import static org.openhab.binding.smarther.internal.SmartherBindingConstants.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.smarther.internal.account.SmartherAccountHandler;
import org.openhab.binding.smarther.internal.api.dto.Location;
import org.openhab.binding.smarther.internal.api.dto.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SmartherModuleDiscoveryService} queries the Smarther API gateway to discover available Chronothermostat
 * modules inside existing plants registered under the configured Bridges.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherModuleDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    // Only modules can be discovered. A bridge must be manually added.
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MODULE);
    // The call to listModules is fast
    private static final int DISCOVERY_TIME_SECONDS = 10;

    // Handling of the background scan for new devices
    private static final boolean BACKGROUND_SCAN_ENABLED = false;
    private static final long BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private static final String ID_SEPARATOR = "-";

    private final Logger logger = LoggerFactory.getLogger(SmartherModuleDiscoveryService.class);

    private @Nullable SmartherAccountHandler bridgeHandler;
    private @Nullable ThingUID bridgeUID;
    private @Nullable ScheduledFuture<?> backgroundFuture;

    /**
     * Constructs a {@code SmartherModuleDiscoveryService}.
     */
    public SmartherModuleDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void activate() {
        Map<String, @Nullable Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartherAccountHandler) {
            final SmartherAccountHandler localBridgeHandler = (SmartherAccountHandler) handler;
            this.bridgeHandler = localBridgeHandler;
            this.bridgeUID = localBridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridgeHandler;
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        if (BACKGROUND_SCAN_ENABLED) {
            this.backgroundFuture = scheduler.scheduleWithFixedDelay(this::startScan, BACKGROUND_SCAN_REFRESH_MINUTES,
                    BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        final ScheduledFuture<?> localBackgroundFuture = this.backgroundFuture;
        if (localBackgroundFuture != null) {
            if (!localBackgroundFuture.isCancelled()) {
                localBackgroundFuture.cancel(true);
            }
            this.backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        final SmartherAccountHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler != null) {
            // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment
            if (localBridgeHandler.isOnline()) {
                logger.debug("Starting modules discovery for bridge {}", this.bridgeUID);
                localBridgeHandler.getLocations()
                        .forEach(l -> localBridgeHandler.getLocationModules(l).forEach(m -> thingDiscovered(l, m)));
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Creates a Chronothermostat module Thing based on the remotely discovered location and module.
     *
     * @param location
     *            the location containing the discovered module
     * @param module
     *            the discovered module
     */
    private void thingDiscovered(Location location, Module module) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(PROPERTY_PLANT_ID, location.getPlantId());
        properties.put(PROPERTY_MODULE_ID, module.getId());
        properties.put(PROPERTY_MODULE_NAME, module.getName());
        properties.put(PROPERTY_DEVICE_TYPE, module.getDeviceType());
        ThingUID thing = new ThingUID(THING_TYPE_MODULE, bridgeUID, getThingIdFromModule(module));

        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thing).withBridge(this.bridgeUID)
                .withProperties(properties).withRepresentationProperty(PROPERTY_MODULE_ID).withLabel(module.getName())
                .build();

        thingDiscovered(discoveryResult);
    }

    /**
     * Generates the Thing identifier based on the Chronothermostat module identifier.
     *
     * @param module
     *            the Chronothermostat module to use
     *
     * @return a string containing the generated Thing identifier
     */
    private String getThingIdFromModule(Module module) {
        final String moduleId = module.getId();
        return moduleId.substring(0, moduleId.indexOf(ID_SEPARATOR));
    }

}
