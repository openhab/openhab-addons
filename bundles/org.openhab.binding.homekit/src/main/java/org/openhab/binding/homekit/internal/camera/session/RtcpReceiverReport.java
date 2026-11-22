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
package org.openhab.binding.homekit.internal.camera.session;

import java.net.SocketAddress;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tracks reception statistics for one incoming RTP stream and builds RTCP Receiver Reports (RR) for it, as defined in
 * RFC 3550 sections 6.4.2 and appendix A. The LSR and DLSR fields are always zero, which RFC 3550 allows when no Sender
 * Report is used.
 * <p>
 * All methods are synchronized because the RTP relay thread updates the statistics while the report thread reads them.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class RtcpReceiverReport {

    private static final int RTP_HEADER_LENGTH = 12;
    private static final int RTCP_RECEIVER_REPORT = 201;
    private static final int RR_HEADER_LENGTH = 8;
    private static final int REPORT_BLOCK_LENGTH = 24;
    private static final int MAX_DROPOUT = 3000;
    private static final int SEQUENCE_MODULUS = 1 << 16;
    private static final long MICROS_PER_SECOND = 1_000_000L;

    private final int clockRate;
    private final long senderSsrc;

    private @Nullable SocketAddress peer;

    private boolean sourceSeen;
    private int sourceSsrc;
    private int baseSeq;
    private int maxSeq;
    private long cycles;
    private long received;
    private long expectedPrior;
    private long receivedPrior;
    private long jitter;
    private long lastTransit;
    private boolean transitSeen;

    /**
     * Creates a receiver report for one RTP stream.
     *
     * @param senderSsrc the SSRC that the binding uses in its Receiver Reports
     * @param clockRate the RTP clock rate of the stream in Hz, used to express the interarrival jitter
     */
    public RtcpReceiverReport(long senderSsrc, int clockRate) {
        this.senderSsrc = senderSsrc;
        this.clockRate = clockRate;
    }

    /**
     * Forgets all statistics and the peer address, so that the stream can be restarted for a new camera session.
     */
    public synchronized void reset() {
        peer = null;
        sourceSeen = false;
        received = 0;
        expectedPrior = 0;
        receivedPrior = 0;
        jitter = 0;
        transitSeen = false;
    }

    /**
     * Records the address that the camera's packets came from, which is where the Receiver Reports are sent.
     *
     * @param address the source address of an authenticated packet
     */
    public synchronized void setPeer(SocketAddress address) {
        peer = address;
    }

    /**
     * @return the address to send Receiver Reports to, or null if no packet has been received yet
     */
    public synchronized @Nullable SocketAddress peer() {
        return peer;
    }

    /**
     * Updates the statistics from an authenticated RTP packet (RFC 3550 appendix A.1 and A.8).
     *
     * @param rtp the decrypted RTP packet
     * @param length the length of the RTP packet
     * @param arrivalMicros the arrival time of the packet in microseconds of a monotonic clock
     */
    public synchronized void onRtp(byte[] rtp, int length, long arrivalMicros) {
        if (length < RTP_HEADER_LENGTH) {
            return;
        }
        int seq = ((rtp[2] & 0xFF) << 8) | (rtp[3] & 0xFF);
        long timestamp = readUnsignedInt(rtp, 4);
        int ssrc = (int) readUnsignedInt(rtp, 8);

        if (!sourceSeen || ssrc != sourceSsrc) {
            startSource(ssrc, seq);
        }
        updateSequence(seq);
        received++;
        updateJitter(timestamp, arrivalMicros);
    }

    /**
     * Builds an RTCP Receiver Report packet (RFC 3550 section 6.4.2). The report contains a single report block for the
     * stream if any packet has been received, otherwise no report block. The fraction lost is calculated over the
     * interval since the previous report, so each call to this method should be made once per report interval.
     *
     * @return the RTCP Receiver Report packet
     */
    public synchronized byte[] buildReport() {
        boolean withBlock = sourceSeen;
        int length = withBlock ? RR_HEADER_LENGTH + REPORT_BLOCK_LENGTH : RR_HEADER_LENGTH;
        byte[] rr = new byte[length];
        rr[0] = (byte) (0x80 | (withBlock ? 1 : 0));
        rr[1] = (byte) RTCP_RECEIVER_REPORT;
        rr[2] = (byte) ((length / 4 - 1) >>> 8);
        rr[3] = (byte) (length / 4 - 1);
        writeInt(rr, 4, (int) senderSsrc);
        if (withBlock) {
            writeReportBlock(rr, RR_HEADER_LENGTH);
        }
        return rr;
    }

    private void writeReportBlock(byte[] rr, int offset) {
        long extendedMax = cycles + maxSeq;
        long expected = extendedMax - baseSeq + 1;
        long expectedInterval = expected - expectedPrior;
        long receivedInterval = received - receivedPrior;
        long lostInterval = expectedInterval - receivedInterval;
        int fraction = expectedInterval <= 0 || lostInterval <= 0 ? 0
                : (int) Math.min(255, (lostInterval << 8) / expectedInterval);
        expectedPrior = expected;
        receivedPrior = received;

        long cumulativeLost = Math.max(-0x800000L, Math.min(0x7FFFFFL, expected - received));

        writeInt(rr, offset, sourceSsrc);
        writeInt(rr, offset + 4, (fraction << 24) | (int) (cumulativeLost & 0xFFFFFF));
        writeInt(rr, offset + 8, (int) extendedMax);
        writeInt(rr, offset + 12, (int) jitter);
        // LSR and DLSR stay zero: no Sender Report is used (RFC 3550 section 6.4.1).
    }

    private void startSource(int ssrc, int seq) {
        sourceSeen = true;
        sourceSsrc = ssrc;
        baseSeq = seq;
        maxSeq = seq;
        cycles = 0;
        received = 0;
        expectedPrior = 0;
        receivedPrior = 0;
        jitter = 0;
        transitSeen = false;
    }

    /**
     * Updates the highest sequence number, counting sequence number wrap-arounds (RFC 3550 appendix A.1).
     */
    private void updateSequence(int seq) {
        int delta = (seq - maxSeq) & (SEQUENCE_MODULUS - 1);
        if (delta < MAX_DROPOUT) {
            if (seq < maxSeq) {
                cycles += SEQUENCE_MODULUS;
            }
            maxSeq = seq;
        }
    }

    /**
     * Updates the interarrival jitter estimate, in RTP timestamp units (RFC 3550 appendix A.8). The difference of
     * timestamps is taken modulo 2^32 so that timestamp wrap-arounds do not cause a jump.
     */
    private void updateJitter(long timestamp, long arrivalMicros) {
        long arrival = (arrivalMicros / MICROS_PER_SECOND) * clockRate
                + (arrivalMicros % MICROS_PER_SECOND) * clockRate / MICROS_PER_SECOND;
        long transit = arrival - timestamp;
        if (transitSeen) {
            long difference = Math.abs((int) (transit - lastTransit));
            jitter += (difference - jitter) / 16;
        }
        lastTransit = transit;
        transitSeen = true;
    }

    private static long readUnsignedInt(byte[] packet, int offset) {
        return ((packet[offset] & 0xFFL) << 24) | ((packet[offset + 1] & 0xFF) << 16)
                | ((packet[offset + 2] & 0xFF) << 8) | (packet[offset + 3] & 0xFF);
    }

    private static void writeInt(byte[] packet, int offset, int value) {
        packet[offset] = (byte) (value >>> 24);
        packet[offset + 1] = (byte) (value >>> 16);
        packet[offset + 2] = (byte) (value >>> 8);
        packet[offset + 3] = (byte) value;
    }
}
