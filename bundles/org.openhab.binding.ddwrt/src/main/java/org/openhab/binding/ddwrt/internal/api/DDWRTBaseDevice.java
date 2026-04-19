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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.openhab.binding.ddwrt.internal.api.SyslogParser.SyslogEvent;
import org.openhab.core.library.types.DateTimeType;
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
public abstract class DDWRTBaseDevice implements SyslogListener {

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

    private static final Pattern ANSI_ESCAPE = Pattern.compile("\\u001B\\[[;?0-9]*[ -/]*[@-~]");
    private static final Pattern OSC_SEQUENCE = Pattern.compile("\\u001B\\].*?(\\u0007|\\u001B\\\\)");

    // hostapd association events: "phy0-ap0: AP-STA-CONNECTED aa:bb:cc:dd:ee:ff" or "AP-STA-DISCONNECTED"
    private static final Pattern HOSTAPD_STA_EVENT = Objects.requireNonNull(Pattern
            .compile("([\\w.-]+):\\s+AP-STA-(CONNECTED|DISCONNECTED)\\s+([0-9a-fA-F:]{17})", Pattern.CASE_INSENSITIVE));

    // DD-WRT kernel cfg80211 MLME events:
    // "wlan1: STA ac:c9:06:c4:be:7b MLME: assoc request, signal -60 (Accepted)"
    // "wlan1: STA ac:c9:06:c4:be:7b IEEE 802.11: disassociated"
    // "wlan1: STA ac:c9:06:c4:be:7b IEEE 802.11: authenticated"
    // Group 1=iface, 2=MAC, 3=action keyword (assoc/disassoc/authenticated/deauthenticated)
    private static final Pattern MLME_STA_EVENT = Objects.requireNonNull(Pattern.compile(
            "([\\w.-]+):\\s+STA\\s+([0-9a-fA-F:]{17})\\s+.*?(assoc|disassoc|authenticated|deauthenticated)",
            Pattern.CASE_INSENSITIVE));

    // DHCPACK events: "DHCPACK(br0) 192.168.0.83 9a:2a:33:74:e4:11 Lee-Pixel-8a"
    private static final Pattern DHCPACK_EVENT = Objects.requireNonNull(Pattern
            .compile("DHCPACK\\(\\S+\\)\\s+(\\S+)\\s+([0-9a-fA-F:]{17})(?:\\s+(\\S+))?", Pattern.CASE_INSENSITIVE));

    // Identity
    protected String mac = "";
    protected String hostname = "";
    protected String model = "";
    protected String firmware = "";
    protected String welcomeBanner = "";
    protected String chipset = "unknown";
    protected String hardware = "";

    // Telemetry (populated during refresh)
    protected long uptimeSeconds = 0;
    protected @Nullable Instant bootInstant = null;
    protected @Nullable Instant lastPublishedBootInstant = null;
    protected boolean uptimeSinceChanged = false;
    protected double cpuLoad = 0.0;
    protected double cpuTemp = 0.0;
    protected String cpuModel = "";
    protected double wl0Temp = 0.0;
    protected double wl1Temp = 0.0;

    // Discovered radio interface names (populated by refreshRadios, used by refreshCpuTemp)
    protected volatile List<String> radioIfaceNames = Objects.requireNonNull(Collections.emptyList());
    protected String wanIp = "";
    protected long wanIn = 0;
    protected long wanOut = 0;
    protected long ifIn = 0;
    protected long ifOut = 0;
    protected boolean online = false;

    // Firewall rules
    protected final List<DDWRTFirewallRule> firewallRules = new ArrayList<>();

    // Syslog event counters and last-event state (like logreader binding)
    protected long warningEventCount = 0;
    protected long errorEventCount = 0;
    protected String lastWarningEvent = "";
    protected String lastErrorEvent = "";
    protected String lastDhcpEvent = "";
    protected String lastWirelessEvent = "";

    // Cached /etc/hosts content for hostname resolution (only on gateway devices)
    private final Map<String, String> hostsCache = new ConcurrentHashMap<>();
    private volatile boolean hostsCacheValid = false;

    // Cached DHCP leases - only updated on initial load and DHCP events
    private volatile boolean dhcpLeasesCacheValid = false;

    // Configuration
    protected DDWRTDeviceConfiguration config;

    // SSH session (persistent, shared across exec + log channels)
    protected @Nullable SshAuthSession authSession;

    // Dedicated refresh thread and scheduler
    private @Nullable ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> periodicRefreshTrigger;
    private @Nullable Thread refreshThread;
    private final ReentrantLock refreshLock = new ReentrantLock();
    private final Condition refreshSignal = refreshLock.newCondition();
    private volatile boolean refreshThreadStopped = false;

    // Syslog follower for real-time log monitoring
    private volatile @Nullable SshLogFollower logFollower;

    // Session recovery backoff — independent counters for auth vs network failures.
    // Auth failure proves network is up, so it resets the network counter.
    // Network failure is independent and does not affect the auth counter.
    private volatile int recoveryAuthFailures = 0;
    private volatile long lastRecoveryAuthAttemptMs = 0;
    private volatile int recoveryNetFailures = 0;
    private volatile long lastRecoveryNetAttemptMs = 0;

    /** Reset recovery backoff so the next recovery attempt happens immediately. */
    public void resetRecoveryBackoff() {
        recoveryAuthFailures = 0;
        lastRecoveryAuthAttemptMs = 0;
        recoveryNetFailures = 0;
        lastRecoveryNetAttemptMs = 0;
    }

    // Updater callback (set by handler)
    protected volatile @Nullable DDWRTThingUpdater updater;

    // Network cache reference (set during createDevice)
    protected @Nullable DDWRTNetworkCache networkCache;

    // Network reference (set by DDWRTNetwork after createDevice)
    private volatile @Nullable DDWRTNetwork network;

    public void setNetwork(@Nullable DDWRTNetwork network) {
        this.network = network;
    }

    protected DDWRTBaseDevice(DDWRTDeviceConfiguration cfg, Logger logger) {
        this.config = cfg;
        this.logger = logger;
    }

    private static String stripControlCodes(String value) {
        if (value.isEmpty()) {
            return value;
        }
        String withoutOsc = OSC_SEQUENCE.matcher(value).replaceAll("");
        return ANSI_ESCAPE.matcher(withoutOsc).replaceAll("").trim();
    }

    // ---- Factory with chipset auto-detection ----

