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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayAuth;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of AirPlay v1 protocol logic.
 *
 * <p>
 * Plain RTSP ANNOUNCE/SETUP with the negotiated ports carried in the {@code Transport}
 * header, unencrypted audio packets, and an optional feedback keep-alive loop when the
 * receiver answers the initial {@code /feedback} probe with 200.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayV1 implements StreamProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayV1.class);

    private static final Map<String, String> PLAY_HEADERS = Map.of("User-Agent", "MediaControl/1.0", "Content-Type",
            "application/x-apple-binary-plist");

    /**
     * Parsed {@code Transport} header of a SETUP request/response.
     *
     * @param params options without a value, in order of appearance
     * @param options {@code key=value} options
     */
    public record TransportInfo(List<String> params, Map<String, String> options) {
    }

    private final StreamContext context;
    private final RtspSession rtsp;
    private final StreamTiming timing;

    private volatile @Nullable Thread keepAliveThread;

    /**
     * Creates a new AirPlay v1 stream protocol.
     *
     * @param context shared stream context
     * @param rtsp RTSP session to the receiver
     * @param timing timing knobs (keep-alive interval)
     */
    public AirPlayV1(StreamContext context, RtspSession rtsp, StreamTiming timing) {
        this.context = context;
        this.rtsp = rtsp;
        this.timing = timing;
    }

    /**
     * Parses a {@code Transport} header value.
     *
     * @param transport the header value, e.g. {@code RTP/AVP/UDP;unicast;server_port=1234}
     * @return parameters and key/value options
     */
    public static TransportInfo parseTransport(String transport) {
        List<String> params = new ArrayList<>();
        Map<String, String> options = new LinkedHashMap<>();
        for (String option : transport.split(";")) {
            int index = option.indexOf('=');
            if (index >= 0) {
                options.put(option.substring(0, index), option.substring(index + 1));
            } else {
                params.add(option);
            }
        }
        return new TransportInfo(params, options);
    }

    @Override
    public void setup(int timingServerPort, int controlClientPort) {
        RaopFutures.await(AirPlayAuth.pairVerify(context.credentials, rtsp.connection()).verifyCredentials());

        RaopFutures
                .await(rtsp.announce(context.bytesPerChannel, context.channels, context.sampleRate, context.password));

        HttpResponse resp = RaopFutures
                .await(rtsp.setup(Map.of("Transport", "RTP/AVP/UDP;unicast;interleaved=0-1;mode=record;control_port="
                        + controlClientPort + ";timing_port=" + timingServerPort), null));
        Object transport = resp.headers().get("Transport");
        if (transport == null) {
            throw new ProtocolError("no Transport header in SETUP response");
        }
        TransportInfo info = parseTransport(transport.toString());
        context.timingPort = Integer.parseInt(info.options().getOrDefault("timing_port", "0"));
        context.controlPort = Integer.parseInt(requireOption(info, "control_port"));
        context.rtspSession = Long.parseLong(String.valueOf(resp.headers().get("Session")));
        context.serverPort = Integer.parseInt(requireOption(info, "server_port"));

        LOGGER.debug("Remote ports: control={}, timing={}, server={}", context.controlPort, context.timingPort,
                context.serverPort);
    }

    private static String requireOption(TransportInfo info, String key) {
        String value = info.options().get(key);
        if (value == null) {
            throw new ProtocolError("missing " + key + " in Transport header");
        }
        return value;
    }

    @Override
    public void teardown() {
        Thread task = keepAliveThread;
        if (task != null) {
            keepAliveThread = null;
            task.interrupt();
        }
    }

    @Override
    public void startFeedback() {
        HttpResponse feedback = RaopFutures.await(rtsp.feedback(true));
        if (feedback.code() == 200) {
            keepAliveThread = Thread.ofVirtual().name("raop-keep-alive").start(this::sendKeepAlive);
        } else {
            LOGGER.debug("Keep-alive not supported, not starting task");
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void sendKeepAlive() {
        LOGGER.debug("Starting keep-alive task");
        while (keepAliveThread == Thread.currentThread()) {
            try {
                Thread.sleep(timing.keepAliveInterval());
                LOGGER.trace("Sending keep-alive feedback");
                RaopFutures.await(rtsp.feedback(false));
            } catch (InterruptedException e) {
                break;
            } catch (ProtocolError e) {
                LOGGER.warn("feedback failed", e);
            } catch (RuntimeException e) {
                LOGGER.warn("feedback failed", e);
            }
        }
        LOGGER.debug("Feedback task finished");
    }

    @Override
    public SentAudioPacket sendAudioPacket(DatagramSocket socket, byte[] rtpHeader, byte[] audio) throws IOException {
        byte[] packet = new byte[rtpHeader.length + audio.length];
        System.arraycopy(rtpHeader, 0, packet, 0, rtpHeader.length);
        System.arraycopy(audio, 0, packet, rtpHeader.length, audio.length);
        socket.send(new DatagramPacket(packet, packet.length));
        return new SentAudioPacket(context.rtpseq, packet);
    }

    @Override
    public HttpResponse playUrl(int timingServerPort, String url, double position) {
        RaopFutures.await(AirPlayAuth.pairVerify(context.credentials, rtsp.connection()).verifyCredentials());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("Content-Location", url);
        body.put("Start-Position", position);
        body.put("X-Apple-Session-ID", UUID.randomUUID().toString());

        return RaopFutures.await(rtsp.connection().post("/play", PLAY_HEADERS, BinaryPlist.dump(body), true));
    }
}
