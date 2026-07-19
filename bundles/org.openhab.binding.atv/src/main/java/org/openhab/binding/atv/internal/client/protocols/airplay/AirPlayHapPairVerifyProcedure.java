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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairVerify;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;

/**
 * Verifies that a device is allowed to perform AirPlay playback using HAP Pair-Verify over
 * HTTP.
 *
 * <p>
 * The M1/M3 TLV8 state machine is the existing {@link HapPairVerify}; this class transports
 * the messages as {@code POST /pair-verify} requests with {@code Content-Type:
 * application/octet-stream} and {@code X-Apple-HKP: 3}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayHapPairVerifyProcedure implements AirPlayPairVerifyProcedure {

    private static final Map<String, String> AIRPLAY_HEADERS = AirPlayHapPairSetupProcedure.airplayHeaders();

    private final HttpConnection http;
    private final HapCredentials credentials;
    private final Function<HapCredentials, HapPairVerify> verifierFactory;

    private @Nullable HapPairVerify verifier;

    /**
     * Creates a new procedure.
     *
     * @param http the HTTP connection to the device
     * @param credentials the stored HAP credentials to verify
     */
    public AirPlayHapPairVerifyProcedure(HttpConnection http, HapCredentials credentials) {
        this(http, credentials, HapPairVerify::new);
    }

    /**
     * Creates a new procedure with an injectable verify state machine, for reproducible
     * tests.
     *
     * @param http the HTTP connection to the device
     * @param credentials the stored HAP credentials to verify
     * @param verifierFactory produces a fresh state machine per {@link #verifyCredentials()}
     */
    public AirPlayHapPairVerifyProcedure(HttpConnection http, HapCredentials credentials,
            Function<HapCredentials, HapPairVerify> verifierFactory) {
        this.http = http;
        this.credentials = credentials;
        this.verifierFactory = verifierFactory;
    }

    @Override
    public CompletableFuture<Boolean> verifyCredentials() {
        HapPairVerify newVerifier = verifierFactory.apply(credentials);
        this.verifier = newVerifier;
        return http
                .post("/pair-verify", AIRPLAY_HEADERS, newVerifier.verify1(), false).thenCompose(response -> http
                        .post("/pair-verify", AIRPLAY_HEADERS, newVerifier.verify2(response.bodyBytes()), false))
                .thenApply(response -> true);
    }

    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        HapPairVerify currentVerifier = verifier;
        if (currentVerifier == null) {
            throw new InvalidStateError("verification not started");
        }
        return currentVerifier.encryptionKeys(salt, outputInfo, inputInfo);
    }
}
