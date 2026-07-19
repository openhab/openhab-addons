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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.auth.HapPairSetup;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;

/**
 * Support for HAP transient pairing over HTTP.
 *
 * <p>
 * Transient pairing only covers the first four states of regular pairing (M1-M4) with the
 * fixed PIN {@link HapPairSetup#TRANSIENT_PIN}; the SRP shared secret is then used to derive
 * session keys. It is implemented as a verification procedure — messages go to
 * {@code POST /pair-setup} with {@code X-Apple-HKP: 4}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayHapTransientPairVerifyProcedure implements AirPlayPairVerifyProcedure {

    private static final Map<String, String> AIRPLAY_HEADERS = transientHeaders();

    private final HttpConnection http;
    private final Supplier<HapPairSetup> srpFactory;

    private @Nullable HapPairSetup srp;

    /**
     * Creates a new procedure.
     *
     * @param http the HTTP connection to the device
     */
    public AirPlayHapTransientPairVerifyProcedure(HttpConnection http) {
        this(http, () -> new HapPairSetup(true));
    }

    /**
     * Creates a new procedure with an injectable pair-setup state machine, for
     * reproducible tests.
     *
     * @param http the HTTP connection to the device
     * @param srpFactory produces a fresh transient-mode state machine per
     *            {@link #verifyCredentials()}
     */
    public AirPlayHapTransientPairVerifyProcedure(HttpConnection http, Supplier<HapPairSetup> srpFactory) {
        this.http = http;
        this.srpFactory = srpFactory;
    }

    private static Map<String, String> transientHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "AirPlay/320.20");
        headers.put("Connection", "keep-alive");
        headers.put("X-Apple-HKP", "4");
        headers.put("Content-Type", "application/octet-stream");
        return headers;
    }

    @Override
    public CompletableFuture<Boolean> verifyCredentials() {
        HapPairSetup newSrp = srpFactory.get();
        this.srp = newSrp;
        return http.post("/pair-pin-start", AIRPLAY_HEADERS, null, false)
                .thenCompose(response -> http.post("/pair-setup", AIRPLAY_HEADERS, newSrp.step1(), false))
                .thenCompose(response -> http.post("/pair-setup", AIRPLAY_HEADERS,
                        newSrp.step2(response.bodyBytes(), HapPairSetup.TRANSIENT_PIN), false))
                .thenApply(response -> true);
    }

    /**
     * Returns derived encryption keys, computed directly from the SRP shared secret.
     */
    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        HapPairSetup currentSrp = srp;
        if (currentSrp == null) {
            throw new InvalidStateError("verification not started");
        }
        return currentSrp.encryptionKeys(salt, outputInfo, inputInfo);
    }
}
