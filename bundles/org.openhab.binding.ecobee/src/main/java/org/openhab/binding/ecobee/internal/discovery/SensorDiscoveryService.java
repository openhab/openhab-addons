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
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
import org.openhab.binding.ecobee.internal.handler.EcobeeThermostatBridgeHandler;
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
 * The {@link SensorDiscoveryService} is responsible for discovering the Ecobee
 * sensors that are assigned to a thermostat.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SensorDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SensorDiscoveryService.class);

    private @NonNullByDefault({}) EcobeeThermostatBridgeHandler bridgeHandler;

    private @Nullable Future<?> sensorDiscoveryJob;

    public SensorDiscoveryService() {
        super(SUPPORTED_SENSOR_THING_TYPES_UIDS, 30, true);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EcobeeThermostatBridgeHandler) {
            this.bridgeHandler = (EcobeeThermostatBridgeHandler) handler;
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
        return SUPPORTED_SENSOR_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("SensorDiscovery: Starting sensor background discovery job");
        Future<?> localSensorDiscoveryJob = sensorDiscoveryJob;
        if (localSensorDiscoveryJob == null || localSensorDiscoveryJob.isCancelled()) {
            // TODO
            sensorDiscoveryJob = scheduler.scheduleWithFixedDelay(this::discoverSensors,
                    DISCOVERY_INITIAL_DELAY_SECONDS, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("SensorDiscovery: Stopping sensor background discovery job");
        Future<?> localSensorDiscoveryJob = sensorDiscoveryJob;
        if (localSensorDiscoveryJob != null) {
            localSensorDiscoveryJob.cancel(true);
            sensorDiscoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        logger.debug("SensorDiscovery: Starting sensor discovery scan for tid {}", bridgeHandler.getThermostatId());
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
        logger.debug("SensorDiscovery: Discovering sensors for '{}'", bridgeHandler.getThermostatId());
        if (!bridgeHandler.isBackgroundDiscoveryEnabled()) {
            return;
        }
        for (RemoteSensorDTO sensor : bridgeHandler.getSensors()) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            ThingUID sensorUID = new ThingUID(UID_SENSOR_THING, bridgeUID, sensor.id.replace(":", "-"));
            thingDiscovered(createDiscoveryResult(sensorUID, bridgeUID, sensor));
            logger.debug("SensorDiscovery: Sensor for '{}' with id '{}' and name '{}' added with UID '{}'",
                    bridgeHandler.getThermostatId(), sensor.id, sensor.name, sensorUID);
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
