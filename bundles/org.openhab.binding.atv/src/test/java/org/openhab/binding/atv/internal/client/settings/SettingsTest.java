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
package org.openhab.binding.atv.internal.client.settings;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Settings}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class SettingsTest {

    private static final String SECRET = "a1b2c3-secret";

    @Test
    void toStringLeavesOutSecrets() {
        ProtocolSettings protocols = ProtocolSettings.ofDefaults()
                .withAirplay(AirPlaySettings.ofDefaults().withIdentifier("airplay-id").withCredentials(SECRET)
                        .withPassword(SECRET))
                .withCompanion(CompanionSettings.ofDefaults().withCredentials(SECRET))
                .withMrp(MrpSettings.ofDefaults().withCredentials(SECRET))
                .withRaop(RaopSettings.ofDefaults().withCredentials(SECRET).withPassword(SECRET));

        String text = Settings.ofDefaults().withProtocols(protocols).toString();

        assertFalse(text.contains(SECRET), text);
        assertTrue(text.contains("airplay-id"), text);
        assertTrue(text.contains("credentials=set"), text);
    }
}
