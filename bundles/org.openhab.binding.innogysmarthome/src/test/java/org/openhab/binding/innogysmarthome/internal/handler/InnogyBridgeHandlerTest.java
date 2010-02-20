/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants;
import org.openhab.binding.innogysmarthome.internal.InnogyWebSocket;
import org.openhab.binding.innogysmarthome.internal.client.InnogyClient;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.DeviceConfig;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class InnogyBridgeHandlerTest {

    private static final int MAXIMUM_RETRY_EXECUTIONS = 10;

    private InnogyBridgeHandlerAccessible bridgeHandler;
    private Bridge bridgeMock;
    private InnogyWebSocket webSocketMock;

    @Before
    public void before() throws Exception {
        bridgeMock = mock(Bridge.class);
        when(bridgeMock.getUID()).thenReturn(new ThingUID("innogysmarthome", "bridge"));

        webSocketMock = mock(InnogyWebSocket.class);

        OAuthClientService oAuthService = mock(OAuthClientService.class);

        OAuthFactory oAuthFactoryMock = mock(OAuthFactory.class);
        when(oAuthFactoryMock.createOAuthClientService(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(oAuthService);

        HttpClient httpClientMock = mock(HttpClient.class);

        bridgeHandler = new InnogyBridgeHandlerAccessible(bridgeMock, oAuthFactoryMock, httpClientMock);
    }

    @Test
    public void testInitializeBridgeNotAvailable() throws Exception {
        Configuration bridgeConfig = new Configuration();
        HashMap<String, Object> map = new HashMap<>();
        map.put("brand", "XY");
        bridgeConfig.setProperties(map);

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock, never()).start();
        assertEquals(0, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testInitialize() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(1, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testInitializeErrorOnStartingWebSocket() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        doThrow(new RuntimeException("Test-Exception")).when(webSocketMock).start();

        bridgeHandler.initialize();

        verify(webSocketMock, times(MAXIMUM_RETRY_EXECUTIONS)).start();
        assertEquals(1, bridgeHandler.getDirectExecutionCount()); // only the first execution should be without a delay
    }

    @Test
    public void testConnectionClosed() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(1, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.connectionClosed();

        verify(webSocketMock, times(2)).start(); // automatically restarted (with a delay)
        assertEquals(1, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.connectionClosed();

        verify(webSocketMock, times(3)).start(); // automatically restarted (with a delay)
        assertEquals(1, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testConnectionClosedReconnectNotPossible() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(1, bridgeHandler.getDirectExecutionCount());

        doThrow(new ConnectException("Connection refused")).when(webSocketMock).start();

        bridgeHandler.connectionClosed();

        verify(webSocketMock, times(10)).start(); // automatic reconnect attempts (with a delay)
        assertEquals(1, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testOnEventDisconnect() throws Exception {
        final String disconnectEventJSON = "{ type: \"Disconnect\" }";

        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(1, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.onEvent(disconnectEventJSON);

        verify(webSocketMock, times(2)).start(); // automatically restarted (with a delay)
        assertEquals(1, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.onEvent(disconnectEventJSON);

        verify(webSocketMock, times(3)).start(); // automatically restarted (with a delay)
        assertEquals(1, bridgeHandler.getDirectExecutionCount());
    }

    private class InnogyBridgeHandlerAccessible extends InnogyBridgeHandler {

        private final InnogyClient innogyClientMock;
        private final ScheduledExecutorService schedulerMock;
        private int executionCount;
        private int directExecutionCount;

        private InnogyBridgeHandlerAccessible(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient)
                throws Exception {
            super(bridge, oAuthFactory, httpClient);

            Device bridgeDevice = new Device();
            bridgeDevice.setId("bridgeId");
            bridgeDevice.setType(InnogyBindingConstants.DEVICE_SHC);
            bridgeDevice.setConfig(new DeviceConfig());

            innogyClientMock = mock(InnogyClient.class);
            when(innogyClientMock.getFullDevices()).thenReturn(Collections.singletonList(bridgeDevice));

            schedulerMock = mock(ScheduledExecutorService.class);

            doAnswer(invocationOnMock -> {
                if (executionCount <= MAXIMUM_RETRY_EXECUTIONS) {
                    executionCount++;
                    invocationOnMock.getArgument(0, Runnable.class).run();
                }
                return null;
            }).when(schedulerMock).execute(any());

            doAnswer(invocationOnMock -> {
                if (executionCount <= MAXIMUM_RETRY_EXECUTIONS) {
                    executionCount++;
                    long seconds = invocationOnMock.getArgument(1);
                    if (seconds <= 0) {
                        directExecutionCount++;
                    }

                    invocationOnMock.getArgument(0, Runnable.class).run();
                }
                return mock(ScheduledFuture.class);
            }).when(schedulerMock).schedule(any(Runnable.class), anyLong(), any());
        }

        public int getDirectExecutionCount() {
            return directExecutionCount;
        }

        @Override
        @NonNull
        InnogyClient createInnogyClient(@NonNull OAuthClientService oAuthService, @NonNull HttpClient httpClient) {
            return innogyClientMock;
        }

        @Override
        @NonNull
        InnogyWebSocket createWebSocket() {
            return webSocketMock;
        }

        @Override
        @NonNull
        ScheduledExecutorService getScheduler() {
            return schedulerMock;
        }
    }
}
