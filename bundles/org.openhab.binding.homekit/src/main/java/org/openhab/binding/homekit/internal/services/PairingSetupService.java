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
package org.openhab.binding.homekit.internal.services;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.SrpClient;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.SessionKeys;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

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
public class PairingSetupService {

    private static final String ENDPOINT_PAIR_SETUP = "/pair-setup";
    private static final String CONTENT_TYPE_TLV8 = "application/pairing+tlv8";

    private final HttpTransport httpTransport;
    private final SrpClient srpClient;

    public PairingSetupService(HttpTransport httpTransport, String accessoryPairingCode) {
        this.httpTransport = httpTransport;
        this.srpClient = new SrpClient(accessoryPairingCode);
    }

    public SessionKeys pair(String baseUrl) throws Exception {
        // M1 — Start Pair-Setup
        Map<Integer, byte[]> tlv1 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                TlvType.METHOD.key, new byte[] { PairingMethod.SETUP.value });
        Validator.validate(PairingMethod.SETUP, tlv1);
        byte[] resp1 = httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv1));

        // M2 — Receive salt & accessory SRP public key
        Map<Integer, byte[]> tlv2 = Tlv8Codec.decode(resp1);
        Validator.validate(PairingMethod.SETUP, tlv2);
        byte[] salt = tlv2.get(TlvType.SALT.key);
        byte[] key = tlv2.get(TlvType.PUBLIC_KEY.key);
        if (salt == null || key == null) {
            throw new IllegalArgumentException("Missing salt public key TLV in M2 response");
        }
        srpClient.processChallenge(salt, key);

        // M3 — Send client SRP public key & proof
        Map<Integer, byte[]> tlv3 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M3.value }, //
                TlvType.PUBLIC_KEY.key, srpClient.getPublicKey(), //
                TlvType.PROOF.key, srpClient.getClientProof());
        Validator.validate(PairingMethod.SETUP, tlv3);
        byte[] resp3 = httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv3));

        // M4 — Verify accessory SRP proof
        Map<Integer, byte[]> tlv4 = Tlv8Codec.decode(resp3);
        Validator.validate(PairingMethod.SETUP, tlv4);
        byte[] proof = tlv4.get(TlvType.PROOF.key);
        if (proof == null) {
            throw new IllegalArgumentException("Missing proof TLV in M4 response");
        }
        srpClient.verifyServerProof(proof);

        // M5 — Exchange encrypted identifiers
        Map<Integer, byte[]> tlv5 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M5.value }, //
                TlvType.ENCRYPTED_DATA.key, srpClient.getEncryptedIdentifiers());
        Validator.validate(PairingMethod.SETUP, tlv5);
        byte[] resp5 = httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv5));

        // M6 — Final confirmation & accessory credentials
        Map<Integer, byte[]> tlv6 = Tlv8Codec.decode(resp5);
        Validator.validate(PairingMethod.SETUP, tlv6);
        byte[] data = tlv6.get(TlvType.ENCRYPTED_DATA.key);
        if (data == null) {
            throw new IllegalArgumentException("Missing data TLV in M6 response");
        }
        srpClient.verifyAccessoryIdentifiers(data);

        // Derive and return session keys
        return srpClient.deriveSessionKeys();
    }

    /**
     * Helper that validates the TLV map for the specification required pairing state.
     */
    protected static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.key, TlvType.METHOD.key), //
                PairingState.M2, Set.of(TlvType.STATE.key, TlvType.SALT.key, TlvType.PUBLIC_KEY.key), //
                PairingState.M3, Set.of(TlvType.STATE.key, TlvType.PUBLIC_KEY.key, TlvType.PROOF.key), //
                PairingState.M4, Set.of(TlvType.STATE.key, TlvType.PROOF.key), //
                PairingState.M5, Set.of(TlvType.STATE.key, TlvType.ENCRYPTED_DATA.key), //
                PairingState.M6, Set.of(TlvType.STATE.key, TlvType.ENCRYPTED_DATA.key));

        /**
         * Validates the TLV map for the specification required pairing state.
         *
         * @throws IllegalArgumentException if required keys are missing or state is invalid
         */
        public static void validate(PairingMethod method, Map<Integer, byte[]> tlv) throws IllegalArgumentException {
            if (tlv.containsKey(TlvType.ERROR.key)) {
                throw new IllegalArgumentException(
                        "Pairing method '%s' action failed with unknown error".formatted(method.name()));
            }

            byte[] stateBytes = tlv.get(TlvType.STATE.key);
            if (stateBytes == null || stateBytes.length != 1) {
                throw new IllegalArgumentException("Missing or invalid 'STATE' TLV (0x06)");
            }

            PairingState state = PairingState.from(stateBytes[0]);
            Set<Integer> expectedKeys = SPECIFICATION_REQUIRED_KEYS.get(state);

            if (expectedKeys == null) {
                throw new IllegalArgumentException(
                        "Pairing method '%s' unexpected state '%s'".formatted(method.name(), state.name()));
            }

            for (Integer key : expectedKeys) {
                if (!tlv.containsKey(key)) {
                    throw new IllegalArgumentException("Pairing method '%s' state '%s' required TLV '0x%02x' missing."
                            .formatted(method.name(), state.name(), key));
                }
            }
        }
    }
}
