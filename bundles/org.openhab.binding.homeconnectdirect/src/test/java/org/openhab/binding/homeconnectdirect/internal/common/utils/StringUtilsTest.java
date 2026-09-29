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
package org.openhab.binding.homeconnectdirect.internal.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringUtils}.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
class StringUtilsTest {

    @Test
    void mapKeyToLabel() {
        // Test WiFi logic
        assertEquals("Deactivate WiFi", StringUtils.mapKeyToLabel("BSH.Common.Command.DeactivateWiFi", null));
        assertEquals("WiFi Settings", StringUtils.mapKeyToLabel("bla.WiFiSettings", null));

        assertEquals("X Ray", StringUtils.mapKeyToLabel("test.XRay", null));
        assertEquals("A String", StringUtils.mapKeyToLabel("test.AString", null));
        assertEquals("My TV", StringUtils.mapKeyToLabel("test.MyTV", null));
        assertEquals("Pre Wash", StringUtils.mapKeyToLabel("test.PreWash", null));
        assertEquals("Re Wash", StringUtils.mapKeyToLabel("test.ReWash", null));
        assertEquals("Pre Rinse", StringUtils.mapKeyToLabel("PreRinse", null));
    }
}
