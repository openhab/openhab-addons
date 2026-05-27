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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for resolving MAC addresses from IP addresses via the operating system's ARP cache.
 * If the MAC address is not found in the cache, it triggers an asynchronous resolution process that
 * involves bulk loading the ARP cache and, if needed, pinging the IP to populate the ARP table. Resolved
 * MAC addresses are cached in-memory with an expiration time to avoid frequent lookups. Listeners can
 * be registered to receive notifications whenever a new MAC address is resolved.
 * <p>
 * This implementation uses a combination of:
 * <ul>
 * <li>In-memory timed cache</li>
 * <li>Bulk reading of the OS ARP table</li>
 * <li>Fallback ping to populate the ARP cache when needed</li>
 * </ul>
 * Note: This class is a candidate to be integrated into OH Core utilities.
 * <p>
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = MacResolver.class, immediate = true)
public class MacResolver {

    private static final long PROCESS_WAIT_SECONDS = 2;
    private static final long CACHE_EXPIRY_SECONDS = 180;
    private static final int PING_TIMEOUT_MILLISEC = 500;

    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}");
    private static final Pattern IP_PATTERN = Pattern
            .compile("\\b((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}" + "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\b");

    private final Logger logger = LoggerFactory.getLogger(MacResolver.class);

    // cache of IP / MAC mappings with expiration time stamps; prevents hitting the OS ARP cache too often
    private final Map<String, ExpiringMac> cache = new ConcurrentHashMap<>();

    // set of listeners that will be notified whenever a new MAC address is resolved
    private final Set<MacResolverListener> listeners = ConcurrentHashMap.newKeySet();

    // tracks IPs that are in flight for resolution (avoid multiple concurrent lookups for same IP)
    private final Set<String> inflightIPs = ConcurrentHashMap.newKeySet();

    // lock to serialize the bulk loading of the ARP cache which involves executing external processes
    private final Object arpLock = new Object();

    private @Nullable ExecutorService executor;

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
            this.expires = Instant.now().plusSeconds(CACHE_EXPIRY_SECONDS);
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
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "OH-MacResolver");
            t.setDaemon(true);
            return t;
        });
    }

    @Deactivate
    protected void deactivate() {
        ExecutorService executor = this.executor;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * Returns the MAC address for the given IP address. This method first checks the in-memory cache, then
     * triggers an asynchronous resolution if not found. The resolution process involves bulk loading the OS
     * ARP cache, and if still not found, pinging the IP to populate the ARP table and trying again. Listeners
     * are notified when a new MAC address is resolved.
     *
     * @param ip the IP address (IPv4) to resolve
     * @return the MAC address in format "00:1A:2B:3C:4D:5E", or {@code null} if not found yet (resolution is
     *         asynchronous)
     */
    public @Nullable String resolveMac(String ipAddress) {
        String ip = extractPureIp(ipAddress);
        if (ip.isBlank()) {
            logger.debug("IP blank");
            return null;
        }

        // try to return a cached value immediately
        cacheFlush();
        String mac = cacheGet(ip);
        if (mac != null) {
            return mac;
        }

        // trigger an asynchronous resolve if not already in-flight for this IP
        if (inflightIPs.add(ip)) {
            Objects.requireNonNull(executor).submit(() -> {
                try {
                    resolveMacAsync(ip);
                } finally {
                    inflightIPs.remove(ip);
                }
            });
        }
        return null;
    }

    /**
     * Registers a listener to be notified whenever a new MAC address is resolved.
     */
    public void addMacResolverListener(MacResolverListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters a previously registered listener.
     */
    public void removeMacResolverListener(MacResolverListener listener) {
        listeners.remove(listener);
    }

    /**
     * Triggers a bulk load of the operating system's ARP cache into the in-memory cache.
     * Each platform-specific loader handles its own exceptions.
     */
    private void bulkLoadArpCache() {
        String os = System.getProperty("os.name", "");
        if (os == null) {
            os = "";
        }
        os = os.toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            bulkLoadLinuxArpCache();
        } else if (os.contains("mac") || os.contains("darwin")) {
            bulkLoadMacOsArpCache();
        } else if (os.contains("win")) {
            bulkLoadWindowsArpCache();
        } else {
            logger.debug("OS '{}' does not support ARP cache load", os);
        }
    }

    /**
     * Loads ARP entries from Linux's {@code /proc/net/arp} file.
     */
    private void bulkLoadLinuxArpCache() {
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
    private void bulkLoadMacOsArpCache() {
        runCommandAndParse("/usr/sbin/arp", "-n");
    }

    /**
     * Loads ARP entries by executing {@code arp -a} on Windows.
     */
    private void bulkLoadWindowsArpCache() {
        runCommandAndParse("arp", "-a");
    }

    /**
     * Removes all expired entries from the in-memory cache.
     */
    private void cacheFlush() {
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * Retrieves a MAC address from the in-memory cache, if present and not expired.
     */
    private @Nullable String cacheGet(String ip) {
        ExpiringMac entry = cache.get(ip);
        if (entry == null) {
            return null;
        }
        String mac = entry.getMac();
        if (mac == null) {
            cache.remove(ip);
        }
        return mac;
    }

    /**
     * Stores an IP => MAC mapping in the cache with expiration, and notifies listeners about the update. If the
     * same MAC is already cached for this IP, listeners will not be notified again.
     */
    private void cachePut(String ip, String mac) {
        if (cache.put(ip, new ExpiringMac(mac)) instanceof ExpiringMac previous && mac.equals(previous.getMac())) {
            return; // identical MAC => don't notify
        }
        listeners.forEach(listener -> notify(listener, ip, mac));
    }

    private String extractPureIp(String input) {
        Matcher m = IP_PATTERN.matcher(input);
        return m.find() ? m.group() : input; // fallback: return original
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
     * Safely notifies a single listener about a resolved MAC address, catching and logging any exceptions to avoid
     * disrupting the notification of other listeners.
     */
    private void notify(MacResolverListener listener, String ip, String mac) {
        try {
            listener.macAddressResolved(ip, mac);
        } catch (Exception e) {
            logger.warn("macAddressResolved error", e);
        }
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
     * Internal method that implements the actual lookup logic: bulk load the ARP cache, and if still
     * not found, ping the IP to populate the ARP table and try again.
     */
    private void resolveMacAsync(String ip) {
        synchronized (arpLock) {
            bulkLoadArpCache();
        }
        String mac = cacheGet(ip);
        if (mac != null) {
            return;
        }
        try {
            InetAddress.getByName(ip).isReachable(PING_TIMEOUT_MILLISEC);
            synchronized (arpLock) {
                bulkLoadArpCache();
            }
        } catch (Exception e) {
            logger.debug("Ping {} failed", ip, e);
        }
    }

    /**
     * Executes a command and parses its output line by line using {@link #parseLine(String)}.
     */
    private void runCommandAndParse(String... command) {
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(true).start();// redirect stderr to stdout
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line);
                }
            }
            p.waitFor(PROCESS_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.debug("Failed to execute command: {}", String.join(" ", command), e);
        }
    }

    // ================ TEST HOOKS — package private, not exported in OSGi ================

    void testClearCache() {
        cache.clear();
    }

    @Nullable
    String testGetCached(String ip) {
        return cacheGet(ip);
    }

    boolean testCacheIsEmpty() {
        return cache.isEmpty();
    }

    void testPutCached(String ip, String mac, Instant expires) {
        ExpiringMac entry = new ExpiringMac(mac);
        try {
            Field f = ExpiringMac.class.getDeclaredField("expires");
            f.setAccessible(true);
            f.set(entry, expires);
        } catch (Exception ignored) {
        }
        cache.put(ip, entry);
    }
}
