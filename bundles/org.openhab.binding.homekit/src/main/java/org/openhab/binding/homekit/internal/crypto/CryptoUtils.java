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

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.enums.TlvType;

/**
 * Utility class for cryptographic operations used in HomeKit communication.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CryptoUtils {

    private static final SecureRandom random = new SecureRandom();

    // Generate ephemeral Curve25519 key pair
    public static AsymmetricCipherKeyPair generateCurve25519KeyPair() {
        X25519KeyPairGenerator generator = new X25519KeyPairGenerator();
        generator.init(new X25519KeyGenerationParameters(random));
        return generator.generateKeyPair();
    }

    // Compute shared secret using ECDH
    public static byte[] computeSharedSecret(AsymmetricKeyParameter privateKey, AsymmetricKeyParameter peerPublicKey) {
        X25519Agreement agreement = new X25519Agreement();
        agreement.init(privateKey);
        byte[] sharedSecret = new byte[agreement.getAgreementSize()];
        agreement.calculateAgreement(peerPublicKey, sharedSecret, 0);
        return sharedSecret;
    }

    // HKDF-SHA512 key derivation
    public static byte[] hkdf(byte[] ikm, String salt, String info) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(
                new HKDFParameters(ikm, salt.getBytes(StandardCharsets.UTF_8), info.getBytes(StandardCharsets.UTF_8)));
        byte[] output = new byte[32];
        hkdf.generateBytes(output, 0, output.length);
        return output;
    }

    // Encrypt with ChaCha20-Poly1305
    public static byte[] encrypt(byte[] key, String nonceStr, byte[] plaintext) throws InvalidCipherTextException {
        return encrypt(key, nonceStr.getBytes(StandardCharsets.UTF_8), plaintext);
    }

    // Encrypt with ChaCha20-Poly1305
    public static byte[] encrypt(byte[] key, byte[] nonce, byte[] plaintext) throws InvalidCipherTextException {
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce);
        cipher.init(true, params);

        byte[] out = new byte[cipher.getOutputSize(plaintext.length)];
        int len = cipher.processBytes(plaintext, 0, plaintext.length, out, 0);
        cipher.doFinal(out, len);
        return out;
    }

    // Decrypt with ChaCha20-Poly1305
    public static byte[] decrypt(byte[] key, String nonceStr, byte[] ciphertext) throws InvalidCipherTextException {
        return decrypt(key, nonceStr.getBytes(StandardCharsets.UTF_8), ciphertext);
    }

    // Decrypt with ChaCha20-Poly1305
    public static byte[] decrypt(byte[] key, byte[] nonce, byte[] ciphertext) throws InvalidCipherTextException {
        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce);
        cipher.init(false, params);

        byte[] out = new byte[cipher.getOutputSize(ciphertext.length)];
        int len = cipher.processBytes(ciphertext, 0, ciphertext.length, out, 0);
        cipher.doFinal(out, len);
        return out;
    }

    // Sign Pair-Verify message with Ed25519
    public static byte[] signVerifyMessage(Ed25519PrivateKeyParameters privateKey, byte[] message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, privateKey);
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    // Validate accessory identity and signature
    public static void validateAccessory(Map<Integer, byte[]> tlv) {
        byte[] identifier = tlv.get(TlvType.IDENTIFIER.key);
        byte[] signature = tlv.get(TlvType.SIGNATURE.key);
        byte[] publicKey = tlv.get(TlvType.PUBLIC_KEY.key);

        if (identifier == null || signature == null || publicKey == null) {
            throw new SecurityException("Missing accessory credentials");
        }

        Ed25519PublicKeyParameters pubKey = new Ed25519PublicKeyParameters(publicKey, 0);
        Ed25519Signer verifier = new Ed25519Signer();
        verifier.init(false, pubKey);
        verifier.update(identifier, 0, identifier.length);

        boolean valid = verifier.verifySignature(signature);
        if (!valid) {
            throw new SecurityException("Accessory signature verification failed");
        }
    }
}
