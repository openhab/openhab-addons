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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mspa.internal.MSpaCommandOptionProvider;
import org.openhab.binding.mspa.internal.MSpaConstants.ServiceRegion;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.binding.mspa.internal.handler.MSpaOwnerAccount;
import org.openhab.binding.mspa.internal.handler.MSpaPool;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.State;

/**
 * {@link TestMessages} tests some generic use cases
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class TestMessages {
    private static VolatileStorageService storageService = new VolatileStorageService();

    @Test
    void testSignature() {
        String fileName = "src/test/resources/Signature.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JSONObject json = new JSONObject(content);
            String calculatedSignature = MSpaUtils.getSignature(json.getString("nonce"), json.getLong("ts"),
                    ServiceRegion.ROW);
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
            assertEquals(86400, token.getExpiresIn(), "Expiration check");
            assertNotNull(token.getCreatedOn(), "Expiration check");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testDiscovery() {
        Bridge thing = new BridgeImpl(THING_TYPE_OWNER_ACCOUNT, new ThingUID("mspa", "account"));
        Map<String, Object> configMap = new HashMap<>();
        MSpaDiscoveryService discovery = new MSpaDiscoveryService();
        DiscoveryListenerMock discoveryListener = new DiscoveryListenerMock();
        discovery.addDiscoveryListener(discoveryListener);
        configMap.put("email", "a@b.c");
        configMap.put("password", "pwd");
        configMap.put("region", "EU");
        MSpaOwnerAccount account = new MSpaOwnerAccount(thing, mock(HttpClient.class), discovery,
                storageService.getStorage(BINDING_ID));
        String fileName = "src/test/resources/DevicelistResponse.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            account.decodeDevices(content);
            List<DiscoveryResult> results = discoveryListener.getResults();
            assertEquals(1, results.size(), "Number of discovery results");
            DiscoveryResult result = results.get(0);
            assertNotNull(result.getBridgeUID(), "Bridge available");
            assertEquals("MSpa Pool OSLO", result.getLabel(), "Label of discovery results");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testDataUpdate() {
        Thing thing = new ThingImpl(THING_TYPE_POOL, new ThingUID("mspa", "pool"));
        MSpaPool pool = new MSpaPool(thing, mock(UnitProvider.class), mock(MSpaCommandOptionProvider.class));
        CallbackMock callback = new CallbackMock();
        pool.setCallback(callback);
        String fileName = "src/test/resources/DataResponse.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            pool.distributeData(content);
            assertEquals(10, callback.numberOfUpdates(), "Number of state updates");

            State heater = callback.getState(CHANNEL_HEATER);
            assertNotNull(heater, "Heater available");
            assertTrue(heater instanceof OnOffType, "Heater OnOffType");
            assertTrue(OnOffType.ON.equals(heater), "Heater is ON");

            State jetStream = callback.getState(CHANNEL_JET_STREAM);
            assertNotNull(jetStream, "Jet-Stream available");
            assertTrue(jetStream instanceof OnOffType, "Jet-Stream OnOffType");
            assertTrue(OnOffType.OFF.equals(jetStream), "Jet-Stream is OFF");

            State bubbles = callback.getState(CHANNEL_BUBBLES);
            assertNotNull(bubbles, "Bubbles available");
            assertTrue(bubbles instanceof OnOffType, "Bubbles OnOffType");
            assertTrue(OnOffType.OFF.equals(bubbles), "Bubbles are OFF");

            State bubbleLevel = callback.getState(CHANNEL_BUBBLE_LEVEL);
            assertNotNull(bubbleLevel, "Bubble level available");
            assertTrue(bubbleLevel instanceof DecimalType, "Bubble level DecimalType");
            DecimalType bubbleLevelDecimal = (DecimalType) bubbleLevel;
            assertEquals(1, bubbleLevelDecimal.intValue(), "Bubble level value");

            State circulation = callback.getState(CHANNEL_CIRCULATE);
            assertNotNull(circulation, "Circulation available");
            assertTrue(circulation instanceof OnOffType, "Circulation OnOffType");
            assertTrue(OnOffType.ON.equals(circulation), "Circulation is ON");

            State uvc = callback.getState(CHANNEL_UVC);
            assertNotNull(uvc, "UVC available");
            assertTrue(uvc instanceof OnOffType, "UVC OnOffType");
            assertTrue(OnOffType.OFF.equals(uvc), "UVC is OFF");

            State ozone = callback.getState(CHANNEL_OZONE);
            assertNotNull(ozone, "Ozone available");
            assertTrue(ozone instanceof OnOffType, "Ozone OnOffType");
            assertTrue(OnOffType.OFF.equals(ozone), "Ozone is OFF");

            State lock = callback.getState(CHANNEL_LOCK);
            assertNotNull(lock, "Lock available");
            assertTrue(lock instanceof OnOffType, "Lock OnOffType");
            assertTrue(OnOffType.OFF.equals(lock), "Lock is OFF");

            State temp = callback.getState(CHANNEL_WATER_CURRENT_TEMPERATURE);
            assertNotNull(temp, "Water temperature available");
            assertTrue(temp instanceof QuantityType, "Water temperature QuantityType");
            QuantityType<?> tempQuantity = (QuantityType<?>) temp;
            assertEquals(SIUnits.CELSIUS, tempQuantity.getUnit(), "Water temperature unit");
            assertEquals(41, tempQuantity.doubleValue(), 0.1, "Water temperature unit");

            State targetTemp = callback.getState(CHANNEL_WATER_TARGET_TEMPERATURE);
            assertNotNull(targetTemp, "Target water temperature available");
            assertTrue(targetTemp instanceof QuantityType, "Target water temperature QuantityType");
            QuantityType<?> targetTempQuantity = (QuantityType<?>) targetTemp;
            assertEquals(SIUnits.CELSIUS, targetTempQuantity.getUnit(), "Target water temperature unit");
            assertEquals(40, targetTempQuantity.doubleValue(), 0.1, "Target water temperature unit");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }
}
