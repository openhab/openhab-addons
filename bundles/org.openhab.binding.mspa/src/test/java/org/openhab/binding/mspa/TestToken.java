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
package org.openhab.binding.mspa;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.binding.mspa.internal.handler.MSpaVisitorAccount;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * {@link TestToken} test can be used to request a real token
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class TestToken {
    private static VolatileStorageService storageService = new VolatileStorageService();

    public void testToken() {
        Bridge thing = new BridgeImpl(THING_TYPE_VISITOR_ACCOUNT, new ThingUID("test", "account"));
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("email", "a@b.c");
        configMap.put("password", "pwd");
        configMap.put("region", "ROW");

        SslContextFactory sslContextFactory = new SslContextFactory(true);
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

    void testJson() {
        String input = "{\"grants\":\"{\\\"abc\\\":\\\"2025-05-13T10:37:31.451941195Z\\\"}\",\"expires\":86400,\"created\":\"2025-05-12T20:13:10.101935500Z\",\"accesstoken\":\"{\\\"expires\\\":86400,\\\"created\\\":\\\"2025-05-13T10:37:31.424045271Z\\\",\\\"token\\\":\\\"b5b2f814d9c74abe13b5bd6fa19b0d7e\\\"}\",\"token\":\"98b3a63d091471cb9ac4009bea7fced2\"}";
        JSONObject test = new JSONObject(input);
        try {
            System.out.println(test.getJSONObject("grants"));
        } catch (Exception e) {
            if (test.has("token")) {
                JSONObject tokenJson = new JSONObject(test.remove("token"));
                test.put("token", tokenJson);
                JSONObject test2 = test.getJSONObject("token");
                System.out.println("Clazz " + test2.get("expires").getClass());
            }
            System.out.println(test);
        }
    }
}
