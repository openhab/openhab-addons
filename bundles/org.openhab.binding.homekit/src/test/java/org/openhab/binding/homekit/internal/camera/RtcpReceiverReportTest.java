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

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.camera.session.RtcpReceiverReport;

/**
 * Tests for {@link RtcpReceiverReport}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class RtcpReceiverReportTest {

    private static final long SENDER_SSRC = 0x11223344L;
    private static final int SOURCE_SSRC = 0xCAFEBABE;
    private static final int VIDEO_CLOCK_RATE = 90000;

    @Test
    void headerOnlyReportBeforeAnyPacket() {
        byte[] rr = new RtcpReceiverReport(SENDER_SSRC, VIDEO_CLOCK_RATE).buildReport();
        assertEquals(8, rr.length);
        assertEquals((byte) 0x80, rr[0]);
        assertEquals(201, rr[1] & 0xFF);
        assertEquals(1, rr[3], "length in 32-bit words minus one");
        assertEquals((int) SENDER_SSRC, readInt(rr, 4));
    }

    @Test
    void reportBlockCountsLossOverInterval() {
        RtcpReceiverReport report = new RtcpReceiverReport(SENDER_SSRC, VIDEO_CLOCK_RATE);
        for (int seq = 100; seq <= 109; seq++) {
            if (seq != 105) {
                report.onRtp(rtp(seq, 0, SOURCE_SSRC), 12, 0);
            }
        }
        byte[] rr = report.buildReport();
        assertEquals(32, rr.length);
        assertEquals((byte) 0x81, rr[0]);
        assertEquals(SOURCE_SSRC, readInt(rr, 8));
        assertEquals(25, (rr[12] & 0xFF), "fraction lost is 1/10 in 1/256 units");
        assertEquals(1, readInt(rr, 12) & 0xFFFFFF, "cumulative lost");
        assertEquals(109, readInt(rr, 16), "extended highest sequence number");
    }

    @Test
    void sequenceWrapAroundExtendsHighestSequenceNumber() {
        RtcpReceiverReport report = new RtcpReceiverReport(SENDER_SSRC, VIDEO_CLOCK_RATE);
        for (int seq : new int[] { 65534, 65535, 0, 1 }) {
            report.onRtp(rtp(seq, 0, SOURCE_SSRC), 12, 0);
        }
        byte[] rr = report.buildReport();
        assertEquals(0x10001, readInt(rr, 16));
        assertEquals(0, readInt(rr, 12) & 0xFFFFFF, "no packets lost");
    }

    @Test
    void steadyArrivalGivesZeroJitter() {
        RtcpReceiverReport report = new RtcpReceiverReport(SENDER_SSRC, VIDEO_CLOCK_RATE);
        for (int i = 0; i < 20; i++) {
            long timestamp = i * 9000L;
            report.onRtp(rtp(i, timestamp, SOURCE_SSRC), 12, i * 100_000L);
        }
        assertEquals(0, readInt(report.buildReport(), 20));
    }

    @Test
    void lastSenderReportFieldsAreAlwaysZero() {
        RtcpReceiverReport report = new RtcpReceiverReport(SENDER_SSRC, VIDEO_CLOCK_RATE);
        report.onRtp(rtp(1, 0, SOURCE_SSRC), 12, 0);
        byte[] rr = report.buildReport();
        assertEquals(32, rr.length);
        assertEquals(0, readInt(rr, 24), "LSR");
        assertEquals(0, readInt(rr, 28), "DLSR");
    }

    @Test
    void resetForgetsPeerAndStatistics() {
        RtcpReceiverReport report = new RtcpReceiverReport(SENDER_SSRC, VIDEO_CLOCK_RATE);
        report.setPeer(new InetSocketAddress("192.0.2.1", 5000));
        report.onRtp(rtp(1, 0, SOURCE_SSRC), 12, 0);
        report.reset();
        assertNull(report.peer());
        assertEquals(8, report.buildReport().length);
    }

    private static byte[] rtp(int seq, long timestamp, int ssrc) {
        byte[] packet = new byte[12];
        packet[0] = (byte) 0x80;
        packet[1] = 96;
        packet[2] = (byte) (seq >>> 8);
        packet[3] = (byte) seq;
        writeInt(packet, 4, (int) timestamp);
        writeInt(packet, 8, ssrc);
        return packet;
    }

    private static void writeInt(byte[] packet, int offset, int value) {
        packet[offset] = (byte) (value >>> 24);
        packet[offset + 1] = (byte) (value >>> 16);
        packet[offset + 2] = (byte) (value >>> 8);
        packet[offset + 3] = (byte) value;
    }

    private static int readInt(byte[] packet, int offset) {
        return ((packet[offset] & 0xFF) << 24) | ((packet[offset + 1] & 0xFF) << 16)
                | ((packet[offset + 2] & 0xFF) << 8) | (packet[offset + 3] & 0xFF);
    }
}
