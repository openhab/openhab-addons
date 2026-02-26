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

import java.util.Arrays;
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
 * Holds the central {@link DDWRTNetworkCache} and manages device discovery,
 * per-device refresh scheduling, and failed-device retry.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTNetwork {

    private volatile @Nullable DDWRTNetworkConfiguration config;
    private volatile @Nullable ThingUID bridgeUID;

    private final DDWRTNetworkCache cache = new DDWRTNetworkCache();

    // Failed device configurations by hostname for retry during refresh
    private final Map<String, DDWRTDeviceConfiguration> failedDeviceConfigs = new ConcurrentHashMap<>();

    // Per-host locks to prevent concurrent createDevice calls for the same hostname
    private final Map<String, Object> hostLocks = new ConcurrentHashMap<>();

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTNetwork.class));

    // ---- Configuration ----

    /** Hand the entire bridge configuration to the network. */
    public void setConfig(DDWRTNetworkConfiguration netCfg) {
        if (!Objects.equals(this.config, netCfg)) {
            logger.info("Config changed.");
            this.config = netCfg;
            failedDeviceConfigs.clear();

            final List<String> hosts = Objects.requireNonNull(
                    Arrays.stream(netCfg.hostnames.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());

            hosts.forEach(hostEntry -> {
                try {
                    HostSpec spec = parseHostSpec(hostEntry, netCfg);
                    logger.debug("Discovering device: {} (user={}, port={})", spec.hostname, spec.user, spec.port);

                    DDWRTDeviceConfiguration devCfg = new DDWRTDeviceConfiguration();
                    devCfg.hostname = spec.hostname;
                    devCfg.port = spec.port;
                    devCfg.user = spec.user;
                    devCfg.password = netCfg.password;
                    devCfg.refreshInterval = netCfg.refreshInterval;

                    DDWRTBaseDevice device = createDeviceLocked(devCfg);
                    if (device != null) {
                        device.startRefresh(netCfg.refreshInterval);
                    } else {
                        addFailedDeviceConfig(spec.hostname, devCfg);
                    }
                } catch (Exception e) {
                    logger.debug("SSH to host {} failed: {}", hostEntry, e.getMessage(), e);
                }
            });
        }
    }

    private static class HostSpec {
        final String hostname;
        final String user;
        final int port;

        HostSpec(String hostname, String user, int port) {
            this.hostname = hostname;
            this.user = user;
            this.port = port;
        }
    }

    private static HostSpec parseHostSpec(String entry, DDWRTNetworkConfiguration defaults) {
        String trimmed = entry.trim();
        String user = defaults.user;
        String hostPort = trimmed;

        int atIdx = trimmed.indexOf('@');
        if (atIdx >= 0) {
            String maybeUser = trimmed.substring(0, atIdx).trim();
            if (!maybeUser.isEmpty()) {
                user = maybeUser;
            }
            hostPort = trimmed.substring(atIdx + 1).trim();
        }

        int port = defaults.port;
        int colonIdx = hostPort.lastIndexOf(':');
        if (colonIdx > 0 && colonIdx < hostPort.length() - 1) {
            String maybePort = hostPort.substring(colonIdx + 1);
            try {
                port = Integer.parseInt(maybePort);
                hostPort = hostPort.substring(0, colonIdx);
            } catch (NumberFormatException e) {
                // ignore, keep default port
            }
        }

        return new HostSpec(hostPort, user, port);
    }

    public void setBridgeUID(ThingUID thingUID) {
        this.bridgeUID = thingUID;
    }

    public @Nullable ThingUID getBridgeUID() {
        return bridgeUID;
    }

    public @Nullable DDWRTNetworkConfiguration getConfig() {
        return config;
    }

    /** Return the central network cache. */
    public DDWRTNetworkCache getCache() {
        return cache;
    }

    // ---- Device access (convenience delegates to cache) ----

    public List<DDWRTBaseDevice> getDevices() {
        return cache.getDevices();
    }

    public @Nullable DDWRTBaseDevice getDeviceByMac(String mac) {
        return cache.getDevice(mac);
    }

    /**
     * Lookup a device by hostname. Iterates devices and matches by configured hostname.
     */
    public @Nullable DDWRTBaseDevice getDeviceByHostname(String hostname) {
        return cache.getDevices().stream().filter(d -> hostname.equalsIgnoreCase(d.getConfig().hostname)).findFirst()
                .orElse(null); // Expected nullable return
    }

    // ---- Failed device management ----

    public void addFailedDeviceConfig(String hostname, DDWRTDeviceConfiguration cfg) {
        failedDeviceConfigs.put(hostname, cfg);
        logger.debug("Tracked failed device config for hostname: {}", hostname);
    }

    /**
     * Manual device addition. Called when a user creates a device thing
     * for a host not yet discovered.
     */
    public @Nullable DDWRTBaseDevice addOrUpdateDevice(DDWRTDeviceConfiguration cfg) {
        logger.debug("Manual add/update device for hostname: {}", cfg.hostname);
        try {
            DDWRTBaseDevice device = createDeviceLocked(cfg);
            if (device != null) {
                failedDeviceConfigs.remove(cfg.hostname);
                DDWRTNetworkConfiguration netCfg = config;
                int interval = netCfg != null ? netCfg.refreshInterval : cfg.refreshInterval;
                device.startRefresh(interval);
                logger.info("Successfully connected to device: {} (MAC: {})", cfg.hostname, device.getMac());
                return device;
            }
        } catch (Exception e) {
            logger.warn("Device addition failed for {}: {}", cfg.hostname, e.getMessage());
        }
        addFailedDeviceConfig(cfg.hostname, cfg);
        return null;
    }

    // ---- Refresh (retries failed devices; active devices refresh themselves) ----

    public void refresh() {
        logger.debug("Network refresh: {} devices active, {} failed configs pending", cache.getTotalDevices(),
                failedDeviceConfigs.size());

        // Retry failed device configurations
        if (!failedDeviceConfigs.isEmpty()) {
            Map<String, DDWRTDeviceConfiguration> copy = new ConcurrentHashMap<>(failedDeviceConfigs);
            copy.forEach((hostname, cfg) -> {
                try {
                    logger.debug("Retrying connection to failed device: {}", hostname);
                    DDWRTBaseDevice device = createDeviceLocked(cfg);
                    if (device != null) {
                        failedDeviceConfigs.remove(hostname);
                        DDWRTNetworkConfiguration netCfg = config;
                        int interval = netCfg != null ? netCfg.refreshInterval : cfg.refreshInterval;
                        device.startRefresh(interval);
                        logger.info("Successfully reconnected to device: {}", hostname);
                    }
                } catch (Exception e) {
                    logger.debug("Retry failed for host {}: {}", hostname, e.getMessage());
                }
            });
        }
    }

    /**
     * Create a device with per-host locking to prevent duplicate SSH sessions.
     * If another thread is already creating a device for the same host, this blocks until done,
     * then returns the cached device (if the first thread succeeded) or retries.
     */
    private @Nullable DDWRTBaseDevice createDeviceLocked(DDWRTDeviceConfiguration cfg) {
        Object lock = hostLocks.computeIfAbsent(cfg.hostname, k -> new Object());
        synchronized (Objects.requireNonNull(lock)) {
            // Check if device was already created by another thread while we waited
            DDWRTBaseDevice existing = getDeviceByHostname(cfg.hostname);
            if (existing != null) {
                logger.debug("Device already exists for {}, skipping createDevice", cfg.hostname);
                return existing;
            }
            return DDWRTBaseDevice.createDevice(cache, cfg);
        }
    }

    /** Returns true if at least one device has an active SSH session. */
    public boolean hasOnlineDevices() {
        return cache.getDevices().stream().anyMatch(DDWRTBaseDevice::isOnline);
    }

    /** Returns true if there are device configurations that have not yet connected. */
    public boolean hasPendingDevices() {
        return !failedDeviceConfigs.isEmpty();
    }

    /** Stop all device thread pools and clear the cache. Called on bridge dispose. */
    public void dispose() {
        cache.getDevices().forEach(DDWRTBaseDevice::dispose);
        cache.clearAll();
        failedDeviceConfigs.clear();
        hostLocks.clear();
    }
}
