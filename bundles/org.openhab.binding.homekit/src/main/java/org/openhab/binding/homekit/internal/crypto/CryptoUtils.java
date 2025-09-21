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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for cryptographic operations used in HomeKit communication.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CryptoUtils {

    public static byte[] concat(byte[]... parts) {
        int total = Arrays.stream(parts).mapToInt(p -> p.length).sum();
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }

    // Decrypt with ChaCha20-Poly1305
    public static byte[] decrypt(byte[] key, byte[] nonce, byte[] cipherText, byte @Nullable [] authTag)
            throws InvalidCipherTextException {
        int length;
        if (authTag != null) {
            length = cipherText.length - authTag.length;
            byte[] cipherTag = Arrays.copyOfRange(cipherText, length, cipherText.length);
            if (!Arrays.equals(cipherTag, authTag)) {
                throw new InvalidCipherTextException("Authentication tag mismatch");
            }
        } else {
            length = cipherText.length;
        }
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, null);
        cipher.init(false, params);
        byte[] plainText = new byte[cipher.getOutputSize(length)];
        length = cipher.processBytes(cipherText, 0, length, plainText, 0);
        cipher.doFinal(plainText, length);
        return plainText;
    }

    // Encrypt with ChaCha20-Poly1305
    public static byte[] encrypt(byte[] key, byte[] nonce, byte[] plainText, byte @Nullable [] authTag)
            throws InvalidCipherTextException {
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, null);
        cipher.init(true, params);
        byte[] cipherText = new byte[cipher.getOutputSize(plainText.length)];
        int length = cipher.processBytes(plainText, 0, plainText.length, cipherText, 0);
        cipher.doFinal(cipherText, length);
        return authTag == null ? cipherText : concat(cipherText, authTag);
    }

    // HKDF-SHA512 key derivation
    public static byte[] generateHkdfKey(byte[] inputKey, byte[] salt, byte[] info) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(inputKey, salt, info));
        byte[] output = new byte[32];
        hkdf.generateBytes(output, 0, output.length);
        return output;
    }

    /**
     * Generates a 12-byte nonce using the given counter.
     * The first 4 bytes are zero, and the last 8 bytes are the counter in big-endian format.
     *
     * @param counter The counter value.
     * @return The generated nonce.
     */
    public static byte[] generateNonce(int counter) {
        byte[] nonce = new byte[12];
        nonce[4] = (byte) ((counter >> 24) & 0xFF);
        nonce[5] = (byte) ((counter >> 16) & 0xFF);
        nonce[6] = (byte) ((counter >> 8) & 0xFF);
        nonce[7] = (byte) (counter & 0xFF);
        return nonce;
    }

    /**
     * Generates a 12-byte nonce using the given label.
     * The first 4 bytes are zero, and the last 8 bytes come from the label.
     *
     * @param counter The counter value.
     * @return The generated nonce.
     */
    public static byte[] generateNonce(String label) {
        byte[] nonce = new byte[12];
        byte[] labelBytes = label.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(labelBytes, 0, nonce, 4, Math.min(labelBytes.length, 8));
        return nonce;
    }

    // Compute shared secret using ECDH
    public static byte[] generateSharedSecret(X25519PrivateKeyParameters clientPrivateKey,
            X25519PublicKeyParameters serverPublicKey) {
        byte[] secret = new byte[32];
        clientPrivateKey.generateSecret(serverPublicKey, secret, 0);
        return secret;
    }

    // Generate ephemeral X25519 (Curve25519) key pair
    public static X25519PrivateKeyParameters generateX25519KeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {
        return new X25519PrivateKeyParameters(new SecureRandom());
    }

    public static byte[] sha512(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        return md.digest(data);
    }

    // Sign message with Ed25519
    public static byte[] signMessage(Ed25519PrivateKeyParameters privateKey, byte[] message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, privateKey);
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    public static byte[] toUnsigned(BigInteger v, BigInteger N) {
        int len = (N.bitLength() + 7) / 8;
        byte[] raw = v.toByteArray();
        if (raw.length == len) {
            return raw;
        }
        if (raw.length == len + 1 && raw[0] == 0) {
            return Arrays.copyOfRange(raw, 1, raw.length);
        }
        byte[] padded = new byte[len];
        System.arraycopy(raw, 0, padded, len - raw.length, raw.length);
        return padded;
    }

    public static boolean verifySignature(Ed25519PublicKeyParameters publicKey, byte[] payLoad, byte[] signature) {
        Ed25519Signer verifier = new Ed25519Signer();
        verifier.init(false, publicKey);
        verifier.update(payLoad, 0, payLoad.length);
        return verifier.verifySignature(signature);
    }

    public static byte[] xor(byte[] a, byte[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("xor length mismatch");
        }
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    public static String toSpaceDelimitedHex(byte @Nullable [] bytes) {
        if (bytes == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%03d]", bytes.length)).append(' ');
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)).append(' ');
        }
        return sb.toString().trim(); // remove trailing space
    }
}
