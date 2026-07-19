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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapChannel;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionFailedError;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.http.HttpParser;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AirPlay v2 {@code play_url} protocol logic.
 *
 * <p>
 * Handles base setup (pair-verify with transparent control channel encryption, RTSP SETUP
 * announcing an NTP timing server, event channel with connect retries), a best-effort
 * feedback loop, RECORD, and finally {@code POST /play} followed by the property/rate
 * commands (most importantly {@code /rate?value=1.000000}, since playback otherwise starts
 * paused). Audio streaming itself is handled by the RAOP protocol implementation.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayV2StreamProtocol implements AirPlayStreamProtocol {

    /**
     * Time between feedback messages ({@code FEEDBACK_INTERVAL}).
     */
    public static final Duration FEEDBACK_INTERVAL = Duration.ofSeconds(2);

    private static final Logger LOGGER = LoggerFactory.getLogger(AirPlayV2StreamProtocol.class);
    private static final int EVENT_CHANNEL_RETRIES = 5;

    private final HapCredentials credentials;
    private final RtspSession rtsp;
    private final ScheduledExecutorService scheduler;
    private final String sessionUuid = UUID.randomUUID().toString().toLowerCase(Locale.ROOT);

    private volatile @Nullable HapChannel eventChannel;
    private volatile @Nullable ScheduledFuture<?> feedbackTask;

    /**
     * Creates a new v2 stream protocol.
     *
     * @param credentials credentials verified before playing
     * @param rtsp RTSP session on the connection to the receiver
     * @param scheduler scheduler used for the feedback loop
     */
    public AirPlayV2StreamProtocol(HapCredentials credentials, RtspSession rtsp, ScheduledExecutorService scheduler) {
        this.credentials = credentials;
        this.rtsp = rtsp;
        this.scheduler = scheduler;
    }

    @Override
    public CompletableFuture<HttpResponse> playUrl(int timingServerPort, String url, double position) {
        return setupBase(timingServerPort).thenCompose(v -> {
            startFeedback();
            return rtsp.record(null, null);
        }).thenCompose(v -> {
            // Most fields below are not strictly necessary, but are kept for compatibility
            Map<String, Object> body = new TreeMap<>();
            body.put("Content-Location", url);
            body.put("Start-Position-Seconds", position);
            body.put("uuid", UUID.randomUUID().toString());
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

            Map<String, Object> headers = new LinkedHashMap<>();
            headers.put("User-Agent", "AirPlay/550.10");
            headers.put("Content-Type", "application/x-apple-binary-plist");
            headers.put("X-Apple-ProtocolVersion", "1");
            headers.put("X-Apple-Session-ID", sessionUuid);
            headers.put("X-Apple-Stream-ID", "1");

            return rtsp.connection().post("/play", headers, BinaryPlist.dump(body), true);
        }).thenCompose(resp ->
        // Various commands, most of which are probably not needed. Most important
        // command is "/rate" as that sets playback rate to 100% (will start paused
        // otherwise).
        rtsp.exchange("PUT", "/setProperty?isInterestedInDateRange", null, null, Map.of("value", true), false,
                "RTSP/1.0")
                .thenCompose(r -> rtsp.exchange("PUT", "/setProperty?actionAtItemEnd", null, null, Map.of("value", 0),
                        false, "RTSP/1.0"))
                .thenCompose(r -> rtsp.exchange("POST", "/rate?value=1.000000", null, null, null, false, "RTSP/1.0"))
                .thenCompose(r -> rtsp.exchange("PUT", "/setProperty?forwardEndTime", null, null,
                        Map.of("value", timeProperty()), false, "RTSP/1.0"))
                .thenCompose(r -> rtsp.exchange("PUT", "/setProperty?reverseEndTime", null, null,
                        Map.of("value", timeProperty()), false, "RTSP/1.0"))
                .thenApply(r -> resp));
    }

    /** Starts sending best-effort feedback every {@link #FEEDBACK_INTERVAL}. */
    private void startFeedback() {
        if (feedbackTask == null) {
            LOGGER.debug("Starting feedback task");
            feedbackTask = scheduler.scheduleWithFixedDelay(() -> rtsp.feedback(true).whenComplete((resp, ex) -> {
                if (ex != null) {
                    // Treat feedback as "best effort" and don't raise any errors
                    LOGGER.debug("Feedback failed: {}", ex.toString());
                }
            }), FEEDBACK_INTERVAL.toMillis(), FEEDBACK_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void teardown() {
        ScheduledFuture<?> task = feedbackTask;
        if (task != null) {
            task.cancel(false);
            feedbackTask = null;
        }
        HapChannel channel = eventChannel;
        if (channel != null) {
            channel.close();
            eventChannel = null;
        }
    }

    private static Map<String, Object> timeProperty() {
        Map<String, Object> value = new TreeMap<>();
        value.put("flags", 0);
        value.put("value", 0);
        value.put("epoch", 0);
        value.put("timescale", 0);
        return value;
    }

    private CompletableFuture<Void> setupBase(int timingServerPort) {
        return AirPlayAuth.verifyConnection(credentials, rtsp.connection()).thenCompose(verifier -> {
            Map<String, Object> body = new TreeMap<>();
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

            return rtsp.setup(null, body).thenCompose(setupResp -> {
                Map<String, Object> resp = HttpParser.decodeBplistFromBody(setupResp);
                LOGGER.debug("Setup response body: {}", resp);
                int eventPort = resp.get("eventPort") instanceof Number port ? port.intValue() : 0;
                return connectEventChannel(verifier, eventPort, EVENT_CHANNEL_RETRIES);
            });
        });
    }

    /**
     * Connects the event channel with retries: some receivers (airplay2-receiver) set up
     * the event channel some time after responding with the port used for it, so retry a
     * few times for compatibility.
     */
    private CompletableFuture<Void> connectEventChannel(AirPlayPairVerifyProcedure verifier, int eventPort,
            int retries) {
        return HapChannel
                .setupChannel(EventChannel::new, verifier, rtsp.connection().remoteIp(), eventPort,
                        Ap2Session.EVENTS_SALT, Ap2Session.EVENTS_READ_INFO, Ap2Session.EVENTS_WRITE_INFO)
                .<CompletableFuture<Void>> handle((channel, error) -> {
                    if (error == null) {
                        this.eventChannel = channel;
                        return CompletableFuture.completedFuture(null);
                    }
                    Throwable directCause = error.getCause();
                    Throwable cause = error instanceof CompletionException && directCause != null ? directCause : error;
                    if (cause instanceof ConnectionFailedError && retries > 1) {
                        LOGGER.debug("Connect failed, retrying");
                        CompletableFuture<Void> retry = new CompletableFuture<>();
                        scheduler
                                .schedule(
                                        () -> connectEventChannel(verifier, eventPort, retries - 1)
                                                .whenComplete((v, err) -> completeWith(retry, v, err)),
                                        1, TimeUnit.SECONDS);
                        return retry;
                    }
                    return CompletableFuture.<Void> failedFuture(cause);
                }).thenCompose(future -> future);
    }

    private static void completeWith(CompletableFuture<Void> target, Void value, Throwable error) {
        if (error != null) {
            target.completeExceptionally(error);
        } else {
            target.complete(value);
        }
    }
}
