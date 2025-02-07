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
package org.openhab.binding.tuya.internal.local.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.core.util.HexUtils;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * The {@link TuyaEncoderTest} is a test class for the {@link TuyaEncoder} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaEncoderTest {

    private final Gson gson = new Gson();
    private @Mock @NonNullByDefault({}) ChannelHandlerContext ctxMock;
    private @Mock @NonNullByDefault({}) Channel channelMock;
    private @Mock @NonNullByDefault({}) Attribute<String> deviceIdAttrMock;
    private @Mock @NonNullByDefault({}) Attribute<ProtocolVersion> protocolAttrMock;
    private @Mock @NonNullByDefault({}) Attribute<byte[]> sessionKeyAttrMock;
    private @Mock @NonNullByDefault({}) ByteBuf out;

    @Test
    public void testEncoding34() throws Exception {
        when(ctxMock.channel()).thenReturn(channelMock);

        when(channelMock.hasAttr(DEVICE_ID_ATTR)).thenReturn(true);
        when(channelMock.attr(DEVICE_ID_ATTR)).thenReturn(deviceIdAttrMock);
        when(deviceIdAttrMock.get()).thenReturn("");

        when(channelMock.hasAttr(PROTOCOL_ATTR)).thenReturn(true);
        when(channelMock.attr(PROTOCOL_ATTR)).thenReturn(protocolAttrMock);
        when(protocolAttrMock.get()).thenReturn(ProtocolVersion.V3_4);

        when(channelMock.hasAttr(SESSION_KEY_ATTR)).thenReturn(true);
        when(channelMock.attr(SESSION_KEY_ATTR)).thenReturn(sessionKeyAttrMock);
        when(sessionKeyAttrMock.get()).thenReturn("5c8c3ccc1f0fbdbb".getBytes(StandardCharsets.UTF_8));

        byte[] payload = HexUtils.hexToBytes("47f877066f5983df0681e1f08be9f1a1");
        byte[] expectedResult = HexUtils.hexToBytes(
                "000055aa000000010000000300000044af06484eb01c2272666a10953aaa23e89328e42ea1f29fd0eca40999ab964927c99646647abb2ab242062a7e911953195ae99b2ee79fa00a95da8cc67e0b42e20000aa55");

        MessageWrapper<?> msg = new MessageWrapper<>(CommandType.SESS_KEY_NEG_START, payload);

        TuyaEncoder encoder = new TuyaEncoder(gson);
        encoder.encode(ctxMock, msg, out);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(out).writeBytes((byte[]) captor.capture());
        byte[] result = (byte[]) captor.getValue();
        assertThat(result.length, is(expectedResult.length));
        assertThat(result, is(expectedResult));
    }
}
