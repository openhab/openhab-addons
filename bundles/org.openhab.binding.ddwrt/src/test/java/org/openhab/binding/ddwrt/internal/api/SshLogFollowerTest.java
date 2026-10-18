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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SshLogFollower} event classification patterns.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class SshLogFollowerTest {

    // Mirror of the DHCP_MESSAGE pattern from SshLogFollower
    private static final Pattern DHCP_MESSAGE = Pattern.compile(
            "DHCPACK|DHCPREQUEST|DHCPDISCOVER|DHCPOFFER|DHCPDECLINE|DHCPNAK|DHCPINFORM|DHCPRELEASE|lease|renew|(?<!DNS-)rebind",
            Pattern.CASE_INSENSITIVE);

    @Test
    void testDnsRebindAttackDoesNotMatchDhcp() {
        String msg = "possible DNS-rebind attack detected: example.com";
        assertThat("DNS-rebind attack should not match DHCP pattern", DHCP_MESSAGE.matcher(msg).find(), is(false));
    }

    @Test
    void testDhcpRebindMatchesDhcp() {
        String msg = "rebind 192.168.1.100";
        assertThat("DHCP rebind should match DHCP pattern", DHCP_MESSAGE.matcher(msg).find(), is(true));
    }

    @Test
    void testDhcpAckMatchesDhcp() {
        String msg = "DHCPACK(br0) 192.168.1.100 aa:bb:cc:dd:ee:ff MyPhone";
        assertThat("DHCPACK should match DHCP pattern", DHCP_MESSAGE.matcher(msg).find(), is(true));
    }

    @Test
    void testDhcpDiscoverMatchesDhcp() {
        String msg = "DHCPDISCOVER(br0) aa:bb:cc:dd:ee:ff";
        assertThat("DHCPDISCOVER should match DHCP pattern", DHCP_MESSAGE.matcher(msg).find(), is(true));
    }

    @Test
    void testLeaseMatchesDhcp() {
        String msg = "lease of 192.168.1.100 expires";
        assertThat("lease should match DHCP pattern", DHCP_MESSAGE.matcher(msg).find(), is(true));
    }

    @Test
    void testDnsQueryDoesNotMatchDhcp() {
        String msg = "query from 10.0.0.1";
        assertThat("DNS query should not match DHCP pattern", DHCP_MESSAGE.matcher(msg).find(), is(false));
    }
}
