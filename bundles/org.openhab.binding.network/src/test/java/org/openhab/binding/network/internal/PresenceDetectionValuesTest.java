/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.network.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests cases for {@see PresenceDetectionValue}
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class PresenceDetectionValuesTest {
    @Test
    public void updateLatencyTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", Duration.ofMillis(10));
        assertThat(value.getLowestLatency(), is(Duration.ofMillis(10)));
        value.updateLatency(Duration.ofMillis(20));
        assertThat(value.getLowestLatency(), is(Duration.ofMillis(10)));
        value.updateLatency(Duration.ofMillis(0));
        assertThat(value.getLowestLatency(), is(Duration.ofMillis(10)));
        value.updateLatency(Duration.ofMillis(5));
        assertThat(value.getLowestLatency(), is(Duration.ofMillis(5)));
    }

    @Test
    public void tcpTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", Duration.ofMillis(10));
        assertFalse(value.isTcpServiceReachable());
        value.addReachableTcpPort(1010);
        assertThat(value.getReachableTcpPorts(), hasItem(1010));
        value.addReachableDetectionType(PresenceDetectionType.TCP_CONNECTION);
        assertTrue(value.isTcpServiceReachable());
        assertThat(value.getSuccessfulDetectionTypes(), is("TCP_CONNECTION"));
    }

    @Test
    public void pingTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", Duration.ofMillis(10));
        assertFalse(value.isPingReachable());
        value.addReachableDetectionType(PresenceDetectionType.ICMP_PING);
        assertTrue(value.isPingReachable());

        value.addReachableDetectionType(PresenceDetectionType.ARP_PING);
        value.addReachableDetectionType(PresenceDetectionType.TCP_CONNECTION);
        assertThat(value.getSuccessfulDetectionTypes(), is("ARP_PING, ICMP_PING, TCP_CONNECTION"));
    }
}