    /**
     * Create and initialize a device by connecting via SSH and auto-detecting the chipset.
     * Returns the appropriate subclass, or throws on failure so callers can classify the error.
     */
    public static @Nullable DDWRTBaseDevice createDevice(DDWRTNetworkCache cache, DDWRTDeviceConfiguration cfg)
            throws IOException {
        Logger log = LoggerFactory.getLogger(DDWRTBaseDevice.class.getName() + "." + cfg.hostname);
        SshAuthSession ssh = null;
        try {
            String host = Objects.requireNonNull(cfg.hostname);
            Duration timeout = Objects.requireNonNull(Duration.ofSeconds(5));
            ssh = SshClientManager.getInstance().openAuthSession(host, cfg.port, cfg.getEffectiveUser(), cfg.password,
                    timeout);

            // Capture MOTD for firmware/chipset detection first
            String motd = stripControlCodes(ssh.captureMotd());

            // Create a runner for chipset detection
            SshRunner runner = ssh.createRunner();

            // Detect OS and chipset, then create appropriate subclass
            DetectionResult result = detectFWandChipset(runner, motd);
            DDWRTBaseDevice device = createForFWandChipset(result.os, result.chipset, cfg, log);
            device.chipset = result.chipset;
            device.hardware = result.hardware;
            device.authSession = ssh;

            // Now get MAC address using device-specific method
            String mac = device.getDeviceMac(runner);
            if (mac.isEmpty()) {
                log.warn("Could not determine MAC for device at {}", cfg.hostname);
                ssh.close();
                return null;
            }

            mac = Objects.requireNonNull(mac.toLowerCase(Locale.ROOT).trim());
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
            String banner = stripControlCodes(safeTrim(ssh.getWelcomeBanner()));
            if (!banner.isEmpty()) {
                device.welcomeBanner = banner;
            }

            // Capture SSH server ident for additional hints
            @Nullable
            String ident = ssh.getClientSession().getServerVersion();

            // Parse banner, MOTD, and ident to extract firmware, model, hostname via regex
            parseBannerAndMotd(device, banner, motd, ident);

            log.debug("Parsed banner/MOTD/ident: firmware='{}', model='{}', hostname='{}'", device.firmware,
                    device.model, device.hostname);

            if ((device.firmware.isEmpty() || device.model.isEmpty())) {
                log.debug("MOTD/banner/ident did not determine firmware/model (firmware='{}', model='{}')",
                        device.firmware, device.model);
            }

            // Fallback: get hostname via SSH if not parsed from MOTD prompt
            if (device.hostname.isEmpty()) {
                device.hostname = safeTrim(runner.execStdout("hostname"));
            }

            // Fallback: chipset-specific identity (only fills empty fields)
            device.refreshIdentity(runner);

            device.online = true;
            device.networkCache = cache;

            // Load hosts cache if this is a gateway device (has WAN IP)
            if (device.isGateway()) {
                device.refreshHostsCache(runner);
            }

            cache.putDevice(mac, device);
            log.info("Created {} device: {} (MAC: {}, model: {})", result.chipset, cfg.hostname, mac, device.model);
            return device;
        } catch (IOException e) {
            log.warn("Failed to initialize device at {}: {}", cfg.hostname, e.getMessage());
            if (ssh != null) {
                try {
                    ssh.close();
                } catch (Exception ignore) {
                    // no-op
                }
            }
            throw e;
        } catch (Exception e) {
            log.warn("Failed to initialize device at {}: {}", cfg.hostname, e.getMessage());
            if (ssh != null) {
                try {
                    ssh.close();
                } catch (Exception ignore) {
                    // no-op
                }
            }
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Result of firmware and chipset detection.
     */
    private static class DetectionResult {
        final String os; // openwrt, tomato, dd-wrt, generic
        final String chipset; // atheros, broadcom, marvell, generic
        final String hardware;

        DetectionResult(String os, String chipset, String hardware) {
            this.os = os;
            this.chipset = chipset;
            this.hardware = hardware;
        }
    }

    /**
     * Detect chipset by probing for known wireless tools and analyzing MOTD.
     */
    private static DetectionResult detectFWandChipset(SshRunner runner, String motd) {
        // First try to detect from MOTD
        if (!motd.isEmpty()) {
            String motdLower = motd.toLowerCase(Locale.ROOT);

            // Check for OpenWrt in MOTD
            if (motdLower.contains("openwrt")) {
                // Try to detect chipset for OpenWrt using DISTRIB_TARGET
                try {
                    // Get DISTRIB_TARGET from /etc/openwrt_release
                    String distribTarget = runner
                            .execStdout("grep '^DISTRIB_TARGET=' /etc/openwrt_release 2>/dev/null | cut -d\\' -f2");
                    if (!distribTarget.isEmpty()) {
                        String target = distribTarget.trim();
                        String[] parts = target.split("/");
                        if (parts.length >= 2) {
                            String vendor = parts[0];

                            // Map OpenWrt target vendors to our chipset names
                            switch (vendor) {
                                case "mediatek":
                                    return new DetectionResult("openwrt", "mediatek", target);
                                case "qualcomm":
                                    return new DetectionResult("openwrt", "atheros", target); // Qualcomm Atheros
                                case "ath79":
                                case "ar71xx":
                                    return new DetectionResult("openwrt", "atheros", target);
                                case "brcm":
                                case "broadcom":
                                    return new DetectionResult("openwrt", "broadcom", target);
                                case "mvebu":
                                case "armada":
                                    return new DetectionResult("openwrt", "marvell", target);
                                case "ramips":
                                case "rt305x":
                                    return new DetectionResult("openwrt", "ralink", target);
                                case "ipq":
                                    return new DetectionResult("openwrt", "qualcomm", target); // Qualcomm IPQ
                                default:
                                    return new DetectionResult("openwrt", vendor, target);
                            }
                        }
                    }
                } catch (Exception e) {
                    // continue to generic
                }
                return new DetectionResult("openwrt", "generic", "");
            }

            // Check for Tomato in MOTD
            if (motdLower.contains("tomato")) {
                return new DetectionResult("tomato", "generic", "");
            }

            // Check for DD-WRT in MOTD
            if (motdLower.contains("dd-wrt")) {
                // Further distinguish by checking hardware and tools
                SshRunner.CommandResult result;

                // Check /proc/cpuinfo for hardware line and store it
                try {
                    result = runner.execResult("grep 'Hardware.*:' /proc/cpuinfo");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        String hwLine = result.getStdout().trim();
                        // Extract hardware name after "Hardware :"
                        String hwName = hwLine.replaceFirst("^.*Hardware\s*:\s*(.*)$", "$1").trim();
                        // Check if hardware contains Marvell
                        if (hwLine.toLowerCase(Locale.ROOT).contains("marvell")) {
                            return new DetectionResult("dd-wrt", "marvell", hwName);
                        }
                    }
                } catch (Exception e) {
                    // continue
                }

                try {
                    result = runner.execResult("which wl_atheros");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        return new DetectionResult("dd-wrt", "atheros", "");
                    }
                } catch (Exception e) {
                    // continue
                }

                try {
                    result = runner.execResult("which wl");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        return new DetectionResult("dd-wrt", "broadcom", "");
                    }
                } catch (Exception e) {
                    // continue
                }

                try {
                    result = runner.execResult("which iwinfo");
                    if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                        return new DetectionResult("dd-wrt", "marvell", "");
                    }
                } catch (Exception e) {
                    // continue
                }
            }
        }

        // Fallback to command probing if MOTD detection failed
        SshRunner.CommandResult result;

        // Check /proc/cpuinfo for hardware line first
        try {
            result = runner.execResult("grep 'Hardware.*:' /proc/cpuinfo");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                String hwLine = result.getStdout().trim();
                // Extract hardware name after "Hardware :"
                String hwName = hwLine.replaceFirst("^.*Hardware\s*:\s*(.*)$", "$1").trim();
                // Check if hardware contains Marvell
                if (hwLine.toLowerCase(Locale.ROOT).contains("marvell")) {
                    return new DetectionResult("dd-wrt", "marvell", hwName);
                }
            }
        } catch (Exception e) {
            // continue
        }

        // Try iwinfo (OpenWrt)
        try {
            result = runner.execResult("which iwinfo");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return new DetectionResult("openwrt", "generic", "");
            }
        } catch (Exception e) {
            // continue
        }

        // Try Atheros (DD-WRT)
        try {
            result = runner.execResult("which wl_atheros");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return new DetectionResult("dd-wrt", "atheros", "");
            }
        } catch (Exception e) {
            // continue
        }

        // Try Broadcom (DD-WRT)
        try {
            result = runner.execResult("which wl");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return new DetectionResult("dd-wrt", "broadcom", "");
            }
        } catch (Exception e) {
            // continue
        }

        // Try iw (generic Linux)
        try {
            result = runner.execResult("which iw");
            if (result.isSuccess() && !result.getStdout().trim().isEmpty()) {
                return new DetectionResult("generic", "generic", "");
            }
        } catch (Exception e) {
            // continue
        }

        // Try Tomato detection (check for Tomato-specific files or nvram)
        try {
            result = runner.execResult("nvram get os_name");
            if (result.isSuccess() && result.getStdout().toLowerCase(Locale.ROOT).contains("tomato")) {
                return new DetectionResult("tomato", "generic", "");
            }
        } catch (Exception e) {
            // continue
        }

        return new DetectionResult("generic", "generic", "");
    }

    private static DDWRTBaseDevice createForFWandChipset(String os, String chipset, DDWRTDeviceConfiguration cfg,
            Logger log) {
        return switch (os) {
            case "dd-wrt" -> switch (chipset) {
                case "atheros" -> new DDWRTAtherosDevice(cfg, log);
                case "broadcom" -> new DDWRTBroadcomDevice(cfg, log);
                case "marvell" -> new DDWRTMarvellDevice(cfg, log);
                default -> new DDWRTGenericDevice(cfg, log);
            };
            case "openwrt" -> new DDWRTOpenWrtDevice(cfg, log);
            case "tomato" -> new DDWRTTomatoDevice(cfg, log);
            default -> new DDWRTGenericDevice(cfg, log);
        };
    }

    /**
     * Parse the SSH banner and MOTD to extract firmware type/version, board model, and hostname
     * using regex. Populates the device fields directly, saving SSH commands.
     */
    private static void parseBannerAndMotd(DDWRTBaseDevice device, String banner, String motd, @Nullable String ident) {
        // DD-WRT: prefer banner, but fall back to MOTD (some builds only expose version in MOTD)
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
        if (!motd.isEmpty()) {
            if (device.firmware.isEmpty()) {
                Matcher versionMatcher = DDWRT_VERSION_PATTERN.matcher(motd);
                if (versionMatcher.find()) {
                    device.firmware = Objects.requireNonNull(safeTrim("DD-WRT " + versionMatcher.group(1)));
                }
            }
            if (device.model.isEmpty()) {
                Matcher boardMatcher = DDWRT_BOARD_PATTERN.matcher(motd);
                if (boardMatcher.find()) {
                    device.model = Objects.requireNonNull(safeTrim(boardMatcher.group(1)));
                }
            }
        }

        // MOTD parsing: OpenWrt, Tomato, and shell prompt hostname
        if (!motd.isEmpty()) {
            String firstLine = safeTrim(motd.split("\\R", 2)[0]);

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
            Matcher tomatoVerMatcher = TOMATO_VERSION_PATTERN.matcher(firstLine);
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
     * Start periodic refresh and optional syslog monitoring.
     */
    public void start(DDWRTThingUpdater updater) {
        this.updater = updater;
        startRefresh(config.refreshInterval);
    }

    /**
     * Start periodic refresh without updater (for network layer).
     * The updater will be set later by the device handler.
     */
    public synchronized void startRefresh(int refreshInterval) {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        if (refreshThread == null) {
            // Start syslog follower BEFORE refresh to avoid concurrent SSH channel usage
            startSyslogFollower();

            // Create and start dedicated refresh thread
            refreshThreadStopped = false;
            Thread rt = new Thread(new RefreshRunnable(refreshInterval), "DDWRT-Refresh-" + hostname);
            rt.setDaemon(true);
            rt.start();
            refreshThread = rt;

            logger.info("Started device refresh every {} seconds", refreshInterval);
        }
    }

    /**
     * Start syslog follower for real-time log monitoring.
     */
    private void startSyslogFollower() {
        SshAuthSession auth = authSession;
        if (auth == null || !auth.getClientSession().isOpen()) {
            logger.debug("Skipping syslog follower - no active SSH session");
            return;
        }

        try {
            SshRunner runner = auth.createRunner();
            String command = buildSyslogCommand(runner);
            if (command.isEmpty()) {
                logger.debug("No syslog command available for {} — skipping syslog follower", hostname);
                return;
            }
            SshLogFollower follower = new SshLogFollower(() -> {
                SshAuthSession s = authSession;
                return s != null ? s.getClientSession() : null;
            }, command, getSyslogPattern(), hostname);
            follower.setListener(this);

            Thread t = new Thread(follower, "ddwrt-syslog-" + hostname);
            t.setDaemon(true);
            t.start();
            logFollower = follower;

            logger.info("Started syslog follower for {}: {}", hostname, command);
        } catch (Exception e) {
            logger.warn("Failed to start syslog follower: {}", e.getMessage());
        }
    }

    /**
     * Dedicated refresh thread that waits for signals (periodic or immediate).
     * Ensures only one refresh runs at a time, preventing SSH channel conflicts.
     */
    private class RefreshRunnable implements Runnable {
        private final long refreshIntervalMs;

        RefreshRunnable(int refreshIntervalSeconds) {
            this.refreshIntervalMs = refreshIntervalSeconds * 1000L;
        }

        @Override
        public void run() {
            logger.debug("Refresh thread started for {}", hostname);

            // Schedule periodic trigger
            ScheduledExecutorService sched = scheduler;
            if (sched != null) {
                periodicRefreshTrigger = sched.scheduleWithFixedDelay(() -> {
                    refreshLock.lock();
                    try {
                        refreshSignal.signal();
                    } finally {
                        refreshLock.unlock();
                    }
                }, refreshIntervalMs, refreshIntervalMs, TimeUnit.MILLISECONDS);
            }

            // Main refresh loop
            while (!refreshThreadStopped) {
                refreshLock.lock();
                try {
                    // Wait for signal (periodic or immediate) with timeout
                    refreshSignal.await(refreshIntervalMs, TimeUnit.MILLISECONDS);

                    if (!refreshThreadStopped) {
                        logger.debug("Performing refresh for {}", hostname);
                        refresh();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Catch-all: prevent unchecked exceptions (e.g. from dead SSH session)
                    // from killing the refresh thread. Log and continue so the device can
                    // recover on the next refresh cycle via ensureSession().
                    logger.warn("Refresh failed for {}, will retry next cycle: {}", hostname, e.getMessage());
                    logger.debug("Refresh exception details", e);
                } finally {
                    refreshLock.unlock();
                }
            }

            logger.debug("Refresh thread stopped for {}", hostname);
        }
    }

    /**
     * Returns the syslog regex pattern for this device type.
     * Subclasses override to provide firmware-specific patterns.
     */
    protected @Nullable Pattern getSyslogPattern() {
        // Default standard syslog format
        return Pattern.compile(
                "^([A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(\\S+)\\s+([^:\\[\\s]+(?:\\.[^:\\[\\s]+)*)(?:\\[(\\d+)\\])?:\\s*(.*)$");
    }

    /**
     * Build the syslog command based on firmware type and detected configuration.
     * DD-WRT and Tomato may use BusyBox syslogd with -O <path> for file logging;-O /path/to/messages} for file-based
     * logging;
     * if detected, we tail that file. OpenWrt uses procd/logd with a ring buffer read by
     * {@code logread -f}. Tomato defaults to {@code /var/log/messages}.
     */
    protected String buildSyslogCommand(SshRunner runner) {
        String fwLower = firmware.toLowerCase(Locale.ROOT);

        // OpenWrt: prefer ubus subscribe for real-time log streaming (no orphans)
        if (fwLower.contains("openwrt")) {
            String ubusCheck = safeTrim(runner.execStdout("ubus list log 2>/dev/null"));
            if (!ubusCheck.isEmpty()) {
                logger.debug("Using ubus subscribe for log access on OpenWrt");
                return "ubus subscribe log";
            }
            // Fallback to traditional logread
            return "logread -f";
        }

        // DD-WRT and Tomato may use BusyBox syslogd with -O <path> for file logging
        if (fwLower.contains("dd-wrt") || fwLower.contains("tomato")) {
            String syslogdLine = safeTrim(runner.execStdout("ps w | grep '[s]yslogd' | head -1"));
            String syslogdArgs = "";
            if (!syslogdLine.isEmpty()) {
                // Parse the file path from the syslogd line in Java
                // Example: "1331 root 1264 S syslogd -Z -b 5 -L -O /jffs/log/messages"
                String[] parts = syslogdLine.split("\\s+-O\\s+");
                if (parts.length > 1) {
                    syslogdArgs = parts[1].split("\\s+")[0]; // Get first word after -O
                }
            }
            if (!syslogdArgs.isEmpty()) {
                logger.debug("Detected syslogd file output: {}", syslogdArgs);
                return "tail -F " + syslogdArgs;
            }
            // DD-WRT without -O flag: syslogd logs to /var/log/messages by default.
            // If -C is present, a shared-memory ring buffer is available via logread -f.
            // Otherwise, tail the default log file.
            if (fwLower.contains("dd-wrt")) {
                if (!syslogdLine.isEmpty() && syslogdLine.contains(" -C")) {
                    return "logread -f";
                }
                return "tail -F /var/log/messages";
            }
            // Tomato default
            return "tail -F /var/log/messages";
        }

        // Generic Linux with systemd
        return "journalctl -f --no-pager -p " + config.syslogPriority;
    }

    // ---- SyslogListener implementation ----

    @Override
    public void onDhcpEvent(SyslogEvent event) {
        lastDhcpEvent = event.message;
        logger.debug("DHCP event: {}", event.message);

        DDWRTThingUpdater u = updater;
        if (u != null) {
            u.updateChannel("last-dhcp-event", new StringType(event.message));
            u.fireTrigger("dhcp-event", event.message);
        }

        // Invalidate caches so next periodic refresh picks up full state
        dhcpLeasesCacheValid = false;
        if (isGateway()) {
            hostsCacheValid = false;
        }

        // Try to parse DHCPACK and update cache directly
        DDWRTNetworkCache cache = networkCache;
        if (cache != null) {
            Matcher m = DHCPACK_EVENT.matcher(event.message);
            if (m.find()) {
                String ip = Objects.requireNonNull(m.group(1));
                String clientMac = Objects.requireNonNull(m.group(2)).toLowerCase(Locale.ROOT);
                String dhcpHostname = m.group(3); // may be null if hostname not in ACK

                // Handle MAC randomization: merge if this hostname exists under a different MAC
                if (dhcpHostname != null && !dhcpHostname.isEmpty() && !"*".equals(dhcpHostname)) {
                    cache.mergeRandomizedMac(clientMac, dhcpHostname);
                }

                final String finalHostname = (dhcpHostname != null && !"*".equals(dhcpHostname)) ? dhcpHostname : "";

                // Update the wireless client directly in the cache
                cache.computeWirelessClient(clientMac, client -> {
                    if (!finalHostname.isEmpty() && !finalHostname.equals(client.getHostname())) {
                        client.setHostname(finalHostname);
                    }
                    if (!ip.isEmpty() && !ip.equals(client.getIpAddress())) {
                        client.setIpAddress(ip);
                    }
                    client.setOnline(true);
                    client.setLastSeen(Instant.now());
                    return client;
                });
                logger.debug("[DHCPACK] {} hostname={} ip={} on {}", clientMac, finalHostname, ip, hostname);
                // No full refresh needed — cache listeners will notify handlers
                return;
            }
        }

        // Fallback: non-DHCPACK event — schedule full refresh for reconciliation
        scheduleImmediateRefresh();
    }

    @Override
    public void onWirelessEvent(SyslogEvent event) {
        lastWirelessEvent = event.message;
        logger.debug("Wireless event: {}", event.message);

        DDWRTThingUpdater u = updater;
        if (u != null) {
            u.updateChannel("last-wireless-event", new StringType(event.message));
            u.fireTrigger("wireless-event", event.message);
        }

        // Try to parse AP-STA-CONNECTED/DISCONNECTED and update cache directly
        DDWRTNetworkCache cache = networkCache;
        if (cache != null) {
            Matcher m = HOSTAPD_STA_EVENT.matcher(event.message);
            if (m.find()) {
                String iface = Objects.requireNonNull(m.group(1));
                String action = Objects.requireNonNull(m.group(2));
                String clientMac = Objects.requireNonNull(m.group(3)).toLowerCase(Locale.ROOT);
                String radioName = (hostname.isEmpty() ? mac : hostname) + "-" + iface;

                if ("CONNECTED".equalsIgnoreCase(action)) {
                    // Look up the radio to get its SSID and channel
                    String radioId = mac + ":" + iface;
                    DDWRTRadio radio = cache.getRadio(radioId);
                    String ssid = radio != null ? radio.getSsid() : "";
                    int radioChannel = radio != null ? radio.getChannel() : 0;

                    // Update the wireless client directly in the cache
                    cache.computeWirelessClient(clientMac, client -> {
                        client.setApMac(mac);
                        client.setIface(iface);
                        client.setRadioName(radioName);
                        if (!ssid.isEmpty()) {
                            client.setSsid(ssid);
                        }
                        if (radioChannel > 0) {
                            client.setChannel(radioChannel);
                        }
                        client.setOnline(true);
                        client.setLastSeen(Instant.now());
                        return client;
                    });
                    logger.debug("[AP-CONNECT] {} connected on {} ssid={} ap={}", clientMac, radioName, ssid, hostname);
                } else {
                    // DISCONNECTED — mark client offline
                    DDWRTWirelessClient existing = cache.getWirelessClient(clientMac);
                    if (existing != null && mac.equals(existing.getApMac())) {
                        cache.computeWirelessClient(clientMac, client -> {
                            client.setOnline(false);
                            return client;
                        });
                        logger.debug("[AP-DISCONNECT] {} disconnected from {} ap={}", clientMac, radioName, hostname);
                    }
                }
                // No full refresh needed — cache listeners will notify handlers
                return;
            }

            // Try DD-WRT kernel cfg80211 MLME events:
            // "wlan1: STA xx:xx MLME: assoc request, signal -60 (Accepted)"
            // "wlan1: STA xx:xx IEEE 802.11: disassociated"
            Matcher mlme = MLME_STA_EVENT.matcher(event.message);
            if (mlme.find()) {
                String iface = Objects.requireNonNull(mlme.group(1));
                String clientMac = Objects.requireNonNull(mlme.group(2)).toLowerCase(Locale.ROOT);
                String action = Objects.requireNonNull(mlme.group(3)).toLowerCase(Locale.ROOT);
                String radioName = (hostname.isEmpty() ? mac : hostname) + "-" + iface;

                boolean isConnect = "assoc".equals(action) || "authenticated".equals(action);
                boolean isDisconnect = "disassoc".equals(action) || "deauthenticated".equals(action);

                if (isConnect) {
                    String radioId = mac + ":" + iface;
                    DDWRTRadio radio = cache.getRadio(radioId);
                    String ssid = radio != null ? radio.getSsid() : "";
                    int radioChannel = radio != null ? radio.getChannel() : 0;

                    cache.computeWirelessClient(clientMac, client -> {
                        client.setApMac(mac);
                        client.setIface(iface);
                        client.setRadioName(radioName);
                        if (!ssid.isEmpty()) {
                            client.setSsid(ssid);
                        }
                        if (radioChannel > 0) {
                            client.setChannel(radioChannel);
                        }
                        client.setOnline(true);
                        client.setLastSeen(Instant.now());
                        return client;
                    });
                    logger.debug("[AP-CONNECT] {} connected on {} ssid={} ap={} (MLME)", clientMac, radioName, ssid,
                            hostname);
                } else if (isDisconnect) {
                    DDWRTWirelessClient existing = cache.getWirelessClient(clientMac);
                    if (existing != null && mac.equals(existing.getApMac())) {
                        cache.computeWirelessClient(clientMac, client -> {
                            client.setOnline(false);
                            return client;
                        });
                        logger.debug("[AP-DISCONNECT] {} disconnected from {} ap={} (MLME)", clientMac, radioName,
                                hostname);
                    }
                }
                // No full refresh needed — cache listeners will notify handlers
                return;
            }
        }

        // Fallback: unrecognized wireless event — schedule full refresh
        scheduleImmediateRefresh();
    }

    @Override
    public void onWarningEvent(SyslogEvent event) {
        warningEventCount++;
        lastWarningEvent = event.message;

        DDWRTThingUpdater u = updater;
        if (u != null) {
            u.updateChannel("warning-events", new DecimalType(warningEventCount));
            u.updateChannel("last-warning-event", new StringType(event.message));
            u.fireTrigger("warning-event", event.message);
        }
    }

    @Override
    public void onErrorEvent(SyslogEvent event) {
        errorEventCount++;
        lastErrorEvent = event.message;

        DDWRTThingUpdater u = updater;
        if (u != null) {
            u.updateChannel("error-events", new DecimalType(errorEventCount));
            u.updateChannel("last-error-event", new StringType(event.message));
            u.fireTrigger("error-event", event.message);
        }
    }

    /**
     * Trigger an immediate full refresh (e.g. after DHCP or wireless event).
     * Signals the dedicated refresh thread to wake up and refresh immediately.
     */
    private void scheduleImmediateRefresh() {
        refreshLock.lock();
        try {
            refreshSignal.signal();
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * Stop the per-device thread pool and syslog follower.
     */
    public synchronized void stopRefresh() {
        // Stop syslog follower
        stopSyslogFollower();

        // Stop periodic refresh trigger
        ScheduledFuture<?> trigger = periodicRefreshTrigger;
        if (trigger != null) {
            trigger.cancel(true);
            periodicRefreshTrigger = null;
        }

        // Stop scheduler
        ScheduledExecutorService sched = scheduler;
        if (sched != null && !sched.isShutdown()) {
            sched.shutdown();
            try {
                if (!sched.awaitTermination(5, TimeUnit.SECONDS)) {
                    sched.shutdownNow();
                }
            } catch (InterruptedException e) {
                sched.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        scheduler = null;

        // Stop refresh thread
        refreshLock.lock();
        try {
            refreshThreadStopped = true;
            refreshSignal.signalAll();
            Thread rt = refreshThread;
            if (rt != null) {
                try {
                    rt.join(5000);
                } catch (InterruptedException e) {
                    rt.interrupt();
                    Thread.currentThread().interrupt();
                }
            }
            refreshThread = null;
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * Stop syslog follower.
     */
    private void stopSyslogFollower() {
        SshLogFollower follower = logFollower;
        if (follower != null) {
            try {
                follower.close();
                logger.debug("Stopped syslog follower for {}", hostname);
            } catch (Exception e) {
                logger.debug("Error stopping syslog follower: {}", e.getMessage());
            }
            logFollower = null;
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
                u.reportOffline("@text/offline.ssh-session-unavailable");
            }
            return;
        }

        long startNanos = System.nanoTime();
        SshRunner runner = s.createRunner();
        refreshCommon(runner);

        // DHCP leases and hosts cache only exist on gateway devices (dnsmasq/DNS server)
        if (isGateway()) {
            refreshDhcpLeases(runner);
            refreshHostsCache(runner);
        }

        refreshRadios(runner);
        refreshWirelessClients(runner);
        // Firewall rules only apply to gateway devices; APs have firewall disabled
        if (isGateway()) {
            refreshFirewallRules(runner);
        }
        online = true;

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        logger.debug("Refresh complete for {} in {}.{} s", hostname, elapsedMs / 1000,
                String.format("%03d", elapsedMs % 1000));

        DDWRTThingUpdater u = updater;
        if (u != null) {
            u.reportOnline();
            pushChannels(u);
        }

        // Notify network that refresh is complete (triggers discovery, etc.)
        DDWRTNetwork net = network;
        if (net != null) {
            net.fireRefreshComplete(this);
        }
    }

    /**
     * Common telemetry refresh (all chipsets).
     */
    protected void refreshCommon(SshRunner runner) {
        // Portable uptime in seconds: parse /proc/uptime (works on all Linux including BusyBox)
        String uptimeSecStr = safeTrim(runner.execStdout("cut -d. -f1 /proc/uptime"));
        long parsedSeconds = 0;
        if (!uptimeSecStr.isEmpty()) {
            try {
                parsedSeconds = Long.parseLong(uptimeSecStr);
            } catch (NumberFormatException e) {
                parsedSeconds = 0;
            }
        }

        uptimeSeconds = parsedSeconds;
        Instant currentBootInstant = parsedSeconds > 0 ? Instant.now().minusSeconds(parsedSeconds) : null;
        bootInstant = currentBootInstant;
        // Mark for publication only when boot time moves forward (reboot) or never published
        Instant lastPublished = lastPublishedBootInstant;
        if (currentBootInstant != null && (lastPublished == null || currentBootInstant.isAfter(lastPublished))) {
            uptimeSinceChanged = true;
        }

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

        // Firewall rules are refreshed in refresh() after wanIp is known
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
        if (uptimeSinceChanged && bootInstant != null) {
            ZonedDateTime bootTime = ZonedDateTime.ofInstant(Objects.requireNonNull(bootInstant), ZoneOffset.UTC);
            u.updateChannel("uptime", new DateTimeType(bootTime));
            lastPublishedBootInstant = bootInstant;
            uptimeSinceChanged = false;
        }
        u.updateChannel("online", online ? OnOffType.ON : OnOffType.OFF);
        u.updateChannel("cpu-load", new DecimalType(cpuLoad));
        u.updateChannel("cpu-temp", new QuantityType<>(cpuTemp, SIUnits.CELSIUS));
        if (isGateway()) {
            u.updateChannel("wan-ip", new StringType(wanIp));
            u.updateChannel("wan-in", new DecimalType(wanIn));
            u.updateChannel("wan-out", new DecimalType(wanOut));
        }
        u.updateChannel("if-in", new DecimalType(ifIn));
        u.updateChannel("if-out", new DecimalType(ifOut));
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
     * Refresh DHCP lease list from dnsmasq lease file.
     * Populates the cache with hostname and IP data keyed by MAC.
     * Lease file format: {@code expiry mac ip hostname [clientid]}
     * Only refreshes when cache is invalid (initial load or DHCP events).
     */
    protected void refreshDhcpLeases(SshRunner runner) {
        DDWRTNetworkCache cache = networkCache;
        if (cache == null) {
            return;
        }

        // Skip refresh if cache is valid (not initial load and no DHCP events)
        if (dhcpLeasesCacheValid) {
            return;
        }

        // Find lease file: check dnsmasq config first, then try common paths
        String output = safeTrim(runner.execStdout(
                "cat \"$(grep -sh 'dhcp-leasefile=' /tmp/dnsmasq.conf /var/etc/dnsmasq.conf* 2>/dev/null | head -1 | cut -d= -f2)\" 2>/dev/null"
                        + " || cat /tmp/dnsmasq.leases 2>/dev/null" + " || cat /tmp/dhcp.leases 2>/dev/null"
                        + " || cat /jffs/dnsmasq.leases 2>/dev/null"));
        if (output.isEmpty()) {
            return;
        }

        // Clear existing leases before re-parsing so stale entries are removed
        cache.clearDhcpLeases();

        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            // Format: expiry mac ip hostname [clientid]
            if (parts.length >= 4) {
                String leaseMac = Objects.requireNonNull(parts[1].toLowerCase(Locale.ROOT).trim());
                String ip = Objects.requireNonNull(parts[2]);
                String hostname = Objects.requireNonNull(parts[3]);
                if ("*".equals(hostname)) {
                    hostname = "";
                }

                long expiry = 0;
                try {
                    expiry = Long.parseLong(parts[0]);
                } catch (NumberFormatException e) {
                    // ignore
                }

                // If this hostname already has a lease under a different MAC, keep the most recent
                if (!hostname.isEmpty()) {
                    DDWRTDhcpLease existing = cache.getDhcpLeaseByHostname(hostname);
                    if (existing != null && !existing.getMac().equals(leaseMac)) {
                        if (expiry <= existing.getExpiry()) {
                            // This entry is older; skip it
                            continue;
                        }
                        // This entry is newer; remove the old one
                        logger.debug("DHCP lease for '{}': replacing old MAC {} with newer MAC {}", hostname,
                                existing.getMac(), leaseMac);
                    }
                }

                final String finalHostname = hostname;
                final String finalIp = ip;

                DDWRTDhcpLease lease = new DDWRTDhcpLease(Objects.requireNonNull(leaseMac));
                lease.setIpAddress(finalIp);
                lease.setHostname(finalHostname);
                lease.setExpiry(expiry);
                cache.putDhcpLease(Objects.requireNonNull(leaseMac), lease);

                // Handle MAC randomization: if this hostname exists under a different MAC, merge
                if (!finalHostname.isEmpty()) {
                    cache.mergeRandomizedMac(leaseMac, finalHostname);
                }

                // Skip creating wireless client entries for MACs that were merged away
                // (stale DHCP leases for old randomized MACs)
                if (cache.isMergedAwayMac(leaseMac)) {
                    logger.debug("Skipping merged-away MAC {} (hostname={}) in DHCP lease processing", leaseMac,
                            finalHostname);
                    continue;
                }

                // Thread-safe update existing wireless client with DHCP info
                cache.computeWirelessClient(leaseMac, client -> {
                    boolean changed = false;
                    if (!finalHostname.isEmpty() && !finalHostname.equals(client.getHostname())) {
                        client.setHostname(finalHostname);
                        changed = true;
                    }
                    if (!finalIp.isEmpty() && !finalIp.equals(client.getIpAddress())) {
                        client.setIpAddress(finalIp);
                        changed = true;
                    }
                    if (changed) {
                        logger.debug("Updated wireless client {} with DHCP info: hostname={}, ip={}", leaseMac,
                                finalHostname, finalIp);
                    }
                    return client;
                });
            }
        }
        dhcpLeasesCacheValid = true;
        logger.debug("Refreshed DHCP leases: {} entries", cache.getDhcpLeases().size());
    }

    /**
     * Refresh wireless client list using radio assoclists and DHCP leases.
     * Creates lightweight client entries without per-client SSH queries for RSSI/rates.
     */
    protected void refreshWirelessClients(SshRunner runner) {
        DDWRTNetworkCache cache = networkCache;
        if (cache == null) {
            return;
        }

        logger.debug("Refreshing wireless clients for {}", hostname);
        int totalClients = 0;

        // Use radios already in cache (populated by refreshRadios)
        for (DDWRTRadio radio : cache.getRadios()) {
            // Only process radios belonging to this device
            if (!radio.getParentDeviceMac().equals(mac)) {
                continue;
            }
            String radioName = (hostname.isEmpty() ? mac : hostname) + "-" + radio.getIfaceName();
            for (String clientMac : radio.getAssoclist()) {
                // Thread-safe update of wireless client (computeWirelessClient handles null case)
                cache.computeWirelessClient(clientMac, client -> {
                    // Update device-specific info
                    client.setApMac(mac);
                    client.setIface(radio.getIfaceName());
                    client.setRadioName(radioName);
                    client.setSsid(radio.getSsid());
                    client.setChannel(radio.getChannel());
                    client.setOnline(true);
                    client.setLastSeen(Instant.now());

                    // Populate hostname and IP from DHCP lease
                    // Try MAC-based lookup first, then hostname-based (handles MAC randomization)
                    DDWRTDhcpLease lease = cache.getDhcpLease(clientMac);
                    if (lease == null && !client.getHostname().isEmpty()) {
                        lease = cache.getDhcpLeaseByHostname(client.getHostname());
                    }
                    if (lease != null) {
                        if (!lease.getHostname().isEmpty()) {
                            client.setHostname(lease.getHostname());
                        }
                        if (!lease.getIpAddress().isEmpty()) {
                            client.setIpAddress(lease.getIpAddress());
                        }
                    }

                    // If still no hostname, try to resolve from cached /etc/hosts (only on gateway/DNS servers)
                    if (client.getHostname().isEmpty() && isGateway()) {
                        // Try by IP address if we have it
                        if (!client.getIpAddress().isEmpty()) {
                            String hostsEntry = hostsCache.get(client.getIpAddress());
                            if (hostsEntry != null && !hostsEntry.isEmpty()) {
                                client.setHostname(hostsEntry);
                                logger.debug("Resolved hostname for {} from cached /etc/hosts: {}", clientMac,
                                        hostsEntry);
                            }
                        }
                    }

                    // Last resort: generate hostname from OUI vendor prefix (skip randomized MACs)
                    if (client.getHostname().isEmpty() && !OuiDatabase.isRandomizedMac(clientMac)) {
                        String generated = OuiDatabase.generateHostname(clientMac);
                        if (!generated.isEmpty()) {
                            client.setHostname(generated);
                            logger.debug("Generated OUI hostname for {}: {}", clientMac, generated);
                        }
                    }

                    return client;
                });

                // Handle MAC randomization: merge old entry if hostname matches a different MAC
                DDWRTWirelessClient updated = cache.getWirelessClient(clientMac);
                if (updated != null && !updated.getHostname().isEmpty()) {
                    cache.mergeRandomizedMac(clientMac, updated.getHostname());
                }

                totalClients++;
            }
        }
        logger.debug("Refreshed wireless clients: {} total", totalClients);
    }

    /**
     * Refresh the cached /etc/hosts content for hostname resolution.
     * Only called on gateway devices (those with WAN IP).
     * Only refreshes when cache is invalid (initial load or DHCP events).
     */
    protected void refreshHostsCache(SshRunner runner) {
        // Skip refresh if cache is valid (not initial load and no DHCP events)
        if (hostsCacheValid) {
            return;
        }

        hostsCache.clear();
        String hostsContent = safeTrim(runner.execStdout("cat /etc/hosts 2>/dev/null"));
        if (!hostsContent.isEmpty()) {
            for (String line : hostsContent.split("\n")) {
                line = line.trim();
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Parse: IP hostname [alias1 ...]
                String[] parts = line.split("\\s+");
                if (parts.length >= 2 && parts[0] != null && parts[1] != null) {
                    String ip = parts[0];
                    String hostname = parts[1];
                    // Skip localhost entries
                    if (!ip.startsWith("127.") && !"localhost".equals(hostname)
                            && !"localhost.localdomain".equals(hostname)) {
                        hostsCache.put(Objects.requireNonNull(ip), Objects.requireNonNull(hostname));
                    }
                }
            }
            hostsCacheValid = true;
            logger.debug("Loaded {} entries into hosts cache", hostsCache.size());
        }
    }

    /**
     * Enumerate firewall rules. Default implementation parses DD-WRT nvram filter rules
     * using a single {@code nvram show | grep filter} command.
     * Only runs on firmware types that support nvram (DD-WRT, Tomato).
     * OpenWrt subclasses should override to use iptables/nftables commands.
     */
    protected List<DDWRTFirewallRule> enumerateFirewallRules(SshRunner runner) {
        List<DDWRTFirewallRule> rules = new ArrayList<>();

        // Only attempt nvram-based firewall rules on DD-WRT and Tomato devices
        if (!supportsNvram()) {
            logger.debug("Skipping nvram firewall rule enumeration - firmware '{}' does not support nvram",
                    firmware.isEmpty() ? "unknown" : firmware);
            return rules;
        }

        // Single command to get all filter rules at once
        String output = safeTrim(runner.execStdout("nvram show | grep filter_rule"));
        if (output.isEmpty()) {
            return rules;
        }

        for (String line : output.split("\n")) {
            String trimmed = line.trim();
            int eqIdx = trimmed.indexOf('=');
            if (eqIdx <= 0) {
                continue;
            }
            String ruleKey = Objects.requireNonNull(trimmed.substring(0, eqIdx));
            String ruleValue = Objects.requireNonNull(trimmed.substring(eqIdx + 1));
            if (!ruleValue.isEmpty()) {
                DDWRTFirewallRule rule = parseNvramFilterRule(ruleKey, ruleValue);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    /**
     * Check if the current firmware supports nvram commands.
     * DD-WRT and Tomato use nvram for configuration storage.
     * OpenWrt uses UCI instead.
     */
    protected boolean supportsNvram() {
        String fwLower = firmware.toLowerCase(Locale.ROOT);
        String modelLower = model.toLowerCase(Locale.ROOT);
        return fwLower.contains("dd-wrt") || modelLower.contains("dd-wrt") || fwLower.contains("tomato")
                || modelLower.contains("tomato");
    }

    /**
     * Parse DD-WRT nvram filter rule format: $STAT:$START-$END:from:to:proto:port:description
     */
    private @Nullable DDWRTFirewallRule parseNvramFilterRule(String ruleKey, String ruleValue) {
        if (ruleValue.isEmpty()) {
            return null;
        }

        try {
            String[] parts = ruleValue.split(":");
            if (parts.length < 7) {
                return null;
            }

            String status = parts[0]; // 1=enabled, 0=disabled
            // String range = parts[1]; // START-END (unused for now)
            String from = parts[2]; // source IP/network
            String to = parts[3]; // dest IP/network
            String proto = parts[4]; // tcp/udp/both
            String port = parts[5]; // port range or single port
            String description = parts.length > 6 ? parts[6] : "";

            boolean enabled = "1".equals(status);

            DDWRTFirewallRule rule = new DDWRTFirewallRule(ruleKey.replace("filter_", ""), ruleKey, mac);
            rule.setEnabled(enabled);
            rule.setRawValue(ruleValue);
            rule.setDescription(description);

            // Parse protocol
            if ("tcp".equalsIgnoreCase(proto)) {
                rule.setProtocol(DDWRTFirewallRule.Protocol.TCP);
            } else if ("udp".equalsIgnoreCase(proto)) {
                rule.setProtocol(DDWRTFirewallRule.Protocol.UDP);
            } else if ("both".equalsIgnoreCase(proto) || "tcpudp".equalsIgnoreCase(proto)) {
                rule.setProtocol(DDWRTFirewallRule.Protocol.TCP_UDP);
            }

            // Parse ports
            if (!port.isEmpty() && !"*".equals(port)) {
                if (port.contains("-")) {
                    // Port range - for simplicity, just take the first port
                    String[] portRange = port.split("-");
                    try {
                        rule.setDestPort(Integer.parseInt(portRange[0].trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else {
                    try {
                        rule.setDestPort(Integer.parseInt(port.trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }

            // Parse source/dest IPs
            if (!from.isEmpty() && !"*".equals(from) && !"any".equals(from)) {
                rule.setSourceIp(from);
            }
            if (!to.isEmpty() && !"*".equals(to) && !"any".equals(to)) {
                rule.setDestIp(to);
            }

            return rule;
        } catch (Exception e) {
            logger.debug("Failed to parse nvram rule {}='{}': {}", ruleKey, ruleValue, e.getMessage());
            return null;
        }
    }

    /**
     * Refresh radio information. Enumerates radios, gets lightweight assoclist MACs, and updates cache.
     */
    protected void refreshRadios(SshRunner runner) {
        DDWRTNetworkCache cache = networkCache;
        if (cache == null) {
            return;
        }

        List<DDWRTRadio> radios = enumerateRadios(runner);
        List<String> ifaceNames = new ArrayList<>();
        for (DDWRTRadio radio : radios) {
            List<String> clientMacs = getAssoclistMacs(runner, radio.getIfaceName());
            radio.setAssoclist(clientMacs);
            cache.putRadio(radio.getInterfaceId(), radio);
            ifaceNames.add(radio.getIfaceName());
            logger.debug("Refreshed radio {}: {} clients", radio.getIfaceName(), clientMacs.size());
        }
        radioIfaceNames = Objects.requireNonNull(Collections.unmodifiableList(ifaceNames));
    }

    /**
     * Refresh firewall rules from nvram and update cache.
     */
    protected void refreshFirewallRules(SshRunner runner) {
        firewallRules.clear();
        List<DDWRTFirewallRule> rules = enumerateFirewallRules(runner);
        firewallRules.addAll(rules);

        DDWRTNetworkCache cache = networkCache;
        if (cache != null) {
            for (DDWRTFirewallRule rule : rules) {
                cache.putFirewallRule(rule.getRuleId(), rule);
            }
        }
    }

    /**
     * Get associated wireless clients for a given interface (detailed info).
     */
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        return Objects.requireNonNull(Collections.emptyList());
    }

    /**
     * Get lightweight list of associated client MAC addresses for a given interface.
     * Subclasses override to parse chipset-specific assoclist output without per-client queries.
     */
    protected List<String> getAssoclistMacs(SshRunner runner, String iface) {
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
     * Public method to enable/disable a wireless radio interface.
     */
    public boolean setRadioEnabled(String iface, boolean enabled) {
        SshAuthSession s = authSession;
        if (s == null) {
            return false;
        }
        try {
            SshRunner runner = s.createRunner();
            setRadioEnabled(runner, iface, enabled);
            return true;
        } catch (Exception e) {
            logger.debug("Failed to {} radio {}: {}", enabled ? "enable" : "disable", iface, e.getMessage());
            return false;
        }
    }

    /**
     * Reboot the device.
     */
    public void reboot() {
        SshAuthSession s = authSession;
        if (s != null) {
            try {
                // Get the actual username from the authenticated session
                String sessionUser = s.getClientSession().getUsername();
                String cmd = "root".equals(sessionUser) ? "reboot" : "sudo reboot";
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
        // Independent backoff counters for auth vs network failures.
        // Auth failures (triggers Dropbear lockout): 30s, 60s then stop at 3
        // Network failures (no lockout risk): 10s, 20s, 40s, 80s, 160s, cap 300s, no stop
        // Auth failure proves network reachability → resets network counter.
        // Network failure does not affect auth counter.
        long now = System.currentTimeMillis();

        // Check auth backoff first (higher priority — lockout risk)
        // Stop at 3 (1 initial + 2 retries) to stay under Dropbear's 5-attempt block threshold
        if (recoveryAuthFailures >= 3) {
            logger.debug("Recovery suspended for {} after {} auth failures", config.hostname, recoveryAuthFailures);
            return null;
        }
        if (recoveryAuthFailures > 0) {
            long backoffMs = Math.min(30_000L * (1L << Math.min(recoveryAuthFailures - 1, 4)), 480_000L);
            long elapsed = now - lastRecoveryAuthAttemptMs;
            if (elapsed < backoffMs) {
                logger.debug("Recovery backoff for {} ({}s remaining, auth attempt {})", config.hostname,
                        (backoffMs - elapsed) / 1000, recoveryAuthFailures);
                return null;
            }
        }
        // Check network backoff
        if (recoveryNetFailures > 0) {
            long backoffMs = Math.min(10_000L * (1L << Math.min(recoveryNetFailures - 1, 5)), 300_000L);
            long elapsed = now - lastRecoveryNetAttemptMs;
            if (elapsed < backoffMs) {
                logger.debug("Recovery backoff for {} ({}s remaining, network attempt {})", config.hostname,
                        (backoffMs - elapsed) / 1000, recoveryNetFailures);
                return null;
            }
        }

        closeSessionQuietly();
        try {
            String host = Objects.requireNonNull(config.hostname);
            Duration timeout = Objects.requireNonNull(Duration.ofSeconds(5));
            SshAuthSession newSession = SshClientManager.getInstance().openAuthSession(host, config.port,
                    config.getEffectiveUser(), config.password, timeout);

            // Test the session
            SshRunner runner = newSession.createRunner();
            String test = runner.execStdout("echo ok");
            if ("ok".equals(test)) {
                authSession = newSession;
                recoveryAuthFailures = 0;
                lastRecoveryAuthAttemptMs = 0;
                recoveryNetFailures = 0;
                lastRecoveryNetAttemptMs = 0;
                logger.debug("Recovered SSH session for {}", config.hostname);
                SshLogFollower follower = logFollower;
                if (follower != null) {
                    follower.wakeUp();
                }
                return newSession;
            } else {
                newSession.close();
                recoveryNetFailures++;
                lastRecoveryNetAttemptMs = now;
                return null;
            }
        } catch (Exception e) {
            if (DDWRTNetwork.isAuthFailure(e)) {
                recoveryAuthFailures++;
                lastRecoveryAuthAttemptMs = now;
                // Auth failure proves network is up — reset network counter
                recoveryNetFailures = 0;
                lastRecoveryNetAttemptMs = 0;
                logger.debug("Session recovery auth failure for {} (attempt {}): {}", config.hostname,
                        recoveryAuthFailures, e.getMessage());
            } else {
                recoveryNetFailures++;
                lastRecoveryNetAttemptMs = now;
                logger.debug("Session recovery network failure for {} (attempt {}): {}", config.hostname,
                        recoveryNetFailures, e.getMessage());
            }
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

    public String getHardware() {
        return hardware;
    }

    public @Nullable Instant getBootInstant() {
        return bootInstant;
    }

    public DateTimeType getUptimeSince() {
        Instant b = bootInstant;
        return b != null ? new DateTimeType(ZonedDateTime.ofInstant(Objects.requireNonNull(b), ZoneOffset.UTC))
                : DateTimeType.valueOf("");
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getCpuTemp() {
        return cpuTemp;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public double getWl0Temp() {
        return wl0Temp;
    }

    public double getWl1Temp() {
        return wl1Temp;
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
     * Get the per-device scheduler for scheduling additional tasks (e.g. syslog follower).
     */
    public @Nullable ScheduledExecutorService getExecutor() {
        return scheduler;
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
