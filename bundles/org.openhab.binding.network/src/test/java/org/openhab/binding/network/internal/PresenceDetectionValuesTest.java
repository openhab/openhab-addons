/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.junit.jupiter.api.Test;

/**
 * Tests cases for {@see PresenceDetectionValue}
 *
 * @author David Graeff - Initial contribution
 */
public class PresenceDetectionValuesTest {
    @Test
    public void updateLatencyTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", 10.0);
        assertThat(value.getLowestLatency(), is(10.0));
        value.updateLatency(20.0);
        assertThat(value.getLowestLatency(), is(10.0));
        value.updateLatency(0.0);
        assertThat(value.getLowestLatency(), is(10.0));
        value.updateLatency(5.0);
        assertThat(value.getLowestLatency(), is(5.0));
    }

    @Test
    public void tcpTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", 10.0);
        assertFalse(value.isTCPServiceReachable());
        value.addReachableTcpService(1010);
        assertThat(value.getReachableTCPports(), hasItem(1010));
        value.addType(PresenceDetectionType.TCP_CONNECTION);
        assertTrue(value.isTCPServiceReachable());
        assertThat(value.getSuccessfulDetectionTypes(), is("TCP_CONNECTION"));
    }

    @Test
    public void isFinishedTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", 10.0);
        assertFalse(value.isFinished());
        value.setDetectionIsFinished(true);
        assertTrue(value.isFinished());
    }

    @Test
    public void pingTests() {
        PresenceDetectionValue value = new PresenceDetectionValue("127.0.0.1", 10.0);
        assertFalse(value.isPingReachable());
        value.addType(PresenceDetectionType.ICMP_PING);
        assertTrue(value.isPingReachable());

        value.addType(PresenceDetectionType.ARP_PING);
        value.addType(PresenceDetectionType.TCP_CONNECTION);
        assertThat(value.getSuccessfulDetectionTypes(), is("ARP_PING, ICMP_PING, TCP_CONNECTION"));
    }
}
