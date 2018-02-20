/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.nest.NestBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.config.NestDeviceConfiguration;
import org.openhab.binding.nest.internal.config.NestStructureConfiguration;
import org.openhab.binding.nest.internal.data.BaseNestDevice;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.listener.NestDeviceDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service connects to the Nest bridge and creates the correct discovery results for Nest devices
 * as they are found through the API.
 *
 * @author David Bennett - initial contribution
 * @author Wouter Born - Add representation properties
 */
public class NestDiscoveryService extends AbstractDiscoveryService implements NestDeviceDataListener {
    private final Logger logger = LoggerFactory.getLogger(NestDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(NestBindingConstants.THING_TYPE_THERMOSTAT, NestBindingConstants.THING_TYPE_SMOKE_DETECTOR,
                    THING_TYPE_STRUCTURE, NestBindingConstants.THING_TYPE_CAMERA)
            .collect(Collectors.toSet());

    private final NestBridgeHandler bridge;

    public NestDiscoveryService(NestBridgeHandler bridge) {
        super(SUPPORTED_THING_TYPES, 60, true);
        this.bridge = bridge;
    }

    public void activate() {
        bridge.addDeviceDataListener(this);
    }

    @Override
    public void deactivate() {
        bridge.removeDeviceDataListener(this);
    }

    @Override
    protected void startScan() {
        this.bridge.startDiscoveryScan();
    }

    @Override
    public void onNewNestThermostatData(Thermostat thermostat) {
        onNestDeviceAdded(thermostat, NestBindingConstants.THING_TYPE_THERMOSTAT);
        logger.debug("thingDiscovered called for thermostat");
    }

    @Override
    public void onNewNestCameraData(Camera camera) {
        onNestDeviceAdded(camera, THING_TYPE_CAMERA);
        logger.debug("thingDiscovered called for camera");
    }

    @Override
    public void onNewNestSmokeDetectorData(SmokeDetector smoke) {
        onNestDeviceAdded(smoke, THING_TYPE_SMOKE_DETECTOR);
        logger.debug("thingDiscovered called for smoke detector");
    }

    private void onNestDeviceAdded(BaseNestDevice device, ThingTypeUID typeUID) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID deviceUID = new ThingUID(typeUID, bridgeUID, device.getDeviceId());
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestDeviceConfiguration.DEVICE_ID, device.getDeviceId());
        properties.put(PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(deviceUID)
                .withThingType(typeUID)
                .withLabel(device.getNameLong())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(NestDeviceConfiguration.DEVICE_ID)
                .build()
        );
        // @formatter:on
    }

    @Override
    public void onNewNestStructureData(Structure struct) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(THING_TYPE_STRUCTURE, bridgeUID, struct.getStructureId());
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestStructureConfiguration.STRUCTURE_ID, struct.getStructureId());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(THING_TYPE_STRUCTURE)
                .withLabel(struct.getName())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(NestStructureConfiguration.STRUCTURE_ID)
                .build()
        );
        // @formatter:on
        logger.debug("thingDiscovered called for structure");
    }
}
