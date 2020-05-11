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
package org.openhab.binding.ecobee.internal.discovery;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.binding.ecobee.internal.handler.EcobeeAccountBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThermostatDiscoveryService} is responsible for discovering the Ecobee
 * thermostats that are associated with the Ecobee Account.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ThermostatDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(ThermostatDiscoveryService.class);

    private @NonNullByDefault({}) EcobeeAccountBridgeHandler bridgeHandler;

    public ThermostatDiscoveryService() {
        super(30);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EcobeeAccountBridgeHandler) {
            this.bridgeHandler = (EcobeeAccountBridgeHandler) handler;
            this.bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        logger.debug("ThermostatDiscovery: Activating Ecobee thermostat discovery service for {}",
                bridgeHandler.getThing().getUID());
    }

    @Override
    public void deactivate() {
        logger.debug("ThermostatDiscovery: Deactivating Ecobee thermostat discovery service for {}",
                bridgeHandler.getThing().getUID());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THERMOSTAT_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.trace("ThermostatDiscovery: Performing background discovery scan for {}",
                bridgeHandler.getThing().getUID());
        discoverThermostats();
    }

    @Override
    public void startScan() {
        logger.debug("ThermostatDiscovery: Starting discovery scan for {}", bridgeHandler.getThing().getUID());
        discoverThermostats();
    }

    @Override
    public synchronized void abortScan() {
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    private String buildLabel(String name) {
        return String.format("Ecobee Thermostat %s", name);
    }

    private synchronized void discoverThermostats() {
        for (ThermostatDTO thermostat : bridgeHandler.getRegisteredThermostats()) {
            String name = thermostat.name;
            String identifier = thermostat.identifier;
            if (identifier != null && name != null) {
                ThingUID thingUID = new ThingUID(UID_THERMOSTAT_BRIDGE, bridgeHandler.getThing().getUID(),
                        thermostat.identifier);
                thingDiscovered(createDiscoveryResult(thingUID, identifier, name));
                logger.trace("ThermostatDiscovery: Thermostat with id '{}' and name '{}' added to Inbox with UID '{}'",
                        thermostat.identifier, thermostat.name, thingUID);
            }
        }
    }

    private DiscoveryResult createDiscoveryResult(ThingUID thermostatUID, String identifier, String name) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(CONFIG_THERMOSTAT_ID, identifier);
        return DiscoveryResultBuilder.create(thermostatUID).withProperties(properties)
                .withRepresentationProperty(CONFIG_THERMOSTAT_ID).withBridge(bridgeHandler.getThing().getUID())
                .withLabel(buildLabel(name)).build();
    }
}
