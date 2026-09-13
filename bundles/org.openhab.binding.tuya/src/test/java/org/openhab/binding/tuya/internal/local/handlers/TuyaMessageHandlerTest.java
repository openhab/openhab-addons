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
package org.openhab.binding.tuya.internal.local.handlers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.DeviceStatusListener;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.binding.tuya.internal.local.dto.TcpStatusPayload;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * The {@link TuyaMessageHandlerTest} is a test class for the {@link TuyaMessageHandler} class
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class TuyaMessageHandlerTest {
    private static final String SUB_DEVICE_ID = "e0f6bf69791fc50c";
    private static final Map<Integer, Object> DPS = Map.of(1, Boolean.TRUE);

    private @Mock @NonNullByDefault({}) ChannelHandlerContext ctxMock;
    private @Mock @NonNullByDefault({}) Channel channelMock;
    private @Mock @NonNullByDefault({}) Attribute<String> deviceIdAttrMock;
    private @Mock @NonNullByDefault({}) Attribute<ProtocolVersion> protocolAttrMock;
    private @Mock @NonNullByDefault({}) Attribute<byte[]> sessionKeyAttrMock;
    private @Mock @NonNullByDefault({}) DeviceStatusListener deviceStatusListenerMock;

    @BeforeEach
    public void setUp() {
        when(ctxMock.channel()).thenReturn(channelMock);

        when(channelMock.hasAttr(DEVICE_ID_ATTR)).thenReturn(true);
        when(channelMock.attr(DEVICE_ID_ATTR)).thenReturn(deviceIdAttrMock);
        when(deviceIdAttrMock.get()).thenReturn("bf5d241ed32f3968eej27a");

        when(channelMock.hasAttr(PROTOCOL_ATTR)).thenReturn(true);
        when(channelMock.attr(PROTOCOL_ATTR)).thenReturn(protocolAttrMock);
        when(protocolAttrMock.get()).thenReturn(ProtocolVersion.V3_3);

        when(channelMock.hasAttr(SESSION_KEY_ATTR)).thenReturn(true);
        when(channelMock.attr(SESSION_KEY_ATTR)).thenReturn(sessionKeyAttrMock);
        when(sessionKeyAttrMock.get()).thenReturn("5c8c3ccc1f0fbdbb".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void deviceStatusIsReportedWithoutSubDeviceId() throws Exception {
        TcpStatusPayload payload = new TcpStatusPayload();
        payload.dps = DPS;

        new TuyaMessageHandler(deviceStatusListenerMock).channelRead(ctxMock,
                new MessageWrapper<>(CommandType.STATUS, payload));

        verify(deviceStatusListenerMock).processDeviceStatus(null, DPS);
    }

    @Test
    public void subDeviceStatusIsReportedWithSubDeviceId() throws Exception {
        TcpStatusPayload payload = new TcpStatusPayload();
        payload.cid = SUB_DEVICE_ID;
        payload.dps = DPS;

        new TuyaMessageHandler(deviceStatusListenerMock).channelRead(ctxMock,
                new MessageWrapper<>(CommandType.STATUS, payload));

        verify(deviceStatusListenerMock).processDeviceStatus(SUB_DEVICE_ID, DPS);
    }

    @Test
    public void subDeviceStatusIsReportedWithSubDeviceIdForNestedPayload() throws Exception {
        TcpStatusPayload payload = new TcpStatusPayload();
        payload.protocol = 4;
        payload.data.cid = SUB_DEVICE_ID;
        payload.data.dps = DPS;

        new TuyaMessageHandler(deviceStatusListenerMock).channelRead(ctxMock,
                new MessageWrapper<>(CommandType.STATUS, payload));

        verify(deviceStatusListenerMock).processDeviceStatus(SUB_DEVICE_ID, DPS);
    }
}
