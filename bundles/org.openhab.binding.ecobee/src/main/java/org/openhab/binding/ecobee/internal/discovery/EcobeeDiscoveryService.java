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
package org.openhab.binding.ecobee.internal.discovery;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.binding.ecobee.internal.handler.EcobeeAccountBridgeHandler;
import org.openhab.binding.ecobee.internal.handler.EcobeeThermostatBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcobeeDiscoveryService} is responsible for discovering the Ecobee
 * thermostats that are associated with the Ecobee Account, as well as the sensors
 * are associated with the Ecobee thermostats.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(EcobeeDiscoveryService.class);

    private @NonNullByDefault({}) EcobeeAccountBridgeHandler bridgeHandler;

    private @Nullable Future<?> discoveryJob;

    public EcobeeDiscoveryService() {
        super(SUPPORTED_THERMOSTAT_AND_SENSOR_THING_TYPES_UIDS, 8, true);
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
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THERMOSTAT_AND_SENSOR_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("EcobeeDiscovery: Starting background discovery job");
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::backgroundDiscover, DISCOVERY_INITIAL_DELAY_SECONDS,
                    DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("EcobeeDiscovery: Stopping background discovery job");
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        logger.debug("EcobeeDiscovery: Starting discovery scan");
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
            logger.debug("EcobeeDiscovery: Skipping discovery because Account Bridge thing is not ONLINE");
            return;
        }
        logger.debug("EcobeeDiscovery: Discovering Ecobee devices");
        discoverThermostats();
        discoverSensors();
    }

    private synchronized void discoverThermostats() {
        logger.debug("EcobeeDiscovery: Discovering thermostats");
        for (ThermostatDTO thermostat : bridgeHandler.getRegisteredThermostats()) {
            String name = thermostat.name;
            String identifier = thermostat.identifier;
            if (identifier != null && name != null) {
                ThingUID thingUID = new ThingUID(UID_THERMOSTAT_BRIDGE, bridgeHandler.getThing().getUID(), identifier);
                thingDiscovered(createThermostatDiscoveryResult(thingUID, identifier, name));
                logger.debug("EcobeeDiscovery: Thermostat '{}' and name '{}' added with UID '{}'", identifier, name,
                        thingUID);
            }
        }
    }

    private DiscoveryResult createThermostatDiscoveryResult(ThingUID thermostatUID, String identifier, String name) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_THERMOSTAT_ID, identifier);
        return DiscoveryResultBuilder.create(thermostatUID).withProperties(properties)
                .withRepresentationProperty(CONFIG_THERMOSTAT_ID).withBridge(bridgeHandler.getThing().getUID())
                .withLabel(String.format("Ecobee Thermostat %s", name)).build();
    }

    private synchronized void discoverSensors() {
        List<Thing> thermostatThings = bridgeHandler.getThing().getThings();
        if (thermostatThings.isEmpty()) {
            logger.debug("EcobeeDiscovery: Skipping sensor discovery because there are no thermostat things");
            return;
        }
        logger.debug("EcobeeDiscovery: Discovering sensors");
        for (Thing thermostat : thermostatThings) {
            EcobeeThermostatBridgeHandler thermostatHandler = (EcobeeThermostatBridgeHandler) thermostat.getHandler();
            if (thermostatHandler != null) {
                String thermostatId = thermostatHandler.getThermostatId();
                logger.debug("EcobeeDiscovery: Discovering sensors for thermostat '{}'", thermostatId);
                for (RemoteSensorDTO sensor : thermostatHandler.getSensors()) {
                    ThingUID bridgeUID = thermostatHandler.getThing().getUID();
                    ThingUID sensorUID = new ThingUID(UID_SENSOR_THING, bridgeUID, sensor.id.replace(":", "-"));
                    thingDiscovered(createSensorDiscoveryResult(sensorUID, bridgeUID, sensor));
                    logger.debug("EcobeeDiscovery: Sensor for '{}' with id '{}' and name '{}' added with UID '{}'",
                            thermostatId, sensor.id, sensor.name, sensorUID);
                }
            }
        }
    }

    private DiscoveryResult createSensorDiscoveryResult(ThingUID sensorUID, ThingUID bridgeUID,
            RemoteSensorDTO sensor) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_SENSOR_ID, sensor.id);
        return DiscoveryResultBuilder.create(sensorUID).withProperties(properties)
                .withRepresentationProperty(CONFIG_SENSOR_ID).withBridge(bridgeUID)
                .withLabel(String.format("Ecobee Sensor %s", sensor.name)).build();
    }
}
