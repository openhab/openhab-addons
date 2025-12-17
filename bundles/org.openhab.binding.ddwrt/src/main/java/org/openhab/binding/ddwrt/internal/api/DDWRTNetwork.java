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
package org.openhab.binding.ddwrt.internal.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTNetworkConfiguration;

/**
 * Represents the DD-WRT network known to the bridge.
 * Holds the configured hosts and the discovered devices.
 * 
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTNetwork {

    // Bridge-level config; may be null until the bridge handler sets it
    private volatile @Nullable DDWRTNetworkConfiguration config;

    // Devices indexed by MAC for fast upsert
    private final Map<String, DDWRTDevice> devicesByMac = new ConcurrentHashMap<>();

    /** Hand the entire bridge configuration to the network. */
    public void setConfig(DDWRTNetworkConfiguration config) {
        this.config = config;
    }

    /** Return the current configuration (nullable until set). */
    public @Nullable DDWRTNetworkConfiguration getConfig() {
        return config;
    }

    /** Get a snapshot list of devices (never null). */
    public List<DDWRTDevice> getDevices() {
        return Collections.unmodifiableList(new ArrayList<>(devicesByMac.values()));
    }

    /**
     * Lookup a device by MAC (normalized, case-insensitive).
     * Returns null if the device is not present.
     */
    public @Nullable DDWRTDevice getDeviceByMac(String mac) {
        return devicesByMac.get(normalizeMac(mac));
    }

    /**
     * Upsert a device by MAC. If a device already exists for this MAC, it is returned;
     * otherwise the provided device is inserted and returned. Never returns null.
     */
    public DDWRTDevice upsertDeviceByMac(String mac, DDWRTDevice device) {
        final String key = normalizeMac(mac);
        final @Nullable DDWRTDevice existing = devicesByMac.putIfAbsent(key, device);
        return existing != null ? existing : device;
    }

    /** Clear all currently known devices. */
    public void clearDevices() {
        devicesByMac.clear();
    }

    /** Normalize MAC addresses to lower-case without whitespace. */
    private static String normalizeMac(String mac) {
        return mac.trim().toLowerCase();
    }
}
