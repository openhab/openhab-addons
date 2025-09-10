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
package org.openhab.binding.homekit.internal;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Map;

import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class SRPClient {

    private static final BigInteger N = new BigInteger("..."); // TODO 3072-bit safe prime
    private static final BigInteger g = BigInteger.valueOf(5);

    private final String setupCode;
    private BigInteger a; // private ephemeral
    private BigInteger A; // public ephemeral
    private BigInteger B; // server public
    private byte[] salt;
    private byte[] K; // shared session key

    public SRPClient(String setupCode) {
        this.setupCode = setupCode;
    }

    public void processChallenge(byte[] salt, byte[] serverPublicKey) throws Exception {
        this.salt = salt;
        this.B = new BigInteger(1, serverPublicKey);

        SecureRandom random = new SecureRandom();
        this.a = new BigInteger(256, random);
        this.A = g.modPow(a, N);
    }

    public Map<Integer, byte[]> generateClientProof() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] xH = digest.digest((new String(salt) + setupCode).getBytes());
        BigInteger x = new BigInteger(1, xH);

        BigInteger u = computeU(A, B);
        BigInteger S = (B.subtract(g.modPow(x, N))).modPow(a.add(u.multiply(x)), N);
        this.K = digest.digest(S.toByteArray());

        byte[] M1 = computeM1(A, B, K);
        return Map.of(0x03, A.toByteArray(), 0x04, M1);
    }

    public void verifyServerProof(byte[] M2) throws Exception {
        byte[] expected = computeM2(A, computeM1(A, B, K), K);
        if (!MessageDigest.isEqual(M2, expected)) {
            throw new SecurityException("Server proof mismatch");
        }
    }

    public Map<Integer, byte[]> generateEncryptedIdentifiers() throws Exception {
        // Encrypt controller identifier and public key using shared key K
        byte[] plaintext = "...".getBytes(); // TODO input TLV8 encoded identifiers
        byte[] nonce = generateNonce();
        byte[] encrypted = encryptChaCha20Poly1305(K, nonce, plaintext);
        return Map.of(0x05, nonce, 0x06, encrypted);
    }

    public void verifyAccessoryIdentifiers(Map<Integer, byte[]> tlv6) throws Exception {
        byte[] nonce = tlv6.get(0x05);
        byte[] encrypted = tlv6.get(0x06);
        byte[] decrypted = decryptChaCha20Poly1305(K, nonce, encrypted);
        // TODO Parse TLV8 and validate accessory identity
    }

    public SessionKeys deriveSessionKeys() {
        return new SessionKeys(K);
    }

    private BigInteger computeU(BigInteger A, BigInteger B) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] uH = digest.digest(concat(A.toByteArray(), B.toByteArray()));
        return new BigInteger(1, uH);
    }

    private byte[] computeM1(BigInteger A, BigInteger B, byte[] K) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        return digest.digest(concat(A.toByteArray(), B.toByteArray(), K));
    }

    private byte[] computeM2(BigInteger A, byte[] M1, byte[] K) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        return digest.digest(concat(A.toByteArray(), M1, K));
    }

    private byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] arr : arrays) {
            out.write(arr, 0, arr.length);
        }
        return out.toByteArray();
    }

    private byte[] generateNonce() {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    private byte[] encryptChaCha20Poly1305(byte[] key, byte[] nonce, byte[] plaintext) throws Exception {
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, null);
        cipher.init(true, params);

        byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
        int len = cipher.processBytes(plaintext, 0, plaintext.length, ciphertext, 0);
        cipher.doFinal(ciphertext, len);
        return ciphertext;
    }

    private byte[] decryptChaCha20Poly1305(byte[] key, byte[] nonce, byte[] ciphertext) throws Exception {
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, null);
        cipher.init(false, params);

        byte[] plaintext = new byte[cipher.getOutputSize(ciphertext.length)];
        int len = cipher.processBytes(ciphertext, 0, ciphertext.length, plaintext, 0);
        cipher.doFinal(plaintext, len);
        return plaintext;
    }
}
