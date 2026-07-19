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

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.http.HttpConnection;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;

/**
 * Authenticates a device for AirPlay playback using legacy pairing.
 *
 * <p>
 * {@code POST /pair-pin-start} makes the device show its PIN, then three
 * {@code POST /pair-setup-pin} steps with binary-plist bodies ({@code method}/{@code user},
 * {@code pk}/{@code proof} and {@code epk}/{@code authTag}) drive the SRP 2048/SHA-1 exchange
 * in {@link LegacySrpAuthHandler}.
 *
 * <p>
 * Plist dictionaries are serialized with sorted keys, matching the device's expected
 * encoding. The {@code /pair-setup-pin} requests carry no {@code Content-Type} header.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayLegacyPairSetupProcedure implements AirPlayPairSetupProcedure {

    private static final Map<String, String> AIRPLAY_HEADERS = legacyHeaders();

    private final HttpConnection http;
    private final LegacySrpAuthHandler srp;

    /**
     * Creates a new procedure.
     *
     * @param http the HTTP connection to the device
     * @param srp initialized legacy SRP handler carrying the credentials to establish
     */
    public AirPlayLegacyPairSetupProcedure(HttpConnection http, LegacySrpAuthHandler srp) {
        this.http = http;
        this.srp = srp;
    }

    static Map<String, String> legacyHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "AirPlay/320.20");
        headers.put("Connection", "keep-alive");
        return headers;
    }

    /** The SRP handler driving this procedure (exposed for tests). */
    LegacySrpAuthHandler srp() {
        return srp;
    }

    @Override
    public CompletableFuture<Void> startPairing() {
        return http.post("/pair-pin-start", AIRPLAY_HEADERS, null, false).thenAccept(response -> {
        });
    }

    @Override
    public CompletableFuture<HapCredentials> finishPairing(String username, String pinCode) {
        try {
            // Step 1
            String clientId = HexFormat.of().withUpperCase().formatHex(srp.credentials().clientId());
            srp.step1(clientId, pinCode);

            Map<String, Object> step1 = new TreeMap<>();
            step1.put("method", "pin");
            step1.put("user", clientId);
            return sendPlist(step1).thenCompose(response -> {
                Object body = AirPlayUtils.decodePlistBody(response.bodyBytes());
                if (!(body instanceof Map<?, ?> map)) {
                    throw new ProtocolError(
                            "expected dict, got " + (body == null ? "null" : body.getClass().getSimpleName()));
                }

                // Step 2
                byte[] pk = (byte[]) Objects.requireNonNull(map.get("pk"), "missing pk");
                byte[] salt = (byte[]) Objects.requireNonNull(map.get("salt"), "missing salt");
                LegacySrpAuthHandler.KeyProof keyProof = srp.step2(pk, salt);
                Map<String, Object> step2 = new TreeMap<>();
                step2.put("pk", keyProof.publicKey());
                step2.put("proof", keyProof.proof());
                return sendPlist(step2);
            }).thenCompose(response -> {
                // Step 3
                LegacySrpAuthHandler.Epk epk = srp.step3();
                Map<String, Object> step3 = new TreeMap<>();
                step3.put("epk", epk.epk());
                step3.put("authTag", epk.tag());
                return sendPlist(step3);
            }).thenApply(response -> srp.credentials());
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<HttpResponse> sendPlist(Map<String, Object> plist) {
        // No headers are passed here; the Content-Type header is unused by the device
        return http.post("/pair-setup-pin", null, BinaryPlist.dump(plist), false);
    }
}
