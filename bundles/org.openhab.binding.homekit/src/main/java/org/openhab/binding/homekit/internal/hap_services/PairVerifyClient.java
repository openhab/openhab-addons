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
import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.util.LinkedHashMap;
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
import org.openhab.binding.homekit.internal.enums.ErrorCode;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.AsymmetricSessionKeys;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the 3-step pair-verify process with a HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairVerifyClient {

    private final Logger logger = LoggerFactory.getLogger(PairVerifyClient.class);

    private final IpTransport ipTransport;
    private final byte[] clientPairingId;
    private final Ed25519PrivateKeyParameters controllerKey;
    private final Ed25519PublicKeyParameters accessoryKey;
    private final X25519PrivateKeyParameters controllerEphemeralSecretKey;

    private @NonNullByDefault({}) X25519PublicKeyParameters serverEphemeralPublicKey;
    private @NonNullByDefault({}) byte[] sharedSecret;
    private @NonNullByDefault({}) byte[] sharedKey;
    private @NonNullByDefault({}) byte[] readKey;
    private @NonNullByDefault({}) byte[] writeKey;

    public PairVerifyClient(IpTransport ipTransport, byte[] controllerId, Ed25519PrivateKeyParameters controllerKey,
            Ed25519PublicKeyParameters accessoryKey) throws Exception {
        if (controllerId.length != 16) {
            throw new IllegalArgumentException("Controller Id must be exactly 16 bytes");
        }
        logger.debug("Created..");
        this.ipTransport = ipTransport;
        this.clientPairingId = controllerId;
        this.controllerKey = controllerKey;
        this.accessoryKey = accessoryKey;
        this.controllerEphemeralSecretKey = CryptoUtils.generateX25519KeyPair();
    }

    /**
     * Executes the 4-step pairing verification process with the accessory.
     *
     * @return SessionKeys containing the derived session keys
     * @throws Exception if any step of the pairing process fails
     */
    public AsymmetricSessionKeys verify() throws Exception {
        m1Execute();
        return new AsymmetricSessionKeys(readKey, writeKey);
    }

    // M1 — Create new random client ephemeral X25519 public key and send it to server
    private void m1Execute() throws Exception {
        logger.debug("Pair-Verify M1: Send verification start request with client ephemeral X25519 PK to server");
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvType.STATE.value, new byte[] { PairingState.M1.value });
        tlv.put(TlvType.PUBLIC_KEY.value, controllerEphemeralSecretKey.generatePublicKey().getEncoded());
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.VERIFY, tlv);
        byte[] m1Response = ipTransport.post(ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_PAIRING, Tlv8Codec.encode(tlv));
        m2Execute(m1Response);
    }

    // M2 — Receive server ephemeral X25519 public key and encrypted TLV
    private void m2Execute(byte[] m1Response) throws Exception {
        logger.debug("Pair-Verify M2: Read server ephemeral X25519 PK and encrypted id; validate signature");
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(m1Response);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.VERIFY, tlv);

        serverEphemeralPublicKey = new X25519PublicKeyParameters(tlv.get(TlvType.PUBLIC_KEY.value), 0);
        sharedSecret = generateSharedSecret(controllerEphemeralSecretKey, serverEphemeralPublicKey);
        sharedKey = generateHkdfKey(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);

        byte[] cipherText = tlv.get(TlvType.ENCRYPTED_DATA.value);
        byte[] plainText = CryptoUtils.decrypt(sharedKey, PV_M2_NONCE, Objects.requireNonNull(cipherText), new byte[0]);

        // validate identifier + signature
        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] serverPairingId = subTlv.get(TlvType.IDENTIFIER.value);
        byte[] serverSignature = subTlv.get(TlvType.SIGNATURE.value);
        if (serverPairingId == null || serverSignature == null) {
            throw new SecurityException("Accessory identifier or signature missing");
        }

        verifySignature(accessoryKey, serverSignature, concat(serverEphemeralPublicKey.getEncoded(), serverPairingId,
                controllerEphemeralSecretKey.generatePublicKey().getEncoded()));

        m3Execute();
    }

    // M3 — Send encrypted controller identifier and signature
    private void m3Execute() throws Exception {
        logger.debug("Pair-Verify M3: Send encrypted controller id with signature");
        byte[] clientSignature = signMessage(controllerKey,
                concat(controllerEphemeralSecretKey.generatePublicKey().getEncoded(), clientPairingId,
                        serverEphemeralPublicKey.getEncoded()));

        Map<Integer, byte[]> subTlv = new LinkedHashMap<>();
        subTlv.put(TlvType.IDENTIFIER.value, clientPairingId);
        subTlv.put(TlvType.SIGNATURE.value, clientSignature);

        byte[] plainText = Tlv8Codec.encode(subTlv);
        byte[] cipherText = encrypt(sharedKey, PV_M3_NONCE, plainText, new byte[0]);

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvType.STATE.value, new byte[] { PairingState.M3.value });
        tlv.put(TlvType.ENCRYPTED_DATA.value, cipherText);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.VERIFY, tlv);

        byte[] m3Response = ipTransport.post(ENDPOINT_PAIR_VERIFY, CONTENT_TYPE_PAIRING, Tlv8Codec.encode(tlv));
        m4Execute(m3Response);
    }

    // M4 — Final confirmation
    private void m4Execute(byte[] m3Response) throws Exception {
        logger.debug("Pair-Verify M4: Confirm validation; derive session keys");
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(m3Response);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.VERIFY, tlv);
        readKey = CryptoUtils.generateHkdfKey(sharedSecret, CONTROL_SALT, CONTROL_READ_ENCRYPTION_KEY);
        writeKey = CryptoUtils.generateHkdfKey(sharedSecret, CONTROL_SALT, CONTROL_WRITE_ENCRYPTION_KEY);
    }

    private void loggerTraceTlv(Map<Integer, byte[]> tlv) {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, byte[]> entry : tlv.entrySet()) {
                sb.append(String.format("\n - 0x%02x: %s", entry.getKey(), toHex(entry.getValue())));
            }
            logger.trace("{}", sb.toString());
        }
    }

    /**
     * Helper class that validates the TLV map for the specification required pairing state.
     */
    public static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.value, TlvType.PUBLIC_KEY.value), // TLVType.METHOD not required
                PairingState.M2, Set.of(TlvType.STATE.value, TlvType.PUBLIC_KEY.value, TlvType.ENCRYPTED_DATA.value), //
                PairingState.M3, Set.of(TlvType.STATE.value, TlvType.ENCRYPTED_DATA.value), //
                PairingState.M4, Set.of(TlvType.STATE.value));

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
