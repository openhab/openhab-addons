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
package org.openhab.binding.homekit.internal.crypto;

import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.TlvType;

/**
 * Manages the SRP (Stanford Secure Remote Password) protocol for pairing with a HomeKit accessory.
 * This class handles the SRP steps, including key generation, proof verification, and encryption of identifiers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SRPclient {

    private final String I; // username
    private final byte[] s; // server salt
    private final BigInteger x; // SRP private key derived from password
    private final BigInteger a; // client SRP private ephemeral
    private final BigInteger A; // client SRP public key
    private final BigInteger B; // server SRP public key
    private final BigInteger u; // scrambling parameter
    private final BigInteger S; // shared secret
    private final byte[] K; // session key
    private final byte[] M1; // client proof

    private @Nullable Ed25519PublicKeyParameters serverLongTermPublicKey = null;

    /**
     * M2 — Initializes the SRP client with the given password, salt and server public SRP key.
     *
     * @param password the password (P) used for authentication.
     * @param serverSalt the salt (s) provided by the server.
     * @param serverPublicKey the server's public SRP key (B).
     * @throws Exception if an error occurs during initialization.
     */
    public SRPclient(String password, byte[] serverSalt, byte[] serverPublicKey) throws Exception {
        I = PAIR_SETUP;
        s = serverSalt;
        B = new BigInteger(1, serverPublicKey);

        // Generate ephemeral a and compute public A
        a = new BigInteger(N.bitLength(), new SecureRandom()).mod(N);
        A = g.modPow(a, N);

        // Compute hash x = H(salt || H(username || ":" || password))
        byte[] hIP = sha512((PAIR_SETUP + ":" + password).getBytes(StandardCharsets.UTF_8));
        byte[] xHash = sha512(concat(serverSalt, hIP));
        x = new BigInteger(1, xHash);

        // Compute scrambling parameter u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(A, N), toUnsigned(B, N)));
        u = new BigInteger(1, uHash);
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid scrambling parameter");
        }

        // Compute shared secret S = (B - k·g^x)^(a + u·x) mod N
        BigInteger gx = g.modPow(x, N);
        BigInteger base = B.subtract(k.multiply(gx)).mod(N);
        BigInteger exp = a.add(u.multiply(x));
        S = base.modPow(exp, N);

        // Compute session key K = H(S)
        K = sha512(toUnsigned(S, N));

        // Compute client proof M1 = H(H(N) ⊕ H(g) || H(I) || s || A || B || K)
        byte[] HN = sha512(toUnsigned(N, N));
        byte[] Hg = sha512(toUnsigned(g, N));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));
        M1 = sha512(concat(Hxor, HI, s, toUnsigned(A, N), toUnsigned(B, N), K));
    }

    public byte[] createEncryptedControllerInfo(byte[] pairingId,
            Ed25519PrivateKeyParameters controllerLongTermPrivateKey) throws Exception {
        byte[] sharedKey = generateHkdfKey(getSharedSecret(), PAIR_CONTROLLER_SIGN_SALT, PAIR_CONTROLLER_SIGN_INFO);
        byte[] signingKey = controllerLongTermPrivateKey.generatePublicKey().getEncoded();
        byte[] payload = concat(sharedKey, pairingId, signingKey);
        byte[] signature = signMessage(controllerLongTermPrivateKey, payload);

        Map<Integer, byte[]> subTlv = Map.of( //
                TlvType.IDENTIFIER.key, pairingId, //
                TlvType.PUBLIC_KEY.key, signingKey, //
                TlvType.SIGNATURE.key, signature);

        byte[] plaintext = Tlv8Codec.encode(subTlv);
        byte[] ciphertext = encrypt(getSymmetricKey(), PS_M5_NONCE, plaintext);
        return ciphertext;
    }

    public Ed25519PublicKeyParameters getAccessoryLongTermPublicKey() throws Exception {
        Ed25519PublicKeyParameters serverLongTermPublicKey = this.serverLongTermPublicKey;
        if (serverLongTermPublicKey == null) {
            throw new IllegalStateException("Accessory long-term public key not yet available");
        }
        return serverLongTermPublicKey;
    }

    public byte[] getClientProof() {
        return M1;
    }

    public byte[] getPublicKey() {
        return toUnsigned(A, N);
    }

    private byte[] getSharedSecret() {
        return toUnsigned(S, N);
    }

    public byte[] getSymmetricKey() {
        return generateHkdfKey(getSharedSecret(), PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
    }

    public void verifyEncryptedAccessoryInfo(byte[] cipherText) throws Exception {
        byte[] plainText = decrypt(getSymmetricKey(), PS_M6_NONCE, cipherText);

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] pairingId = subTlv.get(TlvType.IDENTIFIER.key);
        byte[] signingKey = subTlv.get(TlvType.PUBLIC_KEY.key);
        byte[] signature = subTlv.get(TlvType.SIGNATURE.key);

        if (pairingId == null || signingKey == null || signature == null) {
            throw new SecurityException("Missing accessory credentials in M6");
        }

        byte[] sharedKey = generateHkdfKey(getSharedSecret(), PAIR_ACCESSORY_SIGN_SALT, PAIR_ACCESSORY_SIGN_INFO);
        byte[] payload = concat(sharedKey, pairingId, signingKey);

        Ed25519PublicKeyParameters serverLongTermPublicKey = new Ed25519PublicKeyParameters(signingKey, 0);
        verifySignature(serverLongTermPublicKey, payload, signature);
        this.serverLongTermPublicKey = serverLongTermPublicKey;
    }

    public void verifyServerProof(byte[] serverProof) throws Exception {
        byte[] M2 = sha512(concat(toUnsigned(A, N), M1, K));
        if (!Arrays.equals(M2, serverProof)) {
            throw new SecurityException("SRP server proof mismatch");
        }
    }
}
