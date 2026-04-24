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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spike: sends a continuous 2 kHz sine tone encoded according to the negotiated RTP
 * audio codec to the VTO's SIP audio port (Port E) while a call is active.
 *
 * Purpose: verify that the SIP/RTP path (Port E = 20000) actually reaches the VTO speaker.
 * If the tone is audible at the VTO → the path works → proceed to Phase 2 (relay real audio).
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class PitchToneRtpSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitchToneRtpSender.class);

    private static final String CODEC_PCM = "PCM";
    private static final String CODEC_PCMU = "PCMU";
    private static final double TONE_FREQ_HZ = 2000.0;

    // RTP header size: 12 bytes (no CSRC, no extension)
    private static final int RTP_HEADER_SIZE = 12;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private @Nullable Thread senderThread;

    /**
     * Start sending tone to the given SIP audio target.
     */
    public synchronized void start(SipAudioOffer offer, int localPort) {
        stop();
        running.set(true);
        Thread t = new Thread(() -> sendLoop(offer, localPort), "dahuadoor-pitch-tone-rtp");
        t.setDaemon(true);
        senderThread = t;
        t.start();
        LOGGER.info("PitchToneRtpSender started → {}:{} PT={} ({}) localPort={}", offer.getRemoteHost(),
                offer.getRemotePort(), offer.getPayloadType(), offer.getCodecName(), localPort);
    }

    /**
     * Stop sending tone and clean up.
     */
    public synchronized void stop() {
        running.set(false);
        Thread t = senderThread;
        if (t != null) {
            t.interrupt();
            senderThread = null;
        }
        LOGGER.debug("PitchToneRtpSender stopped");
    }

    private void sendLoop(SipAudioOffer offer, int localPort) {
        InetAddress remoteAddr;
        try {
            remoteAddr = InetAddress.getByName(offer.getRemoteHost());
        } catch (UnknownHostException e) {
            LOGGER.warn("PitchToneRtpSender: cannot resolve host {}: {}", offer.getRemoteHost(), e.getMessage());
            return;
        }

        int remotePort = offer.getRemotePort();
        int payloadType = offer.getPayloadType();
        int sampleRate = offer.getClockRate();
        int packetDurationMs = Math.max(10, offer.getPtimeMs());
        int samplesPerPacket = sampleRate * packetDurationMs / 1000;

        byte[] payload = encodePayload(offer.getCodecName(), sampleRate, samplesPerPacket);

        byte[] rtpPacket = new byte[RTP_HEADER_SIZE + payload.length];
        int ssrc = (int) (Math.random() * Integer.MAX_VALUE);
        int seqNum = 0;
        long timestamp = 0;

        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(localPort));
            LOGGER.debug("PitchToneRtpSender: socket bound locally on {} and sending to {}:{}",
                    socket.getLocalSocketAddress(), remoteAddr, remotePort);
            while (running.get()) {
                long sendStart = System.nanoTime();

                // Build RTP header
                rtpPacket[0] = (byte) 0x80; // V=2, P=0, X=0, CC=0
                rtpPacket[1] = (byte) (payloadType & 0x7F); // M=0
                rtpPacket[2] = (byte) ((seqNum >> 8) & 0xFF);
                rtpPacket[3] = (byte) (seqNum & 0xFF);
                rtpPacket[4] = (byte) ((timestamp >> 24) & 0xFF);
                rtpPacket[5] = (byte) ((timestamp >> 16) & 0xFF);
                rtpPacket[6] = (byte) ((timestamp >> 8) & 0xFF);
                rtpPacket[7] = (byte) (timestamp & 0xFF);
                rtpPacket[8] = (byte) ((ssrc >> 24) & 0xFF);
                rtpPacket[9] = (byte) ((ssrc >> 16) & 0xFF);
                rtpPacket[10] = (byte) ((ssrc >> 8) & 0xFF);
                rtpPacket[11] = (byte) (ssrc & 0xFF);
                System.arraycopy(payload, 0, rtpPacket, RTP_HEADER_SIZE, payload.length);

                DatagramPacket packet = new DatagramPacket(rtpPacket, rtpPacket.length, remoteAddr, remotePort);
                socket.send(packet);

                seqNum = (seqNum + 1) & 0xFFFF;
                timestamp += samplesPerPacket;

                // Sleep for remaining time in 20ms slot
                long elapsedNs = System.nanoTime() - sendStart;
                long remainingNs = (packetDurationMs * 1_000_000L) - elapsedNs;
                if (remainingNs > 0) {
                    Thread.sleep(remainingNs / 1_000_000L, (int) (remainingNs % 1_000_000L));
                }
            }
        } catch (SocketException e) {
            if (running.get()) {
                LOGGER.warn("PitchToneRtpSender: socket error: {}", e.getMessage());
            }
        } catch (IOException e) {
            if (running.get()) {
                LOGGER.warn("PitchToneRtpSender: IO error: {}", e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.debug("PitchToneRtpSender: send loop ended");
    }

    /**
     * Encode {@code sampleCount} samples of a 2 kHz sine wave using the negotiated codec.
     */
    private static byte[] encodePayload(String codecName, int sampleRate, int sampleCount) {
        if (CODEC_PCM.equals(codecName)) {
            return encodePcm16Packet(sampleRate, sampleCount);
        }
        if (CODEC_PCMU.equals(codecName)) {
            return encodePcmuPacket(sampleRate, sampleCount);
        }

        throw new IllegalArgumentException("Unsupported codec for tone sender: " + codecName);
    }

    private static byte[] encodePcmuPacket(int sampleRate, int sampleCount) {
        byte[] out = new byte[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            double angle = 2.0 * Math.PI * TONE_FREQ_HZ * i / sampleRate;
            // amplitude 0.7 to keep signal well below clipping
            int pcm16 = (int) (0.7 * 32767.0 * Math.sin(angle));
            out[i] = linearToUlaw(pcm16);
        }
        return out;
    }

    private static byte[] encodePcm16Packet(int sampleRate, int sampleCount) {
        byte[] out = new byte[sampleCount * 2];
        for (int i = 0; i < sampleCount; i++) {
            double angle = 2.0 * Math.PI * TONE_FREQ_HZ * i / sampleRate;
            int pcm16 = (int) (0.7 * 32767.0 * Math.sin(angle));
            out[i * 2] = (byte) ((pcm16 >> 8) & 0xFF);
            out[i * 2 + 1] = (byte) (pcm16 & 0xFF);
        }
        return out;
    }

    /**
     * Convert a 16-bit linear PCM sample to G.711 µ-law (PCMU, PT=0).
     * Standard ITU-T G.711 µ-law compression table.
     */
    private static byte linearToUlaw(int sample) {
        final int BIAS = 0x84;
        final int MAX = 32767;

        int sign;
        if (sample < 0) {
            sample = -sample;
            sign = 0x80;
        } else {
            sign = 0;
        }
        if (sample > MAX) {
            sample = MAX;
        }
        sample += BIAS;

        int exponent = 7;
        for (int expMask = 0x4000; (sample & expMask) == 0 && exponent > 0; exponent--) {
            expMask >>= 1;
        }
        int mantissa = (sample >> (exponent + 3)) & 0x0F;
        int ulawByte = ~(sign | (exponent << 4) | mantissa) & 0xFF;
        return (byte) ulawByte;
    }
}
