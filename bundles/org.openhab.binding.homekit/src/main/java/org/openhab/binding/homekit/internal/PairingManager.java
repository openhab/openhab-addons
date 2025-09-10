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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

/**
 * Handles the 6-step pairing process with a HomeKit accessory.
 * Uses SRP for secure key exchange and derives session keys.
 * Communicates with the accessory using HTTP and TLV8 encoding.
 * Requires the accessory's setup code for pairing.
 * Returns session keys upon successful pairing.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class PairingManager {

    private final SRPClient srpClient;
    private final HttpClient httpClient;

    public PairingManager(HttpClient httpClient, String setupCode) {
        this.httpClient = httpClient;
        this.srpClient = new SRPClient(setupCode);
    }

    /**
     * Initiates the pairing process with the accessory at the given address.
     *
     * @param baseUrl the base URL of the accessory (e.g., "http://123.123.123.123:port")
     */
    public SessionKeys pair(String baseUrl) throws Exception {
        // Step 1: M1 — Start Pairing
        byte[] m1 = TLV8Codec.encode(Map.of(0x00, new byte[] { 0x00 }, 0x01, new byte[] { 0x01 }));
        byte[] resp1 = post(baseUrl + "/pair-setup", m1);

        // Step 2: M2 — Receive SRP salt and public key
        Map<Integer, byte[]> tlv2 = TLV8Codec.decode(resp1);
        srpClient.processChallenge(tlv2.get(0x03), tlv2.get(0x04)); // salt, server public key

        // Step 3: M3 — Send SRP public key and proof
        Map<Integer, byte[]> m3 = srpClient.generateClientProof();
        byte[] resp3 = post(baseUrl + "/pair-setup", TLV8Codec.encode(m3));

        // Step 4: M4 — Verify server proof
        Map<Integer, byte[]> tlv4 = TLV8Codec.decode(resp3);
        srpClient.verifyServerProof(tlv4.get(0x04));

        // Step 5: M5 — Exchange encrypted identifiers
        Map<Integer, byte[]> m5 = srpClient.generateEncryptedIdentifiers();
        byte[] resp5 = post(baseUrl + "/pair-setup", TLV8Codec.encode(m5));

        // Step 6: M6 — Final confirmation
        Map<Integer, byte[]> tlv6 = TLV8Codec.decode(resp5);
        srpClient.verifyAccessoryIdentifiers(tlv6);

        // Derive session keys
        return srpClient.deriveSessionKeys();
    }

    /**
     * Sends a POST request with the given payload to the specified URL.
     *
     * @param url the target URL
     * @param payload the request body
     * @return the response body
     * @throws Exception if an error occurs during the request
     */
    private byte[] post(String url, byte[] payload) throws Exception {
        Request request = httpClient.newRequest(url) //
                .timeout(5, TimeUnit.SECONDS) //
                .method(HttpMethod.POST) //
                .header(HttpHeader.CONTENT_TYPE, "application/pairing+tlv8") //
                .header(HttpHeader.ACCEPT, "application/json") //
                .content(new BytesContentProvider(payload));
        ContentResponse response = request.send();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Pairing failed: HTTP " + response.getStatus());
        }
        return response.getContent();
    }
}
