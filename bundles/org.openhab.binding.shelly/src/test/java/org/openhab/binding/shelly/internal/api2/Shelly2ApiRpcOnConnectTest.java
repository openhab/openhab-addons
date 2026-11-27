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

import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link Shelly2ApiRpc#onConnect}: a WebSocket reconnect on an always-on device must
 * re-arm the periodic status push and trigger an immediate poll, since that subscription is tied to
 * the WebSocket session and is otherwise only ever requested once, during the very first connect.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class Shelly2ApiRpcOnConnectTest {

    @Test
    void reconnectReArmsStatusUpdatesAndTriggersPoll() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        Shelly2RpcSocket rpcSocket = mock(Shelly2RpcSocket.class);
        when(rpcSocket.isConnected()).thenReturn(true);

        Shelly2ApiRpc api = buildApi(thing, thingTable, true, true);
        setField(api, "rpcSocket", rpcSocket);
        when(thingTable.getThing(any(InetSocketAddress.class))).thenReturn(thing);

        api.onConnect(new InetSocketAddress("127.0.0.1", 80), true);

        verify(rpcSocket).sendMessage(contains(Shelly2ApiJsonDTO.SHELLYRPC_METHOD_GETSTATUS));
        verify(thing).requestUpdates(1, false);
    }

    @Test
    void firstConnectDoesNotReArmStatusUpdates() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        Shelly2RpcSocket rpcSocket = mock(Shelly2RpcSocket.class);

        // profile.initialized is false: this is the very first connect, already handled explicitly
        // by getDeviceProfile()'s own firstInit branch, so onConnect must not duplicate it.
        Shelly2ApiRpc api = buildApi(thing, thingTable, false, true);
        setField(api, "rpcSocket", rpcSocket);
        when(thingTable.getThing(any(InetSocketAddress.class))).thenReturn(thing);

        api.onConnect(new InetSocketAddress("127.0.0.1", 80), true);

        verify(rpcSocket, never()).sendMessage(anyString());
        verify(thing, never()).requestUpdates(anyInt(), anyBoolean());
    }

    @Test
    void batteryDeviceReconnectDoesNotReArmStatusUpdates() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        Shelly2RpcSocket rpcSocket = mock(Shelly2RpcSocket.class);

        // Battery/sleeping devices are not alwaysOn and have no persistent WebSocket to re-arm.
        Shelly2ApiRpc api = buildApi(thing, thingTable, true, false);
        setField(api, "rpcSocket", rpcSocket);
        when(thingTable.getThing(any(InetSocketAddress.class))).thenReturn(thing);

        api.onConnect(new InetSocketAddress("127.0.0.1", 80), true);

        verify(rpcSocket, never()).sendMessage(anyString());
        verify(thing, never()).requestUpdates(anyInt(), anyBoolean());
    }

    private Shelly2ApiRpc buildApi(ShellyThingInterface thing, ShellyThingTable thingTable, boolean initialized,
            boolean alwaysOn) throws Exception {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = initialized;
        profile.alwaysOn = alwaysOn;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getUID()).thenReturn(new ThingUID(THING_TYPE_SHELLYPLUS1PM, "test"));

        HttpClient httpClient = mock(HttpClient.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(httpClient);
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-rpc", "");

        return new Shelly2ApiRpc("test-rpc", thingTable, thing, config, mock(WebSocketClient.class),
                mock(ScheduledExecutorService.class));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static NetworkAddressService nullNas() {
        return new NetworkAddressService() {
            @Override
            public @Nullable String getPrimaryIpv4HostAddress() {
                return null;
            }

            @Override
            public @Nullable String getConfiguredBroadcastAddress() {
                return null;
            }

            @Override
            public boolean isUseOnlyOneAddress() {
                return false;
            }

            @Override
            public boolean isUseIPv6() {
                return false;
            }

            @Override
            public void addNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }

            @Override
            public void removeNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }
        };
    }
}
