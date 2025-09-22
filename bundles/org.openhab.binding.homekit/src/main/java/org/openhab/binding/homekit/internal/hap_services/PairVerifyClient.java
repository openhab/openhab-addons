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

import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.transport.IpTransport;

/**
 * Handles the 3-step pair-verify process with a HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairVerifyClient {

    private static final String CONTENT_TYPE_TLV = "application/pairing+tlv8";
    private static final String ENDPOINT_PAIR_VERIFY = "/pair-verify";

    private final IpTransport ipTransport;
    private final byte[] pairingId;
    private final Ed25519PrivateKeyParameters clientLongTermPrivateKey;
    private final Ed25519PublicKeyParameters serverLongTermPublicKey;
    private final X25519PrivateKeyParameters clientKey;

    private @NonNullByDefault({}) byte[] sharedSecret;
    private @NonNullByDefault({}) byte[] sessionKey;
    private @NonNullByDefault({}) byte[] readKey;
    private @NonNullByDefault({}) byte[] writeKey;

    public PairVerifyClient(IpTransport ipTransport, String pairingId,
            Ed25519PrivateKeyParameters clientLongTermPrivateKey, Ed25519PublicKeyParameters serverLongTermPublicKey)
            throws Exception {
        this.ipTransport = ipTransport;
        this.pairingId = pairingId.getBytes(StandardCharsets.UTF_8);
        this.clientLongTermPrivateKey = clientLongTermPrivateKey;
        this.serverLongTermPublicKey = serverLongTermPublicKey;
        this.clientKey = CryptoUtils.generateX25519KeyPair();
    }

    /**
     * Executes the 4-step pairing verification process with the accessory.
     *
     * @return SessionKeys containing the derived session keys
     * @throws Exception if any step of the pairing process fails
     */
    public AsymmetricSessionKeys verify() throws Exception {
        doStep1();
        return new AsymmetricSessionKeys(readKey, writeKey);
    }

    // M1 — Create new random client ephemeral X25519 public key and send it to server
    private void doStep1() throws Exception {
        byte[] clientKey = this.clientKey.generatePublicKey().getEncoded();
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                TlvType.PUBLIC_KEY.key, clientKey);
        Validator.validate(PairingMethod.VERIFY, tlv);
        doStep2(ipTransport.post(ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_TLV, Tlv8Codec.encode(tlv)));
    }

    // M2 — Receive server ephemeral X25519 public key and encrypted TLV
    private void doStep2(byte[] response1) throws Exception {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response1);
        Validator.validate(PairingMethod.VERIFY, tlv);

        byte[] serverKeyBytes = tlv.get(TlvType.PUBLIC_KEY.key);
        X25519PublicKeyParameters serverKey = new X25519PublicKeyParameters(serverKeyBytes, 0);

        sharedSecret = generateSharedSecret(clientKey, serverKey);
        sessionKey = generateHkdfKey(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);

        byte[] ciphertext = tlv.get(TlvType.ENCRYPTED_DATA.key);
        byte[] plaintext = CryptoUtils.decrypt(sessionKey, PV_M2_NONCE, Objects.requireNonNull(ciphertext));

        // validate identifier + signature
        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plaintext);
        byte[] identifier = subTlv.get(TlvType.IDENTIFIER.key);
        byte[] signature = subTlv.get(TlvType.SIGNATURE.key);
        if (identifier == null || signature == null) {
            throw new SecurityException("Accessory identifier or signature missing");
        }
        verifySignature(serverLongTermPublicKey, identifier, signature);

        doStep3();
    }

    // M3 — Send encrypted controller identifier and signature
    private void doStep3() throws Exception {
        byte[] sharedKey = generateHkdfKey(sharedSecret, PAIR_CONTROLLER_SIGN_SALT, PAIR_CONTROLLER_SIGN_INFO);
        byte[] signingKey = clientLongTermPrivateKey.generatePublicKey().getEncoded();
        byte[] payload = concat(sharedKey, pairingId, signingKey);
        byte[] signature = signMessage(clientLongTermPrivateKey, payload);

        Map<Integer, byte[]> subTlv = Map.of( //
                TlvType.IDENTIFIER.key, payload, //
                TlvType.SIGNATURE.key, signature);

        byte[] plaintext = Tlv8Codec.encode(subTlv);
        byte[] ciphertext = encrypt(sessionKey, PV_M3_NONCE, plaintext);

        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M3.value }, //
                TlvType.ENCRYPTED_DATA.key, ciphertext);
        Validator.validate(PairingMethod.VERIFY, tlv);

        doStep4(ipTransport.post(ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_TLV, Tlv8Codec.encode(tlv)));
    }

    // M4 — Final confirmation
    private void doStep4(byte[] response3) throws Exception {
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(response3);
        Validator.validate(PairingMethod.VERIFY, tlv);
        readKey = CryptoUtils.generateHkdfKey(sharedSecret, CONTROL_SALT, CONTROL_READ_ENCRYPTION_KEY);
        writeKey = CryptoUtils.generateHkdfKey(sharedSecret, CONTROL_SALT, CONTROL_WRITE_ENCRYPTION_KEY);
    }

    /**
     * Helper class that validates the TLV map for the specification required pairing state.
     */
    public static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.key, TlvType.PUBLIC_KEY.key), // TLVType.METHOD not required
                PairingState.M2, Set.of(TlvType.STATE.key, TlvType.PUBLIC_KEY.key, TlvType.ENCRYPTED_DATA.key), //
                PairingState.M3, Set.of(TlvType.STATE.key, TlvType.ENCRYPTED_DATA.key), //
                PairingState.M4, Set.of(TlvType.STATE.key));

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
