/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.network.internal.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Examines output lines of the ping command and tries to extract the contained latency value.
 *
 * @author Andreas Hirsch - Initial contribution
 */
@NonNullByDefault
public class LatencyParser {

    private static final Pattern LATENCY_PATTERN = Pattern.compile(".*time=(.*) ?ms");
    private final Logger logger = LoggerFactory.getLogger(LatencyParser.class);

    // This is how the input looks like on Mac and Linux:
    // ping -c 1 192.168.1.1
    // PING 192.168.1.1 (192.168.1.1): 56 data bytes
    // 64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.225 ms
    //
    // --- 192.168.1.1 ping statistics ---
    // 1 packets transmitted, 1 packets received, 0.0% packet loss
    // round-trip min/avg/max/stddev = 1.225/1.225/1.225/0.000 ms

    /**
     * Examine a single ping command output line and try to extract the latency value if it is contained.
     *
     * @param inputLine Single output line of the ping command.
     * @return Latency value provided by the ping command. Optional is empty if the provided line did not contain a
     *         latency value which matches the known patterns.
     */
    public Optional<Double> parseLatency(String inputLine) {
        logger.debug("Parsing latency from input {}", inputLine);

        Matcher m = LATENCY_PATTERN.matcher(inputLine);
        if (m.find() && m.groupCount() == 1) {
            return Optional.of(Double.parseDouble(m.group(1)));
        }

        logger.debug("Did not find a latency value");
        return Optional.empty();
    }
}
