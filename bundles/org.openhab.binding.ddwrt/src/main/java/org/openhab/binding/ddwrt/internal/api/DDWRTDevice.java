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

import org.apache.sshd.client.session.ClientSession;
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
    private static final Logger logger = LoggerFactory.getLogger(DDWRTDevice.class);

    private DDWRTDeviceConfiguration config;

    // Identity / basic attributes
    private String mac = "";
    private String hostname = "";
    private String model = "";
    private String firmware = "";

    // Optional: keep a session handle (depends on your SshRunner API). If used, caller must manage lifecycle.
    private @Nullable ClientSession session;
    private @Nullable SshRunner runner;

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

        try (SshRunner ssh = SshClientManager.getInstance().openRunner(Objects.requireNonNull(cfg.hostname), cfg.port,
                Objects.requireNonNull(cfg.user), cfg.password, null, null,
                Objects.requireNonNull(Duration.ofSeconds(2)))) {
            // Example DD-WRT commands (adjust to environment as needed)
            // Primary MAC address
            String mac = safeTrim(ssh.exec("nvram get lan_hwaddr"));
            // Hostname (WAN hostname or router name)
            String hostname = safeTrim(ssh.exec("hostname"));
            // Model/board or CPU info
            String model = safeTrim(ssh.exec(" grep -i 'Board:' /tmp/loginprompt | cut -d' ' -f 2-"));
            // Firmware / build version
            String fw = safeTrim(ssh.exec("grep -i DD-WRT /tmp/loginprompt | cut -d' ' -f-2"));

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
                net.upsertDeviceByMac(d.mac, d);
            }

            // If you want to persist a session, attach it here (not recommended for long-lived sessions):
            // d.attachSession(ssh); // WARNING: using try-with-resources will close ssh at the end of the block.
        } catch (Exception e) {
            // Device is still returned; caller can retry via refreshBasic/refreshDeep
            logger.warn("Failed to initialize device at host {}: {}", cfg.hostname, e.getMessage(), e);
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
     * private void postString(String ch, String v) {
     * try {
     * updateState(new ChannelUID(getThing().getUID(), ch), new StringType(v));
     * } catch (Exception ignore) {
     * }
     * }
     * 
     * // private void postOnline(boolean on) {
     * // try { updateState(new ChannelUID(getThing().getUID(), CHANNEL_ONLINE), on ? OnOffType.ON : OnOffType.OFF); }
     * // catch (Exception ignore) {}
     * // }
     * 
     */

    // -------------------- Session management (optional) --------------------

    /** Attach a persistent session to this device (caller manages lifecycle). */
    public void attachSession(SshRunner ssh) {
    }

    /** Close and clear any attached session. */
    public void closeSessionQuietly() {
    }

    // -------------------- Utility --------------------

    private static String safeTrim(@Nullable String s) {
        return s == null ? "" : s.trim();
    }

    // -------------------- Getters / setters --------------------

    public DDWRTDeviceConfiguration getConfig() {
        return config;
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

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public void refresh() {
        synchronized (this) {
            logger.debug("Refreshing {} {}", getMac(), getName());
        }
    }
}
