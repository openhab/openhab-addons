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
package org.openhab.binding.samsungtv.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration;

/**
 * Tests for {@link Utils}
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class UtilsTest {

    @Test
    public void getModelYear() {
        assertEquals(22, Utils.getModelYear("22_PONTUSM_FTV"));
        assertEquals(25, Utils.getModelYear("25_PONTUSM2_FTV"));
        assertEquals(0, Utils.getModelYear(""));
        assertEquals(0, Utils.getModelYear("PONTUSM"));
        assertEquals(0, Utils.getModelYear("_PONTUSM"));
    }

    @Test
    public void loggableConfigValueHidesCredentials() {
        assertEquals("***", Utils.loggableConfigValue(SamsungTvConfiguration.WEBSOCKET_TOKEN, "12345678"));
        assertEquals("***", Utils.loggableConfigValue(SamsungTvConfiguration.SMARTTHINGS_API, "abcd-1234"));
        assertEquals("192.168.1.10", Utils.loggableConfigValue(SamsungTvConfiguration.HOST_NAME, "192.168.1.10"));
        assertEquals(8002, Utils.loggableConfigValue(SamsungTvConfiguration.PORT, 8002));
    }
}
