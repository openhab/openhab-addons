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
package org.openhab.binding.network.internal.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.network.internal.utils.NetworkUtils.durationToMillis;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the parser which extracts latency values from the output of the ping command.
 *
 * @author Andreas Hirsch - Initial contribution
 */
@NonNullByDefault
public class LatencyParserTest {

    @Test
    public void parseLinuxAndMacResultFoundTest() {
        // Arrange
        LatencyParser latencyParser = new LatencyParser();
        String input = "64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.225 ms";

        // Act
        Duration resultLatency = latencyParser.parseLatency(input, null);

        // Assert
        assertNotNull(resultLatency);
        assertEquals(1.225, durationToMillis(resultLatency), 0);
    }

    @Test
    public void parseLinuxAndMacResultNotFoundTest() {
        // Arrange
        LatencyParser latencyParser = new LatencyParser();
        // This is the output of the command. We exclude the line which contains the latency, because here we want
        // to test that no latency is returned for all other lines.
        String[] inputLines = { "ping -c 1 192.168.1.1", "PING 192.168.1.1 (192.168.1.1): 56 data bytes",
                // "64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.225 ms",
                "--- 192.168.1.1 ping statistics ---", "1 packets transmitted, 1 packets received, 0.0% packet loss",
                "round-trip min/avg/max/stddev = 1.225/1.225/1.225/0.000 ms" };

        for (String inputLine : inputLines) {
            // Act
            Duration resultLatency = latencyParser.parseLatency(inputLine, null);

            // Assert
            assertNull(resultLatency);
        }
    }

    @Test
    public void parseWindows10ResultFoundTest() {
        // Arrange
        LatencyParser latencyParser = new LatencyParser();
        String input = "Reply from 192.168.178.207: bytes=32 time=2ms TTL=64";

        // Act
        Duration resultLatency = latencyParser.parseLatency(input, null);

        // Assert
        assertNotNull(resultLatency);
        assertEquals(2, durationToMillis(resultLatency), 0);

        input = "Reply from 10.80.5.2: bytes=32 time<1ms TTL=64";
        resultLatency = latencyParser.parseLatency(input, null);
        assertNotNull(resultLatency);
        assertEquals(1, durationToMillis(resultLatency), 0.0);
    }
}
