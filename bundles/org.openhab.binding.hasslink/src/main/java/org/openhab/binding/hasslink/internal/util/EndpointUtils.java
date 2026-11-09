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
package org.openhab.binding.hasslink.internal.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for formatting and normalizing network endpoint representations.
 * <p>
 * Provides helper methods to resolve and canonicalize host addresses and port numbers
 * into a standardized {@code host:port} string format. This ensures openHAB can match
 * discovered bridge instances against manually configured ones regardless of whether
 * an IP address or hostname/mDNS domain was used.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class EndpointUtils {

    /**
     * Private constructor to prevent instantiation of static utility class.
     */
    private EndpointUtils() {
    }

    /**
     * Normalizes a host and port into a canonical {@code "ip_or_host:port"} endpoint string.
     * <p>
     * The method attempts to resolve the provided hostname to its underlying IP address
     * (IPv4 or IPv6) using DNS/mDNS lookup. If resolution succeeds, the IP address is used
     * to form the endpoint. If resolution fails (e.g., system is offline or DNS is unreachable),
     * it gracefully falls back to a trimmed, lowercased hostname.
     *
     * @param host the hostname, mDNS domain, or IP address (e.g., "homeassistant.local", "192.168.1.50");
     *            may be {@code null} or blank
     * @param port the target port number
     * @return a canonical endpoint string formatted as {@code "host:port"}, or an empty string if {@code host} is null
     *         or blank
     */
    public static String normalizeEndpoint(@Nullable String host, int port) {
        if (host == null || host.isBlank()) {
            return "";
        }

        // Clean input string (strip whitespace and convert to lowercase)
        String cleanedHost = host.trim().toLowerCase();

        // Attempt DNS/mDNS lookup to resolve host to an IP address
        try {
            cleanedHost = InetAddress.getByName(cleanedHost).getHostAddress();
        } catch (UnknownHostException e) {
            // Fall back to the cleaned hostname if DNS resolution fails or system is offline
        }

        return cleanedHost + ":" + port;
    }
}
