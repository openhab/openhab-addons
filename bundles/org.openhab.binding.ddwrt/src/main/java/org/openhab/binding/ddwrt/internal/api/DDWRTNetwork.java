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

            hosts.forEach(host -> {
                try {
                    logger.debug("Discovering device: {}", host);
                    DDWRTDeviceConfiguration devCfg = new DDWRTDeviceConfiguration();
                    devCfg.hostname = host;
                    devCfg.port = netCfg.port;
                    devCfg.user = netCfg.user;
                    devCfg.password = netCfg.password;
                    devCfg.refreshInterval = netCfg.refreshInterval;

                    DDWRTBaseDevice device = DDWRTBaseDevice.createDevice(cache, devCfg);
                    if (device != null) {
                        device.startRefresh(netCfg.refreshInterval);
                    } else {
                        addFailedDeviceConfig(host, devCfg);
                    }
                } catch (Exception e) {
                    logger.debug("SSH to host {} failed: {}", host, e.getMessage(), e);
                }
            });
        }
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
            DDWRTBaseDevice device = DDWRTBaseDevice.createDevice(cache, cfg);
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
                    DDWRTBaseDevice device = DDWRTBaseDevice.createDevice(cache, cfg);
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

    /** Stop all device thread pools and clear the cache. Called on bridge dispose. */
    public void dispose() {
        cache.getDevices().forEach(DDWRTBaseDevice::dispose);
        cache.clearAll();
        failedDeviceConfigs.clear();
    }
}
