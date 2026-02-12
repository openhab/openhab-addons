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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all DD-WRT compatible devices (routers, APs).
 * Manages SSH session lifecycle, per-device thread pool, chipset-specific
 * command dispatch, and refresh template.
 *
 * Subclasses override wireless-specific methods for their chipset:
 * {@link DDWRTAtherosDevice}, {@link DDWRTBroadcomDevice},
 * {@link DDWRTMarvellDevice}, {@link DDWRTOpenWrtDevice}, {@link DDWRTGenericDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public abstract class DDWRTBaseDevice {

    private static final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(DDWRTBaseDevice.class));

    // Identity
    protected String mac = "";
    protected String hostname = "";
    protected String model = "";
    protected String firmware = "";
    protected String welcomeBanner = "";
    protected String chipset = "unknown";

    // Telemetry (populated during refresh)
    protected String uptime = "";
    protected double cpuLoad = 0.0;
    protected double cpuTemp = 0.0;
    protected String wanIp = "";
    protected long wanIn = 0;
    protected long wanOut = 0;
    protected long ifIn = 0;
    protected long ifOut = 0;
    protected boolean online = false;

    // Configuration
    protected DDWRTDeviceConfiguration config;

    // SSH session (persistent, shared across exec + log channels)
    protected @Nullable SshAuthSession authSession;

    // Per-device thread pool: refresh + syslog watcher
    private @Nullable ScheduledExecutorService executor;
    private @Nullable ScheduledFuture<?> refreshJob;

    // Updater callback (set by handler)
    protected volatile @Nullable DDWRTThingUpdater updater;

    protected DDWRTBaseDevice(DDWRTDeviceConfiguration cfg) {
        this.config = cfg;
    }

    // ---- Factory with chipset auto-detection ----

    /**
     * Create and initialize a device by connecting via SSH and auto-detecting the chipset.
     * Returns the appropriate subclass. If SSH fails, returns null and tracks the failure.
     */
    public static @Nullable DDWRTBaseDevice createDevice(DDWRTNetworkCache cache, DDWRTDeviceConfiguration cfg) {
        SshAuthSession ssh = null;
        try {
            String host = Objects.requireNonNull(cfg.hostname);
            String user = Objects.requireNonNull(cfg.user);
            Duration timeout = Objects.requireNonNull(Duration.ofSeconds(5));
            ssh = SshClientManager.getInstance().openAuthSession(host, cfg.port, user, cfg.password, null, null,
                    timeout);

            SshRunner runner = ssh.createRunner();

            // Collect identity
            String mac = runner.execStdout("nvram get lan_hwaddr");
            if (mac.isEmpty()) {
                // Fallback for non-nvram systems (OpenWrt, generic Linux)
                mac = runner.execStdout(
                        "ip -br l | grep -E '^(en|eth|wl|br)' | awk '{print tolower($3)}' | LC_ALL=C sort | head -n1");
            }
            if (mac.isEmpty()) {
                logger.warn("Could not determine MAC for device at {}", cfg.hostname);
                ssh.close();
                return null;
            }
            mac = Objects.requireNonNull(mac.toLowerCase().trim());

            // Check if device already exists in cache
            DDWRTBaseDevice existing = cache.getDevice(mac);
            if (existing != null) {
                logger.debug("Device already exists in cache: {} (MAC: {})", cfg.hostname, mac);
                // Update config if credentials changed
                if (!Objects.equals(existing.config.password, cfg.password)
                        || !Objects.equals(existing.config.user, cfg.user) || existing.config.port != cfg.port) {
                    existing.config = cfg;
                    existing.closeSessionQuietly();
                    existing.authSession = ssh;
                    logger.info("Updated credentials for device: {} (MAC: {})", cfg.hostname, mac);
                } else {
                    ssh.close();
                }
                return existing;
            }

            // Detect chipset and create appropriate subclass
            String chipsetType = detectFWandChipset(runner);
            DDWRTBaseDevice device = createForFWandChipset(chipsetType, cfg);
            device.mac = mac;
            device.chipset = chipsetType;
            device.authSession = ssh;

            // Collect remaining identity info
            device.hostname = safeTrim(runner.execStdout("hostname"));

            // Capture welcome banner
            String banner = safeTrim(ssh.getWelcomeBanner());
            if (!banner.isEmpty()) {
                device.welcomeBanner = banner;
            }

            // Model and firmware: chipset-specific (overridden by subclasses)
            device.refreshIdentity(runner);

            device.online = true;
            cache.putDevice(mac, device);
            logger.info("Created {} device: {} (MAC: {}, model: {})", chipsetType, cfg.hostname, mac, device.model);
            return device;

        } catch (Exception e) {
            logger.warn("Failed to initialize device at {}: {}", cfg.hostname, e.getMessage());
            if (ssh != null) {
                try {
                    ssh.close();
                } catch (Exception ignore) {
                    // no-op
                }
            }
            return null;
        }
    }

    /**
     * Detect chipset by probing for known wireless tools.
     */
    private static String detectFWandChipset(SshRunner runner) {
        // Try Atheros (DD-WRT)
        SshRunner.CommandResult result;
        try {
            result = runner.execResult("which wl_atheros 2>/dev/null");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return "atheros";
            }
        } catch (Exception e) {
            // continue
        }

        // Try Broadcom (DD-WRT)
        try {
            result = runner.execResult("which wl 2>/dev/null");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return "broadcom";
            }
        } catch (Exception e) {
            // continue
        }

        // Try iwinfo (OpenWrt / Marvell DD-WRT)
        try {
            result = runner.execResult("which iwinfo 2>/dev/null");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                // Distinguish OpenWrt from Marvell DD-WRT
                SshRunner.CommandResult openwrtCheck = runner.execResult("cat /etc/openwrt_release 2>/dev/null");
                if (openwrtCheck.isSuccess() && !openwrtCheck.getStdout().trim().isEmpty()) {
                    return "openwrt";
                }
                return "marvell";
            }
        } catch (Exception e) {
            // continue
        }

        // Try iw (generic Linux)
        try {
            result = runner.execResult("which iw 2>/dev/null");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return "generic";
            }
        } catch (Exception e) {
            // continue
        }

        return "generic";
    }

    private static DDWRTBaseDevice createForFWandChipset(String chipset, DDWRTDeviceConfiguration cfg) {
        return switch (chipset) {
            case "atheros" -> new DDWRTAtherosDevice(cfg);
            case "broadcom" -> new DDWRTBroadcomDevice(cfg);
            case "marvell" -> new DDWRTMarvellDevice(cfg);
            case "openwrt" -> new DDWRTOpenWrtDevice(cfg);
            default -> new DDWRTGenericDevice(cfg);
        };
    }

    // ---- Thread pool management ----

    /**
     * Start the per-device thread pool and schedule periodic refresh.
     */
    public synchronized void startRefresh(int intervalSeconds) {
        stopRefresh();
        ScheduledExecutorService ex = Executors.newScheduledThreadPool(2,
                r -> new Thread(r, "ddwrt-" + (hostname.isEmpty() ? Objects.requireNonNull(mac) : hostname)));
        executor = ex;
        refreshJob = ex.scheduleWithFixedDelay(this::safeRefresh, 0, intervalSeconds, TimeUnit.SECONDS);
        logger.debug("Started refresh for device {} every {}s", mac, intervalSeconds);
    }

    /**
     * Stop the per-device thread pool.
     */
    public synchronized void stopRefresh() {
        ScheduledFuture<?> rj = refreshJob;
        refreshJob = null;
        if (rj != null) {
            rj.cancel(true);
        }
        ScheduledExecutorService ex = executor;
        executor = null;
        if (ex != null) {
            ex.shutdownNow();
        }
    }

    // ---- Refresh template ----

    private void safeRefresh() {
        try {
            refresh();
        } catch (Exception e) {
            logger.debug("Refresh failed for device {}: {}", mac, e.getMessage());
            online = false;
            DDWRTThingUpdater u = updater;
            if (u != null) {
                u.reportOffline(e.getMessage());
            }
        }
    }

    /**
     * Full refresh cycle. Recovers session if needed, then calls template methods.
     */
    public void refresh() {
        SshAuthSession s = ensureSession();
        if (s == null) {
            online = false;
            DDWRTThingUpdater u = updater;
            if (u != null) {
                u.reportOffline("SSH session not available");
            }
            return;
        }

        SshRunner runner = s.createRunner();
        refreshCommon(runner);
        refreshWirelessClients(runner);
        refreshRadios(runner);
        refreshFirewallRules(runner);
        online = true;

        DDWRTThingUpdater u = updater;
        if (u != null) {
            u.reportOnline();
            pushChannels(u);
        }
    }

    /**
     * Common telemetry refresh (all chipsets).
     */
    protected void refreshCommon(SshRunner runner) {
        uptime = safeTrim(runner.execStdout("uptime -s"));

        String loadStr = safeTrim(runner.execStdout("cat /proc/loadavg | awk '{print $1}'"));
        if (!loadStr.isEmpty()) {
            try {
                cpuLoad = Double.parseDouble(loadStr);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // CPU temp: chipset-specific parsing (overridable)
        cpuTemp = refreshCpuTemp(runner);

        // WAN IP — treat 0.0.0.0 as "no WAN" (AP-only devices)
        String rawWanIp = safeTrim(runner.execStdout("nvram get wan_ipaddr 2>/dev/null"));
        wanIp = (rawWanIp.isEmpty() || "0.0.0.0".equals(rawWanIp)) ? "" : rawWanIp;

        // Interface traffic from /proc/net/dev
        parseInterfaceTraffic(runner);
    }

    /**
     * Read CPU temperature in degrees Celsius. Default implementation tries /proc/dmu/temperature
     * then /sys/class/thermal/thermal_zone0/temp (millidegrees). Subclasses may override for
     * chipset-specific parsing.
     */
    protected double refreshCpuTemp(SshRunner runner) {
        String tempStr = safeTrim(runner.execStdout("cat /proc/dmu/temperature 2>/dev/null | grep -oE '[0-9.]+'"));
        if (tempStr.isEmpty()) {
            tempStr = safeTrim(runner.execStdout("cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null"));
            if (!tempStr.isEmpty()) {
                try {
                    // thermal_zone reports millidegrees
                    return Double.parseDouble(tempStr) / 1000.0;
                } catch (NumberFormatException e) {
                    // ignore
                }
                return 0.0;
            }
        }
        if (!tempStr.isEmpty()) {
            try {
                return Double.parseDouble(tempStr);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return 0.0;
    }

    private void parseInterfaceTraffic(SshRunner runner) {
        // WAN interface — only fetch if device has a real WAN IP (is a gateway)
        String wanIface = isGateway() ? safeTrim(runner.execStdout("nvram get wan_iface 2>/dev/null")) : "";
        if (!wanIface.isEmpty()) {
            String wanLine = safeTrim(
                    runner.execStdout("cat /proc/net/dev | grep '" + wanIface + "' | awk '{print $2, $10}'"));
            if (!wanLine.isEmpty()) {
                String[] parts = wanLine.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        wanIn = Long.parseLong(parts[0]);
                        wanOut = Long.parseLong(parts[1]);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }

        // LAN interface (br0)
        String lanLine = safeTrim(runner.execStdout("cat /proc/net/dev | grep 'br0' | awk '{print $2, $10}'"));
        if (!lanLine.isEmpty()) {
            String[] parts = lanLine.split("\\s+");
            if (parts.length >= 2) {
                try {
                    ifIn = Long.parseLong(parts[0]);
                    ifOut = Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Push current telemetry to the handler via DDWRTThingUpdater.
     */
    protected void pushChannels(DDWRTThingUpdater u) {
        u.updateChannel("uptime", new StringType(uptime));
        u.updateChannel("online", online ? OnOffType.ON : OnOffType.OFF);
        u.updateChannel("cpuLoad", new DecimalType(cpuLoad));
        u.updateChannel("cpuTemp", new QuantityType<>(cpuTemp, SIUnits.CELSIUS));
        if (isGateway()) {
            u.updateChannel("wanIp", new StringType(wanIp));
            u.updateChannel("wanIn", new DecimalType(wanIn));
            u.updateChannel("wanOut", new DecimalType(wanOut));
        }
        u.updateChannel("ifIn", new DecimalType(ifIn));
        u.updateChannel("ifOut", new DecimalType(ifOut));
    }

    // ---- Chipset-specific methods (overridden by subclasses) ----

    /**
     * Populate model and firmware fields. Default implementation uses generic Linux sources.
     * DD-WRT subclasses override to use /tmp/loginprompt; OpenWrt uses /tmp/sysinfo/model.
     */
    protected void refreshIdentity(SshRunner runner) {
        // Generic Linux: DMI or device-tree for model
        model = safeTrim(runner.execStdout(
                "cat /sys/devices/virtual/dmi/id/product_name 2>/dev/null || cat /proc/device-tree/model 2>/dev/null"));
        // Generic Linux: os-release for firmware/distro
        firmware = safeTrim(runner.execStdout("cat /etc/os-release 2>/dev/null | grep PRETTY_NAME | cut -d'\"' -f2"));
    }

    /**
     * Refresh wireless client list. Subclasses parse chipset-specific output.
     */
    protected void refreshWirelessClients(SshRunner runner) {
        // Default no-op; overridden by chipset subclasses
    }

    /**
     * Refresh radio information. Subclasses parse chipset-specific output.
     */
    protected void refreshRadios(SshRunner runner) {
        // Default no-op; overridden by chipset subclasses
    }

    /**
     * Refresh firewall rules from nvram.
     */
    protected void refreshFirewallRules(SshRunner runner) {
        // Default no-op; implemented in Phase 6
    }

    /**
     * Get associated wireless clients for a given interface.
     */
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        return Objects.requireNonNull(Collections.emptyList());
    }

    /**
     * Enumerate wireless radio interfaces.
     */
    protected List<DDWRTRadio> enumerateRadios(SshRunner runner) {
        return Objects.requireNonNull(Collections.emptyList());
    }

    /**
     * Enable or disable a wireless radio interface.
     */
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) {
        // Default no-op; overridden by chipset subclasses
    }

    /**
     * Reboot the device.
     */
    public void reboot() {
        SshAuthSession s = authSession;
        if (s != null) {
            try {
                String cmd = "root".equals(config.user) ? "reboot" : "sudo reboot";
                s.createRunner().execStdout(cmd);
            } catch (Exception e) {
                logger.warn("Reboot command failed for {}: {}", mac, e.getMessage());
            }
        }
    }

    // ---- Session management ----

    protected @Nullable SshAuthSession ensureSession() {
        SshAuthSession s = authSession;
        if (s != null) {
            try {
                if (s.getClientSession().isOpen()) {
                    return s;
                }
            } catch (Exception e) {
                logger.debug("Session check failed: {}", e.getMessage());
            }
        }

        // Attempt recovery
        return recoverSession();
    }

    protected @Nullable SshAuthSession recoverSession() {
        closeSessionQuietly();
        try {
            String host = Objects.requireNonNull(config.hostname);
            String user = Objects.requireNonNull(config.user);
            Duration timeout = Objects.requireNonNull(Duration.ofSeconds(5));
            SshAuthSession newSession = SshClientManager.getInstance().openAuthSession(host, config.port, user,
                    config.password, null, null, timeout);

            // Test the session
            SshRunner runner = newSession.createRunner();
            String test = runner.execStdout("echo ok");
            if ("ok".equals(test)) {
                authSession = newSession;
                logger.info("Recovered SSH session for device: {}", mac);
                return newSession;
            } else {
                newSession.close();
                return null;
            }
        } catch (Exception e) {
            logger.debug("Session recovery failed for {}: {}", mac, e.getMessage());
            return null;
        }
    }

    public void closeSessionQuietly() {
        SshAuthSession s = authSession;
        authSession = null;
        if (s != null) {
            try {
                s.close();
            } catch (Exception ignore) {
                // no-op
            }
        }
    }

    // ---- Getters / Setters ----

    public String getMac() {
        return mac;
    }

    public String getHostname() {
        return hostname;
    }

    public String getModel() {
        return model;
    }

    public String getFirmware() {
        return firmware;
    }

    public String getChipset() {
        return chipset;
    }

    public String getUptime() {
        return uptime;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getCpuTemp() {
        return cpuTemp;
    }

    public String getWanIp() {
        return wanIp;
    }

    /**
     * Returns true if this device has a real WAN interface (is a gateway, not just an AP).
     */
    public boolean isGateway() {
        return !wanIp.isEmpty();
    }

    public long getWanIn() {
        return wanIn;
    }

    public long getWanOut() {
        return wanOut;
    }

    public long getIfIn() {
        return ifIn;
    }

    public long getIfOut() {
        return ifOut;
    }

    public boolean isOnline() {
        return online;
    }

    public String getWelcomeBanner() {
        return welcomeBanner;
    }

    public DDWRTDeviceConfiguration getConfig() {
        return config;
    }

    public void setConfig(DDWRTDeviceConfiguration config) {
        this.config = config;
    }

    public void setUpdater(@Nullable DDWRTThingUpdater updater) {
        this.updater = updater;
    }

    public @Nullable SshAuthSession getAuthSession() {
        return authSession;
    }

    /**
     * Get the per-device executor for scheduling additional tasks (e.g. syslog follower).
     */
    public @Nullable ScheduledExecutorService getExecutor() {
        return executor;
    }

    public void dispose() {
        stopRefresh();
        closeSessionQuietly();
    }

    protected static String safeTrim(@Nullable String s) {
        if (s == null) {
            return "";
        }
        return Objects.requireNonNull(s.trim());
    }
}
