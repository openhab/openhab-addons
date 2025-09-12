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
package org.openhab.binding.homekit.internal.network;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Handles the 6-step pairing process with a HomeKit accessory.
 * Uses SRP for secure key exchange and derives session keys.
 * Communicates with the accessory using HTTP and TLV8 encoding.
 * Requires the accessory's setup code for pairing.
 * Returns session keys upon successful pairing.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairingManager {

    private final SRPClient srpClient;
    private final HttpTransport httpTransport;

    public PairingManager(HttpTransport httpTransport, String pairingCode) {
        this.httpTransport = httpTransport;
        this.srpClient = new SRPClient(pairingCode);
    }

    /**
     * Initiates the pairing process with the accessory at the given address.
     *
     * @param baseUrl the base URL of the accessory (e.g., "http://123.123.123.123:port")
     */
    public SessionKeys pair(String baseUrl) throws Exception {
        // Step 1: M1 — Start Pairing
        byte[] m1 = TLV8Codec.encode(Map.of(0x00, new byte[] { 0x00 }, 0x01, new byte[] { 0x01 }));
        byte[] resp1 = httpTransport.post(baseUrl, ENDPOINT_PAIRING, CONTENT_TYPE_PAIRING, m1);

        // Step 2: M2 — Receive SRP salt and public key
        Map<Integer, byte[]> tlv2 = TLV8Codec.decode(resp1);
        srpClient.processChallenge(Objects.requireNonNull(tlv2.get(0x03)), Objects.requireNonNull(tlv2.get(0x04)));

        // Step 3: M3 — Send SRP public key and proof
        Map<Integer, byte[]> m3 = srpClient.generateClientProof();
        byte[] resp3 = httpTransport.post(baseUrl, ENDPOINT_PAIRING, CONTENT_TYPE_PAIRING, TLV8Codec.encode(m3));

        // Step 4: M4 — Verify server proof
        Map<Integer, byte[]> tlv4 = TLV8Codec.decode(resp3);
        srpClient.verifyServerProof(Objects.requireNonNull(tlv4.get(0x04)));

        // Step 5: M5 — Exchange encrypted identifiers
        Map<Integer, byte[]> m5 = srpClient.generateEncryptedIdentifiers();
        byte[] resp5 = httpTransport.post(baseUrl, ENDPOINT_PAIRING, CONTENT_TYPE_PAIRING, TLV8Codec.encode(m5));

        // Step 6: M6 — Final confirmation
        Map<Integer, byte[]> tlv6 = TLV8Codec.decode(resp5);
        srpClient.verifyAccessoryIdentifiers(tlv6);

        // Derive session keys
        return srpClient.deriveSessionKeys();
    }

    public void removePairing(String baseUrl, String pairingIdentifier, SecureSession secureSession) throws Exception {
        // Step 1: Construct TLV for remove pairing (State = 0x01, Method = 0x04)
        Map<Integer, byte[]> tlv = new HashMap<>();
        tlv.put(0x00, new byte[] { 0x01 }); // State
        tlv.put(0x01, new byte[] { 0x04 }); // Method: Remove pairing
        tlv.put(0x03, pairingIdentifier.getBytes(StandardCharsets.UTF_8)); // Identifier to remove

        byte[] plaintext = TLV8Codec.encode(tlv);

        // Step 2: Encrypt with session keys
        byte[] encrypted = secureSession.encrypt(plaintext);

        // Step 3: Send remove pairing request
        byte[] response = httpTransport.post(baseUrl, ENDPOINT_PAIRING, CONTENT_TYPE_PAIRING, encrypted);

        // Step 4: Decrypt and verify response
        byte[] decrypted = secureSession.decrypt(response);
        Map<Integer, byte[]> tlvResp = TLV8Codec.decode(decrypted);

        if (Objects.requireNonNull(tlvResp.get(0x00))[0] != 0x02) {
            throw new IllegalStateException("Unexpected response state during pairing removal");
        }
    }
}
