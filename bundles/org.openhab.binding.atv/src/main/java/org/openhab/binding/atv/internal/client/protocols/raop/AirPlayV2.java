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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayAuth;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayPairVerifyProcedure;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.Chacha20Cipher;
import org.openhab.binding.atv.internal.client.support.http.HttpParser;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of AirPlay v2 protocol logic.
 *
 * <p>
 * Pair-verify with connection level encryption, binary plist SETUP messages (base
 * session with event channel, then the audio stream), per-packet ChaCha20-Poly1305
 * encryption of the audio payload (AAD is RTP header bytes 4..12, the 8-byte nonce is
 * appended to each packet) and a periodic feedback task.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayV2 implements StreamProtocol {

    /** HKDF salt for the event channel keys. */
    public static final String EVENTS_SALT = "Events-Salt";

    /** HKDF info for the event write key. */
    public static final String EVENTS_WRITE_INFO = "Events-Write-Encryption-Key";

    /** HKDF info for the event read key. */
    public static final String EVENTS_READ_INFO = "Events-Read-Encryption-Key";

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayV2.class);

    private static final int EVENT_CHANNEL_CONNECT_RETRIES = 5;

    private static final Map<String, String> PLAY_HEADERS = Map.of("User-Agent", "AirPlay/550.10", "Content-Type",
            "application/x-apple-binary-plist", "X-Apple-ProtocolVersion", "1", "X-Apple-Session-ID",
            UUID.randomUUID().toString().toLowerCase(Locale.ROOT), "X-Apple-Stream-ID", "1");

    private final StreamContext context;
    private final RtspSession rtsp;
    private final StreamTiming timing;
    private final String uuid = UUID.randomUUID().toString();

    private @Nullable RaopEventChannel eventChannel;
    private @Nullable AirPlayPairVerifyProcedure verifier;
    private @Nullable Chacha20Cipher cipher;
    private volatile @Nullable Thread feedbackThread;

    /**
     * Creates a new AirPlay v2 stream protocol.
     *
     * @param context shared stream context
     * @param rtsp RTSP session to the receiver
     * @param timing timing knobs (feedback interval)
     */
    public AirPlayV2(StreamContext context, RtspSession rtsp, StreamTiming timing) {
        this.context = context;
        this.rtsp = rtsp;
        this.timing = timing;
    }

    /** Base session setup: pair-verify, SETUP #1 and the event channel connection. */
    private void setupBase(int timingServerPort) {
        AirPlayPairVerifyProcedure verifierLocal = RaopFutures
                .await(AirPlayAuth.verifyConnection(context.credentials, rtsp.connection()));
        verifier = verifierLocal;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deviceID", "AA:BB:CC:DD:EE:FF");
        body.put("sessionUUID", UUID.randomUUID().toString().toUpperCase(Locale.ROOT));
        body.put("timingPort", timingServerPort);
        body.put("timingProtocol", "NTP");
        body.put("isMultiSelectAirPlay", true);
        body.put("groupContainsGroupLeader", false);
        body.put("macAddress", "AA:BB:CC:DD:EE:FF");
        body.put("model", "iPhone14,3");
        body.put("name", "openHAB");
        body.put("osBuildVersion", "20F66");
        body.put("osName", "iPhone OS");
        body.put("osVersion", "16.5");
        body.put("senderSupportsRelay", false);
        body.put("sourceVersion", "690.7.1");
        body.put("statsCollectionEnabled", false);

        HttpResponse setupResp = RaopFutures.await(rtsp.setup(null, body));
        Map<String, Object> resp = HttpParser.decodeBplistFromBody(setupResp);
        LOGGER.debug("Setup response body: {}", resp);

        int eventPort = ((Number) resp.getOrDefault("eventPort", 0)).intValue();
        context.eventPort = eventPort;

        // The receiver sets up the event channel some time after responding with its
        // port, so connecting may initially fail. Retry a few times.
        EncryptionKeys keys = verifierLocal.encryptionKeys(EVENTS_SALT, EVENTS_READ_INFO, EVENTS_WRITE_INFO);
        int retries = EVENT_CHANNEL_CONNECT_RETRIES;
        while (eventChannel == null) {
            try {
                eventChannel = new RaopEventChannel(rtsp.connection().remoteIp(), eventPort, keys.outputKey(),
                        keys.inputKey());
            } catch (IOException e) {
                retries -= 1;
                if (retries == 0) {
                    throw new ProtocolError("failed to connect to event channel", e);
                }
                LOGGER.debug("Connect failed, retrying");
                sleepQuietly(1000);
            }
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void setup(int timingServerPort, int controlClientPort) {
        setupBase(timingServerPort);
        setupAudioStream(controlClientPort);
    }

    /** Sets up a new stream used for audio. */
    private void setupAudioStream(int controlClientPort) {
        AirPlayPairVerifyProcedure verifierLocal = verifier;
        if (verifierLocal == null) {
            throw new InvalidStateError("base stream not set up");
        }

        // The shared key should probably derive from the shared secret, but any key works
        // as it is sent to the receiver in the SETUP body. Derive one from the event
        // parameters so it differs per session.
        EncryptionKeys keys = verifierLocal.encryptionKeys(EVENTS_SALT, EVENTS_WRITE_INFO, EVENTS_READ_INFO);
        byte[] sharedSecret = Arrays.copyOf(keys.outputKey(), 32);

        Map<String, Object> stream = new LinkedHashMap<>();
        stream.put("audioFormat", 0x800); // PCM 44100/16/2
        stream.put("audioMode", "default");
        stream.put("controlPort", controlClientPort);
        stream.put("ct", 1); // Raw PCM
        stream.put("isMedia", true);
        stream.put("latencyMax", 88200);
        stream.put("latencyMin", 11025);
        stream.put("shk", sharedSecret);
        stream.put("spf", 352); // Samples Per Frame
        stream.put("sr", 44100); // Sample rate
        stream.put("type", 0x60);
        stream.put("supportsDynamicStreamID", false);
        stream.put("streamConnectionID", rtsp.sessionId());

        HttpResponse setupResp = RaopFutures.await(rtsp.setup(null, Map.of("streams", List.of(stream))));
        Map<String, Object> resp = HttpParser.decodeBplistFromBody(setupResp);
        LOGGER.debug("Setup stream response: {}", resp);

        Object streams = resp.get("streams");
        if (!(streams instanceof List<?> streamList) || streamList.isEmpty()
                || !(streamList.get(0) instanceof Map<?, ?> streamResp)) {
            throw new ProtocolError("no streams in SETUP response");
        }

        context.controlPort = ((Number) streamResp.get("controlPort")).intValue();
        context.serverPort = ((Number) streamResp.get("dataPort")).intValue();

        // 8-byte nonce cipher, keyed and nonce-seeded from the same shared secret
        cipher = new Chacha20Cipher(sharedSecret, sharedSecret, 8);
    }

    @Override
    public void teardown() {
        Thread task = feedbackThread;
        if (task != null) {
            feedbackThread = null;
            task.interrupt();
        }
        if (eventChannel != null) {
            eventChannel.close();
            eventChannel = null;
        }
    }

    @Override
    public void startFeedback() {
        if (feedbackThread == null) {
            feedbackThread = Thread.ofVirtual().name("raop-feedback").start(this::feedbackLoop);
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void feedbackLoop() {
        LOGGER.debug("Starting feedback task");
        while (feedbackThread == Thread.currentThread()) {
            try {
                RaopFutures.await(rtsp.feedback(false));
            } catch (RuntimeException e) {
                // Treat feedback as "best effort" and don't raise any errors
                LOGGER.debug("Feedback failed: {}", e.toString());
            }
            try {
                Thread.sleep(timing.feedbackInterval());
            } catch (InterruptedException e) {
                break;
            }
        }
        LOGGER.debug("Feedback task finished");
    }

    @Override
    public SentAudioPacket sendAudioPacket(DatagramSocket socket, byte[] rtpHeader, byte[] audio) throws IOException {
        byte[] nonce = new byte[0];
        byte[] payload = audio;
        if (cipher != null) {
            // Save the nonce that will be used by the next encrypt call as it is included
            // in the audio packet.
            byte[] fullNonce = cipher.outNonce();
            byte[] aad = Arrays.copyOfRange(rtpHeader, 4, 12);

            // Do _not_ pass an explicit nonce here as that would not increase the internal
            // counter of outgoing messages.
            payload = cipher.encrypt(audio, null, aad);

            // Only the eight low bytes of the twelve byte nonce go on the wire
            nonce = Arrays.copyOfRange(fullNonce, fullNonce.length - 8, fullNonce.length);
        }

        byte[] packet = new byte[rtpHeader.length + payload.length + nonce.length];
        System.arraycopy(rtpHeader, 0, packet, 0, rtpHeader.length);
        System.arraycopy(payload, 0, packet, rtpHeader.length, payload.length);
        System.arraycopy(nonce, 0, packet, rtpHeader.length + payload.length, nonce.length);

        socket.send(new DatagramPacket(packet, packet.length));

        return new SentAudioPacket(context.rtpseq, packet);
    }

    @Override
    public HttpResponse playUrl(int timingServerPort, String url, double position) {
        setupBase(timingServerPort);
        startFeedback();
        RaopFutures.await(rtsp.record(null, null));

        // Most fields are not needed here, but keeping them for reference
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("Content-Location", url);
        body.put("Start-Position-Seconds", position);
        body.put("uuid", uuid);
        body.put("streamType", 1);
        body.put("mediaType", "file");
        body.put("mightSupportStorePastisKeyRequests", true);
        body.put("playbackRestrictions", 0);
        body.put("secureConnectionMs", 22);
        body.put("volume", 1.0);
        body.put("infoMs", 122);
        body.put("connectMs", 18);
        body.put("authMs", 0);
        body.put("bonjourMs", 0);
        body.put("referenceRestrictions", 3);
        body.put("SenderMACAddress", "AA:BB:CC:DD:EE:FF");
        body.put("model", "iPhone14,3");
        body.put("postAuthMs", 0);
        body.put("clientBundleID", "dev.openhab.GPU");
        body.put("clientProcName", "dev.openhab.GPU");
        body.put("osBuildVersion", "20G1116");
        body.put("rate", 1.0);

        // Actually start the stream
        HttpResponse resp = RaopFutures
                .await(rtsp.connection().post("/play", PLAY_HEADERS, BinaryPlist.dump(body), true));

        // Various commands, most of which are probably not needed. Most important is
        // "/rate" as that sets playback rate to 100% (starts paused otherwise).
        RaopFutures.await(rtsp.exchange("PUT", "/setProperty?isInterestedInDateRange", null, null,
                Map.of("value", true), false, "RTSP/1.0"));
        RaopFutures.await(rtsp.exchange("PUT", "/setProperty?actionAtItemEnd", null, null, Map.of("value", 0), false,
                "RTSP/1.0"));
        RaopFutures.await(rtsp.exchange("POST", "/rate?value=1.000000", null, null, null, false, "RTSP/1.0"));
        Map<String, Object> endTime = Map.of("value", Map.of("flags", 0, "value", 0, "epoch", 0, "timescale", 0));
        RaopFutures.await(rtsp.exchange("PUT", "/setProperty?forwardEndTime", null, null, endTime, false, "RTSP/1.0"));
        RaopFutures.await(rtsp.exchange("PUT", "/setProperty?reverseEndTime", null, null, endTime, false, "RTSP/1.0"));

        return resp;
    }
}
