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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Per-device logger includes hostname/IP in logger name for easy identification
    protected Logger logger;

    // Banner/MOTD regex patterns
    // DD-WRT banner: "DD-WRT v3.0-r55630 std (c) 2024 NewMedia-NET GmbH"
    private static final Pattern DDWRT_VERSION_PATTERN = Objects
            .requireNonNull(Pattern.compile("DD-WRT\\s+(v[\\d.\\-r\\w]+)"));
    // DD-WRT banner: "Board: Linksys WRT3200ACM"
    private static final Pattern DDWRT_BOARD_PATTERN = Objects
            .requireNonNull(Pattern.compile("Board:\\s+(.+?)\\s*$", Pattern.MULTILINE));
    // OpenWrt MOTD: "OpenWrt 24.10.4, r28959-29397011cc" or "OpenWrt 24.10.4"
    private static final Pattern OPENWRT_VERSION_PATTERN = Objects
            .requireNonNull(Pattern.compile("OpenWrt\\s+([\\d.]+)(?:,\\s*(r[\\w-]+))?"));
    // Tomato MOTD: "Tomato v1.28.0000 MIPSR2-140 K26 USB AIO" or "FreshTomato 2023.5 K26MIPSR2_RTN USB VPN"
    private static final Pattern TOMATO_VERSION_PATTERN = Objects
            .requireNonNull(Pattern.compile("((?:Fresh)?Tomato)\\s+(v?[\\d.]+\\S*(?:\\s+\\S+)*)"));
    // Tomato MOTD: "Welcome to the Netgear WNR3500L v2 [hostname]"
    private static final Pattern TOMATO_WELCOME_PATTERN = Objects
            .requireNonNull(Pattern.compile("Welcome to the\\s+(.+?)\\s+\\[([\\w-]+)\\]"));
    // Shell prompt in MOTD: "root@hostname:~#"
    private static final Pattern MOTD_PROMPT_PATTERN = Objects
            .requireNonNull(Pattern.compile("(\\w+)@([\\w-]+):[~#/]"));

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

    protected DDWRTBaseDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        this.config = cfg;
        this.logger = logger;
    }

    // ---- Factory with chipset auto-detection ----

    /**
     * Create and initialize a device by connecting via SSH and auto-detecting the chipset.
     * Returns the appropriate subclass. If SSH fails, returns null and tracks the failure.
     */
    public static @Nullable DDWRTBaseDevice createDevice(DDWRTNetworkCache cache, DDWRTDeviceConfiguration cfg) {
        Logger log = Objects
                .requireNonNull(LoggerFactory.getLogger(DDWRTBaseDevice.class.getName() + "." + cfg.hostname));
        SshAuthSession ssh = null;
        try {
            String host = Objects.requireNonNull(cfg.hostname);
            String user = Objects.requireNonNull(cfg.user);
            Duration timeout = Objects.requireNonNull(Duration.ofSeconds(5));
            ssh = SshClientManager.getInstance().openAuthSession(host, cfg.port, user, cfg.password, null, null,
                    timeout);

            // Capture MOTD for firmware/chipset detection first
            String motd = ssh.captureMotd();

            // Create a runner for chipset detection
            SshRunner runner = ssh.createRunner();

            // Detect chipset and create appropriate subclass using MOTD and command probing
            String chipsetType = detectFWandChipset(runner, motd);
            DDWRTBaseDevice device = createForFWandChipset(chipsetType, cfg, log);
            device.chipset = chipsetType;
            device.authSession = ssh;

            // Now get MAC address using device-specific method
            String mac = device.getDeviceMac(runner);
            if (mac.isEmpty()) {
                log.warn("Could not determine MAC for device at {}", cfg.hostname);
                ssh.close();
                return null;
            }
            mac = Objects.requireNonNull(mac.toLowerCase().trim());
            device.mac = mac;

            // Check if device already exists in cache
            DDWRTBaseDevice existing = cache.getDevice(mac);
            if (existing != null) {
                log.debug("Device already exists in cache: {} (MAC: {})", cfg.hostname, mac);
                // Update config if credentials changed
                if (!Objects.equals(existing.config.password, cfg.password)
                        || !Objects.equals(existing.config.user, cfg.user) || existing.config.port != cfg.port) {
                    existing.config = cfg;
                    existing.closeSessionQuietly();
                    existing.authSession = ssh;
                    log.info("Updated credentials for device: {} (MAC: {})", cfg.hostname, mac);
                } else {
                    ssh.close();
                }
                return existing;
            }

            // Capture welcome banner
            String banner = safeTrim(ssh.getWelcomeBanner());
            if (!banner.isEmpty()) {
                device.welcomeBanner = banner;
            }

            // Parse banner and MOTD to extract firmware, model, hostname via regex
            parseBannerAndMotd(device, banner, motd);

            // Fallback: get hostname via SSH if not parsed from MOTD prompt
            if (device.hostname.isEmpty()) {
                device.hostname = safeTrim(runner.execStdout("hostname"));
            }

            // Fallback: chipset-specific identity (only fills empty fields)
            device.refreshIdentity(runner);

            device.online = true;
            cache.putDevice(mac, device);
            log.info("Created {} device: {} (MAC: {}, model: {})", chipsetType, cfg.hostname, mac, device.model);
            return device;
        } catch (Exception e) {
            log.warn("Failed to initialize device at {}: {}", cfg.hostname, e.getMessage());
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
     * Detect chipset by probing for known wireless tools and analyzing MOTD.
     */
    private static String detectFWandChipset(SshRunner runner, String motd) {
        // First try to detect from MOTD
        if (!motd.isEmpty()) {
            String motdLower = motd.toLowerCase();

            // Check for OpenWrt in MOTD
            if (motdLower.contains("openwrt")) {
                return "openwrt";
            }

            // Check for Tomato in MOTD
            if (motdLower.contains("tomato")) {
                return "tomato";
            }

            // Check for DD-WRT in MOTD
            if (motdLower.contains("dd-wrt")) {
                // Further distinguish by checking for specific tools
                SshRunner.CommandResult result;
                try {
                    result = runner.execResult("which wl_atheros");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        return "atheros";
                    }
                } catch (Exception e) {
                    // continue
                }

                try {
                    result = runner.execResult("which wl");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        return "broadcom";
                    }
                } catch (Exception e) {
                    // continue
                }

                try {
                    result = runner.execResult("which iwinfo");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        return "marvell";
                    }
                } catch (Exception e) {
                    // continue
                }
            }
        }

        // Fallback to command probing if MOTD detection failed
        // Try Atheros (DD-WRT)
        SshRunner.CommandResult result;
        try {
            result = runner.execResult("which wl_atheros");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return "atheros";
            }
        } catch (Exception e) {
            // continue
        }

        // Try Broadcom (DD-WRT)
        try {
            result = runner.execResult("which wl");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return "broadcom";
            }
        } catch (Exception e) {
            // continue
        }

        // Try iwinfo (OpenWrt / Marvell DD-WRT)
        try {
            result = runner.execResult("which iwinfo");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                // Distinguish OpenWrt from Marvell DD-WRT
                SshRunner.CommandResult openwrtCheck = runner.execResult("cat /etc/openwrt_release");
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
            result = runner.execResult("which iw");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return "generic";
            }
        } catch (Exception e) {
            // continue
        }

        // Try Tomato detection (check for Tomato-specific files or nvram)
        try {
            result = runner.execResult("nvram get os_name");
            if (result.isSuccess() && result.getStdout().toLowerCase().contains("tomato")) {
                return "tomato";
            }
        } catch (Exception e) {
            // continue
        }

        return "generic";
    }

    private static DDWRTBaseDevice createForFWandChipset(String chipset, DDWRTDeviceConfiguration cfg, Logger log) {
        return switch (chipset) {
            case "atheros" -> new DDWRTAtherosDevice(cfg, log);
            case "broadcom" -> new DDWRTBroadcomDevice(cfg, log);
            case "marvell" -> new DDWRTMarvellDevice(cfg, log);
            case "openwrt" -> new DDWRTOpenWrtDevice(cfg, log);
            case "tomato" -> new DDWRTTomatoDevice(cfg, log);
            default -> new DDWRTGenericDevice(cfg, log);
        };
    }

    /**
     * Parse the SSH banner and MOTD to extract firmware type/version, board model, and hostname
     * using regex. Populates the device fields directly, saving SSH commands.
     */
    private static void parseBannerAndMotd(DDWRTBaseDevice device, String banner, String motd) {
        // DD-WRT banner: extract version and board
        if (!banner.isEmpty()) {
            Matcher versionMatcher = DDWRT_VERSION_PATTERN.matcher(banner);
            if (versionMatcher.find()) {
                device.firmware = Objects.requireNonNull(safeTrim("DD-WRT " + versionMatcher.group(1)));
            }
            Matcher boardMatcher = DDWRT_BOARD_PATTERN.matcher(banner);
            if (boardMatcher.find()) {
                device.model = Objects.requireNonNull(safeTrim(boardMatcher.group(1)));
            }
        }

        // MOTD parsing: OpenWrt, Tomato, and shell prompt hostname
        if (!motd.isEmpty()) {
            // OpenWrt MOTD: extract version
            Matcher owrtMatcher = OPENWRT_VERSION_PATTERN.matcher(motd);
            if (owrtMatcher.find()) {
                String ver = "OpenWrt " + owrtMatcher.group(1);
                if (owrtMatcher.group(2) != null) {
                    ver += " " + owrtMatcher.group(2);
                }
                device.firmware = Objects.requireNonNull(safeTrim(ver));
            }

            // Tomato MOTD: extract version (e.g. "Tomato v1.28..." or "FreshTomato 2023.5...")
            Matcher tomatoVerMatcher = TOMATO_VERSION_PATTERN.matcher(motd);
            if (tomatoVerMatcher.find()) {
                device.firmware = Objects
                        .requireNonNull(safeTrim(tomatoVerMatcher.group(1) + " " + tomatoVerMatcher.group(2)));
            }

            // Tomato MOTD: extract model and hostname from Welcome line
            Matcher tomatoWelcomeMatcher = TOMATO_WELCOME_PATTERN.matcher(motd);
            if (tomatoWelcomeMatcher.find()) {
                if (device.model.isEmpty()) {
                    device.model = Objects.requireNonNull(safeTrim(tomatoWelcomeMatcher.group(1)));
                }
                String parsedHostname = safeTrim(tomatoWelcomeMatcher.group(2));
                if (!parsedHostname.isEmpty()) {
                    device.hostname = parsedHostname;
                }
            }

            // Shell prompt: extract hostname (e.g. "root@movie-ap:~#")
            if (device.hostname.isEmpty()) {
                Matcher promptMatcher = MOTD_PROMPT_PATTERN.matcher(motd);
                if (promptMatcher.find()) {
                    String parsedHostname = safeTrim(promptMatcher.group(2));
                    if (!parsedHostname.isEmpty()) {
                        device.hostname = parsedHostname;
                    }
                }
            }
        }
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
        logger.debug("Started refresh every {}s", intervalSeconds);
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
            logger.debug("Refresh failed: {}", e.getMessage());
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
        // Portable uptime: parse /proc/uptime (works on all Linux including BusyBox)
        uptime = safeTrim(runner.execStdout("awk '{d=int($1/86400);h=int($1%86400/3600);m=int($1%3600/60);"
                + "printf \"%dd %dh %dm\",d,h,m}' /proc/uptime"));

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
        wanIp = refreshWanIp(runner);

        // Interface traffic from /proc/net/dev
        parseInterfaceTraffic(runner);
    }

    /**
     * Read CPU temperature in degrees Celsius. Default implementation tries /proc/dmu/temperature
     * then /sys/class/thermal/thermal_zone0/temp (millidegrees). Subclasses may override for
     * chipset-specific parsing.
     */
    protected double refreshCpuTemp(SshRunner runner) {
        // Single command: try DMU first, fall back to thermal_zone (avoids guaranteed rc=1)
        String tempStr = safeTrim(runner.execStdout(
                "cat /proc/dmu/temperature | grep -oE '[0-9.]+' || cat /sys/class/thermal/thermal_zone0/temp"));
        if (tempStr.isEmpty()) {
            return 0.0;
        }
        try {
            double val = Double.parseDouble(tempStr);
            // thermal_zone reports millidegrees (values > 1000); DMU reports degrees directly
            return val > 1000.0 ? val / 1000.0 : val;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Get WAN IP address. Default uses nvram. Subclasses without nvram should override.
     */
    protected String refreshWanIp(SshRunner runner) {
        String rawWanIp = safeTrim(runner.execStdout("nvram get wan_ipaddr"));
        return (rawWanIp.isEmpty() || "0.0.0.0".equals(rawWanIp)) ? "" : rawWanIp;
    }

    /**
     * Get the LAN bridge interface name for /proc/net/dev traffic parsing.
     * Default is "br0" (DD-WRT/Tomato). OpenWrt overrides to "br-lan".
     */
    protected String getLanInterface() {
        return "br0";
    }

    private void parseInterfaceTraffic(SshRunner runner) {
        // WAN interface — only fetch if device has a real WAN IP (is a gateway)
        String wanIface = isGateway() ? getWanInterface(runner) : "";
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

        // LAN interface (overridable: br0 for DD-WRT/Tomato, br-lan for OpenWrt)
        String lanIface = getLanInterface();
        String lanLine = safeTrim(
                runner.execStdout("cat /proc/net/dev | grep '" + lanIface + "' | awk '{print $2, $10}'"));
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
     * Get the WAN interface name. Default uses nvram. Subclasses without nvram should override.
     */
    protected String getWanInterface(SshRunner runner) {
        return safeTrim(runner.execStdout("nvram get wan_iface"));
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
        if (model.isEmpty()) {
            model = safeTrim(
                    runner.execStdout("cat /sys/devices/virtual/dmi/id/product_name || cat /proc/device-tree/model"));
        }
        // Generic Linux: os-release for firmware/distro
        if (firmware.isEmpty()) {
            firmware = safeTrim(runner.execStdout("cat /etc/os-release | grep PRETTY_NAME | cut -d'\"' -f2"));
        }
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
     * Get a MAC address from 'ip l' output, filtering by interface name prefixes.
     * Uses BusyBox-compatible 'ip l' (not 'ip -br l').
     *
     * @param runner SSH runner
     * @param ifacePrefixes awk regex alternation of interface prefixes, e.g. "en|eth|wl|br"
     * @return lowercase MAC address, or empty string if not found
     */
    protected static String getMacFromIpLink(SshRunner runner, String ifacePrefixes) {
        return safeTrim(runner.execStdout("ip l | awk '/^[0-9]+: (" + ifacePrefixes
                + ")/{f=1} f && /link\\/ether/{print tolower($2); f=0}' | LC_ALL=C sort | head -n1"));
    }

    /**
     * Get the first MAC address from any interface via 'ip l' output.
     * BusyBox-compatible fallback when interface name filtering is not needed.
     */
    protected static String getAnyMacFromIpLink(SshRunner runner) {
        return safeTrim(runner.execStdout("ip l | awk '/link\\/ether/{print tolower($2)}' | head -n1"));
    }

    /**
     * Get the MAC address for this device using chipset-specific method.
     * This method is called after firmware/chipset detection.
     */
    protected abstract String getDeviceMac(SshRunner runner);

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
                logger.warn("Reboot command failed: {}", e.getMessage());
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
                logger.info("Recovered SSH session");
                return newSession;
            } else {
                newSession.close();
                return null;
            }
        } catch (Exception e) {
            logger.debug("Session recovery failed: {}", e.getMessage());
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
