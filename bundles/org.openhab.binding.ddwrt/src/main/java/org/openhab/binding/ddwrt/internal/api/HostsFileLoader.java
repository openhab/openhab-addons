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
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads hostname mappings from a directory of files and from inline config strings.
 * Auto-detects file format per line: /etc/hosts, dnsmasq.leases, or etherhosts.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public final class HostsFileLoader {

    private final Logger logger = LoggerFactory.getLogger(HostsFileLoader.class);

    /** A single hostname mapping with optional IP. */
    public static final class HostEntry {
        public final String mac;
        public final String ip;
        public final String hostname;

        public HostEntry(String mac, String ip, String hostname) {
            this.mac = mac;
            this.ip = ip;
            this.hostname = hostname;
        }
    }

    private static final Pattern MAC_PATTERN = Pattern.compile("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2}){5}$");
    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    /**
     * Load all files in the given directory, auto-detecting format per line.
     * Returns aggregated entries; later files override earlier ones on key collision.
     * Files are processed in alphabetical order for deterministic results.
     */
    public List<HostEntry> loadDirectory(Path dir) {
        if (!Files.isDirectory(dir)) {
            logger.debug("Hosts directory does not exist: {}", dir);
            return Collections.emptyList();
        }
        List<HostEntry> all = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> files = stream.filter(Files::isRegularFile).sorted().toList();
            for (Path file : files) {
                List<HostEntry> entries = loadFile(file);
                logger.debug("Loaded {} entries from {}", entries.size(), file.getFileName());
                all.addAll(entries);
            }
        } catch (IOException e) {
            logger.warn("Failed to list hosts directory {}: {}", dir, e.getMessage());
        }
        return all;
    }

    /**
     * Load and parse a single file with auto-detected format.
     */
    public List<HostEntry> loadFile(Path file) {
        if (!Files.isReadable(file)) {
            return Collections.emptyList();
        }
        try {
            return parse(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.warn("Failed to read {}: {}", file, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parse the inline config string format: "MAC hostname [IP], MAC hostname [IP], ..."
     */
    public List<HostEntry> parseInlineMappings(String csv) {
        if (csv.isBlank()) {
            return Collections.emptyList();
        }
        List<HostEntry> result = new ArrayList<>();
        for (String entry : csv.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            if (parts.length < 2) {
                logger.warn("Ignoring malformed inline mapping (need 'MAC hostname [IP]'): '{}'", trimmed);
                continue;
            }
            String mac = normalizeMac(parts[0]);
            if (mac.isEmpty()) {
                logger.warn("Ignoring inline mapping with invalid MAC: '{}'", trimmed);
                continue;
            }
            String hostname = parts[1];
            String ip = parts.length >= 3 && IP_PATTERN.matcher(parts[2]).matches() ? parts[2] : "";
            result.add(new HostEntry(mac, ip, hostname));
        }
        return result;
    }

    /**
     * Auto-detect format per line and dispatch to the appropriate parser.
     * Each line can independently be /etc/hosts, etherhosts, or dnsmasq.leases format.
     */
    private List<HostEntry> parse(List<String> lines) {
        List<HostEntry> result = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] p = line.split("\\s+");
            if (p.length < 2) {
                continue;
            }

            // dnsmasq.leases: "expiry MAC IP hostname [clientid]" — first token is numeric
            if (p.length >= 4 && p[0].matches("\\d+") && MAC_PATTERN.matcher(p[1]).matches()
                    && IP_PATTERN.matcher(p[2]).matches() && !"*".equals(p[3])) {
                result.add(new HostEntry(normalizeMac(p[1]), p[2], p[3]));
                continue;
            }

            // etherhosts: "MAC hostname" or "MAC IP hostname"
            if (MAC_PATTERN.matcher(p[0]).matches()) {
                if (p.length >= 3 && IP_PATTERN.matcher(p[1]).matches()) {
                    result.add(new HostEntry(normalizeMac(p[0]), p[1], p[2]));
                } else {
                    result.add(new HostEntry(normalizeMac(p[0]), "", p[1]));
                }
                continue;
            }

            // /etc/hosts: "IP hostname [aliases...]"
            if (IP_PATTERN.matcher(p[0]).matches() && !p[0].startsWith("127.") && !"localhost".equals(p[1])
                    && !"localhost.localdomain".equals(p[1])) {
                result.add(new HostEntry("", p[0], p[1]));
            }
        }
        return result;
    }

    private static String normalizeMac(String mac) {
        return MAC_PATTERN.matcher(mac).matches() ? mac.toLowerCase(Locale.ROOT) : "";
    }
}
