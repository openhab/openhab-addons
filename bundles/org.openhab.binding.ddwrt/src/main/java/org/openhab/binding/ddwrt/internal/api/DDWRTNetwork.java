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
package org.openhab.binding.ddwrt.internal.api;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.openhab.binding.ddwrt.internal.DDWRTNetworkConfiguration;
import org.openhab.core.OpenHAB;
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

    private static final Path HOSTS_DIR = Path.of(OpenHAB.getUserDataFolder(), "ddwrt", "hosts");

    private volatile @Nullable DDWRTNetworkConfiguration config;
    private volatile @Nullable ThingUID bridgeUID;

    private final DDWRTNetworkCache cache = new DDWRTNetworkCache();

    // Failed device configurations by hostname for retry during refresh
    private final Map<String, DDWRTDeviceConfiguration> failedDeviceConfigs = new ConcurrentHashMap<>();

    // Per-host locks to prevent concurrent createDevice calls for the same hostname
    private final Map<String, Object> hostLocks = new ConcurrentHashMap<>();

    // Independent failure tracking: auth failures (lockout risk) vs network failures (no risk).
    // Auth failure proves network is up, so it resets the network counters.
    private final Map<String, Integer> hostAuthFailureCount = new ConcurrentHashMap<>();
    private final Map<String, Long> hostAuthFailureTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> hostNetFailureCount = new ConcurrentHashMap<>();
    private final Map<String, Long> hostNetFailureTimes = new ConcurrentHashMap<>();

    /**
     * Stop retrying auth failures after this many attempts; wait for config change.
     * Dropbear blocks after 5 failed auth attempts, so stop at 3 (1 initial + 2 retries)
     * to leave headroom for the user to fix config without triggering the block.
     */
    private static final int MAX_AUTH_RETRY_FAILURES = 3;

    private final Logger logger = LoggerFactory.getLogger(DDWRTNetwork.class);

    // Hostname mapping support
    private final HostsFileLoader hostsLoader = new HostsFileLoader();
    private volatile Map<String, String> macToHostname = Map.of();
    private volatile Map<String, String> ipToHostname = Map.of();

    // Local ARP cache reader (used when useLocalArpCache config is true)
    private final LocalArpReader localArpReader = new LocalArpReader();

    // Refresh listeners (e.g., discovery service)
    private final List<RefreshListener> refreshListeners = new CopyOnWriteArrayList<>();

    // ---- Configuration ----

    /** Hand the entire bridge configuration to the network. */
    public void setConfig(DDWRTNetworkConfiguration netCfg) {
        if (!Objects.equals(this.config, netCfg)) {
            logger.debug("Config changed.");
            this.config = netCfg;
            reloadHostnameMappings();

            // Clear failure tracking for retry logic
            failedDeviceConfigs.clear();
            hostAuthFailureCount.clear();
            hostAuthFailureTimes.clear();
            hostNetFailureCount.clear();
            hostNetFailureTimes.clear();

            // Reset per-device recovery backoff so existing devices retry immediately
            cache.getDevices().forEach(DDWRTBaseDevice::resetRecoveryBackoff);

            final List<String> hosts = Objects.requireNonNull(
                    Arrays.stream(netCfg.hostnames.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());

            // Get list of currently connected devices to preserve them
            List<String> existingHostnames = cache.getDevices().stream().map(DDWRTBaseDevice::getHostname).toList();

            // Create a set of all hostnames (existing + new config)
            Set<String> allHostnames = new LinkedHashSet<>(existingHostnames);
            allHostnames.addAll(hosts);

            // Process all hostnames (preserving existing, adding new from config)
            allHostnames.forEach(hostname -> {
                // Check if this is a config device or existing device
                boolean isFromConfig = hosts.contains(hostname);

                DDWRTDeviceConfiguration devCfg = new DDWRTDeviceConfiguration();
                try {
                    if (isFromConfig) {
                        // Use config-based settings
                        HostSpec spec = parseHostSpec(hostname, netCfg);
                        devCfg.hostname = spec.hostname;
                        devCfg.port = spec.port;
                        devCfg.user = spec.user;
                        devCfg.password = netCfg.password;
                        devCfg.refreshInterval = netCfg.refreshInterval;
                        logger.debug("Discovering config device: {} (user={}, port={})", spec.hostname, spec.user,
                                spec.port);
                    } else {
                        // Preserve existing device settings
                        DDWRTBaseDevice existingDevice = cache.getDevices().stream()
                                .filter(d -> hostname.equals(d.getHostname())).findFirst().orElse(null);
                        if (existingDevice != null) {
                            devCfg.hostname = existingDevice.getHostname();
                            devCfg.port = existingDevice.getConfig().port;
                            devCfg.user = existingDevice.getConfig().user;
                            devCfg.password = existingDevice.getConfig().password;
                            devCfg.refreshInterval = existingDevice.getConfig().refreshInterval;
                            logger.debug("Preserving existing device: {}", hostname);
                        } else {
                            logger.debug("Skipping unknown device: {}", hostname);
                            return;
                        }
                    }

                    DDWRTBaseDevice device = createDeviceLocked(devCfg);
                    if (device != null) {
                        device.startRefresh(netCfg.refreshInterval);
                    }
                } catch (Exception e) {
                    logger.debug("SSH to host {} failed: {}", hostname, e.getMessage(), e);
                    if (isFromConfig) {
                        addFailedDeviceConfig(devCfg.hostname, devCfg);
                        if (isAuthFailure(e)) {
                            hostAuthFailureCount.put(devCfg.hostname, 1);
                            hostAuthFailureTimes.put(devCfg.hostname, System.currentTimeMillis());
                            // Auth failure proves network is up
                            hostNetFailureCount.remove(devCfg.hostname);
                            hostNetFailureTimes.remove(devCfg.hostname);
                        } else {
                            hostNetFailureCount.put(devCfg.hostname, 1);
                            hostNetFailureTimes.put(devCfg.hostname, System.currentTimeMillis());
                        }
                    }
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
        String user = defaults.useSystemUser ? "" : defaults.user;
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
                logger.debug("Successfully connected to device: {} (MAC: {})", cfg.hostname, device.getMac());
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
        // Reload hostname mappings on every refresh so file changes are picked up
        reloadHostnameMappings();

        // ARP/neighbor information comes from the gateway by default.
        // Fall back to the local openHAB host only when no gateway is present.
        boolean hasGateway = cache.getDevices().stream().anyMatch(d -> d.isGateway() && d.isOnline());
        if (config != null && config.useLocalArpCache && !hasGateway) {
            refreshLocalArpCache();
        } else {
            cache.clearArpEntries("local-host");
        }

        // Retry failed device configurations with backoff
        if (!failedDeviceConfigs.isEmpty()) {
            Map<String, DDWRTDeviceConfiguration> copy = new ConcurrentHashMap<>(failedDeviceConfigs);
            copy.forEach((hostname, cfg) -> {
                try {
                    int authFailures = hostAuthFailureCount.getOrDefault(hostname, 0);
                    int netFailures = hostNetFailureCount.getOrDefault(hostname, 0);
                    long currentTime = System.currentTimeMillis();

                    // Auth failures: stop after MAX_AUTH_RETRY_FAILURES; wait for config change
                    if (authFailures >= MAX_AUTH_RETRY_FAILURES) {
                        logger.debug("Host {} has {} auth failures, suspending retries until config change", hostname,
                                authFailures);
                        return;
                    }

                    // Check auth backoff (higher priority — lockout risk)
                    if (authFailures > 0) {
                        Long lastAuthTime = hostAuthFailureTimes.get(hostname);
                        long backoffMs = calculateAuthBackoffMs(authFailures);
                        if (lastAuthTime != null && (currentTime - lastAuthTime) < backoffMs) {
                            logger.debug("Backing off retry for {} ({}s remaining, auth)", hostname,
                                    (backoffMs - (currentTime - lastAuthTime)) / 1000);
                            return;
                        }
                    }

                    // Check network backoff
                    if (netFailures > 0) {
                        Long lastNetTime = hostNetFailureTimes.get(hostname);
                        long backoffMs = calculateNetworkBackoffMs(netFailures);
                        if (lastNetTime != null && (currentTime - lastNetTime) < backoffMs) {
                            logger.debug("Backing off retry for {} ({}s remaining, network)", hostname,
                                    (backoffMs - (currentTime - lastNetTime)) / 1000);
                            return;
                        }
                    }

                    logger.debug("Retrying connection to failed device: {}", hostname);
                    DDWRTBaseDevice device = createDeviceLocked(cfg);
                    if (device != null) {
                        failedDeviceConfigs.remove(hostname);
                        hostAuthFailureCount.remove(hostname);
                        hostAuthFailureTimes.remove(hostname);
                        hostNetFailureCount.remove(hostname);
                        hostNetFailureTimes.remove(hostname);
                        DDWRTNetworkConfiguration netCfg = config;
                        int interval = netCfg != null ? netCfg.refreshInterval : cfg.refreshInterval;
                        device.startRefresh(interval);
                        logger.debug("Successfully reconnected to device: {}", hostname);
                    }
                } catch (Exception e) {
                    if (isAuthFailure(e)) {
                        int count = Objects.requireNonNull(hostAuthFailureCount.merge(hostname, 1, Integer::sum));
                        hostAuthFailureTimes.put(hostname, System.currentTimeMillis());
                        // Auth failure proves network is up
                        hostNetFailureCount.remove(hostname);
                        hostNetFailureTimes.remove(hostname);
                        logger.debug("Retry auth failure for {} (attempt {}): {}", hostname, count, e.getMessage());
                    } else {
                        int count = Objects.requireNonNull(hostNetFailureCount.merge(hostname, 1, Integer::sum));
                        hostNetFailureTimes.put(hostname, System.currentTimeMillis());
                        logger.debug("Retry network failure for {} (attempt {}): {}", hostname, count, e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Create a device with per-host locking to prevent duplicate SSH sessions.
     * If another thread is already creating a device for the same host, this blocks until done,
     * then returns the cached device (if the first thread succeeded) or retries.
     */
    private @Nullable DDWRTBaseDevice createDeviceLocked(DDWRTDeviceConfiguration cfg) throws IOException {
        Object lock = hostLocks.computeIfAbsent(cfg.hostname, k -> new Object());
        synchronized (Objects.requireNonNull(lock)) {
            // Check if device was already created by another thread while we waited
            DDWRTBaseDevice existing = getDeviceByHostname(cfg.hostname);
            if (existing != null) {
                logger.debug("Device already exists for {}, skipping createDevice", cfg.hostname);
                return existing;
            }
            DDWRTBaseDevice device = DDWRTBaseDevice.createDevice(cache, cfg);
            if (device != null) {
                device.setNetwork(this);
            }
            return device;
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

    /** Enable or disable a wireless radio on the specified device. */
    public boolean setRadioEnabled(String deviceMac, String iface, boolean enabled) {
        DDWRTBaseDevice device = cache.getDevice(deviceMac);
        return device != null && device.setRadioEnabled(iface, enabled);
    }

    // ---- Refresh listeners ----

    public void addRefreshListener(RefreshListener listener) {
        refreshListeners.add(listener);
    }

    public void removeRefreshListener(RefreshListener listener) {
        refreshListeners.remove(listener);
    }

    /**
     * Called by a device after a successful refresh cycle.
     * Notifies all registered listeners that new data is available in the cache.
     */
    public void fireRefreshComplete(DDWRTBaseDevice device) {
        for (RefreshListener listener : refreshListeners) {
            try {
                listener.onRefreshComplete(device);
            } catch (Exception e) {
                logger.debug("Refresh listener error: {}", e.getMessage());
            }
        }
    }

    /** Stop all device thread pools and clear the cache. Called on bridge dispose. */
    public void dispose() {
        cache.getDevices().forEach(DDWRTBaseDevice::dispose);
        cache.clearAll();
        failedDeviceConfigs.clear();
        hostAuthFailureCount.clear();
        hostAuthFailureTimes.clear();
        hostNetFailureCount.clear();
        hostNetFailureTimes.clear();
        hostLocks.clear();
    }

    /**
     * Reload all hostname mappings: every file in $OPENHAB_USERDATA/ddwrt/hosts/ + inline config.
     * Inline config takes precedence over files.
     */
    private void reloadHostnameMappings() {
        Map<String, String> macMap = new HashMap<>();
        Map<String, String> ipMap = new HashMap<>();

        // 1. Files first (lowest priority)
        for (HostsFileLoader.HostEntry e : hostsLoader.loadDirectory(HOSTS_DIR)) {
            if (!e.mac.isEmpty()) {
                macMap.put(e.mac, e.hostname);
            }
            if (!e.ip.isEmpty()) {
                ipMap.put(e.ip, e.hostname);
            }
        }

        // 2. Inline config (highest priority — overrides files)
        DDWRTNetworkConfiguration cfg = config;
        if (cfg != null) {
            for (HostsFileLoader.HostEntry e : hostsLoader.parseInlineMappings(cfg.hostnameMappings)) {
                if (!e.mac.isEmpty()) {
                    macMap.put(e.mac, e.hostname);
                }
                if (!e.ip.isEmpty()) {
                    ipMap.put(e.ip, e.hostname);
                }
            }
        }

        macToHostname = Map.copyOf(macMap);
        ipToHostname = Map.copyOf(ipMap);
        logger.debug("Loaded {} MAC and {} IP hostname mappings (files + inline config)", macMap.size(), ipMap.size());
    }

    /** Public lookup API used by DDWRTBaseDevice fallback chain. */
    public @Nullable String resolveHostname(String mac, String ip) {
        String h = macToHostname.get(mac.toLowerCase(Locale.ROOT));
        if (h != null) {
            return h;
        }
        return !ip.isEmpty() ? ipToHostname.get(ip) : null;
    }

    /**
     * Read the local kernel ARP/neighbor cache from the openHAB host.
     * This data is only used when no gateway device is present.
     */
    private void refreshLocalArpCache() {
        List<LocalArpReader.ArpEntry> entries = localArpReader.readLocalArp();
        List<DDWRTNetworkCache.ArpEntry> arpEntries = new java.util.ArrayList<>();
        Instant now = Instant.now();
        for (LocalArpReader.ArpEntry e : entries) {
            arpEntries.add(
                    new DDWRTNetworkCache.ArpEntry(e.mac, e.ip, now, DDWRTNetworkCache.ArpState.ACTIVE, "local-host"));
        }
        cache.replaceArpEntries("local-host", arpEntries);
        logger.debug("Local ARP cache: read {} entries", entries.size());
    }

    /**
     * Classify whether an exception indicates an authentication/block failure
     * (triggers Dropbear lockout) vs a network-level failure (no lockout risk).
     */
    static boolean isAuthFailure(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return false;
        }
        // "No more authentication methods available" — wrong key/password
        // "Session is being closed" — Dropbear actively blocking this client
        return msg.contains("authentication methods") || msg.contains("Session is being closed");
    }

    /**
     * Backoff for auth/block failures. Aggressive because each attempt while
     * Dropbear is blocking adds 5 minutes to the block timer.
     * 1st: 30s, 2nd: 60s, 3rd: 120s, 4th: 240s, 5th+: suspended via MAX_AUTH_RETRY_FAILURES
     */
    private long calculateAuthBackoffMs(int failureCount) {
        if (failureCount <= 0) {
            return 0;
        }
        long backoff = 30_000L * (1L << Math.min(failureCount - 1, 4));
        return Math.min(backoff, 480_000L);
    }

    /**
     * Backoff for network failures (unreachable, refused, timeout).
     * No risk of Dropbear lockout, so use a gentler schedule.
     * 1st: 10s, 2nd: 20s, 3rd: 40s, 4th: 80s, 5th: 160s, 6th+: 300s (5 min)
     */
    private long calculateNetworkBackoffMs(int failureCount) {
        if (failureCount <= 0) {
            return 0;
        }
        long backoff = 10_000L * (1L << Math.min(failureCount - 1, 5));
        return Math.min(backoff, 300_000L);
    }
}
