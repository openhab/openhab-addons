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
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
import org.openhab.binding.ecobee.internal.handler.EcobeeThermostatBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensorDiscoveryService} is responsible for discovering the Ecobee
 * sensors that are assigned to a thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SensorDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SensorDiscoveryService.class);

    private @NonNullByDefault({}) EcobeeThermostatBridgeHandler bridgeHandler;

    public SensorDiscoveryService() {
        super(30);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EcobeeThermostatBridgeHandler) {
            ((EcobeeThermostatBridgeHandler) handler).setDiscoveryService(this);
            bridgeHandler = (EcobeeThermostatBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        logger.debug("SensorDiscovery: Activating Ecobee sensor discovery service");
    }

    @Override
    public void deactivate() {
        logger.debug("SensorDiscovery: Deactivating Ecobee sensor discovery service");
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_SENSOR_THING_TYPES_UIDS;
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.debug("SensorDiscovery: Performing background discovery scan for {}", bridgeHandler.getThing().getUID());
        discoverSensors();
    }

    @Override
    public void startScan() {
        logger.debug("SensorDiscovery: Starting discovery scan for {}", bridgeHandler.getThing().getUID());
        discoverSensors();
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
        return String.format("Ecobee Sensor %s", name);
    }

    private synchronized void discoverSensors() {
        for (RemoteSensorDTO sensor : bridgeHandler.getSensors()) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingUID sensorUID = new ThingUID(UID_SENSOR_THING, bridgeUID, sensor.id.replace(":", "-"));
            thingDiscovered(createDiscoveryResult(sensorUID, bridgeUID, sensor));
            logger.trace("SensorDiscovery: Sensor with id '{}' and name '{}' added to Inbox with UID '{}'", sensor.id,
                    sensor.name, sensorUID);
        }
    }

    private DiscoveryResult createDiscoveryResult(ThingUID sensorUID, ThingUID bridgeUID, RemoteSensorDTO sensor) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(CONFIG_SENSOR_ID, sensor.id);
        return DiscoveryResultBuilder.create(sensorUID).withProperties(properties)
                .withRepresentationProperty(CONFIG_SENSOR_ID).withBridge(bridgeUID).withLabel(buildLabel(sensor.name))
                .build();
    }
}
