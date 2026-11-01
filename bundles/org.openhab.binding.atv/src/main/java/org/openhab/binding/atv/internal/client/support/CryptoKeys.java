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
package org.openhab.binding.atv.internal.client.support;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPrivateKeySpec;
import java.security.spec.XECPublicKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Raw-byte Ed25519 (RFC 8032) and X25519 (RFC 7748) primitives for HAP pairing.
 *
 * <p>
 * Thin wrappers over the JDK's EdDSA and XDH providers with {@code byte[]}-in/{@code byte[]}-out
 * signatures, so callers never touch provider APIs directly. Keys use the RFC wire encodings:
 * 32 little-endian bytes, with the Ed25519 public key carrying the sign of x in its top bit.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CryptoKeys {

    /** Ed25519 seed and raw public key size in bytes. */
    public static final int ED25519_KEY_SIZE = 32;

    /** Ed25519 signature size in bytes. */
    public static final int ED25519_SIGNATURE_SIZE = 64;

    /** X25519 private key, public key and shared secret size in bytes. */
    public static final int X25519_KEY_SIZE = 32;

    private static final String ED25519 = "Ed25519";
    private static final String X25519 = "X25519";

    // The X25519 base point is u = 9, so X25519(k, 9) is the public key for k.
    private static final byte[] X25519_BASE_POINT = basePoint();

    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoKeys() {
    }

    /**
     * An Ed25519 key pair as raw bytes.
     *
     * @param seed the 32-byte private seed
     * @param publicKey the 32-byte raw public key
     */
    public record Ed25519KeyPair(byte[] seed, byte[] publicKey) {
    }

    /**
     * An X25519 key pair as raw bytes.
     *
     * @param privateKey the 32-byte private scalar
     * @param publicKey the 32-byte raw public key (u-coordinate)
     */
    public record X25519KeyPair(byte[] privateKey, byte[] publicKey) {
    }

    /** Generates a new random Ed25519 key pair. */
    public static Ed25519KeyPair ed25519Generate() {
        byte[] seed = new byte[ED25519_KEY_SIZE];
        RANDOM.nextBytes(seed);
        return ed25519FromSeed(seed);
    }

    /** Recreates an Ed25519 key pair from a 32-byte private seed. */
    public static Ed25519KeyPair ed25519FromSeed(byte[] seed) {
        return new Ed25519KeyPair(seed.clone(), ed25519PublicKey(seed));
    }

    /**
     * Derives the 32-byte raw Ed25519 public key from a 32-byte private seed.
     *
     * <p>
     * The JDK has no API to derive an EdDSA public key from a private key, but its key pair
     * generator draws the seed from the supplied random source. Feeding it the seed yields the
     * matching public key, and the generated private key is checked against the seed.
     */
    public static byte[] ed25519PublicKey(byte[] seed) {
        requireLength(seed, ED25519_KEY_SIZE, "Ed25519 seed");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ED25519);
            generator.initialize(NamedParameterSpec.ED25519, new FixedSeedRandom(seed));
            KeyPair pair = generator.generateKeyPair();
            byte[] generatedSeed = ((EdECPrivateKey) pair.getPrivate()).getBytes().orElse(new byte[0]);
            if (!Arrays.equals(generatedSeed, seed)) {
                throw new IllegalStateException("Ed25519 key generator did not use the supplied seed");
            }
            return encodeEdPoint(((EdECPublicKey) pair.getPublic()).getPoint());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Ed25519 is not available", e);
        }
    }

    /**
     * Signs a message with Ed25519.
     *
     * @param seed the 32-byte private seed
     * @param message the message to sign
     * @return the 64-byte signature
     */
    public static byte[] ed25519Sign(byte[] seed, byte[] message) {
        requireLength(seed, ED25519_KEY_SIZE, "Ed25519 seed");
        try {
            PrivateKey key = KeyFactory.getInstance(ED25519)
                    .generatePrivate(new EdECPrivateKeySpec(NamedParameterSpec.ED25519, seed));
            Signature signer = Signature.getInstance(ED25519);
            signer.initSign(key);
            signer.update(message);
            return signer.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Ed25519 signing failed", e);
        }
    }

    /**
     * Verifies an Ed25519 signature.
     *
     * @param publicKey the 32-byte raw public key
     * @param message the signed message
     * @param signature the 64-byte signature
     * @return {@code true} if the signature is valid; {@code false} if it is not, or if the
     *         public key or signature is malformed
     */
    public static boolean ed25519Verify(byte[] publicKey, byte[] message, byte[] signature) {
        if (publicKey.length != ED25519_KEY_SIZE || signature.length != ED25519_SIGNATURE_SIZE) {
            return false;
        }
        try {
            PublicKey key = KeyFactory.getInstance(ED25519)
                    .generatePublic(new EdECPublicKeySpec(NamedParameterSpec.ED25519, decodeEdPoint(publicKey)));
            Signature verifier = Signature.getInstance(ED25519);
            verifier.initVerify(key);
            verifier.update(message);
            return verifier.verify(signature);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    /** Generates a new random X25519 key pair. */
    public static X25519KeyPair x25519Generate() {
        byte[] privateKey = new byte[X25519_KEY_SIZE];
        RANDOM.nextBytes(privateKey);
        // Store the scalar already clamped (RFC 7748 section 5); X25519 clamps on use either way.
        privateKey[0] &= (byte) 0xF8;
        privateKey[X25519_KEY_SIZE - 1] &= 0x7F;
        privateKey[X25519_KEY_SIZE - 1] |= 0x40;
        return x25519FromPrivate(privateKey);
    }

    /** Recreates an X25519 key pair from a 32-byte private scalar. */
    public static X25519KeyPair x25519FromPrivate(byte[] privateKey) {
        return new X25519KeyPair(privateKey.clone(), x25519PublicKey(privateKey));
    }

    /** Derives the 32-byte raw X25519 public key from a 32-byte private scalar. */
    public static byte[] x25519PublicKey(byte[] privateKey) {
        return x25519SharedSecret(privateKey, X25519_BASE_POINT);
    }

    /**
     * Computes the X25519 shared secret.
     *
     * @param privateKey own 32-byte private scalar
     * @param peerPublicKey peer's 32-byte raw public key
     * @return the 32-byte shared secret
     * @throws IllegalStateException if the peer key is a low-order point (all-zero secret)
     */
    public static byte[] x25519SharedSecret(byte[] privateKey, byte[] peerPublicKey) {
        requireLength(privateKey, X25519_KEY_SIZE, "X25519 private key");
        requireLength(peerPublicKey, X25519_KEY_SIZE, "X25519 public key");
        try {
            KeyFactory factory = KeyFactory.getInstance(X25519);
            PrivateKey priv = factory.generatePrivate(new XECPrivateKeySpec(NamedParameterSpec.X25519, privateKey));
            PublicKey pub = factory
                    .generatePublic(new XECPublicKeySpec(NamedParameterSpec.X25519, decodeU(peerPublicKey)));
            KeyAgreement agreement = KeyAgreement.getInstance(X25519);
            agreement.init(priv);
            agreement.doPhase(pub, true);
            return agreement.generateSecret();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("X25519 agreement failed", e);
        }
    }

    /** Decodes a little-endian u-coordinate, ignoring the unused top bit (RFC 7748 section 5). */
    private static BigInteger decodeU(byte[] encoded) {
        return decodeLittleEndianWithoutTopBit(encoded);
    }

    /** Decodes an RFC 8032 point: little-endian y with the sign of x in the top bit. */
    private static EdECPoint decodeEdPoint(byte[] encoded) {
        boolean xOdd = (encoded[encoded.length - 1] & 0x80) != 0;
        return new EdECPoint(xOdd, decodeLittleEndianWithoutTopBit(encoded));
    }

    private static byte[] encodeEdPoint(EdECPoint point) {
        BigInteger y = point.getY();
        byte[] encoded = new byte[ED25519_KEY_SIZE];
        for (int i = 0; i < ED25519_KEY_SIZE; i++) {
            encoded[i] = y.shiftRight(8 * i).byteValue();
        }
        if (point.isXOdd()) {
            encoded[ED25519_KEY_SIZE - 1] |= (byte) 0x80;
        }
        return encoded;
    }

    private static BigInteger decodeLittleEndianWithoutTopBit(byte[] encoded) {
        BigInteger value = BigInteger.valueOf(encoded[encoded.length - 1] & 0x7F);
        for (int i = encoded.length - 2; i >= 0; i--) {
            value = value.shiftLeft(8).or(BigInteger.valueOf(encoded[i] & 0xFF));
        }
        return value;
    }

    private static byte[] basePoint() {
        byte[] u = new byte[X25519_KEY_SIZE];
        u[0] = 9;
        return u;
    }

    private static void requireLength(byte[] bytes, int length, String name) {
        if (bytes.length != length) {
            throw new IllegalArgumentException(name + " must be " + length + " bytes, got " + bytes.length);
        }
    }

    /** A random source that hands out a fixed seed once, used to derive an Ed25519 public key. */
    @NonNullByDefault({})
    private static final class FixedSeedRandom extends SecureRandom {
        private static final long serialVersionUID = 1L;

        private final byte[] seed;
        private boolean used;

        FixedSeedRandom(byte[] seed) {
            this.seed = seed.clone();
        }

        @Override
        public synchronized void nextBytes(byte[] bytes) {
            if (used || bytes.length != seed.length) {
                throw new IllegalStateException("unexpected random request of " + bytes.length + " bytes");
            }
            used = true;
            System.arraycopy(seed, 0, bytes, 0, seed.length);
        }
    }
}
