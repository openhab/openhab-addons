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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;

/**
 * Verifies that a device is allowed to perform AirPlay playback using legacy pairing.
 *
 * <p>
 * Two {@code POST /pair-verify} requests with raw binary bodies ({@code Content-Type:
 * application/octet-stream}) — see {@link LegacySrpAuthHandler#verify1()} and
 * {@link LegacySrpAuthHandler#verify2(byte[], byte[])} for the body layouts. Legacy
 * verification derives no session encryption keys, so {@link #verifyCredentials()} completes
 * with {@code false}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayLegacyPairVerifyProcedure implements AirPlayPairVerifyProcedure {

    private static final Map<String, String> AIRPLAY_HEADERS = verifyHeaders();

    private final HttpConnection http;
    private final LegacySrpAuthHandler srp;

    /**
     * Creates a new procedure.
     *
     * @param http the HTTP connection to the device
     * @param srp initialized legacy SRP handler carrying the credentials to verify
     */
    public AirPlayLegacyPairVerifyProcedure(HttpConnection http, LegacySrpAuthHandler srp) {
        this.http = http;
        this.srp = srp;
    }

    private static Map<String, String> verifyHeaders() {
        Map<String, String> headers = AirPlayLegacyPairSetupProcedure.legacyHeaders();
        headers.put("Content-Type", "application/octet-stream");
        return new LinkedHashMap<>(headers);
    }

    @Override
    public CompletableFuture<Boolean> verifyCredentials() {
        try {
            return send(srp.verify1()).thenCompose(response -> {
                byte[] body = response.bodyBytes();
                byte[] atvPublicSecret = Arrays.copyOfRange(body, 0, 32);
                byte[] data = Arrays.copyOfRange(body, 32, body.length); // purpose unknown
                return send(srp.verify2(atvPublicSecret, data));
            }).thenApply(response -> false);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<HttpResponse> send(byte[] data) {
        return http.post("/pair-verify", AIRPLAY_HEADERS, data, false);
    }

    /**
     * Not supported by legacy authentication.
     *
     * @throws NotSupportedError always
     */
    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        throw new NotSupportedError("encryption keys not supported by legacy auth");
    }
}
