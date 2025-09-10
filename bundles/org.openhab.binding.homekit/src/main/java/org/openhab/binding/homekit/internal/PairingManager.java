/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class PairingManager {

    private final SRPClient srpClient;
    private final HttpClient httpClient;

    public PairingManager(HttpClient httpClient, String setupCode) {
        this.httpClient = httpClient;
        this.srpClient = new SRPClient(setupCode);
    }

    public SessionKeys pair(String accessoryAddress) throws Exception {
        // Step 1: M1 — Start Pairing
        byte[] m1 = TLV8Codec.encode(Map.of(0x00, new byte[] { 0x00 }, 0x01, new byte[] { 0x01 }));
        byte[] resp1 = post(accessoryAddress + "/pair-setup", m1);

        // Step 2: M2 — Receive SRP salt and public key
        Map<Integer, byte[]> tlv2 = TLV8Codec.decode(resp1);
        srpClient.processChallenge(tlv2.get(0x03), tlv2.get(0x04)); // salt, server public key

        // Step 3: M3 — Send SRP public key and proof
        Map<Integer, byte[]> m3 = srpClient.generateClientProof();
        byte[] resp3 = post(accessoryAddress + "/pair-setup", TLV8Codec.encode(m3));

        // Step 4: M4 — Verify server proof
        Map<Integer, byte[]> tlv4 = TLV8Codec.decode(resp3);
        srpClient.verifyServerProof(tlv4.get(0x04));

        // Step 5: M5 — Exchange encrypted identifiers
        Map<Integer, byte[]> m5 = srpClient.generateEncryptedIdentifiers();
        byte[] resp5 = post(accessoryAddress + "/pair-setup", TLV8Codec.encode(m5));

        // Step 6: M6 — Final confirmation
        Map<Integer, byte[]> tlv6 = TLV8Codec.decode(resp5);
        srpClient.verifyAccessoryIdentifiers(tlv6);

        // Derive session keys
        return srpClient.deriveSessionKeys();
    }

    private byte[] post(String url, byte[] payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/pairing+tlv8").header("Accept", "application/pairing+tlv8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload)).build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Pairing failed: HTTP " + response.statusCode());
        }

        return response.body();
    }
}
