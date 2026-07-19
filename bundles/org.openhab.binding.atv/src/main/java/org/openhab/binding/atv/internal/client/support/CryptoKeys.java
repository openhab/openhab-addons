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

import java.security.SecureRandom;

import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Raw-byte Ed25519 (RFC 8032) and X25519 (RFC 7748) primitives for HAP pairing.
 *
 * <p>
 * Thin wrappers over BouncyCastle's lightweight API with {@code byte[]}-in/{@code byte[]}-out
 * signatures, so callers never touch provider APIs directly.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CryptoKeys {

    /** Ed25519 seed and raw public key size in bytes. */
    public static final int ED25519_KEY_SIZE = Ed25519PrivateKeyParameters.KEY_SIZE;

    /** Ed25519 signature size in bytes. */
    public static final int ED25519_SIGNATURE_SIZE = Ed25519PrivateKeyParameters.SIGNATURE_SIZE;

    /** X25519 private key, public key and shared secret size in bytes. */
    public static final int X25519_KEY_SIZE = X25519PrivateKeyParameters.KEY_SIZE;

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

    /**
     * Generates a new random Ed25519 key pair.
     */
    public static Ed25519KeyPair ed25519Generate() {
        Ed25519PrivateKeyParameters priv = new Ed25519PrivateKeyParameters(RANDOM);
        return new Ed25519KeyPair(priv.getEncoded(), priv.generatePublicKey().getEncoded());
    }

    /**
     * Recreates an Ed25519 key pair from a 32-byte private seed.
     */
    public static Ed25519KeyPair ed25519FromSeed(byte[] seed) {
        return new Ed25519KeyPair(seed.clone(), ed25519PublicKey(seed));
    }

    /**
     * Derives the 32-byte raw Ed25519 public key from a 32-byte private seed.
     */
    public static byte[] ed25519PublicKey(byte[] seed) {
        return new Ed25519PrivateKeyParameters(seed, 0).generatePublicKey().getEncoded();
    }

    /**
     * Signs a message with Ed25519.
     *
     * @param seed the 32-byte private seed
     * @param message the message to sign
     * @return the 64-byte signature
     */
    public static byte[] ed25519Sign(byte[] seed, byte[] message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, new Ed25519PrivateKeyParameters(seed, 0));
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    /**
     * Verifies an Ed25519 signature.
     *
     * @param publicKey the 32-byte raw public key
     * @param message the signed message
     * @param signature the 64-byte signature
     * @return {@code true} if the signature is valid
     */
    public static boolean ed25519Verify(byte[] publicKey, byte[] message, byte[] signature) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(false, new Ed25519PublicKeyParameters(publicKey, 0));
        signer.update(message, 0, message.length);
        return signer.verifySignature(signature);
    }

    /**
     * Generates a new random X25519 key pair.
     */
    public static X25519KeyPair x25519Generate() {
        X25519PrivateKeyParameters priv = new X25519PrivateKeyParameters(RANDOM);
        return new X25519KeyPair(priv.getEncoded(), priv.generatePublicKey().getEncoded());
    }

    /**
     * Recreates an X25519 key pair from a 32-byte private scalar.
     */
    public static X25519KeyPair x25519FromPrivate(byte[] privateKey) {
        return new X25519KeyPair(privateKey.clone(), x25519PublicKey(privateKey));
    }

    /**
     * Derives the 32-byte raw X25519 public key from a 32-byte private scalar.
     */
    public static byte[] x25519PublicKey(byte[] privateKey) {
        return new X25519PrivateKeyParameters(privateKey, 0).generatePublicKey().getEncoded();
    }

    /**
     * Computes the X25519 shared secret.
     *
     * @param privateKey own 32-byte private scalar
     * @param peerPublicKey peer's 32-byte raw public key
     * @return the 32-byte shared secret
     */
    public static byte[] x25519SharedSecret(byte[] privateKey, byte[] peerPublicKey) {
        X25519Agreement agreement = new X25519Agreement();
        agreement.init(new X25519PrivateKeyParameters(privateKey, 0));
        byte[] secret = new byte[agreement.getAgreementSize()];
        agreement.calculateAgreement(new X25519PublicKeyParameters(peerPublicKey, 0), secret, 0);
        return secret;
    }
}
