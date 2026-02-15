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
package org.openhab.binding.homekit.internal.crypto;

import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the SRP (Stanford Secure Remote Password) protocol for pairing with a HomeKit accessory.
 * This class handles the SRP steps, including key generation, proof verification, and encryption of identifiers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SRPclient {

    private final Logger logger = LoggerFactory.getLogger(SRPclient.class);

    /*
     * ***************************************************************************************
     *
     * DEVELOPER NOTE:
     *
     * Some of the field names in this class follow the Crytographic "Alice and Bob Notation"
     * where for example 'A' (uppercase) is the conventional meaning for "Alice's Public Key"
     * and 'a' (lowercase) is the conventional meaning for "Alice's Private Key". Such names
     * are legal according to Java language syntax, but the openHAB style checker warns about
     * some of them. => Please ignore such warnings.
     *
     * ***************************************************************************************
     */

    public final BigInteger A; // client SRP public key
    public final BigInteger a; // client SRP private ephemeral
    public final BigInteger B; // server SRP public key
    public final byte[] S; // shared secret
    public final byte[] K; // Apple SRP style session key = H(S)
    public final byte[] M1; // client proof
    public final BigInteger u; // scrambling parameter
    public final BigInteger x; // SRP private key derived from password

    private final String I; // username
    private final byte[] s; // server salt
    private final byte[] M2; // expected accessory server proof

    private @Nullable Ed25519PublicKeyParameters accessoryLongTermPublicKey = null;

    /**
     * M1 - Simplified constructor when user and client private key are not provided.
     *
     * @param password_P the password (P) used for authentication.
     * @param serverSalt the salt (s) provided by the server.
     * @param serverEphemeralPublicKey the server's public SRP key (B).
     * @throws NoSuchAlgorithmException
     */
    public SRPclient(String password_P, byte[] serverSalt, byte[] serverEphemeralPublicKey)
            throws NoSuchAlgorithmException {
        this(password_P, serverSalt, serverEphemeralPublicKey, null, null);
    }

    /**
     * M2 — Initializes the SRP client with the given password, salt and server public SRP key.
     *
     * @param password_P the password (P) used for authentication.
     * @param serverSalt the salt (s) provided by the server.
     * @param accessoryEphemeralPublicKey the server's public SRP key (B).
     * @param user_I the username (I). If null, "Pair-Setup" is used.
     * @param clientEphemeralSecretKey the client's private SRP key (a). If null, a random key is generated.
     * @throws NoSuchAlgorithmException
     */
    public SRPclient(String password_P, byte[] serverSalt, byte[] accessoryEphemeralPublicKey, @Nullable String user_I,
            byte @Nullable [] clientEphemeralSecretKey) throws NoSuchAlgorithmException {
        // set username, salt and server public key
        s = serverSalt;
        B = new BigInteger(1, accessoryEphemeralPublicKey);
        I = user_I != null ? user_I : PAIR_SETUP; // default username is "Pair-Setup"

        // Apply or create ephemeral a and compute public A
        byte[] client_a = clientEphemeralSecretKey;
        if (client_a == null) {
            client_a = new byte[32];
            new SecureRandom().nextBytes(client_a);
        }
        a = new BigInteger(1, client_a);
        A = g.modPow(a, N);

        // Compute hash x = H(salt || H(username || ":" || password))
        byte[] hIP = sha512((I + ":" + password_P).getBytes(StandardCharsets.UTF_8));
        byte[] xHash = sha512(concat(serverSalt, hIP));
        x = new BigInteger(1, xHash);

        // Compute scrambling parameter u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(A, 384), toUnsigned(B, 384)));
        u = new BigInteger(1, uHash);
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid scrambling parameter");
        }

        // Compute shared secret S = (B - k·g^x)^(a + u·x) mod N (384 bytes)
        BigInteger gx = g.modPow(x, N);
        BigInteger base = B.subtract(k.multiply(gx)).mod(N);
        BigInteger exp = a.add(u.multiply(x));
        S = toUnsigned(base.modPow(exp, N), 384);

        // Compute 'Apple SRP style' session key K = H(S) (64 bytes)
        K = sha512(S);

        // Compute client proof M1 = H(H(N) xor H(g) || H(I) || s || A || B || K)
        byte[] HN = sha512(toUnsigned(N, 384));
        byte[] Hg = sha512(toUnsigned(g, 1));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));
        M1 = sha512(concat(Hxor, HI, s, toUnsigned(A, 384), toUnsigned(B, 384), K));

        // Compute expected server proof M2 = H(A || M1 || K)
        M2 = sha512(concat(toUnsigned(A, 384), M1, K));

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Pair-Setup M2: SRP client initialized:\n - K: {}\n - S: {}\n - Controller M1: {}\n - Expected M2: {}\n",
                    toHex(K), toHex(S), toHex(M1), toHex(M2));
        }
    }

    public byte[] getScramblingParameter() {
        return toUnsigned(u, 64);
    }

    public Ed25519PublicKeyParameters getAccessoryLongTermPublicKey() throws IllegalStateException {
        Ed25519PublicKeyParameters accessoryLTPK = this.accessoryLongTermPublicKey;
        if (accessoryLTPK == null) {
            throw new IllegalStateException("Accessory long-term public key not yet available");
        }
        return accessoryLTPK;
    }

    public void m4VerifyAccessoryProof(byte[] accessoryProof) {
        if (logger.isTraceEnabled()) {
            logger.trace("Pair-Setup M4: Accessory info:\n - Controller M2: {}\n - Accessory M2:  {}", toHex(M2),
                    toHex(accessoryProof));
        }
        if (!Arrays.equals(M2, accessoryProof)) {
            throw new SecurityException("SRP server proof mismatch");
        }
    }

    /**
     * M5 - Creates an encrypted TLV containing controller information to be sent to the accessory.
     * The TLV includes the client's pairing Id and the client's LTPK, plus also a signature over a
     * concatenation of { shared session key, client pairing identifier, client LTPK } created by
     * the client's long term secret key.
     *
     * @param iOSDeviceId the pairing identifier.
     * @param iOSDeviceLongTermPrivateKey the controller's long-term private key for signing.
     * @return the encrypted controller information as a byte array.
     * @throws InvalidCipherTextException
     */
    public byte[] m5EncodeControllerInfoAndSign(byte[] iOSDeviceId,
            Ed25519PrivateKeyParameters iOSDeviceLongTermPrivateKey) throws InvalidCipherTextException {
        byte[] iOSDeviceX = generateHkdfKey(K, PAIR_SETUP_CONTROLLER_SIGN_SALT, PAIR_SETUP_CONTROLLER_SIGN_INFO);
        byte[] iOSDeviceLTPK = iOSDeviceLongTermPrivateKey.generatePublicKey().getEncoded();
        byte[] iOSDeviceInfo = concat(iOSDeviceX, iOSDeviceId, iOSDeviceLTPK);
        byte[] iOSDeviceSignature = signMessage(iOSDeviceLongTermPrivateKey, iOSDeviceInfo);

        Map<Integer, byte[]> subTlv = new LinkedHashMap<>();
        subTlv.put(TlvType.IDENTIFIER.value, iOSDeviceId);
        subTlv.put(TlvType.PUBLIC_KEY.value, iOSDeviceLTPK);
        subTlv.put(TlvType.SIGNATURE.value, iOSDeviceSignature);

        byte[] plainText = Tlv8Codec.encode(subTlv);
        byte[] encryptKey = generateHkdfKey(K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Pair-Setup M5: Controller info:\n - X: {}\n - LTPK: {}\n - Info: {}\n - Signature: {}\n - Plain text: {}\n - Key: {}", //
                    toHex(iOSDeviceX), toHex(iOSDeviceLTPK), toHex(iOSDeviceInfo), toHex(iOSDeviceSignature),
                    toHex(plainText), toHex(encryptKey));
        }
        byte[] cipherText = encrypt(encryptKey, PS_M5_NONCE, plainText, new byte[0]);

        if (logger.isTraceEnabled()) {
            logger.trace("Pair-Setup M5: Controller info:\n - Cipher text: {}", toHex(cipherText));
        }
        return cipherText;
    }

    /**
     * M6 - Decrypts the accessory's sub TLV containing information received in M6. Extracts the
     * server pairing identifier, server LTPK, and server signature. Then validates the server
     * signature against a local copy created using the provided LTPK over a locally created
     * concatentation of { shared key, pairing identifier, accessory LTPK} .
     *
     * @param cipherText the encrypted accessory information received from the accessory.
     * @throws InvalidCipherTextException
     */
    public void m6DecodeAccessoryInfoAndVerify(byte[] cipherText) throws InvalidCipherTextException {
        byte[] decryptKey = generateHkdfKey(K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
        if (logger.isTraceEnabled()) {
            logger.trace("Pair-Setup M6: Accessory info:\n - Cipher text: {}\n - Key: {}", toHex(cipherText),
                    toHex(decryptKey));
        }
        byte[] plainText = decrypt(decryptKey, PS_M6_NONCE, cipherText, new byte[0]);

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] accessoryPairingId = subTlv.get(TlvType.IDENTIFIER.value);
        byte[] accessoryLTPK = subTlv.get(TlvType.PUBLIC_KEY.value);
        byte[] accessorySignature = subTlv.get(TlvType.SIGNATURE.value);

        if (accessoryPairingId == null || accessoryLTPK == null || accessorySignature == null) {
            throw new SecurityException("Missing accessory credentials in M6");
        }

        Ed25519PublicKeyParameters accessoryLongTermPublicKey = new Ed25519PublicKeyParameters(accessoryLTPK, 0);
        byte[] accessoryX = generateHkdfKey(K, PAIR_SETUP_ACCESSORY_SIGN_SALT, PAIR_SETUP_ACCESSORY_SIGN_INFO);
        byte[] accessoryInfo = concat(accessoryX, accessoryPairingId, accessoryLTPK);

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Pair-Setup M6: Accessory info:\n - Plain text: {}\n - X: {}\n - LTPK: {}\n - Info: {}\n - Signature: {}",
                    toHex(plainText), toHex(accessoryX), toHex(accessoryLTPK), toHex(accessoryInfo),
                    toHex(accessorySignature));
        }
        verifySignature(accessoryLongTermPublicKey, accessorySignature, accessoryInfo);
        this.accessoryLongTermPublicKey = accessoryLongTermPublicKey;
    }
}
