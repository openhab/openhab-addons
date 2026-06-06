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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for host name checker Regex.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestHostnameRegex {

    @Test
    void validHostnamesShouldMatch() {
        String[] validHosts = {
                // @formatter:off
                "foobar.local", 
                "foobar-1234-FOOBAR.local", 
                "foobar.local:1234", 
                "foobar-2.local", 
                "foobar-3.local:1234",
                "foobar._hap._tcp.local",
                "foobar._hap._tcp.local:12345", 
                "foo\\032bar.local",
                "foo\\032bar-2.local:1234",
                "PlusPMMini._http._tcp.local",
                "shellypmmini-543204828114._http._tcp.local",
                "ShellyPMMini-543204828114.local",
                "Miele-0025DC69B9AF.local",
                "DS-2CD2145FWD-I20191029AAWRD79222689.local"
                // @formatter:on
        };

        for (String host : validHosts) {
            assertTrue(HomekitBindingConstants.HOST_PATTERN.matcher(host).matches(),
                    () -> "Expected valid hostname to match: " + host);
        }
    }

    @Test
    void invalidHostnamesShouldNotMatch() {
        String[] invalidHosts = {
                // @formatter:off
                ".local", // missing name prefix  
                "foobar._hap._tcp", // missing .local suffix  
                "foobar:1234", // missing .local suffix  
                "foobar.local:123456", // port too large
                "foobar(2).local", // parentheses not allowed
                "foo bar.local", // spaces not allowed
                "foo\\033bar.local", // invalid escape sequence
                "foobar.hap.tcp.local", // service type parts without _ prefix
                "foobar._hap.local", // incomplete service type parts 
                "foobar.local.", // trailing dot not allowed
                "foobar.local.:12345" // trailing dot before port not allowed
                // @formatter:on
        };

        for (String host : invalidHosts) {
            assertFalse(HomekitBindingConstants.HOST_PATTERN.matcher(host).matches(),
                    () -> "Expected invalid hostname NOT to match: " + host);
        }
    }
}
