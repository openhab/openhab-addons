/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.govee.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.govee.internal.model.DiscoveryResponse;
import org.openhab.core.config.discovery.DiscoveryResult;

import com.google.gson.Gson;

/**
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeDiscoveryTest {

    String response = """
             {
                "msg":{
                   "cmd":"scan",
                   "data":{
                      "ip":"192.168.178.171",
                      "device":"7D:31:C3:35:33:33:44:15",
                      "sku":"H6076",
                      "bleVersionHard":"3.01.01",
                      "bleVersionSoft":"1.04.04",
                      "wifiVersionHard":"1.00.10",
                      "wifiVersionSoft":"1.02.11"
                   }
                }
            }
             """;

    @Test
    public void testProcessScanMessage() {
        GoveeDiscoveryService service = new GoveeDiscoveryService(new CommunicationManager());
        DiscoveryResponse resp = new Gson().fromJson(response, DiscoveryResponse.class);
        Objects.requireNonNull(resp);
        @Nullable
        DiscoveryResult result = service.responseToResult(resp);
        assertNotNull(result);
        Map<String, Object> deviceProperties = result.getProperties();
        assertEquals(deviceProperties.get(GoveeBindingConstants.DEVICE_TYPE), "H6076");
        assertEquals(deviceProperties.get(GoveeBindingConstants.IP_ADDRESS), "192.168.178.171");
        assertEquals(deviceProperties.get(GoveeBindingConstants.MAC_ADDRESS), "7D:31:C3:35:33:33:44:15");
    }
}
