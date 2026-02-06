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

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    // Failed device configurations by hostname for retry during refresh
    private final Map<String, DDWRTDeviceConfiguration> failedDeviceConfigs = new ConcurrentHashMap<>();

    // Cache for IP->device mappings to avoid repeated DNS lookups
    private final Map<String, DDWRTDevice> devicesByIP = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(DDWRTNetwork.class);

    /** Hand the entire bridge configuration to the network. */
    public void setConfig(DDWRTNetworkConfiguration netCfg) {
        if (!Objects.equals(this.config, netCfg)) {
            logger.info("Config changed.");
            this.config = netCfg;

            // Clear failed device configs when configuration changes
            failedDeviceConfigs.clear();

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
     * Lookup a device by hostname or device name.
     * Returns null if the device is not present.
     */
    public @Nullable DDWRTDevice getDeviceByHostname(String hostname) {
        try {
            String ip = resolveHostnameToIP(hostname);
            int port = extractPortFromHostname(hostname);
            DDWRTDevice device = getDeviceByIPandPort(ip, port);
            if (device != null) {
                logger.debug("Found device by hostname {} -> IP: {} -> MAC: {}", hostname, ip, device.getMac());
            }
            return device;
        } catch (Exception e) {
            logger.debug("Failed to resolve hostname {}: {}", hostname, e.getMessage());
            return null;
        }
    }

    /**
     * Lookup a device by IP address and port.
     * Returns null if the device is not present.
     */
    public @Nullable DDWRTDevice getDeviceByIPandPort(String ip, int port) {
        String key = ip + ":" + port;

        // First try the cache
        DDWRTDevice cached = devicesByIP.get(key);
        if (cached != null) {
            return cached;
        }

        // If not in cache, search through devices and update cache
        return devicesByMac.values().stream().filter(dev -> {
            try {
                String deviceIP = resolveHostnameToIP(dev.getConfig().hostname);
                int devicePort = dev.getConfig().port;
                if (ip.equals(deviceIP) && port == devicePort) {
                    // Update cache for future lookups
                    devicesByIP.put(key, dev);
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }).findFirst().orElse(null);
    }

    /**
     * Resolve hostname to IP address. Handles both hostnames and IP addresses.
     */
    private String resolveHostnameToIP(String hostname) throws UnknownHostException {
        if (hostname == null || hostname.trim().isEmpty()) {
            throw new UnknownHostException("Empty hostname");
        }

        // Extract hostname part if port is included (e.g., "192.168.1.1:8080")
        String hostPart = hostname.split(":")[0];

        // If it's already an IP address, return as-is
        if (hostPart.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
            return hostPart;
        }

        // Resolve hostname to IP
        InetAddress address = InetAddress.getByName(hostPart);
        return address.getHostAddress();
    }

    /**
     * Extract port from hostname string. Returns default SSH port (22) if not specified.
     */
    private int extractPortFromHostname(String hostname) {
        if (hostname == null || hostname.trim().isEmpty()) {
            return 22;
        }

        String[] parts = hostname.split(":");
        if (parts.length > 1) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                logger.debug("Invalid port in hostname '{}', using default 22", hostname);
                return 22;
            }
        }
        return 22;
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
        devicesByIP.clear();
    }

    /** Add a failed device configuration for retry during refresh. */
    public void addFailedDeviceConfig(String hostname, DDWRTDeviceConfiguration config) {
        DDWRTDeviceConfiguration existing = failedDeviceConfigs.get(hostname);
        if (existing == null || !Objects.equals(existing.password, config.password)
                || !Objects.equals(existing.user, config.user) || existing.port != config.port) {
            failedDeviceConfigs.put(hostname, config);
            logger.debug("Added/updated failed device config for hostname: {} (credentials changed: {})", hostname,
                    existing != null);
        }
    }

    /**
     * Handle manual device addition or update. This method is called when a user manually
     * adds a device that may already exist in the network (discovered or failed).
     * Returns the device if successful, null if failed.
     */
    public @Nullable DDWRTDevice addOrUpdateDevice(DDWRTDeviceConfiguration config) {
        logger.debug("Manual add/update device for hostname: {}", config.hostname);

        try {
            DDWRTDevice device = DDWRTDevice.upsertDeviceInNetwork(this, config);

            // If device was successfully added to network (has MAC), remove from failed configs
            if (!device.getMac().isEmpty()) {
                failedDeviceConfigs.remove(config.hostname);
                // Update IP cache
                updateIPCache(device);
                logger.info("Successfully connected to manually added device: {} (MAC: {})", config.hostname,
                        device.getMac());
                return device;
            } else {
                // Device failed to connect, will be tracked in upsertDeviceInNetwork
                return null;
            }
        } catch (Exception e) {
            logger.warn("Manual device addition failed for {}: {}", config.hostname, e.getMessage(), e);
            // Track the failed configuration for retry during refresh
            addFailedDeviceConfig(config.hostname, config);
            return null;
        }
    }

    /** Update the IP cache when a device is added or updated */
    private void updateIPCache(DDWRTDevice device) {
        try {
            String ip = resolveHostnameToIP(device.getConfig().hostname);
            int port = device.getConfig().port;
            String key = ip + ":" + port;
            devicesByIP.put(key, device);
            logger.debug("Updated IP cache: {} -> MAC: {}", key, device.getMac());
        } catch (Exception e) {
            logger.debug("Failed to update IP cache for device {}: {}", device.getConfig().hostname, e.getMessage());
        }
    }

    /** Normalize MAC addresses to lower-case without whitespace. */
    private static String normalizeMac(String mac) {
        return mac.trim().toLowerCase();
    }

    public void refresh() {
        synchronized (this) {
            logger.debug("Refreshing");

            // Refresh existing devices
            getDevices().forEach(device -> {
                device.refresh();
            });

            // Retry failed device configurations
            if (!failedDeviceConfigs.isEmpty()) {
                logger.debug("Retrying {} failed device configurations", failedDeviceConfigs.size());

                // Create a copy to avoid concurrent modification
                Map<String, DDWRTDeviceConfiguration> failedConfigsCopy = new ConcurrentHashMap<>(failedDeviceConfigs);

                failedConfigsCopy.forEach((hostname, config) -> {
                    try {
                        logger.debug("Retrying connection to failed device: {}", hostname);
                        DDWRTDevice device = DDWRTDevice.upsertDeviceInNetwork(this, config);

                        // If device was successfully added to network (has MAC), remove from failed configs
                        if (!device.getMac().isEmpty()) {
                            failedDeviceConfigs.remove(hostname);
                            logger.info("Successfully reconnected to device: {}", hostname);
                        }
                    } catch (Exception e) {
                        logger.debug("Retry failed for host {}: {}", hostname, e.getMessage(), e);
                    }
                });
            }
        }
    }
}
