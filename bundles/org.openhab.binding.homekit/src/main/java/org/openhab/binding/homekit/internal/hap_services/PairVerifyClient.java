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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.SessionKeys;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

/**
 * Handles the 3-step pair-verify process with a HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairVerifyClient {

    private static final String PAIR_VERIFY_ENCRYPT_INFO = "Pair-Verify-Encrypt-Info";
    private static final String PAIR_VERIFY_ENCRYPT_SALT = "Pair-Verify-Encrypt-Salt";
    private static final String CONTENT_TYPE_TLV = "application/pairing+tlv8";
    private static final String ENDPOINT_PAIR_VERIFY = "/pair-verify";
    private static final String CONTROL_WRITE_ENCRYPTION_KEY = "Control-Write-Encryption-Key";
    private static final String CONTROL_READ_ENCRYPTION_KEY = "Control-Read-Encryption-Key";
    private static final String CONTROL_SALT = "Control-Salt";
    private static final byte[] VERIFY_NONCE_M2 = CryptoUtils.generateNonce("PV-Msg02");
    private static final byte[] VERIFY_NONCE_M3 = CryptoUtils.generateNonce("PV-Msg03");

    private final HttpTransport httpTransport;
    private final String baseUrl;
    private final byte[] clientIdentifier;
    private final Ed25519PrivateKeyParameters clientPrivateSigningKey;
    private final Ed25519PublicKeyParameters serverPublicSigningKey;

    public PairVerifyClient(HttpTransport httpTransport, String baseUrl, String clientIdentifier,
            Ed25519PrivateKeyParameters clientPrivateSigningKey, Ed25519PublicKeyParameters serverPublicSigningKey) {
        this.httpTransport = httpTransport;
        this.baseUrl = baseUrl;
        this.clientIdentifier = clientIdentifier.getBytes(StandardCharsets.UTF_8);
        this.clientPrivateSigningKey = clientPrivateSigningKey;
        this.serverPublicSigningKey = serverPublicSigningKey;
    }

    /**
     * Executes the 4-step pairing verification process with the accessory.
     *
     * @return SessionKeys containing the derived session keys
     * @throws Exception if any step of the pairing process fails
     */
    public SessionKeys verify() throws Exception {
        Map<Integer, byte[]> tlv;
        byte[] encoded;
        byte[] response;
        byte[] encrypted;
        byte[] decrypted;

        // M1 — Create new random client ephemeral X25519 public key and send it to server
        X25519PrivateKeyParameters clientKey = CryptoUtils.generateX25519KeyPair();
        byte[] clientKeyBytes = clientKey.generatePublicKey().getEncoded();
        tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M1.value }, //
                TlvType.PUBLIC_KEY.key, clientKeyBytes);
        Validator.validate(PairingMethod.VERIFY, tlv);
        encoded = Tlv8Codec.encode(tlv);
        response = httpTransport.post(baseUrl, ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_TLV, encoded);

        // M2 — Receive server ephemeral X25519 public key and encrypted TLV
        tlv = Tlv8Codec.decode(response);
        Validator.validate(PairingMethod.VERIFY, tlv);
        byte[] serverKeyBytes = tlv.get(TlvType.PUBLIC_KEY.key);
        X25519PublicKeyParameters serverKey = new X25519PublicKeyParameters(serverKeyBytes, 0);

        byte[] sharedSecret = CryptoUtils.computeSharedSecret(clientKey, serverKey);
        byte[] sessionKey = CryptoUtils.hkdf(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);

        encrypted = tlv.get(TlvType.ENCRYPTED_DATA.key);
        decrypted = CryptoUtils.decrypt(sessionKey, VERIFY_NONCE_M2, Objects.requireNonNull(encrypted));
        tlv = Tlv8Codec.decode(decrypted); // inner tlv

        // validate identifier + signature
        byte[] identifier = tlv.get(TlvType.IDENTIFIER.key);
        byte[] signature = tlv.get(TlvType.SIGNATURE.key);
        if (identifier == null || signature == null) {
            throw new SecurityException("Accessory identifier or signature missing");
        }
        Ed25519Signer verifier = new Ed25519Signer();
        verifier.init(false, serverPublicSigningKey);
        verifier.update(identifier, 0, identifier.length);
        boolean valid = verifier.verifySignature(signature);
        if (!valid) {
            throw new SecurityException("Accessory signature verification failed");
        }
        System.out.println("Verified accessory identifier: " + new String(identifier, StandardCharsets.UTF_8));

        // M3 — Send encrypted controller identifier and signature
        byte[] payload = concat(clientKeyBytes, serverKeyBytes);
        signature = CryptoUtils.signVerifyMessage(clientPrivateSigningKey, payload);
        tlv = Map.of( //
                TlvType.IDENTIFIER.key, clientIdentifier, //
                TlvType.SIGNATURE.key, signature);
        encoded = Tlv8Codec.encode(tlv);
        encrypted = CryptoUtils.encrypt(sessionKey, VERIFY_NONCE_M3, encoded);

        tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M3.value }, //
                TlvType.ENCRYPTED_DATA.key, encrypted);
        Validator.validate(PairingMethod.VERIFY, tlv);

        encoded = Tlv8Codec.encode(tlv);
        response = httpTransport.post(baseUrl, ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_TLV, encoded);

        // M4 — Final confirmation
        tlv = Tlv8Codec.decode(response);
        Validator.validate(PairingMethod.VERIFY, tlv);

        // Derive directional session keys
        byte[] readKey = CryptoUtils.hkdf(sharedSecret, CONTROL_SALT, CONTROL_READ_ENCRYPTION_KEY);
        byte[] writeKey = CryptoUtils.hkdf(sharedSecret, CONTROL_SALT, CONTROL_WRITE_ENCRYPTION_KEY);

        return new SessionKeys(readKey, writeKey);
    }

    private static byte[] concat(byte[]... parts) {
        int total = Arrays.stream(parts).mapToInt(p -> p.length).sum();
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }

    /**
     * Helper that validates the TLV map for the specification required pairing state.
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
