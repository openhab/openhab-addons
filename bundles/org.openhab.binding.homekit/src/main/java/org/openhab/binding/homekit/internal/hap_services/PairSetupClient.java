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

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.toHex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.SRPclient;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.ErrorCode;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(PairSetupClient.class);

    private final IpTransport ipTransport;
    private final String password;
    private final byte[] controllerId;
    private final Ed25519PrivateKeyParameters controllerKey;

    public PairSetupClient(IpTransport ipTransport, byte[] controllerId, Ed25519PrivateKeyParameters controllerKey,
            String pairingCode) throws Exception {
        logger.debug("Created with pairing code: {}", pairingCode);
        this.ipTransport = ipTransport;
        this.password = pairingCode;
        this.controllerId = controllerId;
        this.controllerKey = controllerKey;
    }

    /**
     * Executes the 6-step pairing process with the accessory.
     *
     * @return SessionKeys containing the derived session keys
     * @throws Exception if any step of the pairing process fails
     */
    public Ed25519PublicKeyParameters pair() throws Exception {
        SRPclient client = m1Execute();
        return client.getAccessoryLongTermPublicKey();
    }

    /**
     * Executes step M1 of the pairing process: Start Pair-Setup.
     *
     * @return byte array containing the response from the accessory
     * @throws InterruptedException if the operation is interrupted
     * @throws Exception if an error occurs during execution
     */
    private SRPclient m1Execute() throws Exception {
        logger.debug("Pair-Setup M1: Send pairing start request to server");
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvType.STATE.value, new byte[] { PairingState.M1.value });
        tlv.put(TlvType.METHOD.value, new byte[] { PairingMethod.SETUP.value });
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] m1Response = ipTransport.post(ENDPOINT_PAIR_SETUP, CONTENT_TYPE_PAIRING, Tlv8Codec.encode(tlv));
        return m2Execute(m1Response);
    }

    /**
     * Executes step M2 of the pairing process: Receive salt & accessory SRP public key.
     * And initializes the SRP client with the received parameters.
     *
     * @param m1Response byte array containing the response from step M1
     * @throws Exception if an error occurs during processing
     */
    private SRPclient m2Execute(byte[] m1Response) throws Exception {
        logger.debug("Pair-Setup M2: Read server salt and accessory ephemeral PK; initialize SRP client");
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(m1Response);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] serverSalt = tlv.get(TlvType.SALT.value);
        byte[] serverPublicKey = tlv.get(TlvType.PUBLIC_KEY.value);
        SRPclient client = new SRPclient(password, Objects.requireNonNull(serverSalt),
                Objects.requireNonNull(serverPublicKey));
        return m3Execute(client);
    }

    /**
     * Executes step M3 of the pairing process: Send client SRP public key & M1 proof.
     *
     * @return byte array containing the response from the accessory
     * @throws Exception if an error occurs during processing
     */
    private SRPclient m3Execute(SRPclient client) throws Exception {
        logger.debug("Pair-Setup M3: Send controller ephemeral PK and M1 proof to accessory");
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvType.STATE.value, new byte[] { PairingState.M3.value });
        tlv.put(TlvType.PUBLIC_KEY.value, CryptoUtils.toUnsigned(client.A, 384));
        tlv.put(TlvType.PROOF.value, client.M1);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] m3Response = ipTransport.post(ENDPOINT_PAIR_SETUP, CONTENT_TYPE_PAIRING, Tlv8Codec.encode(tlv));
        return m4Execute(client, m3Response);
    }

    /**
     * Executes step M4 of the pairing process: Verify accessory SRP proof.
     *
     * @param m3Response byte array containing the response from step M3
     * @throws Exception if an error occurs during processing
     */
    private SRPclient m4Execute(SRPclient client, byte[] m3Response) throws Exception {
        logger.debug("Pair-Setup M4: Read accessory M2 proof; and verify it");
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(m3Response);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] accessoryProofM2 = tlv.get(TlvType.PROOF.value);
        client.m4VerifyAccessoryProof(Objects.requireNonNull(accessoryProofM2));
        return m5Execute(client);
    }

    /**
     * Executes step M5 of the pairing process: Exchange encrypted identifiers.
     * Sends the session key, pairing identifier, client LTPK, and signature to the accessory.
     *
     * @return byte array containing the response from the accessory
     * @throws Exception if an error occurs during processing
     */
    private SRPclient m5Execute(SRPclient client) throws Exception {
        logger.debug("Pair-Setup M5: Send controller id, LTPK, and signature to accessory");
        byte[] cipherText = client.m5EncodeControllerInfoAndSign(controllerId, controllerKey);
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvType.STATE.value, new byte[] { PairingState.M5.value });
        tlv.put(TlvType.ENCRYPTED_DATA.value, cipherText);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] m5Response = ipTransport.post(ENDPOINT_PAIR_SETUP, CONTENT_TYPE_PAIRING, Tlv8Codec.encode(tlv));
        return m6Execute(client, m5Response);
    }

    /**
     * Executes step M6 of the pairing process: Final confirmation & accessory credentials.
     * Derives and returns the session keys.
     *
     * @param m5Response byte array containing the response from step M5
     * @throws Exception if an error occurs during processing
     */
    private SRPclient m6Execute(SRPclient client, byte[] m5Response) throws Exception {
        logger.debug("Pair-Setup M6: Read accessory id, LTPK, and signature; and verify it");
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(m5Response);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.SETUP, tlv);
        byte[] cipherText = tlv.get(TlvType.ENCRYPTED_DATA.value);
        client.m6DecodeAccessoryInfoAndVerify(Objects.requireNonNull(cipherText));
        return client;
    }

    private void loggerTraceTlv(Map<Integer, byte[]> tlv) {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, byte[]> entry : tlv.entrySet()) {
                sb.append(String.format("\n - 0x%02x: %s {%d}", entry.getKey(), toHex(entry.getValue()),
                        entry.getValue().length));
            }
            logger.trace("{}", sb.toString());
        }
    }

    /**
     * Helper class that validates the TLV map for the specification required pairing state.
     */
    public static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.value, TlvType.METHOD.value), //
                PairingState.M2, Set.of(TlvType.STATE.value, TlvType.SALT.value, TlvType.PUBLIC_KEY.value), //
                PairingState.M3, Set.of(TlvType.STATE.value, TlvType.PUBLIC_KEY.value, TlvType.PROOF.value), //
                PairingState.M4, Set.of(TlvType.STATE.value, TlvType.PROOF.value), //
                PairingState.M5, Set.of(TlvType.STATE.value, TlvType.ENCRYPTED_DATA.value), //
                PairingState.M6, Set.of(TlvType.STATE.value, TlvType.ENCRYPTED_DATA.value));

        /**
         * Validates the TLV map for the specification required pairing state.
         *
         * @throws SecurityException if required keys are missing or state is invalid
         */
        public static void validate(PairingMethod method, Map<Integer, byte[]> tlv) throws SecurityException {
            if (tlv.containsKey(TlvType.ERROR.value)) {
                byte[] err = tlv.get(TlvType.ERROR.value);
                ErrorCode code = err != null && err.length > 0 ? ErrorCode.from(err[0]) : ErrorCode.UNKNOWN;
                throw new SecurityException(
                        "Pairing method '%s' action failed with error '%s'".formatted(method.name(), code.name()));
            }

            byte[] state = tlv.get(TlvType.STATE.value);
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
