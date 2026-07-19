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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.DeviceListener;
import org.openhab.binding.atv.internal.client.auth.HapChannel;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.core.Heartbeater;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.settings.InfoSettings;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.openhab.binding.atv.internal.client.support.http.HttpParser;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level support for an AirPlay 2 session.
 *
 * <p>
 * Sets up a connection to an AirPlay 2 receiver and takes care of encryption and the
 * low-level channel plumbing. The flow is: {@link #connect()} (TCP + pair-verify + transparent
 * {@code Control-Salt} encryption on the control connection), then
 * {@link #setupRemoteControl()} which performs RTSP SETUP for the event channel (with
 * {@code isRemoteControlOnly=true}), connects back to the announced event port (with
 * {@code Events-Salt} keys — read/write infos <em>reversed</em> since that connection
 * originates from the receiver), sends RECORD and finally sets up the data channel
 * (second SETUP with a stream of type 130 and a random 64-bit seed appended to the
 * {@code DataStream-Salt} when deriving its keys).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Ap2Session {

    /**
     * Time between feedback (keep-alive) messages; this is what iOS uses.
     */
    public static final Duration FEEDBACK_INTERVAL = Duration.ofSeconds(2);

    /** HKDF salt for the event channel keys ({@code EVENTS_SALT}). */
    public static final String EVENTS_SALT = "Events-Salt";
    /** HKDF info for the event channel write key ({@code EVENTS_WRITE_INFO}). */
    public static final String EVENTS_WRITE_INFO = "Events-Write-Encryption-Key";
    /** HKDF info for the event channel read key ({@code EVENTS_READ_INFO}). */
    public static final String EVENTS_READ_INFO = "Events-Read-Encryption-Key";

    /** HKDF salt prefix for the data channel keys; the seed must be appended. */
    public static final String DATASTREAM_SALT = "DataStream-Salt";
    /** HKDF info for the data channel output key ({@code DATASTREAM_OUTPUT_INFO}). */
    public static final String DATASTREAM_OUTPUT_INFO = "DataStream-Output-Encryption-Key";
    /** HKDF info for the data channel input key ({@code DATASTREAM_INPUT_INFO}). */
    public static final String DATASTREAM_INPUT_INFO = "DataStream-Input-Encryption-Key";

    /** Client type UUID sent in the data channel SETUP request. */
    public static final String CLIENT_TYPE_UUID = "1910A70F-DBC0-4242-AF95-115DB30604E1";

    private static final Logger LOGGER = LoggerFactory.getLogger(Ap2Session.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String address;
    private final int controlPort;
    private final HapCredentials credentials;
    private final InfoSettings info;
    private final ScheduledExecutorService scheduler;
    private final Duration feedbackInterval;
    private final LongSupplier seedSupplier;

    private final List<HapChannel> channels = new CopyOnWriteArrayList<>();

    private volatile @Nullable HttpConnection connection;
    private volatile @Nullable AirPlayPairVerifyProcedure verifier;
    private volatile @Nullable RtspSession rtsp;
    private volatile @Nullable DataStreamChannel dataChannel;
    private volatile @Nullable Heartbeater feedback;

    /**
     * Creates a new session with the default feedback interval and a random data channel
     * seed.
     *
     * @param address receiver address
     * @param controlPort AirPlay (control) port
     * @param credentials HAP credentials used for pair-verify
     * @param info client identity settings
     * @param scheduler scheduler used for the keep-alive loop
     */
    public Ap2Session(String address, int controlPort, HapCredentials credentials, InfoSettings info,
            ScheduledExecutorService scheduler) {
        // Constrained to non-negative longs so the value can be carried in a binary plist
        // integer without sign issues
        this(address, controlPort, credentials, info, scheduler, FEEDBACK_INTERVAL, () -> RANDOM.nextLong() >>> 1);
    }

    /**
     * Creates a new session with an injectable feedback interval and data channel seed,
     * for reproducible tests.
     *
     * @param address receiver address
     * @param controlPort AirPlay (control) port
     * @param credentials HAP credentials used for pair-verify
     * @param info client identity settings
     * @param scheduler scheduler used for the keep-alive loop
     * @param feedbackInterval time between keep-alive messages
     * @param seedSupplier supplies the 64-bit random seed for the data channel salt
     */
    public Ap2Session(String address, int controlPort, HapCredentials credentials, InfoSettings info,
            ScheduledExecutorService scheduler, Duration feedbackInterval, LongSupplier seedSupplier) {
        this.address = address;
        this.controlPort = controlPort;
        this.credentials = credentials;
        this.info = info;
        this.scheduler = scheduler;
        this.feedbackInterval = feedbackInterval;
        this.seedSupplier = seedSupplier;
    }

    /**
     * The control connection, or {@code null} when not connected.
     */
    public @Nullable HttpConnection connection() {
        return connection;
    }

    /**
     * The RTSP session on the control connection, or {@code null} when not connected.
     */
    public @Nullable RtspSession rtsp() {
        return rtsp;
    }

    /**
     * The data stream channel, or {@code null} until {@link #setupRemoteControl()} ran.
     */
    public @Nullable DataStreamChannel dataChannel() {
        return dataChannel;
    }

    /**
     * Opens the connection to the receiver: TCP connect, pair-verify (with transparent
     * {@code Control-Salt} channel encryption when the scheme provides keys) and RTSP
     * session creation.
     *
     * @return future completing when connected and verified
     */
    public CompletableFuture<Void> connect() {
        LOGGER.debug("Setting up remote connection to {}:{}", address, controlPort);
        return HttpConnection.connect(address, controlPort).thenCompose(newConnection -> {
            this.connection = newConnection;
            return AirPlayAuth.verifyConnection(credentials, newConnection);
        }).thenAccept(newVerifier -> {
            this.verifier = newVerifier;
            HttpConnection currentConnection = this.connection;
            if (currentConnection == null) {
                throw new InvalidStateError("connection missing after connect");
            }
            this.rtsp = new RtspSession(currentConnection);
        });
    }

    /**
     * Sets up the remote control session over the data channel: event channel SETUP,
     * RECORD, data channel SETUP.
     *
     * @return future completing when the data channel is connected
     */
    public CompletableFuture<Void> setupRemoteControl() {
        RtspSession session = rtsp;
        if (connection == null || session == null) {
            return CompletableFuture.failedFuture(new InvalidStateError("not connected to remote"));
        }
        String remoteAddress = connection.remoteIp();
        return setupEventChannel(remoteAddress).thenCompose(v -> session.record(null, null))
                .thenCompose(v -> setupDataChannel(remoteAddress));
    }

    /**
     * Starts sending keep alive messages: a {@code POST /feedback} every
     * {@link #FEEDBACK_INTERVAL}; a persistent failure is reported to the device listener as
     * a lost connection.
     *
     * @param deviceListener listener notified when the keep-alive fails, may be {@code null}
     */
    public void startKeepAlive(@Nullable DeviceListener deviceListener) {
        Heartbeater beater = new Heartbeater("AirPlay:" + address, () -> {
            RtspSession session = rtsp;
            return session != null ? session.feedback(false) : CompletableFuture.completedFuture(null);
        }, () -> {
            if (deviceListener != null) {
                deviceListener.connectionLost(new InvalidStateError("feedback failed"));
            }
        }, scheduler, feedbackInterval, Heartbeater.DEFAULT_RETRIES);
        this.feedback = beater;
        beater.start();
    }

    /**
     * Closes all open connections.
     */
    public void stop() {
        Heartbeater beater = feedback;
        if (beater != null) {
            beater.stop();
            feedback = null;
        }
        HttpConnection currentConnection = connection;
        if (currentConnection != null) {
            currentConnection.close();
            connection = null;
        }
        for (HapChannel channel : channels) {
            channel.close();
        }
        channels.clear();
        dataChannel = null;
    }

    private CompletableFuture<Map<String, Object>> setup(Object body) {
        RtspSession session = rtsp;
        if (session == null) {
            return CompletableFuture.failedFuture(new InvalidStateError("not in connected state"));
        }
        return session.setup(null, body).thenApply(HttpParser::decodeBplistFromBody);
    }

    private CompletableFuture<Void> setupEventChannel(String remoteAddress) {
        AirPlayPairVerifyProcedure currentVerifier = verifier;
        if (currentVerifier == null) {
            return CompletableFuture.failedFuture(new InvalidStateError("not in connected state"));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isRemoteControlOnly", true);
        body.put("osName", info.osName());
        body.put("sourceVersion", "550.10");
        body.put("timingProtocol", "None");
        body.put("model", info.model());
        body.put("deviceID", info.deviceId());
        body.put("osVersion", info.osVersion());
        body.put("osBuildVersion", info.osBuild());
        body.put("macAddress", info.mac());
        body.put("sessionUUID", newUuid());
        body.put("name", info.name());

        return setup(body).thenCompose(resp -> {
            int eventPort = ((Number) resp.get("eventPort")).intValue();

            // Event channel is not used so we don't care about it (must be set up
            // though). Note: Read/Write info reversed here as connection originates
            // from the receiver!
            return HapChannel.setupChannel(EventChannel::new, currentVerifier, remoteAddress, eventPort, EVENTS_SALT,
                    EVENTS_READ_INFO, EVENTS_WRITE_INFO);
        }).thenAccept(channels::add);
    }

    private CompletableFuture<Void> setupDataChannel(String remoteAddress) {
        AirPlayPairVerifyProcedure currentVerifier = verifier;
        if (currentVerifier == null) {
            return CompletableFuture.failedFuture(new InvalidStateError("not in connected state"));
        }
        // A 64 bit random seed is included and used as part of the salt in encryption
        long seed = seedSupplier.getAsLong();

        Map<String, Object> stream = new LinkedHashMap<>();
        stream.put("controlType", 2);
        stream.put("channelID", newUuid());
        stream.put("seed", seed);
        stream.put("clientUUID", newUuid());
        stream.put("type", 130);
        stream.put("wantsDedicatedSocket", true);
        stream.put("clientTypeUUID", CLIENT_TYPE_UUID);

        return setup(Map.of("streams", List.of(stream))).thenCompose(resp -> {
            List<?> streams = (List<?>) resp.get("streams");
            Map<?, ?> first = (Map<?, ?>) streams.get(0);
            int dataPort = ((Number) first.get("dataPort")).intValue();

            // The decimal representation of the (unsigned) seed is appended to the salt
            // string
            return HapChannel.setupChannel(DataStreamChannel::new, currentVerifier, remoteAddress, dataPort,
                    DATASTREAM_SALT + Long.toUnsignedString(seed), DATASTREAM_OUTPUT_INFO, DATASTREAM_INPUT_INFO);
        }).thenAccept(channel -> {
            channels.add(channel);
            this.dataChannel = channel;
        });
    }

    private static String newUuid() {
        return UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    }
}
