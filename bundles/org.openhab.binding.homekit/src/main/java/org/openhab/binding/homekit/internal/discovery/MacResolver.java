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
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for resolving MAC addresses from IP addresses via the operating system's ARP cache.
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
public class MacResolver {

    private static final long PROCESS_WAIT_SECONDS = 2;
    private static final long CACHE_EXPIRY_SECONDS = 60;
    private static final int PING_TIMEOUT_MILLISEC = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(MacResolver.class);

    private static final Pattern MAC_PATTERN = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");

    private static final Map<String, ExpiringMac> CACHE = new ConcurrentHashMap<>();

    private MacResolver() {
        // Private constructor to prevent instantiation
    }

    /**
     * Simple wrapper to hold a MAC address with its expiration time-stamp.
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
         * Checks whether this entry has expired.
         */
        public boolean isExpired() {
            return Instant.now().isAfter(expires);
        }

        /**
         * Returns the MAC address if not expired, otherwise {@code null}.
         */
        public @Nullable String getMac() {
            return isExpired() ? null : mac;
        }
    }

    /**
     * Returns the MAC address for the given IP address.
     * <p>
     * The lookup follows this order:
     * <ol>
     * <li>Check in-memory cache</li>
     * <li>Bulk load the OS ARP cache</li>
     * <li>If still not found, send a ping to populate the ARP table and try again</li>
     * </ol>
     *
     * @param ipAddress the IP address (IPv4) to resolve
     * @return the MAC address in format "00:1A:2B:3C:4D:5E", or {@code null} if it could not be resolved
     */
    public static synchronized @Nullable String getMacFromIp(String ipAddress) {
        String ip = ipAddress.split(":")[0].trim();
        String mac = getMacFromIpInternal(ip);
        LOGGER.debug("MAC for {} is {}", ip, mac);
        return mac;
    }

    /**
     * Internal method that implements the actual lookup logic with caching and ARP table loading.
     */
    private static @Nullable String getMacFromIpInternal(String ip) {
        if (ip.isBlank()) {
            return null;
        }

        // First, remove expired entries and check the cache
        cacheFlush();
        String mac = cacheGet(ip);
        if (mac != null) {
            return mac;
        }

        // Not found in cache, so trigger a bulk load of the ARP cache and check again
        bulkLoadArpCache();
        mac = cacheGet(ip);
        if (mac != null) {
            return mac;
        }

        // Still not found, so send a ping to populate the ARP cache and check one last time
        try {
            LOGGER.debug("MAC for {} not found, pinging...", ip);
            InetAddress.getByName(ip).isReachable(PING_TIMEOUT_MILLISEC);
            bulkLoadArpCache();
            mac = cacheGet(ip);
        } catch (Exception e) {
            LOGGER.debug("Ping for {} failed", ip, e);
        }

        return mac;
    }

    /**
     * Triggers a bulk load of the operating system's ARP cache into the in-memory cache.
     * Each platform-specific loader handles its own exceptions.
     */
    private static void bulkLoadArpCache() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            bulkLoadLinuxArpCache();
        } else if (os.contains("mac") || os.contains("darwin")) {
            bulkLoadMacOsArpCache();
        } else if (os.contains("win")) {
            bulkLoadWindowsArpCache();
        } else {
            LOGGER.debug("OS '{}' does not support ARP cache load", os);
        }
    }

    /**
     * Loads ARP entries from Linux's {@code /proc/net/arp} file.
     */
    private static void bulkLoadLinuxArpCache() {
        File arpFile = new File("/proc/net/arp");
        if (!arpFile.exists()) {
            LOGGER.debug("ARP file {} does not exist", arpFile.getAbsolutePath());
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(arpFile.toPath(), StandardCharsets.UTF_8)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            LOGGER.debug("Error reading /proc/net/arp", e);
        }
    }

    /**
     * Loads ARP entries by executing {@code arp -n} on macOS.
     */
    private static void bulkLoadMacOsArpCache() {
        runCommandAndParse("/usr/sbin/arp", "-n");
    }

    /**
     * Loads ARP entries by executing {@code arp -a} on Windows.
     */
    private static void bulkLoadWindowsArpCache() {
        runCommandAndParse("arp", "-a");
    }

    /**
     * Executes a command and parses its output line by line using {@link #parseLine(String)}.
     */
    private static void runCommandAndParse(String... command) {
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(true).start();// redirect stderr to stdout
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line);
                }
            }
            p.waitFor(PROCESS_WAIT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.debug("Failed to execute command: {}", String.join(" ", command), e);
        }
    }

    /**
     * Parses a single line from ARP output, extracts the IP MAC mapping if present, and caches it.
     */
    private static void parseLine(String line) {
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
     * Removes all expired entries from the in-memory cache.
     */
    private static void cacheFlush() {
        CACHE.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * Retrieves a MAC address from the in-memory cache, if present and not expired.
     */
    private static @Nullable String cacheGet(String ip) {
        ExpiringMac entry = CACHE.get(ip);
        if (entry == null) {
            return null;
        }
        String mac = entry.getMac();
        if (mac == null) {
            CACHE.remove(ip);
        }
        return mac;
    }

    /**
     * Stores an IP => MAC mapping in the in-memory cache with expiration.
     */
    private static void cachePut(String ip, String mac) {
        CACHE.put(ip, new ExpiringMac(mac));
    }

    /**
     * Converts a MAC address to the standard format {@code XX:XX:XX:XX:XX:XX}.
     */
    private static String normalizeMac(String mac) {
        return mac.toUpperCase().replaceAll("[^A-F0-9]", "").replaceAll("(.{2})(?=.)", "$1:");
    }

    /**
     * Checks if a standard format MAC address is valid.
     */
    private static boolean isValidMac(String mac) {
        return mac.length() == 17 && !mac.startsWith("00:00:00:00:00:00");
    }
}
