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
package org.openhab.binding.goveelan.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeLanDiscoveryTest {

    String response = "{\"msg\":{\"cmd\":\"scan\",\"data\":{\"ip\":\"192.168.178.171\",\"device\":\"7D:31:C3:35:33:33:44:15\",\"sku\":\"H6076\",\"bleVersionHard\":\"3.01.01\",\"bleVersionSoft\":\"1.04.04\",\"wifiVersionHard\":\"1.00.10\",\"wifiVersionSoft\":\"1.02.11\"}}}";

    @Test
    public void testProcessScanMessage() {
        GoveeLanDiscoveryService service = new GoveeLanDiscoveryService();
        Map<String, Object> deviceProperties = service.getDeviceProperties(response);
        assertNotNull(deviceProperties);
        assertEquals(deviceProperties.get(GoveeLanConfiguration.DEVICETYPE), "H6076");
        assertEquals(deviceProperties.get(GoveeLanConfiguration.IPADDRESS), "192.168.178.171");
        assertEquals(deviceProperties.get(GoveeLanConfiguration.MAC_ADDRESS), "7D:31:C3:35:33:33:44:15");
    }
}
