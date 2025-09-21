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
import java.util.Objects;
import java.util.Set;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.SRPclient;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.ErrorCode;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
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
public class PairSetupClient {

    private static final String ENDPOINT_PAIR_SETUP = "/pair-setup";

    private static final String CONTENT_TYPE_TLV8 = "application/pairing+tlv8";
    private final HttpTransport httpTransport;
    private final String baseUrl;
    private final String password;
    private final byte[] pairingId;
    private final Ed25519PrivateKeyParameters clientLongTermPrivateKey;

    public PairSetupClient(HttpTransport httpTransport, String baseUrl, String pairingId,
            Ed25519PrivateKeyParameters clientLongTermPrivateKey, String password) throws Exception {
        this.httpTransport = httpTransport;
        this.baseUrl = baseUrl;
        this.password = password;
        this.pairingId = pairingId.getBytes(StandardCharsets.UTF_8);
        this.clientLongTermPrivateKey = clientLongTermPrivateKey;
    }

    /**
     * Executes the 6-step pairing process with the accessory.
     *
     * @return SessionKeys containing the derived session keys
     * @throws Exception if any step of the pairing process fails
     */
    public Ed25519PublicKeyParameters pair() throws Exception {
        SRPclient client = doStepM1();
        return client.getAccessoryLongTermPublicKey();
    }

    /**
     * Executes step M1 of the pairing process: Start Pair-Setup.
     *
     * @return byte array containing the response from the accessory
     * @throws InterruptedException if the operation is interrupted
     * @throws Exception if an error occurs during execution
     */
    private SRPclient doStepM1() throws Exception {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                TlvType.METHOD.key, new byte[] { PairingMethod.SETUP.value });
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] response1 = httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv));
        return doStepM2(response1);
    }

    /**
     * Executes step M2 of the pairing process: Receive salt & accessory SRP public key.
     * And initializes the SRP client with the received parameters.
     *
     * @param response1 byte array containing the response from step M1
     * @throws Exception if an error occurs during processing
     */
    private SRPclient doStepM2(byte[] response1) throws Exception {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response1);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] serverSalt = tlv.get(TlvType.SALT.key);
        byte[] serverPublicKey = tlv.get(TlvType.PUBLIC_KEY.key);
        SRPclient client = new SRPclient(password, Objects.requireNonNull(serverSalt),
                Objects.requireNonNull(serverPublicKey));
        return doStepM3(client);
    }

    /**
     * Executes step M3 of the pairing process: Send client SRP public key & proof.
     *
     * @return byte array containing the response from the accessory
     * @throws Exception if an error occurs during processing
     */
    private SRPclient doStepM3(SRPclient client) throws Exception {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M3.value }, //
                TlvType.PUBLIC_KEY.key, client.getPublicKey(), //
                TlvType.PROOF.key, client.getClientProof());
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] response3 = httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv));
        return doStepM4(client, response3);
    }

    /**
     * Executes step M4 of the pairing process: Verify accessory SRP proof.
     *
     * @param response3 byte array containing the response from step M3
     * @throws Exception if an error occurs during processing
     */
    private SRPclient doStepM4(SRPclient client, byte[] response3) throws Exception {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response3);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] proof = tlv.get(TlvType.PROOF.key);
        client.verifyServerProof(Objects.requireNonNull(proof));
        return doStepM5(client);
    }

    /**
     * Executes step M5 of the pairing process: Exchange encrypted identifiers.
     *
     * @return byte array containing the response from the accessory
     * @throws Exception if an error occurs during processing
     */
    private SRPclient doStepM5(SRPclient client) throws Exception {
        byte[] cipherText = client.createEncryptedControllerInfo(pairingId, clientLongTermPrivateKey);
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M5.value }, //
                TlvType.ENCRYPTED_DATA.key, cipherText);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] response5 = httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv));
        return doStepM6(client, response5);
    }

    /**
     * Executes step M6 of the pairing process: Final confirmation & accessory credentials.
     * Derives and returns the session keys.
     *
     * @param response5 byte array containing the response from step M5
     * @throws Exception if an error occurs during processing
     */
    private SRPclient doStepM6(SRPclient client, byte[] response5) throws Exception {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response5);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] ciphertext = tlv.get(TlvType.ENCRYPTED_DATA.key);
        client.verifyEncryptedAccessoryInfo(Objects.requireNonNull(ciphertext));
        return client;
    }

    /**
     * Helper class that validates the TLV map for the specification required pairing state.
     */
    public static class Validator {

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
         * @throws SecurityException if required keys are missing or state is invalid
         */
        public static void validate(PairingMethod method, Map<Integer, byte[]> tlv) throws SecurityException {
            if (tlv.containsKey(TlvType.ERROR.key)) {
                byte[] err = tlv.get(TlvType.ERROR.key);
                ErrorCode code = err != null && err.length > 0 ? ErrorCode.from(err[0]) : ErrorCode.UNKNOWN;
                throw new SecurityException(
                        "Pairing method '%s' action failed with error '%s'".formatted(method.name(), code.name()));
            }

            byte[] state = tlv.get(TlvType.STATE.key);
            if (state == null || state.length != 1) {
                throw new SecurityException("Missing or invalid 'STATE' TLV (0x06)");
            }

            PairingState pairingState = PairingState.from(state[0]);
            Set<Integer> expectedKeys = SPECIFICATION_REQUIRED_KEYS.get(pairingState);

            if (expectedKeys == null) {
                throw new SecurityException(
                        "Pairing method '%s' unexpected state '%s'".formatted(method.name(), pairingState.name()));
            }

            for (Integer key : expectedKeys) {
                if (!tlv.containsKey(key)) {
                    throw new SecurityException("Pairing method '%s' state '%s' required TLV '0x%02x' missing."
                            .formatted(method.name(), pairingState.name(), key));
                }
            }
        }
    }
}
