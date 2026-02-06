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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single DD-WRT device (router/AP). Handles its own SSH-backed initialization
 * and refresh routines.
 *
 * This class provides a static factory {@code createDevice(DDWRTDeviceConfiguration cfg)}
 * to construct and initialize a device using SSH based on the provided configuration.
 *
 * NOTE on session lifecycle:
 * - By default the factory uses short-lived SSH sessions to query attributes.
 * - If you prefer to keep a persistent session attached to the device, call
 * {@link #attachSession(SshRunner)} from the caller and manage its lifecycle.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTDevice {
    @SuppressWarnings("null")
    private static final Logger logger = LoggerFactory.getLogger(DDWRTDevice.class);

    private DDWRTDeviceConfiguration config;

    // Identity / basic attributes
    private String mac = "";
    private String hostname = "";
    private String model = "";
    private String firmware = "";
    private String welcomeBanner = "";

    // Persistent SSH session owned by this device
    private @Nullable SshAuthSession authSession;

    private volatile @Nullable DDWRTThingUpdater updater;

    // // ---- autonomous refresh state ----
    // private volatile long intervalSeconds = 0;
    // private volatile boolean deepRefresh = false;

    // private final AtomicBoolean refreshRunning = new AtomicBoolean(false);

    // // Core Scheduler job (preferred)
    // private @Nullable ScheduledCompletableFuture<@Nullable Void> ohJob;

    // // Fallback local executor (used only if Core Scheduler unavailable)
    // private static final ScheduledThreadPoolExecutor FALLBACK_EXEC =
    // new ScheduledThreadPoolExecutor(Math.max(2, Runtime.getRuntime().availableProcessors() / 4));

    // :TODO: ballle98/openhab-addon#18 add refresh and log monitoring threads to device class
    // private @Nullable ScheduledFuture<?> refreshJob;
    // private @Nullable ScheduledFuture<?> logFollowerJob;

    /** Private constructor: use the factory methods. */
    private DDWRTDevice(DDWRTDeviceConfiguration cfg) {
        this.config = cfg;
    }

    /**
     * Factory: create and initialize a device by connecting via SSH using the provided configuration.
     * The method performs lightweight commands to collect MAC, model and firmware. If SSH fails,
     * a partially initialized device is returned (hostname is set from cfg.hostname).
     *
     * @param cfg device configuration (host, user, password, port, refresh interval)
     * @return device (initialized where possible; fields may be partial if SSH fails)
     */
    public static DDWRTDevice upsertDeviceInNetwork(DDWRTNetwork net, DDWRTDeviceConfiguration cfg) {
        DDWRTDevice d = new DDWRTDevice(cfg);
        SshAuthSession ssh = null;

        try {
            ssh = SshClientManager.getInstance().openAuthSession(Objects.requireNonNull(cfg.hostname),
                    cfg.port, Objects.requireNonNull(cfg.user), cfg.password, null, null,
                    Objects.requireNonNull(Duration.ofSeconds(2)));

            // Capture banner for later inspection
            String banner = safeTrim(ssh.getWelcomeBanner());
            if (!banner.isEmpty()) {
                d.welcomeBanner = banner;
                logger.debug("Welcome banner:\n{}", banner);
            }

            SshRunner runner = ssh.createRunner();

            // Get MAC address with fallback methods for DD-WRT/Linux hosts
            String mac = runner.execStdout("nvram get lan_hwaddr");
            if (mac.isEmpty()) {
                // Fallback: Try modern Linux ip command (ethernet interfaces)
                mac = runner.execStdout(
                        "ip -br l | grep -E '^(en|eth|wl)' | awk '{print tolower($3)}' | LC_ALL=C sort | head -n1");
            }

            String hostname = runner.execStdout("hostname");
            String model = runner.execStdout("grep -i 'Board:' /tmp/loginprompt | cut -d' ' -f 2-");
            String fw = runner.execStdout("grep -i DD-WRT /tmp/loginprompt | cut -d' ' -f-2");

            if (!hostname.isEmpty()) {
                d.hostname = hostname;
            }
            if (!model.isEmpty()) {
                d.model = model;
            }
            if (!fw.isEmpty()) {
                d.firmware = fw;
            }
            if (!mac.isEmpty()) {
                d.mac = mac.toLowerCase();

                // Check if device already exists with this MAC
                DDWRTDevice existingDevice = net.getDeviceByMac(d.mac);
                if (existingDevice != null) {
                    // Device exists, check if credentials need updating
                    DDWRTDeviceConfiguration existingConfig = existingDevice.getConfig();
                    if (!Objects.equals(existingConfig.password, cfg.password)
                            || !Objects.equals(existingConfig.user, cfg.user) || existingConfig.port != cfg.port) {

                        logger.info("Updating credentials for existing device: {} (MAC: {})", cfg.hostname, d.mac);
                        // Update the existing device with new configuration
                        existingDevice.setConfig(cfg);
                        // Close old session and attach new one
                        existingDevice.closeSessionQuietly();
                        existingDevice.attachSession(ssh);
                        return existingDevice;
                    } else {
                        // Same credentials, use existing device
                        logger.debug("Device already exists with same credentials: {} (MAC: {})", cfg.hostname, d.mac);
                        return existingDevice;
                    }
                }

                // New device, add to network
                d.attachSession(ssh);
                net.upsertDeviceByMac(d.mac, d);
            }

            // Session is now attached to device for persistent use

        } catch (Exception e) {
            logger.warn("Failed to initialize device at host {}: {}", cfg.hostname, e.getMessage(), e);
            // Track the failed configuration for retry during refresh
            net.addFailedDeviceConfig(cfg.hostname, cfg);
        } finally {
            // Close session only if it wasn't attached to a device
            if (ssh != null && (d.mac.isEmpty() || net.getDeviceByMac(d.mac) == null)) {
                try {
                    ssh.close();
                } catch (Exception e) {
                    logger.debug("Error closing SSH session for {}: {}", cfg.hostname, e.getMessage());
                }
            }
        }

        // If MAC was not successfully retrieved, track as failed
        if (d.mac.isEmpty()) {
            logger.debug("Device at host {} did not provide MAC address, marking as failed", cfg.hostname);
            net.addFailedDeviceConfig(cfg.hostname, cfg);
        }

        return d;
    }

    // /**
    // * Enable device-managed periodic refresh. No handler required.
    // */
    // public synchronized void enableAutonomousRefresh(long intervalSeconds, boolean deep) {
    // disableAutonomousRefresh(); // cleanup previous schedule

    // this.intervalSeconds = Math.max(1, intervalSeconds);
    // this.deepRefresh = deep;

    // final @Nullable Scheduler scheduler = DDWRTRuntime.getScheduler();
    // if (scheduler != null) {
    // // Use openHAB Core Scheduler (preferred)
    // TemporalAdjuster everyNSeconds = temporal -> temporal.plus(Duration.ofSeconds(this.intervalSeconds));
    // this.ohJob = scheduler.schedule(() -> {
    // runRefreshSafely(this.deepRefresh);
    // return null;
    // }, everyNSeconds);
    // LOGGER.debug("Autonomous {} refresh started via Core Scheduler every {}s (mac={}, host={})",
    // deep ? "deep" : "basic", this.intervalSeconds, mac, hostname);
    // } else {
    // // Fallback: local executor (unit-test or non-OH context)
    // this.fallbackFuture = FALLBACK_EXEC.scheduleWithFixedDelay(() -> {
    // runRefreshSafely(this.deepRefresh);
    // }, 0, this.intervalSeconds, TimeUnit.SECONDS);
    // LOGGER.debug("Autonomous {} refresh started via fallback executor every {}s (mac={}, host={})",
    // deep ? "deep" : "basic", this.intervalSeconds, mac, hostname);
    // }
    // }

    // /** Stop autonomous periodic refresh. */
    // public synchronized void disableAutonomousRefresh() {
    // final var job = this.ohJob;
    // this.ohJob = null;
    // if (job != null) {
    // job.cancel(true);
    // }
    // final var f = this.fallbackFuture;
    // this.fallbackFuture = null;
    // if (f != null) {
    // f.cancel(true);
    // }
    // LOGGER.debug("Autonomous refresh stopped (mac={}, host={})", mac, hostname);
    // }

    // /** On-demand refresh (independent of handlers). */
    // public void triggerRefresh(boolean deep) {
    // runRefreshSafely(deep);
    // }

    // /** Guard against overlapping runs. */
    // private void runRefreshSafely(boolean deep) {
    // if (!refreshRunning.compareAndSet(false, true)) {
    // LOGGER.trace("Refresh already running; skipping (deep={})", deep);
    // return;
    // }
    // try {
    // if (deep) {
    // refreshDeep();
    // } else {
    // refreshBasic();
    // }
    // } finally {
    // refreshRunning.set(false);
    // }
    // }

    // // ---- existing refresh logic (SSH etc.) ----
    // public void refreshBasic() {
    // final DDWRTDeviceConfiguration cfg = this.lastConfig;
    // if (cfg == null) {
    // LOGGER.debug("No configuration available for refreshBasic (mac={}, host={})", mac, hostname);
    // return;
    // }
    // try {
    // // SSH quick attributes…
    // LOGGER.trace("refreshBasic completed (mac={}, host={})", mac, hostname);
    // } catch (Exception e) {
    // LOGGER.debug("refreshBasic failed for {}: {}", cfg.hostname, e.getMessage(), e);
    // }
    // }

    // public void refreshDeep() {
    // final DDWRTDeviceConfiguration cfg = this.lastConfig;
    // if (cfg == null) {
    // LOGGER.debug("No configuration available for refreshDeep (mac={}, host={})", mac, hostname);
    // return;
    // }
    // try {
    // // SSH deeper attributes…
    // LOGGER.trace("refreshDeep completed (mac={}, host={})", mac, hostname);
    // } catch (Exception e) {
    // LOGGER.debug("refreshDeep failed for {}: {}", cfg.hostname, e.getMessage(), e);
    // }
    // }

    /*
     * scheduler.execute(() -> {
     * try {
     * connect(config);
     * startLogFollowerIfEnabled(config);
     * schedulePeriodicRefresh(config);
     * updateStatus(ThingStatus.ONLINE);
     * } catch (Exception e) {
     * logger.warn("Init failed for {}: {}", getThing().getUID(), e.getMessage(), e);
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
     * }
     * });
     */

    // private void startLogFollowerIfEnabled(DDWRTDeviceConfiguration c) {
    // if (!c.logFollow) return;
    // final SshRunner s = this.ssh;
    // if (s == null) return;

    // this.logFollower = new SshLogFollower(s, "sh -lc 'logread -f'", line -> {
    // // MVP: just log syslog lines; add parser later.
    // log.trace("[{}] {}", getThing().getUID(), line);
    // }, () -> { if (!disposing) scheduler.schedule(this::restartLogFollowerSafe, 1500, TimeUnit.MILLISECONDS); });

    // scheduler.execute(logFollower);
    // }

    // private void restartLogFollowerSafe() {
    // stopLogFollower();
    // Config c = this.cfg;
    // if (c != null && !disposing) startLogFollowerIfEnabled(c);
    // }

    /*
     * 
     * private void schedulePeriodicRefresh(DDWRTDeviceConfiguration c) {
     * cancelPeriodicRefresh();
     * this.refreshJob = scheduler.scheduleWithFixedDelay(this::safeRefresh, 0, 600, TimeUnit.SECONDS);
     * }
     * 
     * private void cancelPeriodicRefresh() {
     * ScheduledFuture<?> job = this.refreshJob;
     * this.refreshJob = null;
     * if (job != null)
     * job.cancel(true);
     * }
     * 
     * private void safeRefresh() {
     * try {
     * doRefresh();
     * } catch (Exception e) {
     * logger.debug("Refresh failed: {}", e.getMessage(), e);
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
     * }
     * }
     * 
     * private void doRefresh() throws IOException {
     * final SshRunner s = requireSsh();
     * // final DDWRTDeviceConfiguration c = Objects.requireNonNull(this.cfg);
     * 
     * // Example 1: uptime
     * String uptime = s.exec("uptime -s", Duration.ofSeconds(2)).trim();
     * postString(CHANNEL_UPTIME, uptime);
     * 
     * // // Example 2: WAN IP if gateway
     * // if ("gateway".equalsIgnoreCase(Optional.ofNullable(c.role).orElse(""))) {
     * // String wanIf = s.exec("nvram get wan_iface", Duration.ofSeconds(2)).trim();
     * // if (!wanIf.isEmpty()) {
     * // String wanIp = s.exec("ip -4 -o addr show dev " + wanIf + " | awk '{print $4}' | cut -d/ -f1",
     * // Duration.ofSeconds(2)).trim();
     * // if (!wanIp.isEmpty()) postString(CHANNEL_WAN_IP, wanIp);
     * // }
     * // }
     * 
     * // postOnline(true);
     * }
     * 
     * // helpers
     * private SshRunner requireSsh() throws IOException {
     * SshRunner s = this.ssh;
     * if (s == null)
     * throw new IOException("SSH not connected");
     * return s;
     * }
     * 
     * private void closeSsh() {
     * final SshRunner s = this.ssh; // copy nullable field to a local
     * try {
     * if (s != null) {
     * s.close();
     * }
     * } catch (Exception ignore) {
     * // no-op
     * }
     * this.ssh = null;
     * }
     * // private void stopLogFollower() { try { if (logFollower != null) logFollower.close(); } catch (Exception
     * ignore)
     * // {} logFollower = null; }
     * 
     * 
     * // private void postOnline(boolean on) {
     * // try { updateState(new ChannelUID(getThing().getUID(), CHANNEL_ONLINE), on ? OnOffType.ON : OnOffType.OFF); }
     * // catch (Exception ignore) {}
     * // }
     * 
     */

    /*
     * private void postString(String ch, String v) {
     * try {
     * updateState(new ChannelUID(thingUID, ch), new StringType(v));
     * } catch (Exception ignore) {
     * }
     * }
     */

    // -------------------- Session management --------------------
    /** Attach a persistent authenticated session to this device (device owns lifecycle). */
    public void attachSession(SshAuthSession ssh) {
        closeSessionQuietly();
        this.authSession = ssh;
    }

    public void closeSessionQuietly() {
        SshAuthSession s = this.authSession;
        this.authSession = null;
        if (s != null) {
            try {
                s.close();
            } catch (Exception ignore) {
                // no-op
            }
        }
    }

    private static String safeTrim(@Nullable String s) {
        return s == null ? "" : s.trim();
    }

    // -------------------- Getters / setters --------------------

    public DDWRTDeviceConfiguration getConfig() {
        return config;
    }

    public void setConfig(DDWRTDeviceConfiguration config) {
        this.config = config;
    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return hostname;
    }

    public String getModel() {
        return model;
    }

    public String getFirmware() {
        return firmware;
    }

    public String getWelcomeBanner() {
        return welcomeBanner;
    }

    public void setUpdater(@Nullable DDWRTThingUpdater updater) {
        this.updater = updater;
    }

    /** Check if device has an active SSH connection */
    public boolean hasActiveConnection() {
        SshAuthSession session = this.authSession;
        if (session == null) {
            return false;
        }
        try {
            return session.getClientSession().isOpen();
        } catch (Exception e) {
            logger.debug("Error checking SSH connection status: {}", e.getMessage());
            return false;
        }
    }

    /** Attempt to recover SSH session if it's closed */
    public boolean recoverSession() {
        if (config == null) {
            logger.debug("Cannot recover session: no configuration available");
            return false;
        }

        try {
            logger.debug("Attempting to recover SSH session for device: {}", getMac());

            // Close any existing dead session
            closeSessionQuietly();

            // Create new session
            SshAuthSession newSession = SshClientManager.getInstance().openAuthSession(
                    Objects.requireNonNull(config.hostname), config.port, Objects.requireNonNull(config.user),
                    config.password, null, null, Objects.requireNonNull(java.time.Duration.ofSeconds(2)));

            // Test the new session with a simple command
            SshRunner runner = newSession.createRunner();
            String testResult = runner.exec("echo 'session_test'");

            if ("session_test".equals(testResult.trim())) {
                this.authSession = newSession;
                logger.info("Successfully recovered SSH session for device: {}", getMac());
                return true;
            } else {
                newSession.close();
                logger.warn("Session recovery test failed for device: {}", getMac());
                return false;
            }

        } catch (Exception e) {
            logger.warn("Failed to recover SSH session for device {}: {}", getMac(), e.getMessage(), e);
            return false;
        }
    }

    public void refresh() {
        synchronized (this) {
            logger.debug("Refreshing {} {}", getMac(), getName());

            final DDWRTThingUpdater u = this.updater; // local copy for thread safety

            try {
                SshAuthSession s = this.authSession;
                if (s == null) {
                    logger.debug("No SSH session available, attempting recovery for device: {}", getMac());
                    if (recoverSession()) {
                        s = this.authSession;
                    } else {
                        if (u != null) {
                            u.reportOffline("SSH session not connected and recovery failed");
                        }
                        return;
                    }
                }

                // Validate session is still active before using it
                if (!hasActiveConnection()) {
                    logger.debug("SSH session is no longer active, attempting recovery for device: {}", getMac());
                    if (recoverSession()) {
                        s = this.authSession;
                        if (s == null) {
                            logger.error("Recovery returned null session for device: {}", getMac());
                            if (u != null) {
                                u.reportOffline("Session recovery failed - null session");
                            }
                            return;
                        }
                    } else {
                        logger.debug("Session recovery failed for device: {}", getMac());
                        this.authSession = null; // Clear dead session
                        if (u != null) {
                            u.reportOffline("SSH session closed and recovery failed");
                        }
                        return;
                    }
                }

                // Final check that session is not null
                if (s == null) {
                    logger.error("SSH session is null before command execution for device: {}", getMac());
                    if (u != null) {
                        u.reportOffline("SSH session is null");
                    }
                    return;
                }

                SshRunner runner = s.createRunner();
                String uptime = safeTrim(runner.exec("uptime -s"));

                // postString(CHANNEL_UPTIME, uptime);
                if (u != null) {
                    u.updateChannel("uptime", new org.openhab.core.library.types.StringType(uptime));
                    u.reportOnline();
                }

            } catch (Exception e) {
                logger.debug("Refresh failed for device {}: {}", getMac(), e.getMessage(), e);

                // Check if this is a session-related error and attempt recovery
                if (e.getMessage() != null && (e.getMessage().contains("session is being closed")
                        || e.getMessage().contains("Channel") || e.getMessage().contains("closed"))) {
                    logger.warn("SSH session error during refresh for device: {}, clearing session and will retry",
                            getMac());
                    this.authSession = null; // Clear dead session
                    // Note: Recovery will be attempted on next refresh cycle
                }

                if (u != null) {
                    u.reportOffline(e.getMessage());
                }
            }

        }
    }
}
