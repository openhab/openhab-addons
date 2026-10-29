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
package org.openhab.binding.dahuadoor.internal.media;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SipBackchannelRtpRelay}.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
class SipBackchannelRtpRelayTest {

    @Test
    void buildForwardPacketRewritesPayloadType() {
        byte[] packet = new byte[16];
        packet[0] = (byte) 0x80; // RTP v2
        packet[1] = (byte) 0x80; // marker set, PT=0
        packet[12] = 0x01;
        packet[13] = 0x02;
        packet[14] = 0x03;
        packet[15] = 0x04;

        DatagramPacket incoming = new DatagramPacket(packet, packet.length);
        SipBackchannelRtpRelay.RelayTarget target = new SipBackchannelRtpRelay.RelayTarget(
                InetAddress.getLoopbackAddress(), 5004, 97, "PCM", 16000);

        byte[] forwarded = SipBackchannelRtpRelay.buildForwardPacket(incoming, target);
        assertNotNull(forwarded);
        assertEquals(16, forwarded.length);
        assertEquals(0x80, forwarded[1] & 0x80);
        assertEquals(97, forwarded[1] & 0x7F);
    }

    @Test
    void buildForwardPacketRejectsInvalidVersion() {
        byte[] packet = new byte[12];
        packet[0] = 0x00; // not RTP v2

        DatagramPacket incoming = new DatagramPacket(packet, packet.length);
        SipBackchannelRtpRelay.RelayTarget target = new SipBackchannelRtpRelay.RelayTarget(
                InetAddress.getLoopbackAddress(), 5004, 97, "PCM", 16000);

        assertNull(SipBackchannelRtpRelay.buildForwardPacket(incoming, target));
    }

    @Test
    void buildForwardPacketRejectsShortPackets() {
        byte[] packet = new byte[10];
        packet[0] = (byte) 0x80;

        DatagramPacket incoming = new DatagramPacket(packet, packet.length);
        SipBackchannelRtpRelay.RelayTarget target = new SipBackchannelRtpRelay.RelayTarget(
                InetAddress.getLoopbackAddress(), 5004, 97, "PCM", 16000);

        assertNull(SipBackchannelRtpRelay.buildForwardPacket(incoming, target));
    }

    @Test
    void buildForwardPacketTranscodesPcm16LeToPcma() {
        byte[] packet = new byte[20];
        packet[0] = (byte) 0x80;
        packet[1] = (byte) 0x80;
        packet[6] = 0x01;
        packet[7] = 0x40; // RTP timestamp 320

        // Four 16-bit little-endian PCM samples, all zero.
        packet[12] = 0x00;
        packet[13] = 0x00;
        packet[14] = 0x00;
        packet[15] = 0x00;
        packet[16] = 0x00;
        packet[17] = 0x00;
        packet[18] = 0x00;
        packet[19] = 0x00;

        DatagramPacket incoming = new DatagramPacket(packet, packet.length);
        SipBackchannelRtpRelay.RelayTarget target = new SipBackchannelRtpRelay.RelayTarget(
                InetAddress.getLoopbackAddress(), 5004, 8, "PCMA", 8000);

        byte[] forwarded = SipBackchannelRtpRelay.buildForwardPacket(incoming, target);
        assertNotNull(forwarded);
        assertEquals(14, forwarded.length);
        assertEquals(8, forwarded[1] & 0x7F);
        assertEquals(0x00, forwarded[4] & 0xFF);
        assertEquals(0x00, forwarded[5] & 0xFF);
        assertEquals(0x00, forwarded[6] & 0xFF);
        assertEquals(0xA0, forwarded[7] & 0xFF);
        assertEquals(0xD5, forwarded[12] & 0xFF);
        assertEquals(0xD5, forwarded[13] & 0xFF);
    }

    @Test
    void buildForwardPacketTranscodesPcm16LeToPcmu() {
        byte[] packet = new byte[20];
        packet[0] = (byte) 0x80;
        packet[1] = 0x00;
        packet[6] = 0x01;
        packet[7] = 0x40; // RTP timestamp 320

        packet[12] = 0x00;
        packet[13] = 0x00;
        packet[14] = 0x00;
        packet[15] = 0x00;
        packet[16] = 0x00;
        packet[17] = 0x00;
        packet[18] = 0x00;
        packet[19] = 0x00;

        DatagramPacket incoming = new DatagramPacket(packet, packet.length);
        SipBackchannelRtpRelay.RelayTarget target = new SipBackchannelRtpRelay.RelayTarget(
                InetAddress.getLoopbackAddress(), 5004, 0, "PCMU", 8000);

        byte[] forwarded = SipBackchannelRtpRelay.buildForwardPacket(incoming, target);
        assertNotNull(forwarded);
        assertEquals(14, forwarded.length);
        assertEquals(0, forwarded[1] & 0x7F);
        assertEquals(0x00, forwarded[4] & 0xFF);
        assertEquals(0x00, forwarded[5] & 0xFF);
        assertEquals(0x00, forwarded[6] & 0xFF);
        assertEquals(0xA0, forwarded[7] & 0xFF);
        assertEquals(0xFF, forwarded[12] & 0xFF);
        assertEquals(0xFF, forwarded[13] & 0xFF);
    }

