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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SipBackchannelRtpRelay}.
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
                InetAddress.getLoopbackAddress(), 5004, 97);

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
                InetAddress.getLoopbackAddress(), 5004, 97);

        assertNull(SipBackchannelRtpRelay.buildForwardPacket(incoming, target));
    }

    @Test
    void buildForwardPacketRejectsShortPackets() {
        byte[] packet = new byte[10];
        packet[0] = (byte) 0x80;

        DatagramPacket incoming = new DatagramPacket(packet, packet.length);
        SipBackchannelRtpRelay.RelayTarget target = new SipBackchannelRtpRelay.RelayTarget(
                InetAddress.getLoopbackAddress(), 5004, 97);

        assertNull(SipBackchannelRtpRelay.buildForwardPacket(incoming, target));
    }
}
