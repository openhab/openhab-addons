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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairSetup;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.CryptoKeys;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;

/**
 * Authenticates a device for AirPlay playback using HAP Pair-Setup over HTTP.
 *
 * <p>
 * The M1-M6 TLV8 state machine is the existing {@link HapPairSetup}; this class transports
 * the messages as {@code POST /pair-setup} requests with {@code Content-Type:
 * application/octet-stream} and {@code X-Apple-HKP: 3} (preceded by {@code POST
 * /pair-pin-start} to make the device show its PIN).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayHapPairSetupProcedure implements AirPlayPairSetupProcedure {

    private static final Map<String, String> AIRPLAY_HEADERS = airplayHeaders();

    private final HttpConnection http;
    private final Supplier<HapPairSetup> srpFactory;

    private @Nullable HapPairSetup srp;
    private byte @Nullable [] m2Tlv;

    /**
     * Creates a new procedure.
     *
     * @param http the HTTP connection to the device
     * @param name display name included in the M5 message, or {@code null}
     */
    public AirPlayHapPairSetupProcedure(HttpConnection http, @Nullable String name) {
        this(http, () -> new HapPairSetup(false, name, CryptoKeys.ed25519Generate().seed(),
                UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Creates a new procedure with an injectable pair-setup state machine, for
     * reproducible tests.
     *
     * @param http the HTTP connection to the device
     * @param srpFactory produces a fresh state machine per {@link #startPairing()}
     */
    public AirPlayHapPairSetupProcedure(HttpConnection http, Supplier<HapPairSetup> srpFactory) {
        this.http = http;
        this.srpFactory = srpFactory;
    }

    static Map<String, String> airplayHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "AirPlay/320.20");
        headers.put("Connection", "keep-alive");
        headers.put("X-Apple-HKP", "3");
        headers.put("Content-Type", "application/octet-stream");
        return headers;
    }

    @Override
    public CompletableFuture<Void> startPairing() {
        HapPairSetup newSrp = srpFactory.get();
        this.srp = newSrp;
        return http.post("/pair-pin-start", AIRPLAY_HEADERS, null, false)
                .thenCompose(response -> http.post("/pair-setup", AIRPLAY_HEADERS, newSrp.step1(), false))
                .thenAccept(response -> m2Tlv = response.bodyBytes());
    }

    @Override
    public CompletableFuture<HapCredentials> finishPairing(String username, String pinCode) {
        HapPairSetup currentSrp = srp;
        byte[] currentM2Tlv = m2Tlv;
        if (currentSrp == null || currentM2Tlv == null) {
            return CompletableFuture.failedFuture(new InvalidStateError("pairing was not started"));
        }
        try {
            return http.post("/pair-setup", AIRPLAY_HEADERS, currentSrp.step2(currentM2Tlv, pinCode), false)
                    .thenCompose(response -> http.post("/pair-setup", AIRPLAY_HEADERS,
                            currentSrp.step3(response.bodyBytes()), false))
                    .thenApply(response -> currentSrp.step4(response.bodyBytes()));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
