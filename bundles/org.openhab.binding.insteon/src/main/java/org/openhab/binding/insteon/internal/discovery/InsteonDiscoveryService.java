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
package org.openhab.binding.insteon.internal.discovery;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonDiscoveryService} is responsible for device discovery.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class InsteonDiscoveryService extends AbstractDiscoveryService {
    private static final int SCAN_TIMEOUT = 2; // in seconds

    private final Logger logger = LoggerFactory.getLogger(InsteonDiscoveryService.class);

    private InsteonBridgeHandler handler;

    public InsteonDiscoveryService(InsteonBridgeHandler handler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, SCAN_TIMEOUT, false);
        this.handler = handler;

        logger.debug("Initializing InsteonDiscoveryService");

        handler.setInsteonDiscoveryService(this);
    }

    @Override
    protected void startScan() {
        discoverMissingThings();
    }

    public void discoverMissingThings() {
        InsteonBinding binding = handler.getInsteonBinding();

        if (!binding.isModemDBComplete()) {
            logger.debug("Modem database not complete, scanning aborted.");
            return;
        }

        if (handler.isDeviceDiscoveryEnabled()) {
            Map<String, @Nullable ProductData> devices = binding.getMissingDevices();
            if (!devices.isEmpty()) {
                devices.forEach((address, productData) -> addInsteonDevice(address, productData));
            } else {
                logger.debug("no missing Insteon device found.");
            }
        } else {
            logger.debug("device discovery is disabled, no missing device will be discovered.");
        }

        if (handler.isSceneDiscoveryEnabled()) {
            List<Integer> scenes = binding.getMissingScenes();
            if (!scenes.isEmpty()) {
                scenes.forEach(group -> addInsteonScene(group));
            } else {
                logger.debug("no missing Insteon scene found.");
            }
        } else {
            logger.debug("scene discovery is disabled, no missing scene will be discovered.");
        }
    }

    public void addInsteonDevice(String address, @Nullable ProductData productData) {
        ThingUID bridgeUID = handler.getThing().getUID();
        String id = address.replace(".", "");
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, id);
        String label = productData != null && productData.getLabel() != null ? productData.getLabel()
                : "Insteon Device " + address;
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_DEVICE_ADDRESS, address);

        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(PROPERTY_DEVICE_ADDRESS).build());

        if (logger.isDebugEnabled()) {
            logger.debug("added Insteon device {} to inbox", address);
        }
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

        if (logger.isDebugEnabled()) {
            logger.debug("added Insteon scene {} to inbox", group);
        }
    }
}
