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
package org.openhab.binding.homekit.internal.camera;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.camera.session.SrtpCryptoSuite;
import org.openhab.binding.homekit.internal.camera.session.SrtpParameters;
import org.openhab.binding.homekit.internal.camera.tlv.AudioSelectedParameters;
import org.openhab.binding.homekit.internal.camera.tlv.SetupEndpointsRequest;
import org.openhab.binding.homekit.internal.camera.tlv.SetupEndpointsResponse;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;
import org.openhab.binding.homekit.internal.enums.SetupEndpointsStatus;

/**
 * Tests for the HomeKit camera talk-back TLV records.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class CameraTlvTest {

    private static final byte[] SESSION_ID = new byte[16];
    private static final byte[] KEY_16 = new byte[16];
    private static final byte[] SALT_14 = new byte[14];

    static {
        Arrays.fill(SESSION_ID, (byte) 0x11);
        Arrays.fill(KEY_16, (byte) 0x22);
        Arrays.fill(SALT_14, (byte) 0x33);
    }

    @Test
    void srtpRoundTrip() {
        SrtpParameters original = new SrtpParameters(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, KEY_16, SALT_14);
        SrtpParameters decoded = SrtpParameters.decode(original.encode());
        assertEquals(original.cryptoSuite(), decoded.cryptoSuite());
        assertArrayEquals(KEY_16, decoded.masterKey());
        assertArrayEquals(SALT_14, decoded.masterSalt());
    }

    @Test
    void srtpNoneSuiteRequiresEmptyKeyAndSalt() {
        SrtpParameters none = new SrtpParameters(SrtpCryptoSuite.NONE, new byte[0], new byte[0]);
        assertEquals(SrtpCryptoSuite.NONE, SrtpParameters.decode(none.encode()).cryptoSuite());
        assertThrows(IllegalArgumentException.class,
                () -> new SrtpParameters(SrtpCryptoSuite.NONE, KEY_16, new byte[0]));
    }

    @Test
    void srtpRejectsWrongKeyLength() {
        assertThrows(IllegalArgumentException.class,
                () -> new SrtpParameters(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, new byte[15], SALT_14));
        assertThrows(IllegalArgumentException.class,
                () -> new SrtpParameters(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, KEY_16, new byte[13]));
    }

    @Test
    void setupEndpointsRequestEncodesAddressAndPorts() {
        SrtpParameters video = new SrtpParameters(SrtpCryptoSuite.AES_CM_256_HMAC_SHA1_80, new byte[32], SALT_14);
        SrtpParameters audio = new SrtpParameters(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, KEY_16, SALT_14);
        SetupEndpointsRequest request = new SetupEndpointsRequest(SESSION_ID, true, "fd00::1", 5004, 5006, video,
                audio);

        Map<Integer, byte[]> tlv = Tlv8Codec.decode(request.encode());
        assertArrayEquals(SESSION_ID, tlv.get(0x01));

        byte[] bytes = tlv.get(0x03);
        assertNotNull(bytes);
        Map<Integer, byte[]> address = Tlv8Codec.decode(bytes);
        assertArrayEquals(new byte[] { 1 }, address.get(0x01));
        assertArrayEquals("fd00::1".getBytes(StandardCharsets.UTF_8), address.get(0x02));
        assertArrayEquals(new byte[] { (byte) 0x8C, 0x13 }, address.get(0x03));
        assertArrayEquals(new byte[] { (byte) 0x8E, 0x13 }, address.get(0x04));

        bytes = tlv.get(0x04);
        assertNotNull(bytes);
        byte[] videoCryptoSuite = Tlv8Codec.decode(bytes).get(0x01);
        assertNotNull(videoCryptoSuite);
        assertEquals(SrtpCryptoSuite.AES_CM_256_HMAC_SHA1_80.value, videoCryptoSuite[0]);
        bytes = tlv.get(0x05);
        assertNotNull(bytes);
        byte[] audioCryptoSuite = Tlv8Codec.decode(bytes).get(0x01);
        assertNotNull(audioCryptoSuite);
        assertEquals(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80.value, audioCryptoSuite[0]);
    }

    @Test
    void setupEndpointsRequestRejectsBadSessionIdAndPort() {
        SrtpParameters srtp = new SrtpParameters(SrtpCryptoSuite.NONE, new byte[0], new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> new SetupEndpointsRequest(new byte[15], false, "10.0.0.1", 1, 2, srtp, srtp));
        assertThrows(IllegalArgumentException.class,
                () -> new SetupEndpointsRequest(SESSION_ID, false, "10.0.0.1", 70000, 2, srtp, srtp));
    }

    @Test
    void setupEndpointsResponseSuccessCarriesAudioSsrcAndSrtp() {
        SrtpParameters srtp = new SrtpParameters(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, new byte[16], new byte[14]);
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(0x01, SESSION_ID);
        tlv.put(0x02, new byte[] { 0 });
        tlv.put(0x05, srtp.encode());
        tlv.put(0x07, new byte[] { 0x78, 0x56, 0x34, 0x12 });
        SetupEndpointsResponse response = SetupEndpointsResponse.decode(Tlv8Codec.encode(tlv));
        assertEquals(SetupEndpointsStatus.SUCCESS, response.status());
        assertEquals(0x12345678L, response.audioSyncSource());
        SrtpParameters audioSrtp = response.audioSrtpParameters();
        assertNotNull(audioSrtp);
        assertEquals(SrtpCryptoSuite.AES_CM_128_HMAC_SHA1_80, audioSrtp.cryptoSuite());
        assertArrayEquals(srtp.masterKey(), audioSrtp.masterKey());
        assertArrayEquals(srtp.masterSalt(), audioSrtp.masterSalt());
    }

    @Test
    void setupEndpointsResponseSuccessWithoutSrtpIsRejected() {
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(0x01, SESSION_ID);
        tlv.put(0x02, new byte[] { 0 });
        tlv.put(0x07, new byte[] { 0x78, 0x56, 0x34, 0x12 });
        byte[] encoded = Tlv8Codec.encode(tlv);
        assertThrows(IllegalArgumentException.class, () -> SetupEndpointsResponse.decode(encoded));
    }

    @Test
    void setupEndpointsResponseSuccessWithoutSsrcIsRejected() {
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(0x01, SESSION_ID);
        tlv.put(0x02, new byte[] { 0 });
        byte[] encoded = Tlv8Codec.encode(tlv);
        assertThrows(IllegalArgumentException.class, () -> SetupEndpointsResponse.decode(encoded));
    }

    @Test
    void setupEndpointsResponseCarriesVideoSsrcSrtpAndPort() {
        SrtpParameters srtp = new SrtpParameters(SrtpCryptoSuite.AES_CM_256_HMAC_SHA1_80, new byte[32], new byte[14]);
        Map<Integer, byte[]> address = new LinkedHashMap<>();
        address.put(0x03, new byte[] { (byte) 0x39, (byte) 0x30 });
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(0x01, SESSION_ID);
        tlv.put(0x02, new byte[] { 0 });
        tlv.put(0x03, Tlv8Codec.encode(address));
        tlv.put(0x04, srtp.encode());
        tlv.put(0x05, srtp.encode());
        tlv.put(0x06, new byte[] { 0x21, 0x43, 0x65, (byte) 0x87 });
        tlv.put(0x07, new byte[] { 0x78, 0x56, 0x34, 0x12 });
        SetupEndpointsResponse response = SetupEndpointsResponse.decode(Tlv8Codec.encode(tlv));
        assertEquals(0x87654321L, response.videoSynchSource());
        assertEquals(0x3039, response.videoPort());
        SrtpParameters videoSrtp = response.videoSrtpParameters();
        assertNotNull(videoSrtp);
        assertEquals(SrtpCryptoSuite.AES_CM_256_HMAC_SHA1_80, videoSrtp.cryptoSuite());
    }

    @Test
    void setupEndpointsResponseBusyHasNoSsrc() {
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(0x01, SESSION_ID);
        tlv.put(0x02, new byte[] { 1 });
        SetupEndpointsResponse response = SetupEndpointsResponse.decode(Tlv8Codec.encode(tlv));
        assertEquals(SetupEndpointsStatus.BUSY, response.status());
        assertNull(response.audioSyncSource());
    }

    @Test
    void selectedAudioEncodesCodecAndRtpParameters() {
        AudioSelectedParameters audio = new AudioSelectedParameters(AudioCodecType.OPUS, 1, true, 16, 20, 110,
                0xAABBCCDDL, 24, 0.5f, false, 13);
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(audio.encode());
        byte[] codec = tlv.get(0x01);
        assertNotNull(codec);
        assertEquals(AudioCodecType.OPUS.value, codec[0]);
        byte[] comfortNoise = tlv.get(0x04);
        assertNotNull(comfortNoise);
        assertEquals(0, comfortNoise[0]);

        byte[] bytes = tlv.get(0x03);
        assertNotNull(bytes);
        Map<Integer, byte[]> rtp = Tlv8Codec.decode(bytes);
        byte[] packetTime = rtp.get(0x01);
        assertNotNull(packetTime);
        assertEquals(110, packetTime[0]);
        assertArrayEquals(new byte[] { (byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA }, rtp.get(0x02));
        assertArrayEquals(new byte[] { 24, 0 }, rtp.get(0x03));
        assertArrayEquals(new byte[] { 0, 0, 0, 0x3F }, rtp.get(0x04));
    }

    @Test
    void selectedAudioRejectsUnsupportedValues() {
        assertThrows(IllegalArgumentException.class, () -> new AudioSelectedParameters(AudioCodecType.AAC_ELD, 1, true,
                44, 20, 110, 1, 24, 0.5f, false, 13));
        assertThrows(IllegalArgumentException.class, () -> new AudioSelectedParameters(AudioCodecType.AAC_ELD, 0, true,
                16, 20, 110, 1, 24, 0.5f, false, 13));
        assertThrows(IllegalArgumentException.class, () -> new AudioSelectedParameters(AudioCodecType.AAC_ELD, 1, true,
                16, 20, 200, 1, 24, 0.5f, false, 13));
    }
}
