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
package org.openhab.binding.bticinosmarther.internal.discovery;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.account.SmartherAccountHandler;
import org.openhab.binding.bticinosmarther.internal.api.dto.Location;
import org.openhab.binding.bticinosmarther.internal.api.dto.Module;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SmartherModuleDiscoveryService} queries the Smarther API gateway to discover available Chronothermostat
 * modules inside existing plants registered under the configured Bridges.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherModuleDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    // Only modules can be discovered. A bridge must be manually added.
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MODULE);

    private static final int DISCOVERY_TIME_SECONDS = 30;

    private static final String ID_SEPARATOR = "-";

    private final Logger logger = LoggerFactory.getLogger(SmartherModuleDiscoveryService.class);

    private @Nullable SmartherAccountHandler bridgeHandler;
    private @Nullable ThingUID bridgeUID;

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
        logger.debug("Bridge[{}] Activating chronothermostat discovery service", this.bridgeUID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void deactivate() {
        logger.debug("Bridge[{}] Deactivating chronothermostat discovery service", this.bridgeUID);
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
    protected void startBackgroundDiscovery() {
        logger.debug("Bridge[{}] Performing background discovery scan for chronothermostats", this.bridgeUID);
        discoverChronothermostats();
    }

    @Override
    protected void startScan() {
        logger.debug("Bridge[{}] Starting discovery scan for chronothermostats", this.bridgeUID);
        discoverChronothermostats();
    }

    @Override
    public synchronized void abortScan() {
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Discovers Chronothermostat devices for the given bridge handler.
     */
    private synchronized void discoverChronothermostats() {
        final SmartherAccountHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler != null) {
            // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment
            if (localBridgeHandler.isOnline()) {
                localBridgeHandler.getLocations()
                        .forEach(l -> localBridgeHandler.getLocationModules(l).forEach(m -> addDiscoveredDevice(l, m)));
            }
        }
    }

    /**
     * Creates a Chronothermostat module Thing based on the remotely discovered location and module.
     *
     * @param location
     *            the location containing the discovered module
     * @param module
     *            the discovered module
     */
    private void addDiscoveredDevice(Location location, Module module) {
        ThingUID localBridgeUID = this.bridgeUID;
        if (localBridgeUID != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_PLANT_ID, location.getPlantId());
            properties.put(PROPERTY_MODULE_ID, module.getId());
            properties.put(PROPERTY_MODULE_NAME, module.getName());
            properties.put(PROPERTY_DEVICE_TYPE, module.getDeviceType());

            ThingUID thingUID = new ThingUID(THING_TYPE_MODULE, localBridgeUID, getThingIdFromModule(module));

            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(localBridgeUID)
                    .withProperties(properties).withRepresentationProperty(PROPERTY_MODULE_ID)
                    .withLabel(module.getName()).build();
            thingDiscovered(discoveryResult);
            logger.debug("Bridge[{}] Chronothermostat with id '{}' and name '{}' added to Inbox with UID '{}'",
                    localBridgeUID, module.getId(), module.getName(), thingUID);
        }
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
