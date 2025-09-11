/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mspa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.binding.mspa.internal.handler.MSpaVisitorAccount;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * {@link TestGeneric} for some basic tests
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class TestGeneric {
    private static VolatileStorageService storageService = new VolatileStorageService();

    @Test
    void testDeviceProperties() {
        String fileName = "src/test/resources/DevicelistResponse.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject devices = new JSONObject(content);
            if (devices.has("data")) {
                JSONObject data = devices.getJSONObject("data");
                if (data.has("list")) {
                    JSONArray list = data.getJSONArray("list");
                    JSONObject device = list.getJSONObject(0);
                    Map<String, Object> deviceProperties = MSpaUtils.getDiscoveryProperties(device.toMap());
                    Object deviceId = deviceProperties.get("deviceId");
                    assertNotNull(deviceId, "Device ID must be available");
                    assertEquals("test_device_id", deviceId.toString(), "Device ID value");
                    Object productId = deviceProperties.get("productId");
                    assertNotNull(productId, "Product ID must be available");
                    assertEquals("test_product_id", productId.toString(), "Device ID value");
                } else {
                    fail("No list found");
                }
            } else {
                fail("No data found");
            }
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    public void testToken() {
        Bridge thing = new BridgeImpl(THING_TYPE_VISITOR_ACCOUNT, new ThingUID("test", "account"));
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("email", "a@b.c");
        configMap.put("password", "pwd");
        configMap.put("region", "ROW");

        SslContextFactory sslContextFactory = new SslContextFactory.Client(true);
        HttpClient client = new HttpClient(sslContextFactory);
        try {
            client.start();
            MSpaVisitorAccount account = new MSpaVisitorAccount(thing, client, mock(MSpaDiscoveryService.class),
                    storageService.getStorage(BINDING_ID));
            account.setCallback(mock(ThingHandlerCallback.class));
            account.handleConfigurationUpdate(configMap);
            account.initialize();
            account.requestToken();
        } catch (Exception e) {
            fail();
        }
    }
}
