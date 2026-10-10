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
package org.openhab.binding.shelly.internal.api2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthChallenge;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * The Wall Display rejects the Digest Authorization header on POST /rpc and wants the "auth" object in the RPC message.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class Shelly2ApiRpcWallDisplayAuthTest {

    private static final String WALLDISPLAY_BODY = "{\"code\":401,\"message\":\"{\\\"auth_type\\\":\\\"digest\\\",\\\"nonce\\\":1791588805,\\\"nc\\\":\\\"1\\\",\\\"realm\\\":\\\"ShellyWallDisplay-00082214DC9F\\\",\\\"algorithm\\\":\\\"SHA-256\\\"}\"}";
    private static final String WALLDISPLAY_HEADER = "Digest realm=\"ShellyWallDisplay-00082214DC9F\", qop=\"auth\", nonce=\"13d2a2c1\", opaque=\"\", algorithm=SHA-256";
    private static final String PLUS_HEADER = "Digest qop=\"auth\", realm=\"shellyplus1pm-aabbccddeeff\", nonce=\"1698245525\", algorithm=SHA-256";

    @Test
    void wallDisplayIsAuthenticatedInsideTheMessage() throws Exception {
        RecordingApi api = buildApi(WALLDISPLAY_BODY, WALLDISPLAY_HEADER);

        api.apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2RpcBaseMessage.class);

        assertEquals(2, api.dataUsed.size());
        assertFalse(api.dataUsed.get(0).contains("\"auth\":"));
        assertNull(api.authUsed.get(1), "no Authorization header for the Wall Display");
        String retry = api.dataUsed.get(1);
        assertTrue(retry.contains("\"auth\":{"), retry);
        assertTrue(retry.contains("\"username\":\"admin\""), retry);
        assertTrue(retry.contains("\"nonce\":\"1791588805\""), retry);
        assertTrue(retry.contains("\"realm\":\"ShellyWallDisplay-00082214DC9F\""), retry);
        assertTrue(retry.contains("\"algorithm\":\"SHA-256\""), retry);
        assertTrue(retry.contains("\"nc\":\"00000001\""), retry);
    }

    @Test
    void wallDisplayNonceIsReusedWithIncreasingCounter() throws Exception {
        RecordingApi api = buildApi(WALLDISPLAY_BODY, WALLDISPLAY_HEADER);

        api.apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2RpcBaseMessage.class);
        api.apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2RpcBaseMessage.class);

        assertEquals(3, api.dataUsed.size(), "the second request needs no challenge round trip");
        String second = api.dataUsed.get(2);
        assertTrue(second.contains("\"nonce\":\"1791588805\""), second);
        assertTrue(second.contains("\"nc\":\"00000002\""), second);
    }

    @Test
    void otherDevicesKeepTheAuthorizationHeader() throws Exception {
        RecordingApi api = buildApi("", PLUS_HEADER);

        api.apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2RpcBaseMessage.class);

        assertEquals(2, api.dataUsed.size());
        assertNotNull(api.authUsed.get(1), "Authorization header challenge expected");
        assertEquals("1698245525", api.authUsed.get(1).nonce);
        assertFalse(api.dataUsed.get(1).contains("\"auth\":"), api.dataUsed.get(1));
    }

    private static RecordingApi buildApi(String unauthorizedBody, String unauthorizedHeader) throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = false;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getUID()).thenReturn(new ThingUID(THING_TYPE_SHELLYPLUS1PM, "test"));

        HttpClient httpClient = mock(HttpClient.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(httpClient);
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-rpc", "");

        return new RecordingApi("test-rpc", thingTable, thing, config, mock(WebSocketClient.class),
                mock(ScheduledExecutorService.class), unauthorizedBody, unauthorizedHeader);
    }

    private static class RecordingApi extends Shelly2ApiRpc {
        private final List<@Nullable Shelly2AuthChallenge> authUsed = new ArrayList<>();
        private final List<String> dataUsed = new ArrayList<>();
        private final String unauthorizedBody;
        private final String unauthorizedHeader;
        private boolean challenged;

        RecordingApi(String thingName, ShellyThingTable thingTable, ShellyThingInterface thing,
                ShellyApiConfiguration config, WebSocketClient wsClient, ScheduledExecutorService executor,
                String unauthorizedBody, String unauthorizedHeader) {
            super(thingName, thingTable, thing, config, wsClient, executor);
            this.unauthorizedBody = unauthorizedBody;
            this.unauthorizedHeader = unauthorizedHeader;
        }

        @Override
        public String httpPost(@Nullable Shelly2AuthChallenge auth, String data) throws ShellyApiException {
            authUsed.add(auth);
            dataUsed.add(data);
            if (!challenged) { // the device asks for the credentials on the first request only
                challenged = true;
                ShellyApiResult result = ShellyApiResult.builder().httpCode(HttpStatus.UNAUTHORIZED_401)
                        .response(unauthorizedBody).authChallenge(unauthorizedHeader).build();
                throw new ShellyApiException(result);
            }
            return "{}";
        }
    }
}
