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

import java.lang.reflect.Field;
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
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class Shelly2ApiRpcAuthGuardTest {

    @Test
    void concurrentRefreshDuringRetryIsNotOverwritten() throws Exception {
        Shelly2AuthChallenge refreshedByOtherThread = challenge("nonce-other");
        RecordingApi api = buildApi();
        setAuthInfo(api, challenge("nonce-old"));
        api.injectedRefresh = refreshedByOtherThread;

        api.apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2RpcBaseMessage.class);

        assertSame(refreshedByOtherThread, api.authUsed.get(1));
        assertSame(refreshedByOtherThread, getAuthInfo(api));
    }

    @Test
    void staleChallengeIsRefreshedWhenNoConcurrentUpdate() throws Exception {
        Shelly2AuthChallenge original = challenge("nonce-old");
        RecordingApi api = buildApi();
        setAuthInfo(api, original);

        api.apiRequest(SHELLYRPC_METHOD_GETSTATUS, null, Shelly2RpcBaseMessage.class);

        Shelly2AuthChallenge afterRetry = getAuthInfo(api);
        assertNotSame(original, afterRetry);
        assertEquals("nonce-new", afterRetry.nonce);
        assertSame(afterRetry, api.authUsed.get(1));
    }

    private static Shelly2AuthChallenge challenge(String nonce) {
        Shelly2AuthChallenge c = new Shelly2AuthChallenge();
        c.authType = SHELLY2_AUTHTTYPE_DIGEST;
        c.realm = "shelly";
        c.nonce = nonce;
        c.algorithm = SHELLY2_AUTHALG_SHA256;
        return c;
    }

    private static ShellyApiException unauthorized() {
        ShellyApiResult result = ShellyApiResult.builder().httpCode(HttpStatus.UNAUTHORIZED_401)
                .authChallenge("Digest qop=\"auth\", realm=\"shelly\", nonce=\"nonce-new\", algorithm=SHA-256").build();
        return new ShellyApiException(result);
    }

    private static RecordingApi buildApi() throws Exception {
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
                mock(ScheduledExecutorService.class));
    }

    private static Field authInfoField() throws Exception {
        Field f = Shelly2ApiRpc.class.getDeclaredField("authInfo");
        f.setAccessible(true);
        return f;
    }

    private static void setAuthInfo(Shelly2ApiRpc api, @Nullable Shelly2AuthChallenge value) throws Exception {
        authInfoField().set(api, value);
    }

    private static @Nullable Shelly2AuthChallenge getAuthInfo(Shelly2ApiRpc api) throws Exception {
        return (Shelly2AuthChallenge) authInfoField().get(api);
    }

    private static class RecordingApi extends Shelly2ApiRpc {
        private final List<@Nullable Shelly2AuthChallenge> authUsed = new ArrayList<>();
        private @Nullable Shelly2AuthChallenge injectedRefresh;

        RecordingApi(String thingName, ShellyThingTable thingTable, ShellyThingInterface thing,
                ShellyApiConfiguration config, WebSocketClient wsClient, ScheduledExecutorService executor) {
            super(thingName, thingTable, thing, config, wsClient, executor);
        }

        @Override
        public String httpPost(@Nullable Shelly2AuthChallenge auth, String data) throws ShellyApiException {
            authUsed.add(auth);
            if (authUsed.size() == 1) {
                Shelly2AuthChallenge refresh = injectedRefresh;
                if (refresh != null) {
                    try {
                        setAuthInfo(this, refresh);
                    } catch (Exception e) {
                        throw new ShellyApiException(e);
                    }
                }
                throw unauthorized();
            }
            return "{}";
        }
    }
}
