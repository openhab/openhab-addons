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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.core.util.HexUtils;

import com.google.gson.Gson;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * The {@link TuyaDecoderTest} is a test class for the {@link TuyaDecoder} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaDecoderTest {

    private final Gson gson = new Gson();
    private @Mock @NonNullByDefault({}) ChannelHandlerContext ctxMock;
    private @Mock @NonNullByDefault({}) Channel channelMock;
    private @Mock @NonNullByDefault({}) Attribute<String> deviceIdAttrMock;
    private @Mock @NonNullByDefault({}) Attribute<ProtocolVersion> protocolAttrMock;
    private @Mock @NonNullByDefault({}) Attribute<byte[]> sessionKeyAttrMock;

    @Test
    public void decode34Test() throws Exception {
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

        byte[] packet = HexUtils.hexToBytes(
                "000055aa0000fc6c0000000400000068000000004b578f442ec0802f26ca6794389ce4ebf57f94561e9367569b0ff90afebe08765460b35678102c0a96b666a6f6a3aabf9328e42ea1f29fd0eca40999ab964927c340dba68f847cb840b473c19572f8de9e222de2d5b1793dc7d4888a8b4f11b00000aa55");
        byte[] expectedResult = HexUtils.hexToBytes(
                "3965333963353564643232333163336605ca4f27a567a763d0df1ed6c34fa5bb334a604d900cc86b8085eef6acd0193d");

        List<Object> out = new ArrayList<>();

        TuyaDecoder decoder = new TuyaDecoder(gson);
        decoder.decode(ctxMock, Unpooled.copiedBuffer(packet), out);

        assertThat(out, hasSize(1));
        MessageWrapper<?> result = (MessageWrapper<?>) out.get(0);
        assertThat(result.content, is(expectedResult));
    }
}
