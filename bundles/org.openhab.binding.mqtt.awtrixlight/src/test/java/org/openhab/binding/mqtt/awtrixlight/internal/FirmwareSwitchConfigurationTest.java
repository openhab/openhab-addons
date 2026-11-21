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
package org.openhab.binding.mqtt.awtrixlight.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;

/**
 * Test cases for the {@link FirmwareSwitchConfiguration}.
 *
 * @author Keith T. Garner - Initial contribution
 */
class FirmwareSwitchConfigurationTest {

    @Test
    void clockKeepsTheSharedParametersOnly() {
        Configuration awtrix3 = new Configuration(Map.of("basetopic", "awtrix_abc", "appLockTimeout", 12,
                "discoverDefaultApps", true, "lowBatteryThreshold", 30));

        Configuration kept = FirmwareSwitchConfiguration.forClock(awtrix3);

        // Configuration turns numbers into BigDecimal, so the expectation has to go through it as well
        assertEquals(
                new Configuration(Map.of("basetopic", "awtrix_abc", "appLockTimeout", 12, "discoverDefaultApps", true))
                        .getProperties(),
                kept.getProperties());
    }

    @Test
    void clockWithMissingParametersKeepsWhatIsThere() {
        Configuration kept = FirmwareSwitchConfiguration.forClock(new Configuration(Map.of("basetopic", "a")));

        assertEquals(Map.of("basetopic", "a"), kept.getProperties());
    }

    @Test
    void parameterWithoutValueIsLeftOut() {
        Configuration awtrix3 = new Configuration(Map.of("basetopic", "a"));
        awtrix3.put("appLockTimeout", null);

        Configuration kept = FirmwareSwitchConfiguration.forClock(awtrix3);

        assertEquals(Map.of("basetopic", "a"), kept.getProperties());
    }

    @Test
    void appKeepsNameAndButtons() {
        Configuration kept = FirmwareSwitchConfiguration
                .forApp(new Configuration(Map.of("appname", "washing", "useButtons", true, "unknown", "x")));

        assertEquals(Map.of("appname", "washing", "useButtons", true), kept.getProperties());
    }
}
