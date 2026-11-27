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
package org.openhab.binding.myvaillant.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the helpers of {@link MyVaillantApi}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class MyVaillantApiTest {

    @Test
    void realmDependsOnBrandAndCountry() {
        assertEquals("vaillant-germany-b2c", MyVaillantApi.getRealm("vaillant", "germany"));
        assertEquals("sdbg-france-b2c", MyVaillantApi.getRealm("sdbg", "france"));
        assertEquals("bulex-b2c", MyVaillantApi.getRealm("bulex", "belgium"));
        assertEquals("glow-worm-b2c", MyVaillantApi.getRealm("glow-worm", null));
    }

    @Test
    void unescapesTheLoginFormUrl() {
        assertEquals(
                "https://identity.vaillant-group.com/auth/realms/vaillant-germany-b2c/login-actions/authenticate?session_code=a-b&execution=1=2&client_id=myvaillant&tab_id=x",
                MyVaillantApi.unescapeHtml(
                        "https://identity.vaillant-group.com/auth/realms/vaillant-germany-b2c/login-actions/authenticate?session_code=a-b&amp;execution=1&#61;2&amp;client_id=myvaillant&#x26;tab_id=x"));
    }

    @Test
    void quotaPauseFromMessageOrHeader() {
        assertEquals(Duration.ofMinutes(29).plusSeconds(22), MyVaillantApi.getQuotaPause(
                "{\"statusCode\":403,\"message\":\"Out of call volume quota. Quota will be replenished in 00:29:12.\"}",
                null));
        assertEquals(Duration.ofSeconds(120), MyVaillantApi.getQuotaPause("Quota exceeded", "120"));
        assertEquals(Duration.ofHours(1),
                MyVaillantApi.getQuotaPause("Quota exceeded", "Wed, 21 Oct 2026 07:28:00 GMT"));
    }
}
