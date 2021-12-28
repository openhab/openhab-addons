/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.sensorpush.internal;

import static org.openhab.binding.sensorpush.internal.SensorPushBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensorpush.internal.handler.CloudBridgeHandler;
import org.openhab.binding.sensorpush.internal.protocol.Sensor;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensorPushDiscoveryService} handles discovery of sensors as they are identified by the bridge handler.
 * Requests from the framework to startScan() will initiate a call to the bridge handler's pollSensors() method.
 * Otherwise the bridge handler will poll for sensors every other poll interval.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class SensorPushDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SensorPushDiscoveryService.class);

    private @NonNullByDefault({}) CloudBridgeHandler bridgeHandler;
    private final Set<String> discoveredSensorSet = new HashSet<>();

    public SensorPushDiscoveryService() {
        super(DISCOVERABLE_DEVICE_TYPE_UIDS, 0, false);
    }

    @Override
    protected void startScan() {
        logger.trace("Starting discovery scan");
        discoveredSensorSet.clear();
        if (bridgeHandler != null) {
            bridgeHandler.pollSensors();
        }
    }

    public void processSensor(Sensor sensor) {
        Boolean active = sensor.active;
        String deviceId = sensor.deviceId;
        String name = sensor.name;
        if (deviceId != null && name != null && active != null) {
            if (!discoveredSensorSet.contains(deviceId) && active) {
                notifyDiscovery(deviceId, name);
                discoveredSensorSet.add(deviceId);
            }
        } else {
            logger.debug("Processing sensor with unexpected null values. Ignoring.");
        }
    }

    private void notifyDiscovery(String idString, String label) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(THING_TYPE_SENSOR, bridgeUID, idString.replace(".", ""));

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_ID, idString);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(properties)
                .withLabel(label).withRepresentationProperty(PROPERTY_ID).build();
        thingDiscovered(result);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof CloudBridgeHandler) {
            bridgeHandler = (CloudBridgeHandler) handler;
            bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
