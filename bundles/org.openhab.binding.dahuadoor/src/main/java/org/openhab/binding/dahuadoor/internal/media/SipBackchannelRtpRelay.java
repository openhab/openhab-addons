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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relays local go2rtc backchannel RTP packets to the currently active SIP audio target.
 *
 * <p>
 * go2rtc currently uses ffmpeg to transcode browser backchannel audio to RTP PCM/16 kHz on a local
 * loopback port. This relay only forwards those RTP packets to the negotiated SIP target and rewrites
 * the payload type to the negotiated one.
 * </p>
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class SipBackchannelRtpRelay {

    private static final Logger LOGGER = LoggerFactory.getLogger(SipBackchannelRtpRelay.class);

    private static final int MAX_PACKET_SIZE = 2048;
    private static final int RTP_HEADER_MIN_LEN = 12;
    private static final int RTP_VERSION_MASK = 0xC0;
    private static final int RTP_VERSION_2 = 0x80;

    private static final class RelayTarget {
        final InetAddress remoteAddress;
        final int remotePort;
        final int payloadType;

        RelayTarget(InetAddress remoteAddress, int remotePort, int payloadType) {
            this.remoteAddress = remoteAddress;
            this.remotePort = remotePort;
            this.payloadType = payloadType;
        }
    }

    private final int listenPort;
    private final int sourcePort;
    private final Object lifecycleLock = new Object();

    private volatile @Nullable RelayTarget relayTarget;

    private @Nullable DatagramSocket receiveSocket;
    private @Nullable DatagramSocket sendSocket;
    private @Nullable Thread workerThread;

    private volatile boolean running;
    private volatile long receivedPackets;
    private volatile long forwardedPackets;
    private volatile long droppedNoTargetPackets;
    private volatile long droppedInvalidPackets;
    private volatile long lastIncomingPacketAtMillis;
    private volatile long targetSetAtMillis;
    private volatile long lastNoInputWarningAtMillis;

    public SipBackchannelRtpRelay(int listenPort, int sourcePort) {
        this.listenPort = listenPort;
        this.sourcePort = sourcePort;
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
            DatagramSocket localSendSocket = new DatagramSocket(sourcePort);

            running = true;
            receivedPackets = 0;
            forwardedPackets = 0;
            droppedNoTargetPackets = 0;
            droppedInvalidPackets = 0;
            lastIncomingPacketAtMillis = System.currentTimeMillis();
            targetSetAtMillis = 0;
            lastNoInputWarningAtMillis = 0;
            receiveSocket = localReceiveSocket;
            sendSocket = localSendSocket;

            Thread worker = new Thread(this::runLoop, "dahua-sip-backchannel-relay-" + listenPort);
            worker.setDaemon(true);
            worker.start();
            workerThread = worker;

            LOGGER.info("SIP backchannel RTP relay started on 127.0.0.1:{} with source port {}", listenPort,
                    sourcePort);
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

        LOGGER.info(
                "SIP backchannel RTP relay stopped (listenPort={}, sourcePort={}, received={}, forwarded={}, droppedNoTarget={}, droppedInvalid={})",
                listenPort, sourcePort, receivedPackets, forwardedPackets, droppedNoTargetPackets,
                droppedInvalidPackets);
    }

    public void setTarget(@Nullable SipAudioOffer offer, String reason) {
        if (offer == null) {
            relayTarget = null;
            targetSetAtMillis = 0;
            lastNoInputWarningAtMillis = 0;
            LOGGER.info("SIP backchannel relay target cleared (reason={})", reason);
            return;
        }

        try {
            String codecName = offer.getCodecName();
            if (!("PCM".equals(codecName) && offer.getClockRate() == 16000)) {
                relayTarget = null;
                LOGGER.warn(
                        "SIP backchannel relay target rejected (expected PCM/16000, got codec={} rate={}Hz, reason={})",
                        codecName, offer.getClockRate(), reason);
                return;
            }

            InetAddress address = InetAddress.getByName(offer.getRemoteHost());
            relayTarget = new RelayTarget(address, offer.getRemotePort(), offer.getPayloadType());
            targetSetAtMillis = System.currentTimeMillis();
            lastNoInputWarningAtMillis = 0;
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
                RelayTarget target = relayTarget;
                if (target != null) {
                    long now = System.currentTimeMillis();
                    if (now - targetSetAtMillis >= 2000 && now - lastIncomingPacketAtMillis >= 2000
                            && now - lastNoInputWarningAtMillis >= 2000) {
                        LOGGER.debug(
                                "SIP backchannel relay has active target {}:{} but no incoming RTP on 127.0.0.1:{} for {} ms",
                                target.remoteAddress.getHostAddress(), target.remotePort, listenPort,
                                now - lastIncomingPacketAtMillis);
                        lastNoInputWarningAtMillis = now;
                    }
                }
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

            receivedPackets++;
            lastIncomingPacketAtMillis = System.currentTimeMillis();
            RelayTarget target = relayTarget;
            if (target == null) {
                droppedNoTargetPackets++;
                if (droppedNoTargetPackets == 1 || droppedNoTargetPackets % 100 == 0) {
                    LOGGER.debug("SIP backchannel relay dropping RTP packet without target (count={}, listenPort={})",
                            droppedNoTargetPackets, listenPort);
                }
                continue;
            }

            byte[] forwardedPacket = buildForwardPacket(incoming, target);
            if (forwardedPacket == null) {
                droppedInvalidPackets++;
                if (droppedInvalidPackets == 1 || droppedInvalidPackets % 100 == 0) {
                    LOGGER.debug("SIP backchannel relay dropped invalid RTP packet (count={}, length={})",
                            droppedInvalidPackets, incoming.getLength());
                }
                continue;
            }

            DatagramPacket forwarded = new DatagramPacket(forwardedPacket, forwardedPacket.length, target.remoteAddress,
                    target.remotePort);
            try {
                localSendSocket.send(forwarded);
                forwardedPackets++;
                if (forwardedPackets == 1 || forwardedPackets % 100 == 0) {
                    LOGGER.debug("SIP backchannel relay forwarded RTP packet (count={}, bytes={}, target={}:{}, pt={})",
                            forwardedPackets, forwardedPacket.length, target.remoteAddress.getHostAddress(),
                            target.remotePort, target.payloadType);
                }
            } catch (IOException e) {
                LOGGER.debug("SIP backchannel relay forward failed to {}:{}: {}", target.remoteAddress,
                        target.remotePort, e.getMessage());
            }
        }
    }

    private static byte @Nullable [] buildForwardPacket(DatagramPacket incoming, RelayTarget target) {
        int length = incoming.getLength();
        if (length < RTP_HEADER_MIN_LEN) {
            return null;
        }

        byte[] packet = new byte[length];
        System.arraycopy(incoming.getData(), incoming.getOffset(), packet, 0, length);
        int payloadOffset = getRtpPayloadOffset(packet, length);
        if (payloadOffset < 0 || payloadOffset >= length) {
            return null;
        }

        rewriteRtpPayloadType(packet, target.payloadType);
        return packet;
    }

    private static void rewriteRtpPayloadType(byte[] packet, int payloadType) {
        int markerBit = packet[1] & 0x80;
        packet[1] = (byte) (markerBit | (payloadType & 0x7F));
    }

    private static int getRtpPayloadOffset(byte[] packet, int length) {
        if (length < RTP_HEADER_MIN_LEN) {
            return -1;
        }
        if ((packet[0] & RTP_VERSION_MASK) != RTP_VERSION_2) {
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
}
