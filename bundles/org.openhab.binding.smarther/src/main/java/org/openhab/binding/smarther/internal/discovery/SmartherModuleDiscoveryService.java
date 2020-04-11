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

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.WordUtils;
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
import org.openhab.binding.smarther.internal.api.model.Location;
import org.openhab.binding.smarther.internal.api.model.Module;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartherModuleDiscoveryService} queries the Smarther API gateway for available modules.
 *
 * @author Fabio Possieri - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.smarther")
@NonNullByDefault
public class SmartherModuleDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    // Only modules can be discovered. A bridge must be manually added.
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MODULE);
    // The call to listModules is fast
    private static final int DISCOVERY_TIME_SECONDS = 10;
    // Handling of the background scan for new devices
    private static final boolean BACKGROUND_SCAN_ENABLED = false;
    private static final long BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private @NonNullByDefault({}) SmartherAccountHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    private @Nullable ScheduledFuture<?> backgroundFuture;

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
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SmartherAccountHandler) {
            bridgeHandler = (SmartherAccountHandler) handler;
            bridgeUID = bridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected synchronized void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        if (BACKGROUND_SCAN_ENABLED) {
            backgroundFuture = scheduler.scheduleWithFixedDelay(this::startScan, BACKGROUND_SCAN_REFRESH_MINUTES,
                    BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    protected synchronized void stopBackgroundDiscovery() {
        if (backgroundFuture != null) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        if (bridgeHandler != null && bridgeHandler.isOnline()) {
            logger.debug("Starting modules discovery for bridge {}", bridgeUID);
            try {
                bridgeHandler.listLocations()
                        .forEach(l -> bridgeHandler.listModules(l).forEach(m -> thingDiscovered(l, m)));
            } catch (RuntimeException e) {
                logger.warn("Finding modules failed with message: {}", e.getMessage(), e);
            }
        }
    }

    private void thingDiscovered(Location location, Module module) {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put(PROPERTY_PLANT_ID, location.getPlantId());
        properties.put(PROPERTY_MODULE_ID, module.getId());
        properties.put(PROPERTY_MODULE_NAME, module.getName());
        properties.put(PROPERTY_DEVICE_TYPE, WordUtils.capitalizeFully(module.getDeviceType()));
        ThingUID thing = new ThingUID(THING_TYPE_MODULE, bridgeUID, getThingIdFromModule(module));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thing).withBridge(bridgeUID)
                .withProperties(properties).withRepresentationProperty(PROPERTY_MODULE_ID).withLabel(module.getName())
                .build();

        thingDiscovered(discoveryResult);
    }

    private String getThingIdFromModule(Module module) {
        final String moduleId = module.getId();
        return moduleId.substring(0, moduleId.indexOf("-"));
    }

}
