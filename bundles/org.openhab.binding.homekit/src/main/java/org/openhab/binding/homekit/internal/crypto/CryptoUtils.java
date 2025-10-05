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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import org.bouncycastle.util.encoders.Hex;
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
    public static byte[] decrypt(byte[] key, byte[] nonce64, byte[] cipherText, byte[] aad)
            throws InvalidCipherTextException {
        byte[] nonce96 = new byte[12]; // 96 bit nonce
        System.arraycopy(nonce64, 0, nonce96, 4, 8);
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce96, aad);
        cipher.init(false, params);
        byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
        int offset = cipher.processBytes(cipherText, 0, cipherText.length, plainText, 0);
        cipher.doFinal(plainText, offset);
        return plainText;
    }

    // Encrypt with ChaCha20-Poly1305
    public static byte[] encrypt(byte[] key, byte[] nonce64, byte[] plainText, byte[] aad)
            throws InvalidCipherTextException {
        byte[] nonce96 = new byte[12]; // 96 bit nonce
        System.arraycopy(nonce64, 0, nonce96, 4, 8);
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce96, aad);
        cipher.init(true, params);
        byte[] cipherText = new byte[cipher.getOutputSize(plainText.length)];
        int offset = cipher.processBytes(plainText, 0, plainText.length, cipherText, 0);
        cipher.doFinal(cipherText, offset);
        return cipherText;
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
     * Generates a 64 bit nonce using the given counter.
     *
     * @param counter The counter value.
     * @return The generated nonce.
     */
    public static byte[] generateNonce64(int counter) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(counter).array();
    }

    // Compute shared secret using ECDH
    public static byte[] generateSharedSecret(X25519PrivateKeyParameters clientEphemeralSecretKey,
            X25519PublicKeyParameters serverEphemeralPublicKey) {
        byte[] secret = new byte[32];
        clientEphemeralSecretKey.generateSecret(serverEphemeralPublicKey, secret, 0);
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

    // Create 64 bit (8-byte) hash
    public static byte[] sha64(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Arrays.copyOf(md.digest(data), 8);
    }

    // Sign message with Ed25519
    public static byte[] signMessage(Ed25519PrivateKeyParameters secretKey, byte[] message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, secretKey);
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    public static BigInteger toBigInteger(String hexBlock) {
        String plainHex = hexBlock.replaceAll("\\s+", "");
        if (plainHex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        return new BigInteger(plainHex, 16);
    }

    public static byte[] toBytes(String hexBlock) {
        String plainHex = hexBlock.replaceAll("\\s+", "");
        if (plainHex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        int length = plainHex.length();
        byte[] result = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int hi = Character.digit(plainHex.charAt(i), 16);
            int lo = Character.digit(plainHex.charAt(i + 1), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException(
                        "Invalid hex character: " + plainHex.charAt(i) + plainHex.charAt(i + 1));
            }
            result[i / 2] = (byte) ((hi << 4) + lo);
        }
        return result;
    }

    public static String toHex(byte @Nullable [] bytes) {
        return bytes == null ? "null" : Hex.toHexString(bytes);
    }

    /**
     * Converts a BigInteger to an unsigned byte array of the specified length.
     * If the byte array representation of the BigInteger is shorter than the specified length,
     * it is left-padded with zeros. If it is longer, an exception is thrown.
     *
     * @param bigInteger the BigInteger to convert.
     * @param length the desired length of the resulting byte array.
     * @return a byte array of the given length representing the unsigned BigInteger.
     * @throws IllegalArgumentException if the BigInteger cannot fit in the specified length.
     */
    public static byte[] toUnsigned(BigInteger bigInteger, int length) {
        byte[] raw = bigInteger.toByteArray();
        if (raw.length == length && raw[0] != 0) {
            return raw;
        }

        byte[] unsigned;
        if (raw[0] == 0) {
            // strip leading sign byte
            unsigned = new byte[raw.length - 1];
            System.arraycopy(raw, 1, unsigned, 0, unsigned.length);
        } else {
            unsigned = raw;
        }

        if (unsigned.length == length) {
            return unsigned;
        }

        // pad to fixed length
        byte[] padded = new byte[length];
        System.arraycopy(unsigned, 0, padded, length - unsigned.length, unsigned.length);
        return padded;
    }

    public static void verifySignature(Ed25519PublicKeyParameters publicKey, byte[] signature, byte[] payload)
            throws Exception {
        Ed25519Signer verifier = new Ed25519Signer();
        verifier.init(false, publicKey);
        verifier.update(payload, 0, payload.length);
        if (!verifier.verifySignature(signature)) {
            throw new SecurityException("Signature verification failed");
        }
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
}
