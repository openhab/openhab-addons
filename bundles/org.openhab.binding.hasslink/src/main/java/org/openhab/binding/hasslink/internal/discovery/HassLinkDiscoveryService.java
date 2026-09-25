/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.discovery;

import static org.openhab.binding.hasslink.internal.HassLinkBindingConstants.THING_TYPE_DEVICE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.config.HassLinkBridgeConfiguration;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.binding.hasslink.internal.registry.DeviceRegistryEntry;
import org.openhab.binding.hasslink.internal.registry.EntityRegistryEntry;
import org.openhab.binding.hasslink.internal.registry.HomeAssistantRegistry;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.binding.hasslink.internal.util.HassLinkEntityFilter;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes device Things from Home Assistant registries, evaluating bridge-level area, label,
 * and domain filtering parameters via {@link HassLinkEntityFilter}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HassLinkDiscoveryService.class);

    private volatile @Nullable HassLinkBridgeHandler bridgeHandler;

    private final Set<ThingUID> lastDiscoveredThingUIDs = new HashSet<>();

    public HassLinkDiscoveryService() {
        super(Set.of(THING_TYPE_DEVICE), 10);
    }

    public void setBridgeHandler(HassLinkBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        HassLinkBridgeHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler == null) {
            logger.warn("Cannot start discovery scan: bridge handler is not set");
            return;
        }

        localBridgeHandler.sendRegistryListRequest();
    }

    public void onRegistryUpdated() {
        publishDiscoveryResults();
    }

    public synchronized void removeDiscoveredEntities() {
        for (ThingUID uid : lastDiscoveredThingUIDs) {
            thingRemoved(uid);
        }
        lastDiscoveredThingUIDs.clear();
    }

    /**
     * Iterates through the Home Assistant device and entity registries to discover and publish
     * candidate Things to the openHAB inbox according to bridge-level configuration filters.
     */
    private synchronized void publishDiscoveryResults() {
        HassLinkBridgeHandler localBridgeHandler = this.bridgeHandler;
        if (localBridgeHandler == null) {
            return;
        }

        HomeAssistantRegistry registry = localBridgeHandler.getRegistry();
        List<DeviceRegistryEntry> devices = registry.getDevices();
        List<EntityRegistryEntry> entities = registry.getEntities();

        if (devices.isEmpty() && entities.isEmpty()) {
            return;
        }

        HassLinkBridgeConfiguration bridgeConfig = localBridgeHandler.getBridgeConfiguration();
        ThingUID bridgeUID = localBridgeHandler.getThing().getUID();
        Set<ThingUID> currentScanThingUIDs = new HashSet<>();

        // 1. Process Hardware / Logical Devices
        for (DeviceRegistryEntry device : devices) {
            logger.debug("Processing device registry entry: {}", device);
            if (!device.isPhysicalHardware()) {
                continue;
            }
            String deviceId = device.id();

            if (!HassLinkEntityFilter.isDeviceAllowedByBridge(device, registry, bridgeConfig)) {
                logger.debug("Device {} is excluded by bridge configuration filters. BridgeConfig: {}", deviceId,
                        bridgeConfig);
                continue;
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put("deviceId", deviceId);
            addIfPresent(properties, Thing.PROPERTY_VENDOR, device.manufacturer());
            addIfPresent(properties, Thing.PROPERTY_MODEL_ID, device.model());
            addIfPresent(properties, Thing.PROPERTY_FIRMWARE_VERSION, device.swVersion());

            String thingId = EntityUtils.sanitize(deviceId);
            ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, thingId);
            currentScanThingUIDs.add(thingUID);

            String label = Objects.requireNonNullElse(device.nameByUser(),
                    Objects.requireNonNullElse(device.name(), deviceId));
            thingDiscovered(DiscoveryResultBuilder.create(thingUID) //
                    .withBridge(bridgeUID) //
                    .withProperties(properties) //
                    .withLabel(label) //
                    .withRepresentationProperty("deviceId") //
                    .build());
        }

        // 2. Process Independent / Standalone Entities
        if (!bridgeConfig.ignoreIndependentEntities) {
            for (EntityRegistryEntry entity : entities) {
                String deviceId = entity.deviceId();
                if (deviceId != null && !deviceId.isBlank()) {
                    continue; // Belongs to a device, handled in step 1
                }

                if (entity.isDisabled()) {
                    continue;
                }

                if (!HassLinkEntityFilter.isAllowedByBridge(entity, false, bridgeConfig)) {
                    continue;
                }

                String entityId = entity.entityId();
                Map<String, Object> properties = new HashMap<>();
                properties.put("entityIds", List.of(entityId));

                String thingId = EntityUtils.sanitize(entityId);
                ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, bridgeUID, thingId);
                currentScanThingUIDs.add(thingUID);

                String label = Objects.requireNonNullElse(entity.name(),
                        Objects.requireNonNullElse(entity.originalName(), entityId));
                thingDiscovered(DiscoveryResultBuilder.create(thingUID) //
                        .withBridge(bridgeUID) //
                        .withProperties(properties) //
                        .withLabel(label) //
                        .withRepresentationProperty("entityIds") //
                        .build());
            }
        }

        // Remove stale Things no longer matching current scan
        Set<ThingUID> removedThingUIDs = new HashSet<>(lastDiscoveredThingUIDs);
        removedThingUIDs.removeAll(currentScanThingUIDs);
        for (ThingUID removedUID : removedThingUIDs) {
            thingRemoved(removedUID);
        }

        lastDiscoveredThingUIDs.clear();
        lastDiscoveredThingUIDs.addAll(currentScanThingUIDs);
    }

    private static void addIfPresent(Map<String, Object> properties, String key, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value);
        }
    }
}
