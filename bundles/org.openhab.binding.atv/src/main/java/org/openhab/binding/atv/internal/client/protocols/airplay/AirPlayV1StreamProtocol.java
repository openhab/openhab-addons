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
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.support.BinaryPlist;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;

/**
 * AirPlay v1 {@code play_url} protocol logic.
 *
 * <p>
 * Pair-verify on the connection followed by {@code POST /play} with a binary plist body
 * ({@code Content-Location}, {@code Start-Position}, {@code X-Apple-Session-ID}) and the
 * {@code MediaControl/1.0} user agent. Audio streaming itself is handled by the RAOP
 * protocol implementation.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AirPlayV1StreamProtocol implements AirPlayStreamProtocol {

    private final HapCredentials credentials;
    private final RtspSession rtsp;

    /**
     * Creates a new v1 stream protocol.
     *
     * @param credentials credentials verified before playing
     * @param rtsp RTSP session on the connection to the receiver
     */
    public AirPlayV1StreamProtocol(HapCredentials credentials, RtspSession rtsp) {
        this.credentials = credentials;
        this.rtsp = rtsp;
    }

    @Override
    public CompletableFuture<HttpResponse> playUrl(int timingServerPort, String url, double position) {
        AirPlayPairVerifyProcedure verifier = AirPlayAuth.pairVerify(credentials, rtsp.connection());
        return verifier.verifyCredentials().thenCompose(v -> {
            // Keep the body in a sorted map, since the plist encoder expects sorted keys
            Map<String, Object> body = new TreeMap<>();
            body.put("Content-Location", url);
            body.put("Start-Position", position);
            body.put("X-Apple-Session-ID", UUID.randomUUID().toString());

            Map<String, Object> headers = new LinkedHashMap<>();
            headers.put("User-Agent", "MediaControl/1.0");
            headers.put("Content-Type", "application/x-apple-binary-plist");

            return rtsp.connection().post("/play", headers, BinaryPlist.dump(body), true);
        });
    }
}
