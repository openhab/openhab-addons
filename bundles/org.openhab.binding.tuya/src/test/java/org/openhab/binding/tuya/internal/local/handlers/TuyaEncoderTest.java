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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tuya.internal.local.TuyaDevice.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tuya.internal.local.CommandType;
import org.openhab.binding.tuya.internal.local.MessageWrapper;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.binding.tuya.internal.util.CryptoUtil;
import org.openhab.core.util.HexUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

/**
 * The {@link TuyaEncoderTest} is a test class for the {@link TuyaEncoder} class
 *
 * @author Jan N. Klug - Initial contribution
 * @author Maciej Jarzebowski - Add sub-device (cid) tests
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class TuyaEncoderTest {

    private static final String DEVICE_ID = "bf5d241ed32f3968eej27a";
    private static final String SUB_DEVICE_ID = "e0f6bf69791fc50c";
    private static final byte[] KEY = "5c8c3ccc1f0fbdbb".getBytes(StandardCharsets.UTF_8);

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

    @Test
    public void testDeviceIsAddressedByDeviceId33() throws Exception {
        JsonObject payload = encodeControl33(null);

        assertThat(payload.get("devId").getAsString(), is(DEVICE_ID));
        assertThat(payload.get("gwId").getAsString(), is(DEVICE_ID));
        assertThat(payload.get("uid").getAsString(), is(DEVICE_ID));
        assertThat(payload.get("cid"), is(nullValue()));
        assertThat(payload.get("dps"), is(not(nullValue())));
    }

    @Test
    public void testSubDeviceIsAddressedByNodeIdOnly33() throws Exception {
        JsonObject payload = encodeControl33(SUB_DEVICE_ID);

        assertThat(payload.get("cid").getAsString(), is(SUB_DEVICE_ID));
        // A gateway does not accept a sub-device message that also carries the identification of the gateway itself.
        assertThat(payload.get("devId"), is(nullValue()));
        assertThat(payload.get("gwId"), is(nullValue()));
        assertThat(payload.get("uid"), is(nullValue()));
        assertThat(payload.get("dps"), is(not(nullValue())));
    }

    @Test
    public void testSubDeviceIsAddressedByNodeIdInData34() throws Exception {
        JsonObject payload = encodeControl34(SUB_DEVICE_ID);

        assertThat(payload.getAsJsonObject("data").get("cid").getAsString(), is(SUB_DEVICE_ID));
    }

    @Test
    public void testDeviceIsAddressedByDeviceIdInData34() throws Exception {
        JsonObject payload = encodeControl34(null);

        assertThat(payload.getAsJsonObject("data").get("cid").getAsString(), is(DEVICE_ID));
    }

    private JsonObject encodeControl33(@Nullable String cid) throws Exception {
        mockChannel(ProtocolVersion.V3_3);

        TuyaEncoder encoder = new TuyaEncoder(gson);
        encoder.encode(ctxMock, new MessageWrapper<>(CommandType.CONTROL, Map.of("dps", Map.of(1, true)), cid), out);

        // prefix, sequence, command and length take 16 bytes, the trailing checksum and suffix another 8
        byte[] frame = captureFrame();
        byte[] encrypted = Arrays.copyOfRange(frame, 16, frame.length - 8);

        // CONTROL messages carry a 15 byte version header in front of the encrypted payload
        return decrypt(Arrays.copyOfRange(encrypted, 15, encrypted.length));
    }

    private JsonObject encodeControl34(@Nullable String cid) throws Exception {
        mockChannel(ProtocolVersion.V3_4);

        TuyaEncoder encoder = new TuyaEncoder(gson);
        encoder.encode(ctxMock, new MessageWrapper<>(CommandType.CONTROL_NEW, Map.of("dps", Map.of(1, true)), cid),
                out);

        // prefix, sequence, command and length take 16 bytes, the trailing HMAC and suffix another 36
        byte[] frame = captureFrame();
        byte[] decrypted = Objects
                .requireNonNull(CryptoUtil.decryptAesEcb(Arrays.copyOfRange(frame, 16, frame.length - 36), KEY, true));

        // the decrypted payload carries a 15 byte version header in front of the JSON
        return Objects.requireNonNull(
                gson.fromJson(new String(Arrays.copyOfRange(decrypted, 15, decrypted.length), StandardCharsets.UTF_8),
                        JsonObject.class));
    }

    private void mockChannel(ProtocolVersion protocol) {
        when(ctxMock.channel()).thenReturn(channelMock);

        when(channelMock.hasAttr(DEVICE_ID_ATTR)).thenReturn(true);
        when(channelMock.attr(DEVICE_ID_ATTR)).thenReturn(deviceIdAttrMock);
        when(deviceIdAttrMock.get()).thenReturn(DEVICE_ID);

        when(channelMock.hasAttr(PROTOCOL_ATTR)).thenReturn(true);
        when(channelMock.attr(PROTOCOL_ATTR)).thenReturn(protocolAttrMock);
        when(protocolAttrMock.get()).thenReturn(protocol);

        when(channelMock.hasAttr(SESSION_KEY_ATTR)).thenReturn(true);
        when(channelMock.attr(SESSION_KEY_ATTR)).thenReturn(sessionKeyAttrMock);
        when(sessionKeyAttrMock.get()).thenReturn(KEY);
    }

    private byte[] captureFrame() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(out).writeBytes((byte[]) captor.capture());
        return (byte[]) captor.getValue();
    }

    private JsonObject decrypt(byte[] encrypted) {
        byte[] decrypted = Objects.requireNonNull(CryptoUtil.decryptAesEcb(encrypted, KEY, true));
        return Objects.requireNonNull(gson.fromJson(new String(decrypted, StandardCharsets.UTF_8), JsonObject.class));
    }
}
