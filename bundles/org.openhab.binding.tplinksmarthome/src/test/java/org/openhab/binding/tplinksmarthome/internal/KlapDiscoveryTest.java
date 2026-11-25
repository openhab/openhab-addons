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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link KlapDiscovery}. The replies are shaped like those of an EP10 on firmware 1.1.2 and of a Kasa device
 * on the SMART protocol, with made-up ids.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class KlapDiscoveryTest {

    static final String DEVICE_ID = "8006000000000000000000000000000000000001";
    static final String HASHED_DEVICE_ID = "A637D531B234A0D710C24AE500ACE081";

    static String reply(String deviceType, String encryptType) {
        return """
                {"result":{"device_id":"%s","owner":"00000000000000000000000000000000",
                "device_type":"%s","device_model":"EP10(US)","hw_ver":"1.0","ip":"192.168.1.20",
                "mac":"00-11-22-33-44-55","obd_src":"tplink","protocol_version":1,"factory_default":false,
                "mgt_encrypt_schm":{"is_support_https":false,"encrypt_type":"%s","new_klap":1,"ANS":true,
                "http_port":80,"lv":2}},"error_code":0}
                """.formatted(HASHED_DEVICE_ID.toLowerCase(), deviceType, encryptType);
    }

    static byte[] replyPacket(String json) {
        return KlapDiscovery.packet(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testRequestHeaderHasLengthAndChecksum() throws Exception {
        byte[] request = KlapDiscovery.request();
        ByteBuffer header = ByteBuffer.wrap(request);

        assertArrayEquals(new byte[] { 0x02, 0x00, 0x00, 0x01 }, Arrays.copyOf(request, 4));
        assertEquals(request.length - 16, header.getShort(4));
        assertEquals((short) 0x1100, header.getShort(6));
        String payload = new String(request, 16, request.length - 16, StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"rsa_key\":\"-----BEGIN PUBLIC KEY-----"), payload);

        byte[] seeded = request.clone();
        ByteBuffer.wrap(seeded).putInt(12, 0x5A6B7C8D);
        CRC32 crc = new CRC32();
        crc.update(seeded);
        assertEquals((int) crc.getValue(), header.getInt(12));
    }

    @Test
    public void testParsesKasaDeviceUsingKlap() {
        byte[] packet = replyPacket(reply("IOT.SMARTPLUGSWITCH", "KLAP"));

        assertEquals(Optional.of(new KlapDiscovery.Device("EP10(US)", HASHED_DEVICE_ID, "00-11-22-33-44-55", "1.0")),
                KlapDiscovery.parse(packet, packet.length));
    }

    @Test
    public void testIgnoresDeviceOnSmartProtocol() {
        byte[] packet = replyPacket(reply("SMART.KASAPLUG", "KLAP"));

        assertEquals(Optional.empty(), KlapDiscovery.parse(packet, packet.length));
    }

    @Test
    public void testIgnoresDeviceWithoutKlap() {
        byte[] packet = replyPacket(reply("IOT.SMARTPLUGSWITCH", "XOR"));

        assertEquals(Optional.empty(), KlapDiscovery.parse(packet, packet.length));
    }

    @Test
    public void testIgnoresMalformedReplies() {
        byte[] notJson = replyPacket("not json");
        byte[] noResult = replyPacket("{\"error_code\":-1}");

        assertEquals(Optional.empty(), KlapDiscovery.parse(notJson, notJson.length));
        assertEquals(Optional.empty(), KlapDiscovery.parse(noResult, noResult.length));
        assertEquals(Optional.empty(), KlapDiscovery.parse(new byte[8], 8));
    }

    @Test
    public void testHashesDeviceIdAsTheDiscoveryReplyDoes() {
        assertEquals(HASHED_DEVICE_ID, KlapDiscovery.hashDeviceId(DEVICE_ID));
    }
}
