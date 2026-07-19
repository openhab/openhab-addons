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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionLostError;
import org.openhab.binding.atv.internal.client.exceptions.PlaybackError;
import org.openhab.binding.atv.internal.client.protocols.raop.TimingServer;
import org.openhab.binding.atv.internal.client.support.http.HttpParser;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays media on a device by sending an URL.
 *
 * <p>
 * Sets up a local NTP timing server for the duration of the playback, retries the
 * {@code /play} request on internal server errors and then polls {@code /playback-info}
 * until the media has finished playing (the request must stay open during the entire play
 * duration).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayPlayer {

    /** Number of retries of the {@code /play} request ({@code PLAY_RETRIES}). */
    public static final int PLAY_RETRIES = 3;

    /** Poll attempts waiting for playback to start ({@code WAIT_RETRIES}). */
    public static final int WAIT_RETRIES = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayPlayer.class);

    private final RtspSession rtsp;
    private final AirPlayStreamProtocol streamProtocol;
    private final Duration retryInterval;
    private final Duration pollInterval;

    /**
     * Creates a player with one second retry and poll intervals.
     *
     * @param rtsp RTSP session on the connection to the receiver
     * @param streamProtocol the version-specific stream protocol
     */
    public AirPlayPlayer(RtspSession rtsp, AirPlayStreamProtocol streamProtocol) {
        this(rtsp, streamProtocol, Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    /**
     * Creates a player with injectable intervals, for fast tests.
     *
     * @param rtsp RTSP session on the connection to the receiver
     * @param streamProtocol the version-specific stream protocol
     * @param retryInterval delay between {@code /play} retries
     * @param pollInterval delay between {@code /playback-info} polls
     */
    public AirPlayPlayer(RtspSession rtsp, AirPlayStreamProtocol streamProtocol, Duration retryInterval,
            Duration pollInterval) {
        this.rtsp = rtsp;
        this.streamProtocol = streamProtocol;
        this.retryInterval = retryInterval;
        this.pollInterval = pollInterval;
    }

    /**
     * Plays media from an URL on the device. The returned future
     * does not complete until the media has finished playing.
     *
     * @param url URL to play
     * @param position start position in seconds
     * @return future completing when playback ended
     */
    public CompletableFuture<Void> playUrl(String url, double position) {
        return CompletableFuture.runAsync(() -> play(url, position),
                runnable -> Thread.ofVirtual().name("airplay-player").start(runnable));
    }

    private void play(String url, double position) {
        try (TimingServer server = createTimingServer()) {
            int retry = 0;
            while (retry < PLAY_RETRIES) {
                LOGGER.debug("Starting to play {}", url);

                HttpResponse resp = join(streamProtocol.playUrl(server.port(), url, position));

                // Sometimes AirPlay fails with "Internal Server Error", we apply a
                // "lets try again"-approach to that
                if (resp.code() == 500) {
                    retry += 1;
                    LOGGER.debug("Failed to stream {}, retry {} of {}", url, retry, PLAY_RETRIES);
                    sleep(retryInterval);
                    continue;
                }

                if (resp.code() >= 400 && resp.code() < 600) {
                    throw new AuthenticationError("status code: " + resp.code());
                }

                waitForMediaToEnd();
                return;
            }
            throw new PlaybackError("Max retries exceeded");
        } finally {
            streamProtocol.teardown();
        }
    }

    private TimingServer createTimingServer() {
        try {
            return new TimingServer(Clock.systemUTC(), InetAddress.getByName(rtsp.connection().localIp()));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start timing server", e);
        }
    }

    /**
     * Polls playback-info to find out if something is playing. It might take some time
     * until the media starts playing, give it {@link #WAIT_RETRIES} attempts.
     */
    private void waitForMediaToEnd() {
        int attempts = WAIT_RETRIES;
        boolean videoStarted;

        while (true) {
            HttpResponse resp;
            try {
                resp = join(rtsp.connection().get("/playback-info", false));
            } catch (ConnectionLostError | IllegalStateException e) {
                // In some cases this call will fail if video was stopped by the sender,
                // e.g. stopping video via remote control; handle gracefully
                LOGGER.debug("Connection was lost, assuming video playback stopped");
                break;
            }

            LOGGER.debug("Playback-info: {}", resp);

            Map<String, Object> parsed;
            if (resp.bodyBytes().length > 0) {
                parsed = HttpParser.decodeBplistFromBody(resp);
            } else {
                parsed = Map.of();
                LOGGER.debug("Got playback-info response without content");
            }

            // In case we got an error, abort with that here
            if (parsed.get("error") instanceof Map<?, ?> error) {
                Object code = error.get("code") != null ? error.get("code") : "unknown";
                Object domain = error.get("domain") != null ? error.get("domain") : "unknown domain";
                throw new PlaybackError("got error " + code + " (" + domain + ") when playing video");
            }

            // duration is only available if something is playing
            if (parsed.containsKey("duration")) {
                videoStarted = true;
                attempts = -1;
            } else {
                videoStarted = false;
                if (attempts >= 0) {
                    attempts -= 1;
                }
            }

            if (!videoStarted && attempts < 0) {
                LOGGER.debug("media playback ended");
                break;
            }

            sleep(pollInterval);
        }
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw e;
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlaybackError("interrupted while playing");
        }
    }
}
