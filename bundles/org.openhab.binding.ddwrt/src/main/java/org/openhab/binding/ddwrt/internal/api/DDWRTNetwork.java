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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.openhab.binding.ddwrt.internal.DDWRTNetworkConfiguration;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private volatile @Nullable ThingUID bridgeUID;

    // Devices indexed by MAC for fast upsert
    private final Map<String, DDWRTDevice> devicesByMac = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(DDWRTNetwork.class);

    /** Hand the entire bridge configuration to the network. */
    public void setConfig(DDWRTNetworkConfiguration netCfg) {
        if (!Objects.equals(this.config, netCfg)) {
            logger.info("Config changed.");
            this.config = netCfg;

            final DDWRTDeviceConfiguration devCfg = new DDWRTDeviceConfiguration();
            devCfg.port = netCfg.port;
            devCfg.user = netCfg.user;
            devCfg.password = netCfg.password;
            devCfg.refreshInterval = netCfg.refreshInterval;

            // Parse hostnames from config

            final List<String> hosts = Arrays.stream(netCfg.hostnames.split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).toList();

            hosts.forEach(host -> {
                try {
                    logger.debug("discovering device: {}", host);

                    devCfg.hostname = host;

                    DDWRTDevice.upsertDeviceInNetwork(this, devCfg);

                } catch (Exception e) {
                    logger.debug("SSH to host {} failed: {}", host, e.getMessage(), e);
                }
            });
        }
    }

    public void setBridgeUID(ThingUID thingUID) {
        this.bridgeUID = thingUID;
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

    public void refresh() {
        synchronized (this) {
            logger.debug("Refreshing");
            getDevices().forEach(device -> {
                device.refresh();
            });
        }
    }
}
