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
package org.openhab.binding.shelly.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ShellyThingTable}.
 *
 * The main focus is the {@code findThing(InetSocketAddress)} two-step lookup introduced to fix
 * range extender routing: Device B (range extender client) and Device A (gateway) share the same
 * IP address but are disambiguated by port. IP:port is tried first; if there is no exact port
 * match the lookup falls back to IP-only so that normal (non-range-extender) lookups are unaffected.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
public class ShellyThingTableTest {

    private static final String GATEWAY_IP = "192.168.1.100";
    private static final int CLIENT_PORT = 8080;
    private static final String CLIENT_KEY = GATEWAY_IP + ":" + CLIENT_PORT;

    // Returns a mock that answers true to checkRepresentation only for the supplied keys.
    // Mockito's default for unmatched boolean calls is false, which is correct here.
    private static ShellyThingInterface stubThing(String... keys) {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        for (String key : keys) {
            when(thing.checkRepresentation(key)).thenReturn(true);
        }
        return thing;
    }

    // ── findThing(InetSocketAddress) ─────────────────────────────────────────

    @Test
    void findBySocketAddr_exactPortMatch_returnsClientDevice() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);
        ShellyThingInterface client = stubThing(CLIENT_KEY);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);
        table.addThing(CLIENT_KEY, client);

        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), CLIENT_PORT);
        assertThat(table.findThing(addr), is(client));
    }

    @Test
    void findBySocketAddr_noPortMatch_fallsBackToIpOnly() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);

        // Port 80 is not registered — must fall back to IP-only.
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), 80);
        assertThat(table.findThing(addr), is(gateway));
    }

    @Test
    void findBySocketAddr_portZero_usesIpOnlyLookup() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);

        // port == 0 means "no port specified" — skip IP:port attempt, go straight to IP-only.
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), 0);
        assertThat(table.findThing(addr), is(gateway));
    }

    @Test
    void findBySocketAddr_unknownAddress_returnsNull() throws UnknownHostException {
        ShellyThingTable table = new ShellyThingTable();

        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("10.0.0.99"), 80);
        assertThat(table.findThing(addr), is(nullValue()));
    }

    // ── getThing(InetSocketAddress) ───────────────────────────────────────────

    @Test
    void getBySocketAddr_unknownAddress_throwsIllegalArgument() throws UnknownHostException {
        ShellyThingTable table = new ShellyThingTable();

        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("10.0.0.99"), 80);
        assertThrows(IllegalArgumentException.class, () -> table.getThing(addr));
    }

    @Test
    void getBySocketAddr_knownAddress_returnsCorrectThing() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);

        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), 80);
        assertThat(table.getThing(addr), is(gateway));
    }

    // ── Range extender scenario (end-to-end) ─────────────────────────────────

    /**
     * Device A (Pro 1PM, gateway) and Device B (Pro 2, range extender client) share the same IP.
     * Device B is identified by IP:CLIENT_PORT. A WebSocket connect from IP:CLIENT_PORT must
     * resolve to Device B, not to Device A — preventing the crash where Device A's profile
     * (numMeters=1) is applied to Device B (numMeters=0).
     */
    @Test
    void rangeExtender_clientPortRoutedToClientDevice() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);
        ShellyThingInterface client = stubThing(CLIENT_KEY);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);
        table.addThing(CLIENT_KEY, client);

        InetSocketAddress clientAddr = new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), CLIENT_PORT);

        assertThat("client port must route to client device", table.getThing(clientAddr), is(client));
        assertThat("client port must not route to gateway", table.getThing(clientAddr), is(not(gateway)));
    }

    /**
     * When Device A (gateway) connects, its WebSocket address uses a non-client port (e.g. 80).
     * The lookup must fall back to the IP-only entry and return Device A.
     */
    @Test
    void rangeExtender_gatewayPortRoutedToGatewayDevice() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);
        ShellyThingInterface client = stubThing(CLIENT_KEY);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);
        table.addThing(CLIENT_KEY, client);

        InetSocketAddress gatewayAddr = new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), 80);

        assertThat("gateway port must route to gateway device", table.getThing(gatewayAddr), is(gateway));
        assertThat("gateway port must not route to client", table.getThing(gatewayAddr), is(not(client)));
    }

    /**
     * With only a gateway registered (no range extender client), any port falls back to IP-only
     * and returns the gateway. This verifies that the two-step lookup does not break the
     * pre-existing non-range-extender behaviour.
     */
    @Test
    void noRangeExtender_anyPortRoutedToSingleDevice() throws UnknownHostException {
        ShellyThingInterface gateway = stubThing(GATEWAY_IP);

        ShellyThingTable table = new ShellyThingTable();
        table.addThing(GATEWAY_IP, gateway);

        assertThat(table.getThing(new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), 80)), is(gateway));
        assertThat(table.getThing(new InetSocketAddress(InetAddress.getByName(GATEWAY_IP), CLIENT_PORT)), is(gateway));
    }
}
