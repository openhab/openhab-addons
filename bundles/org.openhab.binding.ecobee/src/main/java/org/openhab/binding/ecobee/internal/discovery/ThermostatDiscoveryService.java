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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.binding.ecobee.internal.handler.EcobeeAccountBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThermostatDiscoveryService} is responsible for discovering the Ecobee
 * thermostats that are associated with the Ecobee Account.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ThermostatDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(ThermostatDiscoveryService.class);

    private @NonNullByDefault({}) EcobeeAccountBridgeHandler bridgeHandler;

    private @Nullable Future<?> thermostatDiscoveryJob;

    public ThermostatDiscoveryService() {
        super(SUPPORTED_THERMOSTAT_BRIDGE_THING_TYPES_UIDS, 30, true);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EcobeeAccountBridgeHandler) {
            this.bridgeHandler = (EcobeeAccountBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
        ThingHandlerService.super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THERMOSTAT_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("ThermostatDiscovery: Starting thermostat background discovery job");
        Future<?> localThermostatDiscoveryJob = thermostatDiscoveryJob;
        if (localThermostatDiscoveryJob == null || localThermostatDiscoveryJob.isCancelled()) {
            // TODO
            thermostatDiscoveryJob = scheduler.scheduleWithFixedDelay(this::discoverThermostats,
                    DISCOVERY_INITIAL_DELAY_SECONDS, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("ThermostatDiscovery: Stopping thermostat background discovery job");
        Future<?> localThermostatDiscoveryJob = thermostatDiscoveryJob;
        if (localThermostatDiscoveryJob != null) {
            localThermostatDiscoveryJob.cancel(true);
            thermostatDiscoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        logger.debug("ThermostatDiscovery: Starting thermostat discovery scan");
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
        logger.debug("ThermostatDiscovery: Discovering thermostats");
        if (!bridgeHandler.isBackgroundDiscoveryEnabled()) {
            return;
        }
        for (ThermostatDTO thermostat : this.bridgeHandler.getRegisteredThermostats()) {
            String name = thermostat.name;
            String identifier = thermostat.identifier;
            if (identifier != null && name != null) {
                ThingUID thingUID = new ThingUID(UID_THERMOSTAT_BRIDGE, this.bridgeHandler.getThing().getUID(),
                        thermostat.identifier);
                thingDiscovered(createDiscoveryResult(thingUID, identifier, name));
                logger.debug("ThermostatDiscovery: Thermostat with id '{}' and name '{}' added to Inbox with UID '{}'",
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
