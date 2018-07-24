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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.config.NestDeviceConfiguration;
import org.openhab.binding.nest.internal.config.NestStructureConfiguration;
import org.openhab.binding.nest.internal.data.BaseNestDevice;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.listener.NestThingDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service connects to the Nest bridge and creates the correct discovery results for Nest devices
 * as they are found through the API.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add representation properties
 */
@NonNullByDefault
public class NestDiscoveryService extends AbstractDiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(THING_TYPE_CAMERA, THING_TYPE_THERMOSTAT, THING_TYPE_SMOKE_DETECTOR, THING_TYPE_STRUCTURE)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(NestDiscoveryService.class);

    private final DiscoveryDataListener<Camera> cameraDiscoveryDataListener = new DiscoveryDataListener<>(Camera.class,
            THING_TYPE_CAMERA, this::addDeviceDiscoveryResult);
    private final DiscoveryDataListener<SmokeDetector> smokeDetectorDiscoveryDataListener = new DiscoveryDataListener<>(
            SmokeDetector.class, THING_TYPE_SMOKE_DETECTOR, this::addDeviceDiscoveryResult);
    private final DiscoveryDataListener<Structure> structureDiscoveryDataListener = new DiscoveryDataListener<>(
            Structure.class, THING_TYPE_STRUCTURE, this::addStructureDiscoveryResult);
    private final DiscoveryDataListener<Thermostat> thermostatDiscoveryDataListener = new DiscoveryDataListener<>(
            Thermostat.class, THING_TYPE_THERMOSTAT, this::addDeviceDiscoveryResult);

    private final List<DiscoveryDataListener> discoveryDataListeners = Stream.of(cameraDiscoveryDataListener,
            smokeDetectorDiscoveryDataListener, structureDiscoveryDataListener, thermostatDiscoveryDataListener)
            .collect(Collectors.toList());

    private final NestBridgeHandler bridge;

    private static class DiscoveryDataListener<T> implements NestThingDataListener<T> {
        private Class<T> dataClass;
        private ThingTypeUID thingTypeUID;
        private BiConsumer<T, ThingTypeUID> onDiscovered;

        private DiscoveryDataListener(Class<T> dataClass, ThingTypeUID thingTypeUID,
                BiConsumer<T, ThingTypeUID> onDiscovered) {
            this.dataClass = dataClass;
            this.thingTypeUID = thingTypeUID;
            this.onDiscovered = onDiscovered;
        }

        @Override
        public void onNewData(T data) {
            onDiscovered.accept(data, thingTypeUID);
        }

        @Override
        public void onUpdatedData(T oldData, T data) {
        }

        @Override
        public void onMissingData(String nestId) {
        }
    };

    public NestDiscoveryService(NestBridgeHandler bridge) {
        super(SUPPORTED_THING_TYPES, 60, true);
        this.bridge = bridge;
    }

    @SuppressWarnings("unchecked")
    public void activate() {
        discoveryDataListeners.forEach(l -> bridge.addThingDataListener(l.dataClass, l));
        addDiscoveryResultsFromLastUpdates();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deactivate() {
        discoveryDataListeners.forEach(l -> bridge.removeThingDataListener(l.dataClass, l));
    }

    @Override
    protected void startScan() {
        addDiscoveryResultsFromLastUpdates();
    }

    @SuppressWarnings("unchecked")
    private void addDiscoveryResultsFromLastUpdates() {
        discoveryDataListeners
                .forEach(l -> addDiscoveryResultsFromLastUpdates(l.dataClass, l.thingTypeUID, l.onDiscovered));
    }

    private <T> void addDiscoveryResultsFromLastUpdates(Class<T> dataClass, ThingTypeUID thingTypeUID,
            BiConsumer<T, ThingTypeUID> onDiscovered) {
        List<T> lastUpdates = bridge.getLastUpdates(dataClass);
        lastUpdates.forEach(lastUpdate -> onDiscovered.accept(lastUpdate, thingTypeUID));
    }

    private void addDeviceDiscoveryResult(BaseNestDevice device, ThingTypeUID typeUID) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, device.getDeviceId());
        logger.debug("Discovered {}", thingUID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestDeviceConfiguration.DEVICE_ID, device.getDeviceId());
        properties.put(PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(typeUID)
                .withLabel(device.getNameLong())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(NestDeviceConfiguration.DEVICE_ID)
                .build()
        );
        // @formatter:on
    }

    public void addStructureDiscoveryResult(Structure structure, ThingTypeUID typeUID) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, structure.getStructureId());
        logger.debug("Discovered {}", thingUID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(NestStructureConfiguration.STRUCTURE_ID, structure.getStructureId());
        // @formatter:off
        thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                .withThingType(THING_TYPE_STRUCTURE)
                .withLabel(structure.getName())
                .withBridge(bridgeUID)
                .withProperties(properties)
                .withRepresentationProperty(NestStructureConfiguration.STRUCTURE_ID)
                .build()
        );
        // @formatter:on
    }
}
