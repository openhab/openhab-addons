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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.SRPclient;
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
public class PairSetupClient {

    private static final String ENDPOINT_PAIR_SETUP = "/pair-setup";

    private static final String CONTENT_TYPE_TLV8 = "application/pairing+tlv8";
    private final HttpTransport httpTransport;
    private final String baseUrl;
    private final String password;
    private final String username;
    private final Ed25519PrivateKeyParameters clientPrivateSigningKey;
    private final String accessoryIdentifier;

    private @NonNullByDefault({}) SRPclient client = null;

    public PairSetupClient(HttpTransport httpTransport, String baseUrl, String accessoryIdentifier,
            Ed25519PrivateKeyParameters clientPrivateSigningKey, String username, String password) throws Exception {
        this.httpTransport = httpTransport;
        this.baseUrl = baseUrl;
        this.password = password;
        this.username = username;
        this.clientPrivateSigningKey = clientPrivateSigningKey;
        this.accessoryIdentifier = accessoryIdentifier;
    }

    /**
     * Executes the 6-step pairing process with the accessory.
     *
     * @return SessionKeys containing the derived session keys
     * @throws Exception if any step of the pairing process fails
     */
    public SessionKeys pair() throws Exception {
        byte[] response;

        // Execute the 6-step pairing process
        response = doClientStepM1();
        doClientStepM2(response);
        response = doClientStepM3();
        doClientStepM4(response);
        response = doClientStepM5();
        doClientStepM6(response);

        return client.deriveSessionKeys();
    }

    /**
     * Returns the SRP public key generated during the pairing process.
     *
     * @return byte array containing the SRP public key
     * @throws IllegalStateException if the SRP client is not initialized
     */
    public byte[] getPublicKey() throws IllegalStateException {
        SRPclient client = this.client;
        if (client == null) {
            throw new IllegalStateException("SRP Client not initialized");
        }
        return client.getPublicKey();
    }

    /**
     * Executes step M1 of the pairing process: Start Pair-Setup.
     *
     * @return byte array containing the response from the accessory
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     * @throws Exception if an error occurs during execution
     */
    private byte[] doClientStepM1() throws Exception {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                TlvType.METHOD.key, new byte[] { PairingMethod.SETUP.value });
        Validator.validate(PairingMethod.SETUP, tlv);

        return httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv));
    }

    /**
     * Executes step M2 of the pairing process: Receive salt & accessory SRP public key.
     * And initializes the SRP client with the received parameters.
     *
     * @param response byte array containing the response from step M1
     * @throws Exception if an error occurs during processing
     */
    private void doClientStepM2(byte[] response) throws Exception {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response);
        Validator.validate(PairingMethod.SETUP, tlv);

        byte[] serverSalt = tlv.get(TlvType.SALT.key);
        byte[] serverPublicKey = tlv.get(TlvType.PUBLIC_KEY.key);
        if (serverSalt == null || serverPublicKey == null) {
            throw new SecurityException("Missing salt or public key TLV in M2 response");
        }
        SRPclient client = new SRPclient(username, password, serverSalt);
        client.processChallenge(serverPublicKey);

        this.client = client;
    }

    /**
     * Executes step M3 of the pairing process: Send client SRP public key & proof.
     *
     * @return byte array containing the response from the accessory
     * @throws Exception if an error occurs during processing
     */
    private byte[] doClientStepM3() throws Exception {
        SRPclient client = this.client;
        if (client == null) {
            throw new IllegalStateException("SrpClient not initialized");
        }
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M3.value }, //
                TlvType.PUBLIC_KEY.key, client.getPublicKey(), //
                TlvType.PROOF.key, client.getClientProof());
        Validator.validate(PairingMethod.SETUP, tlv);

        return httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv));
    }

    /**
     * Executes step M4 of the pairing process: Verify accessory SRP proof.
     *
     * @param response byte array containing the response from step M3
     * @throws Exception if an error occurs during processing
     */
    private void doClientStepM4(byte[] response) throws Exception {
        SRPclient client = this.client;
        if (client == null) {
            throw new IllegalStateException("SrpClient not initialized");
        }
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response);
        Validator.validate(PairingMethod.SETUP, tlv);

        byte[] proof = tlv.get(TlvType.PROOF.key);
        if (proof == null) {
            throw new SecurityException("Missing proof TLV in M4 response");
        }
        client.verifyServerProof(proof);
    }

    /**
     * Executes step M5 of the pairing process: Exchange encrypted identifiers.
     *
     * @return byte array containing the response from the accessory
     * @throws Exception if an error occurs during processing
     */
    private byte[] doClientStepM5() throws Exception {
        byte[] encryptedIdentifiers = client.getEncryptedDeviceInfoBlob(client.deriveIOSDeviceXKey(),
                accessoryIdentifier, clientPrivateSigningKey.generatePublicKey(), clientPrivateSigningKey);
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M5.value }, //
                TlvType.ENCRYPTED_DATA.key, encryptedIdentifiers);
        Validator.validate(PairingMethod.SETUP, tlv);

        return httpTransport.post(baseUrl, ENDPOINT_PAIR_SETUP, CONTENT_TYPE_TLV8, Tlv8Codec.encode(tlv));
    }

    /**
     * Executes step M6 of the pairing process: Final confirmation & accessory credentials.
     *
     * @param response byte array containing the response from step M5
     * @throws Exception if an error occurs during processing
     */
    private void doClientStepM6(byte[] response) throws Exception {
        SRPclient client = this.client;
        if (client == null) {
            throw new IllegalStateException("SrpClient not initialized");
        }
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response);
        Validator.validate(PairingMethod.SETUP, tlv);

        byte[] data = tlv.get(TlvType.ENCRYPTED_DATA.key);
        if (data == null) {
            throw new SecurityException("Missing data TLV in M6 response");
        }
        client.verifyAccessoryIdentifiers(data);
    }

    /**
     * Helper that validates the TLV map for the specification required pairing state.
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
