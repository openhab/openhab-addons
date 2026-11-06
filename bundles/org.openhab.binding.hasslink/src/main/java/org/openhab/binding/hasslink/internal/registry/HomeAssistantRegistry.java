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
package org.openhab.binding.hasslink.internal.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Stores, indexes, and maintains state for Home Assistant device and entity registry snapshots.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantRegistry {

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final List<JsonObject> rawDeviceSnapshot = new ArrayList<>();
    private final List<JsonObject> rawEntitySnapshot = new ArrayList<>();

    private final List<DeviceRegistryEntry> devices = new CopyOnWriteArrayList<>();
    private final List<EntityRegistryEntry> entities = new CopyOnWriteArrayList<>();
    private final Map<String, EntityRegistryEntry> entityById = new HashMap<>();
    private final Map<String, Set<String>> deviceToEntitiesMap = new ConcurrentHashMap<>();

    private boolean deviceSnapshotLoaded = false;
    private boolean entitySnapshotLoaded = false;

    /**
     * Updates the device snapshot with raw JSON objects and updates indexes.
     */
    public synchronized void updateDeviceSnapshot(List<JsonObject> deviceJsonList) {
        this.rawDeviceSnapshot.clear();
        this.rawDeviceSnapshot.addAll(deviceJsonList);
        this.deviceSnapshotLoaded = true;
        rebuildIndex();
    }

    /**
     * Updates the entity snapshot with raw JSON objects and updates indexes.
     */
    public synchronized void updateEntitySnapshot(List<JsonObject> entityJsonList) {
        this.rawEntitySnapshot.clear();
        this.rawEntitySnapshot.addAll(entityJsonList);
        this.entitySnapshotLoaded = true;
        rebuildIndex();
    }

    /**
     * Resets all cached snapshots and index mappings.
     */
    public synchronized void clear() {
        this.rawDeviceSnapshot.clear();
        this.rawEntitySnapshot.clear();
        this.deviceSnapshotLoaded = false;
        this.entitySnapshotLoaded = false;
        rebuildIndex();
    }

    /**
     * Checks whether both the device and entity registry snapshots have been received.
     */
    public synchronized boolean isLoaded() {
        return deviceSnapshotLoaded && entitySnapshotLoaded;
    }

    public synchronized List<DeviceRegistryEntry> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    public synchronized List<EntityRegistryEntry> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public synchronized Optional<EntityRegistryEntry> getEntity(String entityId) {
        return Optional.ofNullable(entityById.get(entityId));
    }

    public Set<String> getEntityIdsForDevice(String deviceId) {
        Set<String> entityIds = deviceToEntitiesMap.get(deviceId);
        return entityIds != null ? Collections.unmodifiableSet(entityIds) : Set.of();
    }

    private void rebuildIndex() {
        devices.clear();
        entities.clear();
        entityById.clear();
        deviceToEntitiesMap.clear();

        for (JsonObject json : rawDeviceSnapshot) {
            DeviceRegistryEntry entry = gson.fromJson(json, DeviceRegistryEntry.class);
            if (entry != null) {
                devices.add(entry);
            }
        }

        for (JsonObject json : rawEntitySnapshot) {
            EntityRegistryEntry entry = gson.fromJson(json, EntityRegistryEntry.class);
            if (entry != null) {
                entities.add(entry);
                entityById.put(entry.entityId(), entry);

                String deviceId = entry.deviceId();
                if (deviceId != null && !deviceId.isBlank()) {
                    Objects.requireNonNull(deviceToEntitiesMap //
                            .computeIfAbsent(deviceId, k -> ConcurrentHashMap.newKeySet())) //
                            .add(entry.entityId());
                }
            }
        }
    }
}
