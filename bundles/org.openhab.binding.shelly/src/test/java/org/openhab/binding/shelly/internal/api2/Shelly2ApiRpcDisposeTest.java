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

import java.io.EOFException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link Shelly2ApiRpc#dispose} and WebSocket callbacks: neither a WebSocket torn down together with
 * the Thing nor a late callback of a replaced connection may set the handler offline.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class Shelly2ApiRpcDisposeTest {

    private static final InetSocketAddress DEVICE_ADDRESS = new InetSocketAddress("192.168.1.55", 80);

    @Test
    void disposeDisposesTheRpcSocket() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2RpcSocket rpcSocket = mock(Shelly2RpcSocket.class);
        Shelly2ApiRpc api = buildApi(thing, mock(ShellyThingTable.class));
        setField(api, "rpcSocket", rpcSocket);

        api.dispose();

        verify(rpcSocket).dispose();
    }

    @Test
    void webSocketErrorAfterDisposeDoesNotSetTheThingOffline() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2ApiRpc api = buildApi(thing, mock(ShellyThingTable.class));
        setField(api, "rpcSocket", mock(Shelly2RpcSocket.class));

        api.dispose();
        api.onError(0, new EOFException("connection reset"));

        verify(thing, never()).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString(), any());
    }

    @Test
    void webSocketErrorWhileTheThingIsStoppingDoesNotSetTheThingOffline() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2RpcSocket rpcSocket = mock(Shelly2RpcSocket.class);
        Shelly2ApiRpc api = buildApi(thing, mock(ShellyThingTable.class));
        setField(api, "rpcSocket", rpcSocket);
        when(rpcSocket.isCurrentConnection(anyLong())).thenReturn(true);
        when(thing.isStopping()).thenReturn(true);

        api.onError(0, new EOFException("connection reset"));

        verify(thing, never()).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString(), any());
    }

    @Test
    void webSocketErrorOfTheCurrentConnectionSetsTheThingOffline() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2ApiRpc api = buildApi(thing, mock(ShellyThingTable.class));
        Shelly2RpcSocket rpcSocket = new Shelly2RpcSocket("test-rpc", mock(ShellyThingTable.class), DEVICE_ADDRESS,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));
        rpcSocket.addMessageHandler(api);
        setField(api, "rpcSocket", rpcSocket);

        rpcSocket.onError(null, new EOFException("connection reset"));

        verify(thing).setThingOfflineAndDisconnect(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString(), any());
    }

    @Test
    void webSocketErrorDeliveredAfterAReconnectDoesNotSetTheThingOffline() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2ApiRpc api = buildApi(thing, mock(ShellyThingTable.class));
        Shelly2RpcSocket rpcSocket = new Shelly2RpcSocket("test-rpc", mock(ShellyThingTable.class), DEVICE_ADDRESS,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));
        CountDownLatch callbackPaused = new CountDownLatch(1);
        CountDownLatch replacementConnected = new CountDownLatch(1);
        Shelly2RpctInterface delayedHandler = mock(Shelly2RpctInterface.class);
        doAnswer(invocation -> {
            callbackPaused.countDown();
            assertTrue(replacementConnected.await(5, TimeUnit.SECONDS));
            api.onError(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(delayedHandler).onError(anyLong(), any());
        rpcSocket.addMessageHandler(delayedHandler);
        setField(api, "rpcSocket", rpcSocket);

        CompletableFuture<Void> callback = CompletableFuture
                .runAsync(() -> rpcSocket.onError(null, new EOFException("connection reset")));
        assertTrue(callbackPaused.await(5, TimeUnit.SECONDS));
        rpcSocket.connect();
        replacementConnected.countDown();
        callback.get(5, TimeUnit.SECONDS);

        verify(thing, never()).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString(), any());
    }

    @Test
    void webSocketErrorOfAReplacedSocketDoesNotSetTheThingOffline() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        Shelly2ApiRpc api = buildApi(thing, mock(ShellyThingTable.class));
        Shelly2RpcSocket oldSocket = new Shelly2RpcSocket("test-rpc", mock(ShellyThingTable.class), DEVICE_ADDRESS,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));
        Shelly2RpctInterface handler = mock(Shelly2RpctInterface.class);
        oldSocket.addMessageHandler(handler);
        oldSocket.onError(null, new EOFException("connection reset"));
        ArgumentCaptor<Long> failedConnectionId = ArgumentCaptor.forClass(Long.class);
        verify(handler).onError(failedConnectionId.capture(), any());
        setField(api, "rpcSocket", new Shelly2RpcSocket("test-rpc", mock(ShellyThingTable.class), DEVICE_ADDRESS,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class)));

        api.onError(failedConnectionId.getValue(), new EOFException("connection reset"));

        verify(thing, never()).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString(), any());
    }

    @Test
    void connectWhileTheThingIsStoppingIsIgnored() throws Exception {
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        Shelly2RpcSocket rpcSocket = mock(Shelly2RpcSocket.class);
        Shelly2ApiRpc api = buildApi(thing, thingTable);
        setField(api, "rpcSocket", rpcSocket);
        when(thingTable.getThing(any(InetSocketAddress.class))).thenReturn(thing);
        when(thing.isStopping()).thenReturn(true);

        api.onConnect(new InetSocketAddress("127.0.0.1", 80), true);

        verify(rpcSocket, never()).sendMessage(anyString());
        verify(thing, never()).requestUpdates(anyInt(), anyBoolean());
    }

    private Shelly2ApiRpc buildApi(ShellyThingInterface thing, ShellyThingTable thingTable) throws Exception {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = true;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getUID()).thenReturn(new ThingUID(THING_TYPE_SHELLYPLUS1PM, "test"));

        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
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
