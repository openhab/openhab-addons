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
package org.openhab.binding.homekit.internal.hap_services;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.SessionKeys;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

/**
 * Service to remove an existing pairing with a HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairingRemoveService {

    private static final String CONTENT_TYPE = "application/pairing+tlv8";
    private static final String ENDPOINT = "/pairings";
    private static final byte[] NONCE_M5 = CryptoUtils.generateNonce("PV-Msg05");
    private static final byte[] NONCE_M6 = CryptoUtils.generateNonce("PV-Msg06");

    private final HttpTransport httpTransport;
    private final String baseUrl;
    private final SessionKeys sessionKeys;
    private final String controllerIdentifier;

    public PairingRemoveService(HttpTransport httpTransport, String baseUrl, SessionKeys sessionKeys,
            String controllerIdentifier) {
        this.httpTransport = httpTransport;
        this.baseUrl = baseUrl;
        this.sessionKeys = sessionKeys;
        this.controllerIdentifier = controllerIdentifier;
    }

    public void remove() throws Exception {
        // M1 Construct TLV payload for RemovePairing
        Map<Integer, byte[]> tlv1 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                TlvType.METHOD.key, new byte[] { PairingMethod.REMOVE.value }, //
                TlvType.IDENTIFIER.key, controllerIdentifier.getBytes(StandardCharsets.UTF_8));
        Validator.validate(PairingMethod.REMOVE, tlv1);
        byte[] encoded = Tlv8Codec.encode(tlv1);

        // Encrypt payload using write key
        byte[] encrypted = CryptoUtils.encrypt(sessionKeys.getWriteKey(), NONCE_M5, encoded);

        // Send to /pairings endpoint
        byte[] response = httpTransport.post(baseUrl, ENDPOINT, CONTENT_TYPE, encrypted);

        // M2 Decrypt response using read key
        byte[] decrypted = CryptoUtils.decrypt(sessionKeys.getReadKey(), NONCE_M6, response);
        Map<Integer, byte[]> tlv2 = Tlv8Codec.decode(decrypted);
        Validator.validate(PairingMethod.REMOVE, tlv2);
    }

    /**
     * Helper that validates the TLV map for the specification required pairing state.
     */
    protected static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.key, TlvType.METHOD.key, TlvType.IDENTIFIER.key), //
                PairingState.M2, Set.of(TlvType.STATE.key));

        /**
         * Validates the TLV map for the specification required pairing state.
         *
         * @throws SecurityException if required keys are missing or state is invalid
         */
        public static void validate(PairingMethod method, Map<Integer, byte[]> tlv) throws SecurityException {
            if (tlv.containsKey(TlvType.ERROR.key)) {
                throw new SecurityException(
                        "Pairing method '%s' action failed with unknown error".formatted(method.name()));
            }

            byte[] stateBytes = tlv.get(TlvType.STATE.key);
            if (stateBytes == null || stateBytes.length != 1) {
                throw new SecurityException("Missing or invalid 'STATE' TLV (0x06)");
            }

            PairingState state = PairingState.from(stateBytes[0]);
            Set<Integer> expectedKeys = SPECIFICATION_REQUIRED_KEYS.get(state);

            if (expectedKeys == null) {
                throw new SecurityException(
                        "Pairing method '%s' unexpected state '%s'".formatted(method.name(), state.name()));
            }

            for (Integer key : expectedKeys) {
                if (!tlv.containsKey(key)) {
                    throw new SecurityException("Pairing method '%s' state '%s' required TLV '0x%02x' missing."
                            .formatted(method.name(), state.name(), key));
                }
            }
        }
    }
}
