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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.LegacySrpClient;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.support.CryptoKeys;

/**
 * SRP data and crypto routines for AirPlay legacy device authentication and verification.
 *
 * <p>
 * The AES encryption routine lives in {@link Aes}. The SRP arithmetic itself (2048-bit
 * group, SHA-1, Apple's dual-hash session key) is provided by {@link LegacySrpClient}.
 *
 * <p>
 * The 32-byte credentials seed ({@code ltsk}) triples as the Ed25519 signing seed, the
 * X25519 verify private key and the SRP client private ephemeral.
 *
 * <p>
 * Note: {@code step2} does not perform a real server-proof check — it verifies its own
 * locally computed proof against itself, which always succeeds — so authentication failures
 * surface as HTTP 403 responses from the device instead.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class LegacySrpAuthHandler {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * The client public key and proof produced by {@link #step2(byte[], byte[])}
     * (already unhexlified).
     *
     * @param publicKey the SRP client public value {@code A}
     * @param proof the SRP client proof {@code M1}
     */
    public record KeyProof(byte[] publicKey, byte[] proof) {
    }

    /**
     * The encrypted public key and GCM tag produced by {@link #step3()}.
     *
     * @param epk our Ed25519 public key encrypted with AES-128-GCM
     * @param tag the 16-byte GCM authentication tag
     */
    public record Epk(byte[] epk, byte[] tag) {
    }

    private final HapCredentials credentials;

    private @Nullable LegacySrpClient session;
    private byte @Nullable [] authPrivate;
    private byte @Nullable [] authPublic;
    private byte @Nullable [] verifyPrivate;
    private byte @Nullable [] publicBytes;

    /**
     * Creates a new handler for the given credentials.
     *
     * @param credentials legacy credentials whose {@code ltsk} is the 32-byte seed and whose
     *            {@code clientId} is the pairing identifier
     */
    public LegacySrpAuthHandler(HapCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Generates a new identifier and seed for authentication.
     *
     * <p>
     * Auth here is technically not HAP, but for the sake of abstraction HAP is emulated:
     * LTSK holds the 32-byte seed and client id the 8-byte identifier.
     *
     * @return fresh legacy credentials
     */
    public static HapCredentials newCredentials() {
        byte[] seed = new byte[32];
        byte[] identifier = new byte[8];
        RANDOM.nextBytes(seed);
        RANDOM.nextBytes(identifier);
        return new HapCredentials(new byte[0], seed, new byte[0], identifier);
    }

    /**
     * The credentials this handler operates on.
     */
    public HapCredentials credentials() {
        return credentials;
    }

    /**
     * Initializes handler operation: derives the Ed25519 authentication key pair from the
     * credentials seed.
     */
    public void initialize() {
        byte[] newAuthPrivate = credentials.ltsk();
        this.authPrivate = newAuthPrivate;
        this.authPublic = CryptoKeys.ed25519PublicKey(newAuthPrivate);
    }

    /**
     * First device verification step: derives the X25519 verify key
     * pair from the credentials seed and produces the raw body
     * {@code 0x01 0x00 0x00 0x00 || verifyPublic || authPublic}.
     *
     * @return raw {@code /pair-verify} step 1 body
     */
    public byte[] verify1() {
        byte[] authPub = requireAuthPublic();
        byte[] newVerifyPrivate = credentials.ltsk();
        this.verifyPrivate = newVerifyPrivate;
        byte[] newPublicBytes = CryptoKeys.x25519PublicKey(newVerifyPrivate);
        this.publicBytes = newPublicBytes;
        return concat(new byte[] { 0x01, 0x00, 0x00, 0x00 }, newPublicBytes, authPub);
    }

    /**
     * Last device verification step: performs the X25519 exchange,
     * derives the AES-128-CTR key/IV from SHA-512 of {@code "Pair-Verify-AES-Key"}/{@code
     * "Pair-Verify-AES-IV"} plus the shared secret, signs {@code ourPublic || atvPublic} with
     * Ed25519 and encrypts the signature (the device's extra data advances the keystream
     * first). The result is prepended with {@code 0x00000000}.
     *
     * @param atvPublicKey the device's X25519 public key (first 32 bytes of the step-1 reply)
     * @param data the remaining bytes of the step-1 reply (purpose unknown)
     * @return raw {@code /pair-verify} step 2 body
     */
    public byte[] verify2(byte[] atvPublicKey, byte[] data) {
        byte[] currentVerifyPrivate = verifyPrivate;
        if (currentVerifyPrivate == null) {
            throw new InvalidStateError("verify1 not performed");
        }

        // Generate a shared secret key
        byte[] shared = CryptoKeys.x25519SharedSecret(currentVerifyPrivate, atvPublicKey);

        // Derive new AES key and IV from shared key
        byte[] aesKey = Arrays.copyOf(hashSha512("Pair-Verify-AES-Key".getBytes(StandardCharsets.UTF_8), shared), 16);
        byte[] aesIv = Arrays.copyOf(hashSha512("Pair-Verify-AES-IV".getBytes(StandardCharsets.UTF_8), shared), 16);

        // Sign public keys and encrypt with AES
        byte[] signed = CryptoKeys.ed25519Sign(requireAuthPrivate(), concat(requirePublicBytes(), atvPublicKey));
        byte[] signature = Aes.ctrEncryptLast(aesKey, aesIv, data, signed);

        // Signature is prepended with 0x00000000 (alignment?)
        return concat(new byte[] { 0x00, 0x00, 0x00, 0x00 }, signature);
    }

    /**
     * First authentication step: begins the SRP session with the
     * username (uppercase-hex client identifier) and PIN, using the credentials seed as the
     * client private ephemeral.
     *
     * @param username the SRP username
     * @param password the PIN shown on screen
     */
    public void step1(String username, String password) {
        byte[] currentAuthPrivate = requireAuthPrivate();
        LegacySrpClient newSession = new LegacySrpClient(currentAuthPrivate::clone);
        this.session = newSession;
        newSession.step1(username, password);
    }

    /**
     * Second authentication step: processes the device's SRP public
     * key and salt and returns our public key and proof.
     *
     * @param pubKey the device SRP public value {@code B}
     * @param salt the SRP salt
     * @return our public key and proof
     */
    public KeyProof step2(byte[] pubKey, byte[] salt) {
        LegacySrpClient currentSession = requireSession();
        currentSession.process(pubKey, salt);
        // Skipping the tautological verification of our own locally computed proof.
        return new KeyProof(currentSession.publicKey(), currentSession.proof());
    }

    /**
     * Last authentication step: derives the AES-128-GCM key/IV from
     * SHA-512 of {@code "Pair-Setup-AES-Key"}/{@code "Pair-Setup-AES-IV"} plus the SRP
     * session key — the last IV byte is increased by one — and encrypts our Ed25519 public
     * key.
     *
     * @return the encrypted public key ({@code epk}) and GCM tag
     */
    public Epk step3() {
        LegacySrpClient currentSession = requireSession();
        byte[] sessionKey = currentSession.sessionKey();

        byte[] aesKey = Arrays.copyOf(hashSha512("Pair-Setup-AES-Key".getBytes(StandardCharsets.UTF_8), sessionKey),
                16);
        byte[] aesIv = Arrays.copyOf(hashSha512("Pair-Setup-AES-IV".getBytes(StandardCharsets.UTF_8), sessionKey), 16);
        aesIv[aesIv.length - 1] = (byte) (aesIv[aesIv.length - 1] + 1); // Last byte must be increased by 1

        Aes.GcmResult result = Aes.gcmEncrypt(aesKey, aesIv, requireAuthPublic());
        return new Epk(result.ciphertext(), result.tag());
    }

    private byte[] requireAuthPrivate() {
        byte[] value = authPrivate;
        if (value == null) {
            throw new InvalidStateError("handler not initialized");
        }
        return value;
    }

    private byte[] requireAuthPublic() {
        byte[] value = authPublic;
        if (value == null) {
            throw new InvalidStateError("handler not initialized");
        }
        return value;
    }

    private byte[] requirePublicBytes() {
        byte[] value = publicBytes;
        if (value == null) {
            throw new InvalidStateError("verify1 not performed");
        }
        return value;
    }

    private LegacySrpClient requireSession() {
        LegacySrpClient value = session;
        if (value == null) {
            throw new InvalidStateError("step1 not performed");
        }
        return value;
    }

    /** SHA-512 over the concatenation of the inputs. */
    private static byte[] hashSha512(byte[]... inputs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            for (byte[] input : inputs) {
                digest.update(input);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 not available", e);
        }
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
