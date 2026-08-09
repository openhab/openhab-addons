/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.auth;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.Chacha20Cipher;
import org.openhab.binding.atv.internal.client.support.CryptoKeys;
import org.openhab.binding.atv.internal.client.support.Hkdf;
import org.openhab.binding.atv.internal.client.support.Tlv8;
import org.openhab.binding.atv.internal.client.support.Tlv8.TlvValue;

/**
 * Client-side HAP Pair-Verify state machine.
 *
 * <p>
 * Transport agnostic: steps consume and produce raw TLV8 byte arrays.
 *
 * <p>
 * Flow: {@link #verify1()} produces the M1 TLV with our X25519 session public key; the
 * device answers with M2 (its session public key plus an encrypted TLV);
 * {@link #verify2(byte[])} performs the ECDH exchange, verifies the device's Ed25519
 * signature with the stored LTPK, and produces the M3 TLV containing our signature
 * encrypted with nonce {@code "PV-Msg03"}. Afterwards
 * {@link #encryptionKeys(String, String, String)} derives the channel keys — this is
 * what MRP/Companion/AirPlay call to set up their encrypted sessions.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class HapPairVerify implements PairVerifyProcedure {

    private final HapCredentials credentials;
    private final byte[] verifyPrivate;
    private final byte[] verifyPublic;

    private byte @Nullable [] shared;

    /**
     * Creates a verifier with a fresh random X25519 session key pair.
     *
     * @param credentials the stored HAP credentials to verify
     */
    public HapPairVerify(HapCredentials credentials) {
        this(credentials, CryptoKeys.x25519Generate().privateKey());
    }

    /**
     * Creates a verifier with an injectable X25519 session private key, for reproducible
     * tests.
     *
     * @param credentials the stored HAP credentials to verify
     * @param x25519Private our 32-byte X25519 session private scalar
     */
    public HapPairVerify(HapCredentials credentials, byte[] x25519Private) {
        this.credentials = credentials;
        this.verifyPrivate = x25519Private.clone();
        this.verifyPublic = CryptoKeys.x25519PublicKey(verifyPrivate);
    }

    /** Our X25519 session public key sent in M1. */
    public byte[] publicKey() {
        return verifyPublic.clone();
    }

    /**
     * First verification step: produces the M1 TLV ({@code SeqNo=M1} plus our X25519
     * session public key).
     *
     * @return M1 TLV bytes
     */
    public byte[] verify1() {
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvValue.SeqNo.value(), new byte[] { (byte) Tlv8.State.M1.value() });
        tlv.put(TlvValue.PublicKey.value(), verifyPublic);
        return Tlv8.write(tlv);
    }

    /**
     * Second verification step: consumes M2, performs the X25519 exchange, decrypts the
     * device sub-TLV (nonce {@code "PV-Msg02"}, key
     * {@code HKDF("Pair-Verify-Encrypt-Salt", "Pair-Verify-Encrypt-Info", shared)}),
     * verifies the device's Ed25519 signature over
     * {@code deviceSessionPub || deviceId || ourSessionPub} using the stored LTPK, and
     * produces the M3 TLV whose encrypted payload (nonce {@code "PV-Msg03"}) contains our
     * identifier and our signature over
     * {@code ourSessionPub || clientId || deviceSessionPub} made with our LTSK.
     *
     * @param m2Tlv M2 TLV bytes received from the device
     * @return M3 TLV bytes
     * @throws AuthenticationError if M2 contains an error entry, the device identifier
     *             does not match the credentials, or the signature does not verify
     */
    public byte[] verify2(byte[] m2Tlv) {
        Map<Integer, byte[]> pairingData = HapTlv.readChecked(m2Tlv);
        byte[] sessionPubKey = HapTlv.required(pairingData, TlvValue.PublicKey);
        byte[] encrypted = HapTlv.required(pairingData, TlvValue.EncryptedData);

        byte[] localShared = CryptoKeys.x25519SharedSecret(verifyPrivate, sessionPubKey);
        this.shared = localShared;
        byte[] sessionKey = Hkdf.expand("Pair-Verify-Encrypt-Salt", "Pair-Verify-Encrypt-Info", localShared);

        Chacha20Cipher chacha = new Chacha20Cipher(sessionKey, sessionKey);
        Map<Integer, byte[]> decryptedTlv = Tlv8
                .read(chacha.decrypt(encrypted, "PV-Msg02".getBytes(StandardCharsets.UTF_8), null));

        byte[] identifier = HapTlv.required(decryptedTlv, TlvValue.Identifier);
        byte[] signature = HapTlv.required(decryptedTlv, TlvValue.Signature);

        if (!Arrays.equals(identifier, credentials.atvId())) {
            throw new AuthenticationError("incorrect device response");
        }

        byte[] info = concat(sessionPubKey, identifier, verifyPublic);
        if (!CryptoKeys.ed25519Verify(credentials.ltpk(), info, signature)) {
            throw new AuthenticationError("signature error");
        }

        byte[] deviceInfo = concat(verifyPublic, credentials.clientId(), sessionPubKey);
        byte[] deviceSignature = CryptoKeys.ed25519Sign(credentials.ltsk(), deviceInfo);

        Map<Integer, byte[]> subTlv = new LinkedHashMap<>();
        subTlv.put(TlvValue.Identifier.value(), credentials.clientId());
        subTlv.put(TlvValue.Signature.value(), deviceSignature);
        byte[] encryptedData = chacha.encrypt(Tlv8.write(subTlv), "PV-Msg03".getBytes(StandardCharsets.UTF_8), null);

        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvValue.SeqNo.value(), new byte[] { (byte) Tlv8.State.M3.value() });
        tlv.put(TlvValue.EncryptedData.value(), encryptedData);
        return Tlv8.write(tlv);
    }

    /**
     * Derives the channel encryption keys from the X25519 shared secret.
     *
     * @throws InvalidStateError if called before {@link #verify2(byte[])}
     */
    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        byte[] localShared = shared;
        if (localShared == null) {
            throw new InvalidStateError("pair verify not completed");
        }
        byte[] outputKey = Hkdf.expand(salt, outputInfo, localShared);
        byte[] inputKey = Hkdf.expand(salt, inputInfo, localShared);
        return new EncryptionKeys(outputKey, inputKey);
    }

    private static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, pos, part.length);
            pos += part.length;
        }
        return result;
    }
}
