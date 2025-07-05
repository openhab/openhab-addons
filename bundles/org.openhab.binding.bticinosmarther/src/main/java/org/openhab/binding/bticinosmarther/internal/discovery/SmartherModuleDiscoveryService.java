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
package org.openhab.binding.bticinosmarther.internal.discovery;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.account.SmartherAccountHandler;
import org.openhab.binding.bticinosmarther.internal.api.dto.Location;
import org.openhab.binding.bticinosmarther.internal.api.dto.Module;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code SmartherModuleDiscoveryService} queries the Smarther API gateway to discover available Chronothermostat
 * modules inside existing plants registered under the configured Bridges.
 *
 * @author Fabio Possieri - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SmartherModuleDiscoveryService.class)
@NonNullByDefault
public class SmartherModuleDiscoveryService extends AbstractThingHandlerDiscoveryService<SmartherAccountHandler> {

    // Only modules can be discovered. A bridge must be manually added.
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MODULE);

    private static final int DISCOVERY_TIME_SECONDS = 30;

    private static final String ID_SEPARATOR = "-";

    private final Logger logger = LoggerFactory.getLogger(SmartherModuleDiscoveryService.class);

    private @Nullable ThingUID bridgeUID;

    /**
     * Constructs a {@code SmartherModuleDiscoveryService}.
     */
    public SmartherModuleDiscoveryService() {
        super(SmartherAccountHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void activate() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void initialize() {
        logger.debug("Bridge[{}] Activating chronothermostat discovery service", this.bridgeUID);
        this.bridgeUID = thingHandler.getThing().getUID();
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("Bridge[{}] Deactivating chronothermostat discovery service", this.bridgeUID);
        removeOlderResults(Instant.now());
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
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment
        if (thingHandler.isOnline()) {
            thingHandler.getLocations()
                    .forEach(l -> thingHandler.getLocationModules(l).forEach(m -> addDiscoveredDevice(l, m)));
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
