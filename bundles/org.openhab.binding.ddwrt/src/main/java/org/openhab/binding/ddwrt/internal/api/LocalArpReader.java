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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the ARP/neighbor cache from the machine where openHAB is running.
 * Used when {@code useLocalArpCache} is enabled on the network bridge so that
 * clients visible to the openHAB host (but possibly not to managed routers/APs)
 * are still tracked.
 *
 * Strategy (in order):
 * <ol>
 * <li>Read {@code /proc/net/arp} directly (Linux, including openhabian) — fastest, no subprocess</li>
 * <li>Fall back to {@code arp -an} (macOS, BSD, Windows) — works cross-platform</li>
 * </ol>
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public final class LocalArpReader {

    private final Logger logger = LoggerFactory.getLogger(LocalArpReader.class);

    private static final Path PROC_NET_ARP = Path.of("/proc/net/arp");
    private static final Pattern MAC_PATTERN = Objects
            .requireNonNull(Pattern.compile("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2}){5}$"));
    private static final Pattern IP_PATTERN = Objects
            .requireNonNull(Pattern.compile("\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b"));
    private static final Pattern MAC_EXTRACT = Objects
            .requireNonNull(Pattern.compile("\\b([0-9a-fA-F]{2}(?::[0-9a-fA-F]{2}){5})\\b"));

    /** A single ARP entry mapping a MAC address to an IPv4 address. */
    public static final class ArpEntry {
        public final String mac;
        public final String ip;

        public ArpEntry(String mac, String ip) {
            this.mac = mac;
            this.ip = ip;
        }
    }

    /**
     * Read the local kernel ARP/neighbor cache.
     * Returns an empty list on any error.
     */
    public List<ArpEntry> readLocalArp() {
        // Strategy 1: /proc/net/arp (Linux)
        if (Files.isReadable(PROC_NET_ARP)) {
            try {
                return parseProcNetArp(Files.readAllLines(PROC_NET_ARP, StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.debug("Failed to read /proc/net/arp: {}", e.getMessage());
            }
        }

        // Strategy 2: arp -an subprocess (macOS, BSD, Windows)
        return runArpCommand();
    }

    /**
     * Parse {@code /proc/net/arp} content. Format:
     * 
     * <pre>
     * IP address       HW type     Flags       HW address            Mask     Device
     * 192.168.0.1      0x1         0x2         24:f5:a2:c6:16:59     *        br-lan
     * </pre>
     * 
     * Flag 0x2 = NUD_REACHABLE / complete; flag 0x0 = INCOMPLETE (skip).
     */
    private List<ArpEntry> parseProcNetArp(List<String> lines) {
        List<ArpEntry> result = new ArrayList<>();
        boolean firstLine = true;
        for (String line : lines) {
            if (firstLine) {
                firstLine = false;
                continue; // header
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            if (parts.length >= 6) {
                String ip = parts[0];
                String flags = parts[2];
                String mac = parts[3].toLowerCase(Locale.ROOT);
                // Skip incomplete entries (flag 0x0 reports MAC as 00:00:00:00:00:00)
                if (!"0x0".equals(flags) && isValidMac(mac)) {
                    result.add(new ArpEntry(mac, ip));
                }
            }
        }
        return result;
    }

    /**
     * Fallback: run {@code arp -an} and parse the output.
     * Works on Linux (net-tools), macOS, BSD, and Windows.
     */
    private List<ArpEntry> runArpCommand() {
        try {
            ProcessBuilder pb = new ProcessBuilder("arp", "-an");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            List<ArpEntry> result = new ArrayList<>();
            try (var reader = proc.inputReader(StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    var ipMatcher = IP_PATTERN.matcher(line);
                    var macMatcher = MAC_EXTRACT.matcher(line);
                    if (ipMatcher.find() && macMatcher.find()) {
                        String ip = Objects.requireNonNull(ipMatcher.group(1));
                        String mac = Objects.requireNonNull(macMatcher.group(1)).toLowerCase(Locale.ROOT);
                        if (isValidMac(mac)) {
                            result.add(new ArpEntry(mac, ip));
                        }
                    }
                }
            }
            proc.waitFor();
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Failed to run 'arp -an': {}", e.getMessage());
            return Collections.emptyList();
        } catch (IOException e) {
            logger.debug("Failed to run 'arp -an': {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static boolean isValidMac(String mac) {
        return MAC_PATTERN.matcher(mac).matches() && !"00:00:00:00:00:00".equals(mac);
    }
}
