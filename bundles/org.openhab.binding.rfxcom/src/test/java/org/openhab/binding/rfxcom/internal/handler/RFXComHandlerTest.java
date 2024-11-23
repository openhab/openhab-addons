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
package org.openhab.binding.rfxcom.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComRawDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessageFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;

/**
 * The {@link RFXComRawHandler} is responsible for extra validation for Raw things.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class RFXComHandlerTest {

    @Mock
    Bridge bridge;

    @Mock
    RFXComBridgeHandler bridgeHandler;

    @Mock
    Thing thing;

    @Mock
    ThingHandlerCallback callback;

    @Mock
    RFXComMessageFactory messageFactory;

    @Mock
    RFXComMessage message;

    @Captor
    ArgumentCaptor<ThingStatusInfo> thingStatusInfoCaptor;

    @Captor
    ArgumentCaptor<RFXComGenericDeviceConfiguration> deviceConfigurationCaptor;

    @Captor
    ArgumentCaptor<RFXComDeviceMessage> deviceMessageCaptor;

    RFXComHandler handler;

    private void initBridge() {
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(thing.getBridgeUID()).thenReturn(bridgeUID);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
    }

    private void initOfflineBridge() {
        initBridge();

        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
    }

    private void initOnlineBridge() {
        initBridge();

        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
    }

    private void verifyStatusUpdated(ThingStatus status, ThingStatusDetail thingStatusDetail) {
        verify(callback).statusUpdated(eq(thing), thingStatusInfoCaptor.capture());
        ThingStatusInfo tsi = thingStatusInfoCaptor.getValue();
        assertEquals(status, tsi.getStatus());
        assertEquals(thingStatusDetail, tsi.getStatusDetail());
    }

    private RFXComGenericDeviceConfiguration sendMessageToGetConfig(String channel, Command command)
            throws RFXComException {
        ChannelUID cuid = new ChannelUID(thing.getUID(), channel);
        handler.handleCommand(cuid, command);
        verify(messageFactory).createMessage(any(PacketType.class), deviceConfigurationCaptor.capture(), eq(cuid),
                eq(command));
        return deviceConfigurationCaptor.getValue();
    }

    @BeforeEach
    public void before() {
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(thingTypeUID);

        handler = new RFXComHandler(thing, messageFactory);
        handler.setCallback(callback);
    }

    @Test
    public void testValidConfig() {
        initOnlineBridge();
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", "1088338.11", "subType", "AC")));

        handler.initialize();
        verifyStatusUpdated(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testInvalidConfig() {
        initOnlineBridge();
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of()));

        handler.initialize();
        verifyStatusUpdated(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testOfflineBridge() {
        initOfflineBridge();
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", "1088338.11", "subType", "AC")));

        handler.initialize();
        verifyStatusUpdated(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Test
    public void testUnititialisedBridge() {
        initBridge();
        when(thing.getConfiguration())
                .thenReturn(new Configuration(Map.of("deviceId", "RAW", "subType", "RAW_PACKET1")));

        handler.initialize();
        verifyStatusUpdated(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
    }

    @Test
    public void testWithoutBridge() {
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("deviceId", "1088338.11", "subType", "AC")));

        handler.initialize();
        verifyStatusUpdated(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
    }

    @Test
    public void testConfigType() throws RFXComException {
        initOnlineBridge();
        when(thing.getConfiguration()).thenReturn(
                new Configuration(Map.of("deviceId", "RAW", "subType", "RAW_PACKET1", "onPulses", "1 2 3 4")));

        handler.initialize();
        verifyStatusUpdated(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        RFXComDeviceConfiguration config = sendMessageToGetConfig("command", OnOffType.ON);
        assertEquals(RFXComRawDeviceConfiguration.class, config.getClass());
    }
}