    private static SipBackchannelRtpRelay.RelayTarget target(String codec, int clockRate) {
        return new SipBackchannelRtpRelay.RelayTarget(InetAddress.getLoopbackAddress(), 5004, 97, codec, clockRate);
    }

    private static byte @Nullable [] extract(byte[] packet, SipBackchannelRtpRelay.RelayTarget target) {
        return SipBackchannelRtpRelay.extractIngestPayload(new DatagramPacket(packet, packet.length), target);
    }

    /** Builds an RTP v2 packet with a 12 byte header, the given payload and optional trailing bytes. */
    private static byte[] rtpPacket(int firstByte, byte[] payload, byte... trailer) {
        byte[] packet = new byte[12 + payload.length + trailer.length];
        packet[0] = (byte) firstByte;
        System.arraycopy(payload, 0, packet, 12, payload.length);
        System.arraycopy(trailer, 0, packet, 12 + payload.length, trailer.length);
        return packet;
    }

    @Test
    void extractIngestPayloadStripsRtpHeader() {
        byte[] payload = extract(rtpPacket(0x80, new byte[] { 1, 2, 3, 4 }), target("PCM", 16000));
        assertNotNull(payload);
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, payload);
    }

    @Test
    void extractIngestPayloadRejectsInvalidPackets() {
        SipBackchannelRtpRelay.RelayTarget pcm = target("PCM", 16000);
        assertNull(extract(new byte[16], pcm));
        assertNull(extract(rtpPacket(0x80, new byte[0]), pcm));
        assertNull(extract(rtpPacket(0x80, new byte[] { 1, 2, 3 }), pcm));
    }

    @Test
    void extractIngestPayloadRemovesRtpPadding() {
        SipBackchannelRtpRelay.RelayTarget pcm = target("PCM", 16000);
        byte[] samples = { 1, 2, 3, 4 };

        assertArrayEquals(samples, extract(rtpPacket(0xA0, samples, (byte) 0, (byte) 0, (byte) 0, (byte) 4), pcm));
        // An odd padding count must not discard valid samples
        assertArrayEquals(samples, extract(rtpPacket(0xA0, samples, (byte) 0, (byte) 0, (byte) 3), pcm));
    }

    @Test
    void extractIngestPayloadRejectsInvalidPadding() {
        SipBackchannelRtpRelay.RelayTarget pcm = target("PCM", 16000);
        byte[] samples = { 1, 2, 3, 4 };

        assertNull(extract(rtpPacket(0xA0, samples, (byte) 0), pcm));
        assertNull(extract(rtpPacket(0xA0, samples, (byte) 0, (byte) 0, (byte) 0, (byte) 5), pcm));
        assertNull(extract(rtpPacket(0xA0, new byte[0], (byte) 0, (byte) 0, (byte) 2), pcm));
    }

    @Test
    void extractIngestPayloadDecodesPcmaToPcm16k() {
        // A-law 0xAA is the positive maximum (32256), 0xD5 is the smallest positive value (8)
        byte[] payload = extract(rtpPacket(0x80, new byte[] { (byte) 0xAA, (byte) 0xD5 }), target("PCMA", 8000));
        assertNotNull(payload);
        // 32256, interpolated 16132, 8, 8 as 16 bit little endian
        assertArrayEquals(new byte[] { 0x00, 0x7E, 0x04, 0x3F, 0x08, 0x00, 0x08, 0x00 }, payload);
    }

    @Test
    void extractIngestPayloadDecodesPcmuToPcm16k() {
        // mu-law 0x80 is the positive maximum (32124), 0xFF is zero
        byte[] payload = extract(rtpPacket(0x80, new byte[] { (byte) 0x80, (byte) 0xFF }), target("PCMU", 8000));
        assertNotNull(payload);
        // 32124, interpolated 16062, 0, 0 as 16 bit little endian
        assertArrayEquals(new byte[] { 0x7C, 0x7D, (byte) 0xBE, 0x3E, 0x00, 0x00, 0x00, 0x00 }, payload);
    }

    @Test
    void extractIngestPayloadRemovesPaddingFromG711() {
        byte[] payload = extract(rtpPacket(0xA0, new byte[] { (byte) 0xFF }, (byte) 0, (byte) 2), target("PCMU", 8000));
        assertNotNull(payload);
        assertEquals(4, payload.length);
    }

    @Test
    void extractIngestPayloadRejectsUnsupportedCodecs() {
        byte[] packet = rtpPacket(0x80, new byte[] { 1, 2, 3, 4 });
        assertNull(extract(packet, target("PCMA", 16000)));
        assertNull(extract(packet, target("PCM", 8000)));
        assertNull(extract(packet, target("opus", 48000)));
    }
}
