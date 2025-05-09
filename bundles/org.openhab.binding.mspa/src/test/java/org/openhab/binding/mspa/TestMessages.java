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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.binding.mspa.internal.handler.MSpaAccount;
import org.openhab.binding.mspa.internal.handler.MSpaPool;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * {@link TestMessages} tests some generic use cases
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class TestMessages implements DiscoveryListener {
    private static VolatileStorageService storageService = new VolatileStorageService();

    @Test
    void testSignature() {
        String fileName = "src/test/resources/Signature.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject json = new JSONObject(content);
            String calculatedSignature = MSpaUtils.getSignature(json.getString("nonce"), json.getLong("ts"),
                    REGION_ROW);
            String requiredSignature = json.getString("sign");
            assertEquals(requiredSignature, calculatedSignature, "Signature check");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testToken() {
        String fileName = "src/test/resources/TokenResponse.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            AccessTokenResponse token = MSpaUtils.decodeNewToken(content);
            assertEquals("test_token", token.getAccessToken(), "Token check");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testDiscovery() {
        Bridge thing = new BridgeImpl(THING_TYPE_ACCOUNT, new ThingUID("mspa", "account"));
        Map<String, Object> configMap = new HashMap<>();
        MSpaDiscoveryService discovery = new MSpaDiscoveryService();
        discovery.addDiscoveryListener(this);
        configMap.put("email", "a@b.c");
        configMap.put("password", "pwd");
        configMap.put("region", "EU");
        MSpaAccount account = new MSpaAccount(thing, mock(HttpClient.class), discovery,
                storageService.getStorage(BINDING_ID));
        String fileName = "src/test/resources/DevicelistResponse.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            account.decodeDevices(content);
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testCommand() {
        Thing thing = new ThingImpl(THING_TYPE_POOL, new ThingUID("mspa", "pool"));
        MSpaPool pool = new MSpaPool(thing, mock(HttpClient.class));
        pool.handleCommand(new ChannelUID(thing.getUID(), HEATER), OnOffType.ON);
        pool.handleCommand(new ChannelUID(thing.getUID(), BUBBLE_LEVEL), new DecimalType(-10));
        pool.handleCommand(new ChannelUID(thing.getUID(), BUBBLE_LEVEL), new DecimalType(80));
        pool.handleCommand(new ChannelUID(thing.getUID(), BUBBLE_LEVEL), new DecimalType(2));
        pool.handleCommand(new ChannelUID(thing.getUID(), WATER_TARGET_TEMPERATURE), QuantityType.valueOf("80 C"));
        pool.handleCommand(new ChannelUID(thing.getUID(), WATER_TARGET_TEMPERATURE), QuantityType.valueOf("5 C"));
        pool.handleCommand(new ChannelUID(thing.getUID(), WATER_TARGET_TEMPERATURE), QuantityType.valueOf("35 C"));
    }

    @Test
    void testDataUpdate() {
        Thing thing = new ThingImpl(THING_TYPE_POOL, new ThingUID("mspa", "pool"));
        MSpaPool pool = new MSpaPool(thing, mock(HttpClient.class));
        String fileName = "src/test/resources/DataResponse.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            pool.distributeData(content);
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        System.out.println("Thing discovered " + result);
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
    }

    @Override
    public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        return null;
    }
}
