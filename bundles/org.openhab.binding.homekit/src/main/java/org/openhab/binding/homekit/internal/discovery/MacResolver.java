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
package org.openhab.binding.homekit.internal.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for resolving MAC addresses from IP addresses via the operating system's ARP cache.
 * If the MAC address is not found in the cache, it triggers an asynchronous resolution process that
 * involves pinging the IP to populate the ARP table and then bulk loading the OS ARP cache. Resolved
 * MAC addresses are cached in-memory with an expiration time to avoid frequent lookups.
 * <p>
 * This implementation uses a combination of:
 * <ul>
 * <li>In-memory timed cache</li>
 * <li>Bulk reading of the OS ARP table</li>
 * <li>Ping to populate the ARP cache when needed</li>
 * </ul>
 * Note: This class is a candidate to be integrated into OH Core utilities.
 * <p>
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = MacResolver.class)
public class MacResolver {

    private static final Duration IP_ADDRESS_PING_TIMEOUT = Duration.ofMillis(400);
    private static final Duration RESOLVE_MAC_TIMEOUT = Duration.ofSeconds(4);
    private static final Duration ARP_LOAD_PROCESS_TIMEOUT = Duration.ofMillis(1500);
    private static final Duration CACHE_VALIDITY_PERIOD = Duration.ofMinutes(7);
    private static final Duration BACKEND_TASK_RUN_INTERVAL = Duration.ofMillis(1200);

    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}");
    private static final Pattern IP_PATTERN = Pattern
            .compile("\\b((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}" + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\b");

    private final Logger logger = LoggerFactory.getLogger(MacResolver.class);

    // cache of IP / MAC mappings with expiration time stamps; prevents hitting the OS ARP cache too often
    private final Map<String, ExpiringMac> arpCache = new ConcurrentHashMap<>();

    // pending resolution tasks for IP addresses that are currently being resolved asynchronously
    private final Map<String, CompletableFuture<@Nullable String>> pendingFutures = new ConcurrentHashMap<>();

    private @NonNullByDefault({}) ExecutorService frontEndExecutor;
    private @NonNullByDefault({}) ScheduledExecutorService backEndScheduler;
    private @Nullable ScheduledFuture<?> backEndTaskSchedule;

    /**
     * Simple wrapper class to hold a MAC address with its expiration time-stamp.
     */
    private static class ExpiringMac {

        private final String mac;
        private final Instant expires;

        /**
         * Creates a new expiring MAC entry.
         */
        public ExpiringMac(String mac) {
            this.mac = mac;
            this.expires = Instant.now().plus(CACHE_VALIDITY_PERIOD);
        }

        /**
         * Returns the MAC address if not expired, otherwise {@code null}.
         */
        public @Nullable String getMac() {
            return isExpired() ? null : mac;
        }

        /**
         * Checks whether this entry has expired.
         */
        public boolean isExpired() {
            return Instant.now().isAfter(expires);
        }
    }

    @Activate
    protected void activate() {
        frontEndExecutor = ThreadPoolManager.getPool("OH-MacResolver-FrontEnd");
        backEndScheduler = ThreadPoolManager.getScheduledPool("OH-MacResolver-BackEnd");
    }

    @Deactivate
    protected void deactivate() {
        stopBackEndTaskSchedule();
        pendingFutures.values().forEach(future -> future.complete(null));
        pendingFutures.clear();
    }

    /**
     * Schedules a periodic task to load the ARP cache and complete pending futures. The scheduler is started
     * when the first resolution request is made, and stopped when there are no more pending resolutions to
     * avoid unnecessary resource usage.
     */
    private synchronized void startBackEndTaskSchedule() {
        if (backEndTaskSchedule != null) {
            return;
        }
        logger.trace("Starting back end");
        backEndTaskSchedule = backEndScheduler.scheduleWithFixedDelay(this::backEndTask,
                BACKEND_TASK_RUN_INTERVAL.toMillis(), BACKEND_TASK_RUN_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the ARP cache loading schedule if it is running. This is called when there are no more pending
     * resolution tasks to avoid unnecessary resource usage.
     */
    private synchronized void stopBackEndTaskSchedule() {
        ScheduledFuture<?> task;
        task = backEndTaskSchedule;
        backEndTaskSchedule = null;
        if (task != null) {
            logger.trace("Stopping back end");
            task.cancel(false);
        }
    }

    /**
     * Resolves the MAC address for a given IP address. If the MAC address is cached and valid, it is
     * returned immediately. Otherwise, an asynchronous resolution process is triggered that involves
     * pinging the IP to populate the ARP table and then bulk loading the ARP table. The returned future
     * will complete with the resolved MAC address or {@code null} if resolution fails or times out.
     * 
     * @param ipAddress the IP address to resolve e.g. "192.168.1.1" or "192.168.1.1:port"
     * @return a future that completes with the resolved MAC address or {@code null} if resolution fails
     *         or times out
     */
    public CompletableFuture<@Nullable String> resolveMac(String ipAddress) {
        String ip = normalizeIp(ipAddress);
        if (!isValidIp(ip)) {
            logger.debug("{} invalid", ipAddress);
            return CompletableFuture.completedFuture(null);
        }
        if (!isOnLocalSubnet(ip)) {
            logger.debug("{} not on local subnet", ip);
            return CompletableFuture.completedFuture(null);
        }

        // check for direct cache hit before scheduling asynchronous tasks
        cacheFlush();
        String cachedMac = cacheGet(ip);
        if (cachedMac != null) {
            logger.trace("{} -> {} (immediate)", ip, cachedMac);
            return CompletableFuture.completedFuture(cachedMac);
        }

        /*
         * schedule asynchronous resolution
         * concurrent requests for the same IP share the same future
         * the future completes with null if resolution takes too long
         * clean up pending futures when done
         */
        return Objects.requireNonNull(pendingFutures //
                .computeIfAbsent(ip, targetIp -> {
                    CompletableFuture<@Nullable String> baseFuture = new CompletableFuture<>();
                    startBackEndTaskSchedule();
                    frontEndExecutor.submit(() -> resolveMacAsync(targetIp, baseFuture));
                    return baseFuture;
                })) //
                .completeOnTimeout(null, RESOLVE_MAC_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((mac, ex) -> {
                    pendingFutures.remove(ip);
                    ifPendingFuturesEmpty();
                });
    }

    /**
     * Checks if the given IP address is on the same local subnet as any of the host's network interfaces. This
     * avoids ARP cache loads for IP addresses that are not local and therefore cannot be resolved via ARP.
     */
    private boolean isOnLocalSubnet(String ip) {
        try {
            InetAddress target = InetAddress.getByName(ip);
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InterfaceAddress ia : nif.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    int prefix = ia.getNetworkPrefixLength();
                    if (prefix <= 0 || prefix >= 32) {
                        continue;
                    }

                    byte[] a = addr.getAddress();
                    byte[] t = target.getAddress();
                    if (a.length != 4 || t.length != 4) {
                        continue; // IPv4 only
                    }

                    int mask = ~((1 << (32 - prefix)) - 1);
                    int ai = ByteBuffer.wrap(a).getInt();
                    int ti = ByteBuffer.wrap(t).getInt();

                    if ((ai & mask) == (ti & mask)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
            logger.debug("Subnet check failed for {}", ip, ignored);
        }
        return false;
    }

    /**
     * Checks if there are no more pending resolution tasks and stops the back end task schedule.
     * 
     * @param return true if pendingFutures is empty, false otherwise
     */
    private boolean ifPendingFuturesEmpty() {
        if (pendingFutures.isEmpty()) {
            stopBackEndTaskSchedule();
            return true;
        }
        return false;
    }

    /**
     * Asynchronous resolution process for a given IP address. It first checks if the IP is reachable and on the
     * local subnet to avoid unnecessary ARP cache loads for unreachable or non-local addresses. If the IP is
     * valid, it relies on the periodic ARP cache loader to populate the cache and complete the future when
     * the MAC address becomes available. If the IP is not reachable or not local, it completes the future
     * with {@code null} immediately.
     * 
     * @param ip the IP address to resolve
     * @param future the future to complete with the resolved MAC address or {@code null}
     */
    private void resolveMacAsync(String ip, CompletableFuture<@Nullable String> future) {
        boolean reachable = false;
        try {
            reachable = InetAddress.getByName(ip).isReachable((int) IP_ADDRESS_PING_TIMEOUT.toMillis());
        } catch (Exception e) {
            logger.debug("{} ping failed", ip, e);
            completeWithNull(ip, future);
        }
        if (!reachable) {
            logger.debug("{} not reachable", ip);
            completeWithNull(ip, future);
        }
    }

    /**
     * Completes the given future with {@code null} if it is still pending, and removes it from the pending
     * futures map. This is used to clean up pending resolution tasks that have failed or timed out.
     * 
     * @param ip the IP address associated with the future
     * @param future the future to complete with {@code null}
     */
    private void completeWithNull(String ip, CompletableFuture<@Nullable String> future) {
        if (pendingFutures.remove(ip, future)) {
            future.complete(null);
        }
        ifPendingFuturesEmpty();
    }

    /**
     * Periodic task that loads the ARP cache from the operating system and completes any pending futures for IP
     * addresses that have been resolved. If there are no more pending futures after processing, the scheduler
     * is stopped to avoid unnecessary resource usage.
     */
    private void backEndTask() {
        // if there are no pending futures, skip loading and stop the scheduler
        if (ifPendingFuturesEmpty()) {
            return;
        }

        arpCacheLoad();

        // complete pending futures for IPs that have been resolved; remove completed futures from the map
        pendingFutures.entrySet().removeIf(entry -> {
            String ip = entry.getKey();
            CompletableFuture<@Nullable String> future = entry.getValue();
            String mac = cacheGet(ip);
            if (mac != null && !future.isDone()) {
                logger.trace("{} -> {} (deferred)", ip, mac);
                future.complete(mac);
                return true;
            }
            return false;
        });

        // again, if there are no pending futures, stop the scheduler
        ifPendingFuturesEmpty();
    }

    /**
     * Executes a bulk load of the operating system's ARP cache into the in-memory cache.
     * Each platform specific loader handles its own exceptions.
     */
    private void arpCacheLoad() {
        String os = System.getProperty("os.name", "");
        if (os == null) {
            os = "";
        }
        os = os.toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            linuxArpCacheLoad();
        } else if (os.contains("mac") || os.contains("darwin")) {
            macOsArpCacheLoad();
        } else if (os.contains("win")) {
            windowsArpCacheLoad();
        } else {
            logger.warn("OS '{}' does not support ARP cache load", os);
        }
    }

    /**
     * Loads ARP entries from Linux's {@code /proc/net/arp} file.
     */
    private void linuxArpCacheLoad() {
        File arpFile = new File("/proc/net/arp");
        if (!arpFile.exists()) {
            logger.debug("ARP file {} does not exist", arpFile.getAbsolutePath());
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(arpFile.toPath(), StandardCharsets.UTF_8)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            logger.debug("Error reading /proc/net/arp", e);
        }
    }

    /**
     * Loads ARP entries by executing {@code arp -n} on macOS.
     */
    private void macOsArpCacheLoad() {
        runCommandAndParse("/usr/sbin/arp", "-n");
    }

    /**
     * Loads ARP entries by executing {@code arp -a} on Windows.
     */
    private void windowsArpCacheLoad() {
        runCommandAndParse("arp", "-a");
    }

    /**
     * Removes all expired entries from the in-memory cache.
     */
    private void cacheFlush() {
        arpCache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * Retrieves a MAC address from the in-memory cache, if present and not expired.
     */
    private @Nullable String cacheGet(String ip) {
        ExpiringMac entry = arpCache.get(ip);
        if (entry == null) {
            return null;
        }
        String mac = entry.getMac();
        if (mac == null) {
            arpCache.remove(ip);
        }
        return mac;
    }

    /**
     * Stores an IP => MAC mapping in the cache with expiration, and notifies listeners about the update. If the
     * same MAC is already cached for this IP, listeners will not be notified again.
     */
    private void cachePut(String ip, String mac) {
        arpCache.put(ip, new ExpiringMac(mac));
        // eager execution: check if a running future can be completed early
        CompletableFuture<@Nullable String> future = pendingFutures.get(ip);
        if (future != null && !future.isDone()) {
            logger.trace("{} -> {} (eager)", ip, mac);
            future.complete(mac);
            pendingFutures.remove(ip, future);
        }
    }

    /**
     * Checks if a standard format MAC address is valid.
     */
    private boolean isValidMac(String mac) {
        return MAC_PATTERN.matcher(mac).matches() && !"00:00:00:00:00:00".equalsIgnoreCase(mac);
    }

    /**
     * Converts a MAC address to the standard format {@code XX:XX:XX:XX:XX:XX}.
     */
    private String normalizeMac(String mac) {
        return mac.toUpperCase().replaceAll("[^A-F0-9]", "").replaceAll("(.{2})(?=.)", "$1:");
    }

    /**
     * Checks if a standard format IP address is valid.
     */
    private boolean isValidIp(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    /**
     * Convert an IP address to the standard format.
     * For example {@code 192.168.1.1:8080} converts to {@code 192.168.1.1}
     */
    private String normalizeIp(String ip) {
        Matcher m = IP_PATTERN.matcher(ip);
        return m.find() ? m.group() : ip; // fallback: return original
    }

    /**
     * Parses a single line from ARP output, extracts the IP MAC mapping if present, and caches it.
     */
    private void parseLine(String line) {
        if (line.isBlank()) {
            return;
        }
        Matcher ipMatcher = IP_PATTERN.matcher(line);
        Matcher macMatcher = MAC_PATTERN.matcher(line);
        if (ipMatcher.find() && macMatcher.find()) {
            String ip = ipMatcher.group();
            String mac = normalizeMac(macMatcher.group());
            if (isValidMac(mac)) {
                cachePut(ip, mac);
            }
        }
    }

    /**
     * Executes an OS command and parses its output line by line using {@link #parseLine(String)}.
     * Note: redirects error stream to standard output.
     */
    private void runCommandAndParse(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseLine(line);
                }
            }
            process.waitFor(ARP_LOAD_PROCESS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.debug("Failed to execute command: {}", String.join(" ", command), e);
        }
    }

    // ================ TEST HOOKS — package private, not exported in OSGi ================

    protected void testClearCache() {
        arpCache.clear();
    }

    protected @Nullable String testGetCached(String ip) {
        return cacheGet(ip);
    }

    protected boolean testCacheIsEmpty() {
        return arpCache.isEmpty();
    }

    protected void testPutCached(String ip, String mac, Instant expires) {
        ExpiringMac entry = new ExpiringMac(mac);
        try {
            Field f = ExpiringMac.class.getDeclaredField("expires");
            f.setAccessible(true);
            f.set(entry, expires);
        } catch (Exception ignored) {
        }
        arpCache.put(ip, entry);
    }
}
