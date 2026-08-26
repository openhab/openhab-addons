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
package org.openhab.binding.tapocontrol.internal.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Best-effort MAC→IPv4 resolution using the OS neighbor tables (Linux 'ip neigh',
 * macOS/BSD 'arp -a'). Cloud discovery results carry no IP address, so this heuristic
 * bridges the gap; results depend on recent communication with the camera and are purely
 * advisory.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ArpNeighResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArpNeighResolver.class);
    private static final Pattern IPV4_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final String[] TABLE_COMMANDS = { "ip neigh show", "arp -a" };

    public Optional<String> resolveMac(String mac) {
        if (mac.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalizeMac(mac);
        for (String line : readNeighborTable()) {
            if (normalizeMac(line).contains(normalized)) {
                Optional<String> ip = extractValidIp(line);
                if (ip.isPresent()) {
                    LOGGER.debug("resolved {} to {} via neighbor table", mac, ip.get());
                    return ip;
                }
            }
        }
        return Optional.empty();
    }

    /** Overridable seam for tests; default shells out to both table sources. */
    protected List<String> readNeighborTable() {
        List<String> lines = new ArrayList<>();
        for (String command : TABLE_COMMANDS) {
            lines.addAll(readTable(command));
        }
        return lines;
    }

    private static List<String> readTable(String command) {
        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", command).start();
            List<String> lines;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                lines = reader.lines().toList();
            }
            process.waitFor(5, TimeUnit.SECONDS);
            process.destroyForcibly();
            return lines;
        } catch (IOException e) {
            LOGGER.debug("neighbor table lookup '{}' unavailable: {}", command, e.getMessage());
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    private static String normalizeMac(String input) {
        return input.toLowerCase(Locale.ROOT).replace(":", "").replace("-", "");
    }

    private static Optional<String> extractValidIp(String line) {
        Matcher matcher = IPV4_PATTERN.matcher(line);
        while (matcher.find()) {
            String candidate = matcher.group();
            try {
                InetAddress address = InetAddress.getByName(candidate);
                if (address instanceof Inet4Address inet4) {
                    return Optional.of(inet4.getHostAddress());
                }
            } catch (UnknownHostException e) {
                // not a valid literal — try the next match
            }
        }
        return Optional.empty();
    }
}
