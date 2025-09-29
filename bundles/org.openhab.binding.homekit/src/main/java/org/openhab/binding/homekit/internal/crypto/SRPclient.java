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

    public final BigInteger A; // client SRP public key
    public final BigInteger a; // client SRP private ephemeral
    public final BigInteger B; // server SRP public key
    public final byte[] K; // session key
    public final byte[] M1; // client proof
    public final BigInteger S; // shared secret
    public final BigInteger u; // scrambling parameter
    public final BigInteger x; // SRP private key derived from password

    private final String I; // username
    private final byte[] s; // server salt
    private final byte[] M2; // expected server proof

    private @Nullable Ed25519PublicKeyParameters serverLongTermPublicKey = null;

    /**
     * M1 - Simplified constructor when user and client private key are not provided.
     *
     * @param passwordP the password (P) used for authentication.
     * @param serverSalt the salt (s) provided by the server.
     * @param serverEphemeralPublicKey the server's public SRP key (B).
     *
     * @throws Exception if an error occurs during initialization.
     */
    public SRPclient(String passwordP, byte[] serverSalt, byte[] serverEphemeralPublicKey) throws Exception {
        this(passwordP, serverSalt, serverEphemeralPublicKey, null, null);
    }

    /**
     * M2 — Initializes the SRP client with the given password, salt and server public SRP key.
     *
     * @param password_p the password (P) used for authentication.
     * @param serverSalt the salt (s) provided by the server.
     * @param serverEphemeralPublicKey the server's public SRP key (B).
     * @param user_I the username (I). If null, "Pair-Setup" is used.
     * @param clientEphemeralSecretKey the client's private SRP key (a). If null, a random key is generated.
     *
     * @throws Exception if an error occurs during initialization.
     */
    public SRPclient(String password_p, byte[] serverSalt, byte[] serverEphemeralPublicKey, @Nullable String user_I,
            byte @Nullable [] clientEphemeralSecretKey) throws Exception {
        // set username, salt and server public key
        s = serverSalt;
        B = new BigInteger(1, serverEphemeralPublicKey);
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
        byte[] hIP = sha512((I + ":" + password_p).getBytes(StandardCharsets.UTF_8));
        byte[] xHash = sha512(concat(serverSalt, hIP));
        x = new BigInteger(1, xHash);

        // Compute scrambling parameter u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(A, 384), toUnsigned(B, 384)));
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
        K = sha512(toUnsigned(S, 384));

        // Compute client proof M1 = H(H(N) xor H(g) || H(I) || s || A || B || K)
        byte[] HN = sha512(toUnsigned(N, 384));
        byte[] Hg = sha512(toUnsigned(g, 1));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));
        M1 = sha512(concat(Hxor, HI, s, toUnsigned(A, 384), toUnsigned(B, 384), K));

        // Compute expected server proof M2 = H(A || M1 || K)
        M2 = sha512(concat(toUnsigned(A, 384), M1, K));
    }

    public byte[] getScramblingParameter() {
        return toUnsigned(u, 64);
    }

    public Ed25519PublicKeyParameters getServerLongTermPublicKey() throws Exception {
        Ed25519PublicKeyParameters serverLongTermPublicKey = this.serverLongTermPublicKey;
        if (serverLongTermPublicKey == null) {
            throw new IllegalStateException("Accessory long-term public key not yet available");
        }
        return serverLongTermPublicKey;
    }

    public byte[] getSharedKey() {
        return generateHkdfKey(K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
    }

    public void m4VerifyServerProof(byte[] serverProof) throws Exception {
        if (!Arrays.equals(M2, serverProof)) {
            throw new SecurityException("SRP server proof mismatch");
        }
    }

    /**
     * M5 - Creates an encrypted TLV containing controller information to be sent to the accessory.
     * The TLV includes the client's pairing Id and the client's LTPK, plus also a signature over a
     * concatenation of { shared session key, client pairing identifier, client LTPK } created by
     * the client's long term secret key.
     *
     * @param pairingId the pairing identifier.
     * @param clientLongTermSecretKey the controller's long-term private key for signing.
     * @return the encrypted controller information as a byte array.
     * @throws Exception if an error occurs during the encryption or signing process.
     */
    public byte[] m5EncodeClientInfoAndSign(byte[] pairingId, Ed25519PrivateKeyParameters clientLongTermSecretKey)
            throws Exception {
        byte[] sharedKey = generateHkdfKey(K, PAIR_CONTROLLER_SIGN_SALT, PAIR_CONTROLLER_SIGN_INFO);
        byte[] clientSigningKey = clientLongTermSecretKey.generatePublicKey().getEncoded();
        byte[] clientSignature = signMessage(clientLongTermSecretKey, concat(sharedKey, pairingId, clientSigningKey));

        Map<Integer, byte[]> subTlv = Map.of( //
                TlvType.IDENTIFIER.key, pairingId, //
                TlvType.PUBLIC_KEY.key, clientSigningKey, //
                TlvType.SIGNATURE.key, clientSignature);

        byte[] plainText = Tlv8Codec.encode(subTlv);
        byte[] cipherText = encrypt(getSharedKey(), PS_M5_NONCE, plainText, new byte[0]);
        return cipherText;
    }

    /**
     * M6 - Decrypts the accessory's sub TLV containing information received in M6. Extracts the
     * server pairing identifier, server LTPK, and server signature. Then validates the server
     * signature against a local copy created using the provided LTPK over a locally created
     * concatentation of { shared key, pairing identifier, accessory LTPK} .
     *
     * @param cipherText the encrypted accessory information received from the accessory.
     * @throws Exception if an error occurs during decryption or signature verification.
     */
    public void m6DecodeServerInfoAndVerify(byte[] cipherText) throws Exception {
        byte[] plainText = decrypt(getSharedKey(), PS_M6_NONCE, cipherText, new byte[0]);

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] serverPairingId = subTlv.get(TlvType.IDENTIFIER.key);
        byte[] serverSigningKey = subTlv.get(TlvType.PUBLIC_KEY.key);
        byte[] serverSignature = subTlv.get(TlvType.SIGNATURE.key);

        if (serverPairingId == null || serverSigningKey == null || serverSignature == null) {
            throw new SecurityException("Missing accessory credentials in M6");
        }

        byte[] sharedKey = generateHkdfKey(K, PAIR_ACCESSORY_SIGN_SALT, PAIR_ACCESSORY_SIGN_INFO);

        Ed25519PublicKeyParameters serverLongTermPublicKey = new Ed25519PublicKeyParameters(serverSigningKey, 0);
        if (!verifySignature(serverLongTermPublicKey, serverSignature,
                concat(sharedKey, serverPairingId, serverSigningKey))) {
            throw new SecurityException("Accessory signature verification failed");
        }
        this.serverLongTermPublicKey = serverLongTermPublicKey;
    }
}
