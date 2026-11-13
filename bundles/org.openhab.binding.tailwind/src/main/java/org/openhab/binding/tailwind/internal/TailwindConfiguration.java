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
package org.openhab.binding.tailwind.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TailwindConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindConfiguration {
    private static final double DEFAULT_PARTIAL_OPEN_TIME = 2.5;
    public static final int DEFAULT_UDP_PORT = 50904;

    public String hostname = "";
    public String token = "";
    public int udpPort = DEFAULT_UDP_PORT;
    public double door1PartialOpenTime = DEFAULT_PARTIAL_OPEN_TIME;
    public double door2PartialOpenTime = DEFAULT_PARTIAL_OPEN_TIME;
    public double door3PartialOpenTime = DEFAULT_PARTIAL_OPEN_TIME;

    /**
     * Returns the host name of the controller without the parts users often copy along with it, such as
     * {@code http://tailwind-30aea4801880.local/json}.
     */
    public static String normalizeHostname(String hostname) {
        String host = hostname.trim();
        String lower = host.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://")) {
            host = host.substring("http://".length());
        } else if (lower.startsWith("https://")) {
            host = host.substring("https://".length());
        }
        if (host.toLowerCase(Locale.ROOT).endsWith("/json")) {
            host = host.substring(0, host.length() - "/json".length());
        }
        while (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host.trim();
    }

    /**
     * Returns the time in milliseconds that a door runs when it is opened partially.
     *
     * @param doorIndex index of the door, starting at 0
     */
    public long partialOpenTimeMillis(int doorIndex) {
        double seconds = switch (doorIndex) {
            case 0 -> door1PartialOpenTime;
            case 1 -> door2PartialOpenTime;
            case 2 -> door3PartialOpenTime;
            default -> throw new IllegalArgumentException("No door with index " + doorIndex);
        };
        return Math.round(seconds * 1000);
    }
}
