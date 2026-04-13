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
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight local RTP relay for SIP backchannel forwarding.
 *
 * <p>
 * Receives RTP packets on a local loopback UDP port (from go2rtc ffmpeg exec source)
 * and forwards them to the current SIP offer target.
 * </p>
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class SipBackchannelRtpRelay {

    private static final Logger LOGGER = LoggerFactory.getLogger(SipBackchannelRtpRelay.class);

    private static final int RTP_HEADER_MIN_LEN = 12;
    private static final int MAX_PACKET_SIZE = 2048;
    private static final byte RTP_VERSION_2 = (byte) 0x80;

    private enum TargetCodec {
        PCMA,
        PCMU
    }

    private final int listenPort;

    private final Object lifecycleLock = new Object();
    private volatile @Nullable RelayTarget relayTarget;

    private @Nullable DatagramSocket receiveSocket;
    private @Nullable DatagramSocket sendSocket;
    private @Nullable Thread workerThread;

    private volatile boolean running;

    private static final class RelayTarget {
        final InetAddress remoteAddress;
        final int remotePort;
        final int payloadType;
        final TargetCodec codec;

        RelayTarget(InetAddress remoteAddress, int remotePort, int payloadType, TargetCodec codec) {
            this.remoteAddress = remoteAddress;
            this.remotePort = remotePort;
            this.payloadType = payloadType;
            this.codec = codec;
        }
    }

    public SipBackchannelRtpRelay(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void start() throws IOException {
        synchronized (lifecycleLock) {
            if (running) {
                return;
            }

            DatagramSocket localReceiveSocket = new DatagramSocket(new InetSocketAddress("127.0.0.1", listenPort));
            localReceiveSocket.setSoTimeout(500);
            DatagramSocket localSendSocket = new DatagramSocket();

            running = true;
            receiveSocket = localReceiveSocket;
            sendSocket = localSendSocket;

            Thread worker = new Thread(this::runLoop, "dahua-sip-backchannel-relay-" + listenPort);
            worker.setDaemon(true);
            worker.start();
            workerThread = worker;

            LOGGER.info("SIP backchannel RTP relay started on 127.0.0.1:{}", listenPort);
        }
    }

    public void stop() {
        Thread localWorker;
        DatagramSocket localReceiveSocket;
        DatagramSocket localSendSocket;

        synchronized (lifecycleLock) {
            if (!running) {
                relayTarget = null;
                return;
            }

            running = false;
            relayTarget = null;

            localWorker = workerThread;
            workerThread = null;

            localReceiveSocket = receiveSocket;
            receiveSocket = null;

            localSendSocket = sendSocket;
            sendSocket = null;
        }

        if (localReceiveSocket != null) {
            localReceiveSocket.close();
        }
        if (localSendSocket != null) {
            localSendSocket.close();
        }
        if (localWorker != null) {
            localWorker.interrupt();
            try {
                localWorker.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        LOGGER.info("SIP backchannel RTP relay stopped (listenPort={})", listenPort);
    }

    public void setTarget(@Nullable SipAudioOffer offer, String reason) {
        if (offer == null) {
            relayTarget = null;
            LOGGER.info("SIP backchannel relay target cleared (reason={})", reason);
            return;
        }

        try {
            String codecName = offer.getCodecName();
            TargetCodec codec;
            if ("PCMA".equals(codecName)) {
                codec = TargetCodec.PCMA;
            } else if ("PCMU".equals(codecName)) {
                codec = TargetCodec.PCMU;
            } else {
                relayTarget = null;
                LOGGER.warn(
                        "SIP backchannel relay target rejected (unsupported codec={}, expected PCMA/PCMU, reason={})",
                        codecName, reason);
                return;
            }

            InetAddress address = InetAddress.getByName(offer.getRemoteHost());
            RelayTarget newTarget = new RelayTarget(address, offer.getRemotePort(), offer.getPayloadType(), codec);
            relayTarget = newTarget;
            LOGGER.info("SIP backchannel relay target set to {}:{} (pt={}, codec={}, reason={})", offer.getRemoteHost(),
                    offer.getRemotePort(), offer.getPayloadType(), codecName, reason);
        } catch (IOException e) {
            relayTarget = null;
            LOGGER.warn("Failed to resolve SIP backchannel relay target '{}': {}", offer.getRemoteHost(),
                    e.getMessage());
        }
    }

    private void runLoop() {
        byte[] buffer = new byte[MAX_PACKET_SIZE];

        while (running) {
            DatagramSocket localReceiveSocket = receiveSocket;
            DatagramSocket localSendSocket = sendSocket;
            if (localReceiveSocket == null || localSendSocket == null) {
                return;
            }

            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            try {
                localReceiveSocket.receive(incoming);
            } catch (SocketTimeoutException e) {
                continue;
            } catch (SocketException e) {
                if (running) {
                    LOGGER.debug("SIP backchannel relay receive socket closed: {}", e.getMessage());
                }
                continue;
            } catch (IOException e) {
                if (running) {
                    LOGGER.debug("SIP backchannel relay receive failed: {}", e.getMessage());
                }
                continue;
            }

            RelayTarget target = relayTarget;
            if (target == null) {
                continue;
            }

            int length = incoming.getLength();
            if (length < RTP_HEADER_MIN_LEN) {
                continue;
            }

            byte[] out = Arrays.copyOf(incoming.getData(), length);
            int payloadOffset = getRtpPayloadOffset(out, length);
            if (payloadOffset < 0 || payloadOffset >= length) {
                continue;
            }

            if (target.codec == TargetCodec.PCMU) {
                transcodeAlawToUlaw(out, payloadOffset, length);
            }
            rewriteRtpPayloadType(out, target.payloadType);

            DatagramPacket forwarded = new DatagramPacket(out, out.length, target.remoteAddress, target.remotePort);
            try {
                localSendSocket.send(forwarded);
            } catch (IOException e) {
                LOGGER.debug("SIP backchannel relay forward failed to {}:{}: {}", target.remoteAddress,
                        target.remotePort, e.getMessage());
            }
        }
    }

    private static void rewriteRtpPayloadType(byte[] packet, int payloadType) {
        int markerBit = packet[1] & 0x80;
        packet[1] = (byte) (markerBit | (payloadType & 0x7F));
    }

    private static int getRtpPayloadOffset(byte[] packet, int length) {
        if (length < RTP_HEADER_MIN_LEN) {
            return -1;
        }
        if ((packet[0] & RTP_VERSION_2) != RTP_VERSION_2) {
            return -1;
        }

        int csrcCount = packet[0] & 0x0F;
        boolean hasExtension = (packet[0] & 0x10) != 0;
        int offset = RTP_HEADER_MIN_LEN + (csrcCount * 4);

        if (hasExtension) {
            if (length < offset + 4) {
                return -1;
            }
            int extensionWords = ((packet[offset + 2] & 0xFF) << 8) | (packet[offset + 3] & 0xFF);
            offset += 4 + (extensionWords * 4);
        }

        return offset <= length ? offset : -1;
    }

    private static void transcodeAlawToUlaw(byte[] packet, int payloadOffset, int length) {
        for (int i = payloadOffset; i < length; i++) {
            short pcm = alawToLinear(packet[i]);
            packet[i] = linearToUlaw(pcm);
        }
    }

    private static short alawToLinear(byte alaw) {
        int value = alaw ^ 0x55;
        int mantissa = value & 0x0F;
        int segment = (value & 0x70) >> 4;
        int magnitude = mantissa << 4;
        switch (segment) {
            case 0:
                magnitude += 8;
                break;
            case 1:
                magnitude += 0x108;
                break;
            default:
                magnitude += 0x108;
                magnitude <<= segment - 1;
                break;
        }
        return (short) ((value & 0x80) != 0 ? magnitude : -magnitude);
    }

    private static byte linearToUlaw(short sample) {
        final int bias = 0x84;
        final int clip = 32635;

        int pcm = sample;
        int sign = (pcm >> 8) & 0x80;
        if (sign != 0) {
            pcm = -pcm;
        }
        if (pcm > clip) {
            pcm = clip;
        }

        pcm += bias;

        int exponent = 7;
        int mask;
        for (mask = 0x4000; (pcm & mask) == 0 && exponent > 0; exponent--, mask >>= 1) {
            // iterate to find exponent
        }
        int mantissa = (pcm >> (exponent + 3)) & 0x0F;
        int ulaw = ~(sign | (exponent << 4) | mantissa);
        return (byte) ulaw;
    }
}
