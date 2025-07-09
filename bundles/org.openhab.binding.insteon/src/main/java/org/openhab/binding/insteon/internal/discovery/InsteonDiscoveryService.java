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
package org.openhab.binding.insteon.internal.discovery;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonDiscoveryService} is responsible for insteon devices & scenes discovery.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonDiscoveryService extends AbstractDiscoveryService {
    private static final int SCAN_TIMEOUT = 2; // in seconds

    private final Logger logger = LoggerFactory.getLogger(InsteonDiscoveryService.class);

    private final InsteonBridgeHandler handler;

    public InsteonDiscoveryService(InsteonBridgeHandler handler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, SCAN_TIMEOUT, false);
        this.handler = handler;

        logger.debug("initializing discovery service for bridge {}", handler.getThing().getUID());

        handler.setDiscoveryService(this);
    }

    @Override
    protected void startScan() {
        logger.debug("starting manual scan on bridge {}", handler.getThing().getUID());

        discoverMissingThings();
    }

    public void discoverInsteonDevice(InsteonAddress address, @Nullable ProductData productData) {
        InsteonModem modem = handler.getModem();
        if (modem != null && modem.getDB().hasEntry(address) && !modem.hasDevice(address)) {
            addInsteonDevice(address, productData);
        } else {
            removeInsteonDevice(address);
        }
    }

    public void discoverInsteonScene(int group) {
        InsteonModem modem = handler.getModem();
        if (modem != null && modem.getDB().hasBroadcastGroup(group) && !modem.hasScene(group)) {
            addInsteonScene(group);
        } else {
            removeInsteonScene(group);
        }
    }

    public void discoverMissingThings() {
        InsteonModem modem = handler.getModem();
        if (modem == null) {
            logger.debug("modem not initialized, scanning aborted.");
        } else if (!modem.getDB().isComplete()) {
            logger.debug("modem database not complete, scanning aborted.");
        } else {
            Instant startTime = Instant.now();

            if (handler.isDeviceDiscoveryEnabled()) {
                modem.getDB().getDevices().stream().filter(address -> !modem.hasDevice(address)).forEach(address -> {
                    logger.debug("device {} in the modem database, but not configured", address);
                    addInsteonDevice(address, handler.getProductData(address));
                });
            } else {
                logger.debug("device discovery is disabled, no device will be discovered.");
            }

            if (handler.isSceneDiscoveryEnabled()) {
                modem.getDB().getBroadcastGroups().stream().filter(group -> !modem.hasScene(group)).forEach(group -> {
                    logger.debug("scene {} in the modem database, but not configured", group);
                    addInsteonScene(group);
                });
            } else {
                logger.debug("scene discovery is disabled, no scene will be discovered.");
            }

            removeOlderResults(startTime, handler.getThing().getUID());
        }
    }

    private void addInsteonDevice(InsteonAddress address, @Nullable ProductData productData) {
        ThingUID bridgeUID = handler.getThing().getUID();
        String id = address.toString().replace(".", "").toLowerCase();
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, id);
        String label = Optional.ofNullable(productData).map(ProductData::getLabel).orElse("Insteon Device " + address);
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_DEVICE_ADDRESS, address.toString());

        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(PROPERTY_DEVICE_ADDRESS).build());

        logger.debug("added Insteon device {} to inbox", address);
    }

    private void addInsteonScene(int group) {
        ThingUID bridgeUID = handler.getThing().getUID();
        String id = Integer.toString(group);
        ThingUID thingUID = new ThingUID(THING_TYPE_SCENE, bridgeUID, id);
        String label = "Insteon Scene " + group;
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_SCENE_GROUP, group);

        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(PROPERTY_SCENE_GROUP).build());

        logger.debug("added Insteon scene {} to inbox", group);
    }

    private void removeInsteonDevice(InsteonAddress address) {
        ThingUID bridgeUID = handler.getThing().getUID();
        String id = address.toString().replace(".", "").toLowerCase();
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, id);

        thingRemoved(thingUID);
    }

    private void removeInsteonScene(int group) {
        ThingUID bridgeUID = handler.getThing().getUID();
        String id = Integer.toString(group);
        ThingUID thingUID = new ThingUID(THING_TYPE_SCENE, bridgeUID, id);

        thingRemoved(thingUID);
    }
}
