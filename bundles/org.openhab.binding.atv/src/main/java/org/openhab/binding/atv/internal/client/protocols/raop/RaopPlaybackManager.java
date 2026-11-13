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

import java.time.Clock;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayMajorVersion;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayUtils;
import org.openhab.binding.atv.internal.client.settings.Settings;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages current play state for RAOP.
 *
 * <p>
 * Owns the RTSP connection, the {@link StreamContext} and the {@link RaopStreamClient} of
 * the active session, guards against concurrent streams via {@link #acquire()} and picks
 * the AirPlay major version for the streaming protocol.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopPlaybackManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaopPlaybackManager.class);

    private final String address;
    private final BaseService service;
    private final Settings settings;
    private final StreamTiming timing;
    private final Clock clock;
    private final Random rng;
    private final StreamContext context = new StreamContext();

    private volatile @Nullable PlaybackInfo playbackInfo;
    private boolean isAcquired;
    private @Nullable HttpConnection connection;
    private @Nullable RtspSession rtsp;
    private volatile @Nullable RaopStreamClient streamClient;

    /**
     * Creates a new playback manager.
     *
     * @param address device address
     * @param service the RAOP service
     * @param settings settings (protocol version and local ports)
     * @param timing timing knobs for the stream pipeline
     * @param clock wall clock
     * @param rng randomness source for stream randomization
     */
    public RaopPlaybackManager(String address, BaseService service, Settings settings, StreamTiming timing, Clock clock,
            Random rng) {
        this.address = address;
        this.service = service;
        this.settings = settings;
        this.timing = timing;
        this.clock = clock;
        this.rng = rng;
    }

    /** Returns the RTSP context of the (potentially inactive) session. */
    public StreamContext context() {
        return context;
    }

    /** Returns the stream client if a session is active, otherwise {@code null}. */
    public @Nullable RaopStreamClient streamClient() {
        return streamClient;
    }

    /** Returns what is currently playing, or {@code null} when idle. */
    public @Nullable PlaybackInfo playbackInfo() {
        return playbackInfo;
    }

    /** Sets what is currently playing ({@code null} when idle). */
    public void setPlaybackInfo(@Nullable PlaybackInfo playbackInfo) {
        this.playbackInfo = playbackInfo;
    }

    /**
     * Acquires the playback manager for playback.
     *
     * @throws InvalidStateError when a stream is already active
     */
    public synchronized void acquire() {
        if (isAcquired) {
            throw new InvalidStateError("already streaming to device");
        }
        isAcquired = true;
    }

    /**
     * Sets up a session or returns the active one if it exists.
     *
     * @return the stream client of the session
     */
    public synchronized RaopStreamClient setup() {
        RaopStreamClient existing = streamClient;
        if (existing != null) {
            return existing;
        }

        HttpConnection newConnection = RaopFutures.await(HttpConnection.connect(address, service.port()));
        connection = newConnection;
        RtspSession newRtsp = new RtspSession(newConnection);
        rtsp = newRtsp;

        AirPlayMajorVersion protocolVersion = AirPlayUtils.getProtocolVersion(service,
                settings.protocols().raop().protocolVersion());
        LOGGER.debug("Using AirPlay version {}", protocolVersion);

        StreamProtocol streamProtocol = protocolVersion == AirPlayMajorVersion.AirPlayV1
                ? new AirPlayV1(context, newRtsp, timing)
                : new AirPlayV2(context, newRtsp, timing);

        RaopStreamClient client = new RaopStreamClient(newRtsp, context, streamProtocol, settings.protocols().raop(),
                timing, clock, rng);
        streamClient = client;
        return client;
    }

    /** Tears down and disconnects the current session. */
    public synchronized void teardown() {
        RaopStreamClient client = streamClient;
        if (client != null) {
            client.close();
        }
        HttpConnection currentConnection = connection;
        if (currentConnection != null) {
            currentConnection.close();
        }
        streamClient = null;
        context.reset(rng, clock);
        rtsp = null;
        connection = null;
        isAcquired = false;
    }
}
