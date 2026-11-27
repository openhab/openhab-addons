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
package org.openhab.binding.wled.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for the log description of request bodies in {@link WLedHelper}.
 */
@NonNullByDefault
public class WLedHelperTest {

    @Test
    public void stateBodyIsLoggedUnchanged() {
        String json = "{\"on\":true,\"seg\":[{\"id\":0,\"fx\":3}]}";
        assertEquals(json, WLedHelper.describeForLog("/json/state", json));
    }

    @Test
    public void configBodyNeverContainsValues() {
        String json = "{\"ap\":{\"psk\":\"apSecret\"},\"nw\":{\"ins\":[{\"psk\":\"wifiSecret\"}]},"
                + "\"if\":{\"mqtt\":{\"psk\":\"mqttSecret\",\"en\":true}},\"ota\":{\"pwd\":\"otaSecret\"}}";
        String described = WLedHelper.describeForLog("/json/cfg", json);
        assertFalse(described.contains("apSecret"));
        assertFalse(described.contains("wifiSecret"));
        assertFalse(described.contains("mqttSecret"));
        assertFalse(described.contains("otaSecret"));
        assertTrue(described.contains("ap.psk"));
        assertTrue(described.contains("if.mqtt.en"));
        assertTrue(described.contains("ota.pwd"));
    }

    @Test
    public void singleSettingWithSecretIsHidden() {
        String described = WLedHelper.describeForLog("/json/cfg", "{\"ap\":{\"psk\":\"secret\"}}");
        assertFalse(described.contains("secret\""));
        assertEquals("<values hidden> settings:[ap.psk]", described);
    }

    @Test
    public void invalidConfigBodyIsHiddenCompletely() {
        String described = WLedHelper.describeForLog("/json/cfg", "{\"ap\":{\"psk\":\"secret\"");
        assertFalse(described.contains("secret"));
        assertEquals("<hidden>", described);
    }
}
