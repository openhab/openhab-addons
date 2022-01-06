/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal.discovery;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwDiscoveryService} implements the discovery service for the OneWire binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(OwDiscoveryService.class);

    private final OwserverBridgeHandler owBridgeHandler;
    private final ThingUID bridgeUID;

    Map<SensorId, OwDiscoveryItem> owDiscoveryItems = new HashMap<>();
    Set<SensorId> associatedSensors = new HashSet<>();

    public OwDiscoveryService(OwserverBridgeHandler owBridgeHandler) {
        super(SUPPORTED_THING_TYPES, 60, false);
        this.owBridgeHandler = owBridgeHandler;
        this.bridgeUID = owBridgeHandler.getThing().getUID();
        logger.debug("registering discovery service for {}", owBridgeHandler);
    }

    private void scanDirectory(String baseDirectory) {
        List<SensorId> directoryList;

        logger.trace("scanning {} on bridge {}", baseDirectory, bridgeUID);
        try {
            directoryList = owBridgeHandler.getDirectory(baseDirectory);
        } catch (OwException e) {
            logger.info("empty directory '{}' for {}", baseDirectory, bridgeUID);
            return;
        }

        // find all valid sensors
        for (SensorId directoryEntry : directoryList) {
            try {
                OwDiscoveryItem owDiscoveryItem = new OwDiscoveryItem(owBridgeHandler, directoryEntry);
                if (owDiscoveryItem.getSensorType() == OwSensorType.DS2409) {
                    // scan hub sub-directories
                    logger.trace("found hub {}, scanning sub-directories", directoryEntry);

                    scanDirectory(owDiscoveryItem.getSensorId().getFullPath() + "/main/");
                    scanDirectory(owDiscoveryItem.getSensorId().getFullPath() + "/aux/");
                } else {
                    // add found sensor to list
                    logger.trace("found sensor {} (type: {})", directoryEntry, owDiscoveryItem.getSensorType());

                    owDiscoveryItems.put(owDiscoveryItem.getSensorId(), owDiscoveryItem);
                    associatedSensors.addAll(owDiscoveryItem.getAssociatedSensorIds());
                }
            } catch (OwException e) {
                logger.debug("error while scanning for sensors in directory {} on bridge {}: {}", baseDirectory,
                        bridgeUID, e.getMessage());
            }
        }
    }

    @Override
    public void startScan() {
        scanDirectory("/");

        // remove duplicates
        owDiscoveryItems.entrySet().removeIf(s -> associatedSensors.contains(s.getKey()));

        // make discovery results
        for (OwDiscoveryItem owDiscoveryItem : owDiscoveryItems.values()) {
            owDiscoveryItem.checkSensorType();
            try {
                ThingTypeUID thingTypeUID = owDiscoveryItem.getThingTypeUID();

                String normalizedId = owDiscoveryItem.getNormalizedSensorId();
                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, normalizedId);
                logger.debug("created thing UID {} for sensor {}, type {}", thingUID, owDiscoveryItem.getSensorId(),
                        owDiscoveryItem.getSensorType());

                Map<String, Object> properties = new HashMap<>();
                properties.put(PROPERTY_MODELID, owDiscoveryItem.getSensorType().toString());
                properties.put(PROPERTY_VENDOR, owDiscoveryItem.getVendor());
                properties.put(CONFIG_ID, owDiscoveryItem.getSensorId().getFullPath());

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                        .withProperties(properties).withBridge(bridgeUID).withLabel(owDiscoveryItem.getLabel()).build();

                thingDiscovered(discoveryResult);
            } catch (OwException e) {
                logger.info("sensor-id {}: {}", owDiscoveryItem.getSensorId(), e.getMessage());
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }
}
