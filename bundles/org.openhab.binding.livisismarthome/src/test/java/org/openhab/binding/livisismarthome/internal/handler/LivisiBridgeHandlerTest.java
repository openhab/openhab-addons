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
package org.openhab.binding.livisismarthome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.*;
import static org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO.LINK_TYPE_DEVICE;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livisismarthome.internal.LivisiWebSocket;
import org.openhab.binding.livisismarthome.internal.client.LivisiClient;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceConfigDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.StateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.event.EventPropertiesDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.DoubleStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.StringStateDTO;
import org.openhab.binding.livisismarthome.internal.client.exception.WebSocketConnectException;
import org.openhab.binding.livisismarthome.internal.manager.FullDeviceManager;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class LivisiBridgeHandlerTest {

    private static final int MAXIMUM_RETRY_EXECUTIONS = 10;

    private @NonNullByDefault({}) LivisiBridgeHandlerAccessible bridgeHandler;
    private @NonNullByDefault({}) Bridge bridgeMock;
    private @NonNullByDefault({}) LivisiWebSocket webSocketMock;
    private @NonNullByDefault({}) Map<String, State> updatedChannels;

    private @NonNullByDefault({}) Level previousLoggingLevel;

    @BeforeEach
    public void before() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(LivisiBridgeHandler.class);
        previousLoggingLevel = logger.getLevel();
        logger.setLevel(Level.OFF); // avoid (test) exception logs which occur within these reconnect tests

        updatedChannels = new LinkedHashMap<>();

        bridgeMock = mock(Bridge.class);
        when(bridgeMock.getUID()).thenReturn(new ThingUID("livisismarthome", "bridge"));

        webSocketMock = mock(LivisiWebSocket.class);

        OAuthClientService oAuthService = mock(OAuthClientService.class);

        OAuthFactory oAuthFactoryMock = mock(OAuthFactory.class);
        when(oAuthFactoryMock.createOAuthClientService(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(oAuthService);

        HttpClient httpClientMock = mock(HttpClient.class);

        bridgeHandler = new LivisiBridgeHandlerAccessible(bridgeMock, oAuthFactoryMock, httpClientMock);
        bridgeHandler.setCallback(mock(ThingHandlerCallback.class));
    }

    @AfterEach
    public void after() {
        Logger logger = (Logger) LoggerFactory.getLogger(LivisiBridgeHandler.class);
        logger.setLevel(previousLoggingLevel);
    }

    @Test
    public void testInitialize() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(2, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testInitializeErrorOnStartingWebSocket() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        doThrow(new WebSocketConnectException("Test-Exception", new IOException())).when(webSocketMock).start();

        bridgeHandler.initialize();

        verify(webSocketMock, times(MAXIMUM_RETRY_EXECUTIONS - 1)).start();
        assertEquals(2, bridgeHandler.getDirectExecutionCount()); // only the first execution should be without a delay
    }

    @Test
    public void testConnectionClosed() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(2, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.connectionClosed();

        verify(webSocketMock, times(2)).start(); // automatically restarted (with a delay)
        assertEquals(2, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.connectionClosed();

        verify(webSocketMock, times(3)).start(); // automatically restarted (with a delay)
        assertEquals(2, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testConnectionClosedReconnectNotPossible() throws Exception {
        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(2, bridgeHandler.getDirectExecutionCount());

        doThrow(new WebSocketConnectException("Connection refused", new IOException())).when(webSocketMock).start();

        bridgeHandler.connectionClosed();

        verify(webSocketMock, times(MAXIMUM_RETRY_EXECUTIONS - 1)).start(); // automatic reconnect attempts (with a
                                                                            // delay)
        assertEquals(2, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testOnEventDisconnect() throws Exception {
        final String disconnectEventJSON = "{ type: \"Disconnect\" }";

        Configuration bridgeConfig = new Configuration();

        when(bridgeMock.getConfiguration()).thenReturn(bridgeConfig);

        bridgeHandler.initialize();

        verify(webSocketMock).start();
        assertEquals(2, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.onEvent(disconnectEventJSON);

        verify(webSocketMock, times(2)).start(); // automatically restarted (with a delay)
        assertEquals(2, bridgeHandler.getDirectExecutionCount());

        bridgeHandler.onEvent(disconnectEventJSON);

        verify(webSocketMock, times(3)).start(); // automatically restarted (with a delay)
        assertEquals(2, bridgeHandler.getDirectExecutionCount());
    }

    @Test
    public void testOnDeviceStateChangedSHCClassic() {
        DeviceDTO bridgeDevice = createBridgeDevice(true);

        StateDTO state = new StateDTO();
        state.setCPULoad(doubleState(30.5));
        state.setMemoryLoad(doubleState(60.5));
        state.setDiskUsage(doubleState(70.5));
        state.setOSState(stringState("Normal"));

        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setState(state);
        bridgeDevice.setDeviceState(deviceState);

        bridgeHandler.onDeviceStateChanged(bridgeDevice);

        assertTrue(isChannelUpdated(CHANNEL_CPU, QuantityType.valueOf(30.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MEMORY, QuantityType.valueOf(60.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_DISK, QuantityType.valueOf(70.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_STATUS, StringType.valueOf("NORMAL")));
    }

    @Test
    public void testOnDeviceStateChangedSHCA() {
        DeviceDTO bridgeDevice = createBridgeDevice(false);

        StateDTO state = new StateDTO();
        state.setCpuUsage(doubleState(30.5));
        state.setMemoryUsage(doubleState(60.5));
        state.setDiskUsage(doubleState(70.5));
        state.setOperationStatus(stringState("active"));

        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setState(state);
        bridgeDevice.setDeviceState(deviceState);

        bridgeHandler.onDeviceStateChanged(bridgeDevice);

        assertTrue(isChannelUpdated(CHANNEL_CPU, QuantityType.valueOf(30.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MEMORY, QuantityType.valueOf(60.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_DISK, QuantityType.valueOf(70.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_STATUS, StringType.valueOf("ACTIVE")));
    }

    @Test
    public void testOnDeviceStateChangedEventSHCClassic() {
        DeviceDTO bridgeDevice = createBridgeDevice(true);

        // Example SHC-Classic-Event
        // {"sequenceNumber":709,"type":"StateChanged","namespace":"core.RWE","timestamp":"2022-03-17T11:13:20.1100000Z","source":"/device/812ae7233697408378943e5d943a450x","properties":{"updateAvailable":"","lastReboot":"2022-03-17T
        // 10:10:14.3530000Z","MBusDongleAttached":false,"LBDongleAttached":false,"configVersion":649,"discoveryActive":false,"IPAddress":"192.168.178.12","currentUTCOffset":
        // 60,"productsHash":"xy","OSState":"Normal","memoryLoad":67,"CPULoad":39,"diskUsage":20}}

        EventDTO event = createDeviceEvent(c -> {
            c.setOSState("Normal");
            c.setCPULoad(39.5);
            c.setMemoryLoad(67.5);
            c.setDiskUsage(20.5);
        });

        StateDTO state = new StateDTO();
        state.setCPULoad(doubleState(30.5));
        state.setMemoryLoad(doubleState(60.5));
        state.setDiskUsage(doubleState(70.5));
        state.setOSState(stringState("active"));

        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setState(state);
        bridgeDevice.setDeviceState(deviceState);

        bridgeHandler.onDeviceStateChanged(bridgeDevice, event);

        assertTrue(isChannelUpdated(CHANNEL_CPU, QuantityType.valueOf(39.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MEMORY, QuantityType.valueOf(67.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_DISK, QuantityType.valueOf(20.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_STATUS, StringType.valueOf("NORMAL")));
    }

    @Test
    public void testOnDeviceStateChangedEventSHCA() {
        DeviceDTO bridgeDevice = createBridgeDevice(false);

        EventDTO event = createDeviceEvent(c -> {
            c.setOperationStatus("active");
            c.setCpuUsage(39.5);
            c.setMemoryUsage(67.5);
            c.setDiskUsage(20.5);
        });

        StateDTO state = new StateDTO();
        state.setCpuUsage(doubleState(30.5));
        state.setMemoryUsage(doubleState(60.5));
        state.setDiskUsage(doubleState(70.5));
        state.setOperationStatus(stringState("shuttingdown"));

        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setState(state);
        bridgeDevice.setDeviceState(deviceState);

        bridgeHandler.onDeviceStateChanged(bridgeDevice, event);

        assertTrue(isChannelUpdated(CHANNEL_CPU, QuantityType.valueOf(39.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_MEMORY, QuantityType.valueOf(67.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_DISK, QuantityType.valueOf(20.5, Units.PERCENT)));
        assertTrue(isChannelUpdated(CHANNEL_OPERATION_STATUS, StringType.valueOf("ACTIVE")));
    }

    private static DoubleStateDTO doubleState(double value) {
        DoubleStateDTO state = new DoubleStateDTO();
        state.setValue(value);
        return state;
    }

    private static StringStateDTO stringState(String value) {
        StringStateDTO state = new StringStateDTO();
        state.setValue(value);
        return state;
    }

    private static DeviceDTO createBridgeDevice(boolean isSHCClassic) {
        DeviceDTO device = new DeviceDTO();
        device.setId("id");
        device.setConfig(new DeviceConfigDTO());
        device.setCapabilityMap(new HashMap<>());
        if (isSHCClassic) {
            device.setType(DEVICE_SHC);
        } else {
            device.setType(DEVICE_SHCA);
        }
        return device;
    }

    private boolean isChannelUpdated(String channelUID, State expectedState) {
        State state = updatedChannels.get(channelUID);
        return expectedState.equals(state);
    }

    private static EventDTO createDeviceEvent(Consumer<EventPropertiesDTO> eventPropertiesConsumer) {
        EventPropertiesDTO eventProperties = new EventPropertiesDTO();
        eventPropertiesConsumer.accept(eventProperties);

        EventDTO event = new EventDTO();
        event.setSource(LINK_TYPE_DEVICE + "deviceId");
        event.setProperties(eventProperties);
        return event;
    }

    private class LivisiBridgeHandlerAccessible extends LivisiBridgeHandler {

        private final LivisiClient livisiClientMock;
        private final FullDeviceManager fullDeviceManagerMock;
        private final ScheduledExecutorService schedulerMock;
        private int executionCount;
        private int directExecutionCount;

        private LivisiBridgeHandlerAccessible(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient)
                throws Exception {
            super(bridge, oAuthFactory, httpClient);

            DeviceDTO bridgeDevice = new DeviceDTO();
            bridgeDevice.setId("bridgeId");
            bridgeDevice.setType(DEVICE_SHC);
            bridgeDevice.setConfig(new DeviceConfigDTO());

            livisiClientMock = mock(LivisiClient.class);
            fullDeviceManagerMock = mock(FullDeviceManager.class);
            when(fullDeviceManagerMock.getFullDevices()).thenReturn(List.of(bridgeDevice));

            schedulerMock = mock(ScheduledExecutorService.class);

            doAnswer(invocationOnMock -> {
                if (executionCount < MAXIMUM_RETRY_EXECUTIONS) {
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
        FullDeviceManager createFullDeviceManager(LivisiClient client) {
            return fullDeviceManagerMock;
        }

        @Override
        LivisiClient createClient(OAuthClientService oAuthService) {
            return livisiClientMock;
        }

        @Override
        @Nullable
        LivisiWebSocket createAndStartWebSocket(DeviceDTO bridgeDevice) throws WebSocketConnectException {
            webSocketMock.start();
            return webSocketMock;
        }

        @Override
        ScheduledExecutorService getScheduler() {
            return schedulerMock;
        }

        @Override
        protected void updateState(String channelID, State state) {
            super.updateState(channelID, state);
            updatedChannels.put(channelID, state);
        }
    }
}
