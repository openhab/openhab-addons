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
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.SessionKeys;

/**
 * Manages the SRP (Stanford Secure Remote Password) protocol for pairing with a HomeKit accessory.
 * This class handles the SRP steps, including key generation, proof verification, and encryption of identifiers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SRPclient {

    public static final String PAIR_SETUP = "Pair-Setup";

    private static final BigInteger N = new BigInteger("""
            FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1 29024E08 8A67CC74
            020BBEA6 3B139B22 514A0879 8E3404DD EF9519B3 CD3A431B 302B0A6D F25F1437
            4FE1356D 6D51C245 E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED
            EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D C2007CB8 A163BF05
            98DA4836 1C55D39A 69163FA8 FD24CF5F 83655D23 DCA3AD96 1C62F356 208552BB
            9ED52907 7096966D 670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B
            E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9 DE2BCBF6 95581718
            3995497C EA956AE5 15D22618 98FA0510 15728E5A 8AAAC42D AD33170D 04507A33
            A85521AB DF1CBA64 ECFB8504 58DBEF0A 8AEA7157 5D060C7D B3970F85 A6E1E4C7
            ABF5AE8C DB0933D7 1E8C94E0 4A25619D CEE3D226 1AD2EE6B F12FFA06 D98A0864
            D8760273 3EC86A64 521F2B18 177B200C BBE11757 7A615D6C 770988C0 BAD946E2
            08E24FA0 74E5AB31 43DB5BFC E0FD108E 4B82D120 A93AD2CA FFFFFFFF FFFFFFFF
            """.replaceAll("\\s+", ""), 16);

    private static final BigInteger g = BigInteger.valueOf(5);
    private static final BigInteger k = computeK();
    private static final SecureRandom random = new SecureRandom();

    private final String I; // username
    private final byte[] s; // salt
    private final BigInteger x; // private key derived from password

    private @NonNullByDefault({}) BigInteger a; // client private ephemeral
    private @NonNullByDefault({}) BigInteger A; // client public key
    private @NonNullByDefault({}) BigInteger B; // server public key
    private @NonNullByDefault({}) BigInteger u; // scrambling parameter
    private @NonNullByDefault({}) BigInteger S; // shared secret
    private @NonNullByDefault({}) byte[] K; // session key
    private @NonNullByDefault({}) byte[] M1; // client proof

    private @Nullable String serverIdentifier;
    private byte @Nullable [] serverPublicKey;

    // HomeKit‐specific constants for HKDF/ChaCha20‐Poly1305
    private static final byte[] PAIR_SALT = "Pair-Setup-Encrypt-Salt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_INFO = "Pair-Setup-Encrypt-Info".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_SIGN_SALT = "Pair-Setup-Sign-Salt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_SIGN_INFO = "Pair-Setup-Sign-Info".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_NONCE_M5 = CryptoUtils.generateNonce("PS-Msg05");
    private static final byte[] PAIR_NONCE_M6 = CryptoUtils.generateNonce("PS-Msg06");

    /**
     * M1 — Initializes the SRP client with the given username, password, and salt.
     *
     * @param username the username (I).
     * @param password the password (P).
     * @param salt the salt (s) provided by the server.
     * @throws Exception if an error occurs during initialization.
     */
    public SRPclient(String username, String password, byte[] salt) throws Exception {
        this.I = username;
        this.s = salt;

        // Compute verifier: v = g^x mod N where x = H(salt || H(username || ":" || password))
        byte[] hIP = sha512((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        byte[] xHash = sha512(concat(salt, hIP));
        this.x = new BigInteger(1, xHash);
    }

    /**
     * M2 — Process the server's challenge by storing the server's Curve25519 public key B.
     *
     * @param serverPublicKey the server's Curve25519 public key B.
     */
    public void processChallenge(byte[] serverPublicKey) {
        this.B = new BigInteger(1, serverPublicKey);
    }

    /**
     * M3 — Generate the client's Curve25519 ephemeral key pair (a, A) and return the public key A.
     *
     * @return the client's Curve25519 public key A.
     */
    public byte[] getPublicKey() {
        if (A == null) {
            this.a = new BigInteger(N.bitLength(), random).mod(N);
            this.A = g.modPow(a, N);
        }
        return toUnsigned(A, N);
    }

    /**
     * M3 — Compute the client proof M1 = H(H(N) ⊕ H(g) || H(I) || salt || A || B || K).
     *
     * @return the client proof M1.
     * @throws IllegalStateException if the SRP state is not properly initialized.
     */
    public byte[] getClientProof() throws Exception {
        if (M1 != null) {
            return M1;
        }
        if (A == null || B == null || a == null) {
            throw new IllegalStateException("SRP state not initialized");
        }
        if (B.mod(N).equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid server public key");
        }

        // Compute u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(A, N), toUnsigned(B, N)));
        this.u = new BigInteger(1, uHash);
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid scrambling parameter");
        }

        // Compute S = (B - k·g^x)^(a + u·x) mod N
        BigInteger gx = g.modPow(x, N);
        BigInteger base = B.subtract(k.multiply(gx)).mod(N);
        BigInteger exp = a.add(u.multiply(x));
        this.S = base.modPow(exp, N);

        // Compute session key K = H(S)
        this.K = sha512(toUnsigned(S, N));

        // Compute client proof M1 = H(H(N) ⊕ H(g) || H(I) || salt || A || B || K)
        byte[] HN = sha512(toUnsigned(N, N));
        byte[] Hg = sha512(toUnsigned(g, N));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));

        this.M1 = sha512(concat(Hxor, HI, s, toUnsigned(A, N), toUnsigned(B, N), K));
        return M1;
    }

    /**
     * M4 — Verify the server's proof M2 = H(A || M1 || K).
     *
     * @param serverProof the server's proof to verify.
     * @throws SecurityException if the proof does not match.
     */
    public void verifyServerProof(byte[] serverProof) throws Exception {
        byte[] expected = sha512(concat(toUnsigned(A, N), M1, K));
        if (!Arrays.equals(expected, serverProof)) {
            throw new SecurityException("SRP server proof mismatch");
        }
    }

    /**
     * M5 — Derive the 32‐byte signing key from HKDF(S, salt, info).
     */
    public byte[] deriveIOSDeviceXKey() {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(toUnsigned(S, N), PAIR_SIGN_SALT, PAIR_SIGN_INFO));
        byte[] xKey = new byte[32];
        hkdf.generateBytes(xKey, 0, xKey.length);
        return xKey;
    }

    /**
     * M5 — Encrypt the derived Curve25519 key, the accessory identifier, and the long term Curve25519 public key.
     *
     * @param accessoryId UTF-8 string identifier of the controller.
     * @param signingKey Ed25519 private key for signing the TLV.
     *
     * @return the ChaCha20-Poly1305‐encrypted TLV blob for M5.
     */
    public byte[] getEncryptedDeviceInfoBlob(byte[] iOSDeviceXKey, String pairingIdentifier,
            Ed25519PublicKeyParameters controllerLongTermKey, Ed25519PrivateKeyParameters signingKey)
            throws Exception {
        // 1) Build sub-TLV with iOSDeviceXKey, pairing identifier, and controller long-term public key
        byte[] blob = concat(iOSDeviceXKey, pairingIdentifier.getBytes(StandardCharsets.UTF_8),
                controllerLongTermKey.getEncoded());
        Map<Integer, byte[]> subTlv = new LinkedHashMap<>();
        subTlv.put(TlvType.IDENTIFIER.key, blob);
        byte[] controllerPk = signingKey.generatePublicKey().getEncoded();
        subTlv.put(TlvType.PUBLIC_KEY.key, controllerPk);

        // 2) Encode & sign the sub-TLV
        byte[] msg = Tlv8Codec.encode(subTlv);
        byte[] signature = CryptoUtils.signVerifyMessage(signingKey, msg);
        subTlv.put(TlvType.SIGNATURE.key, signature);

        // 3) Re-encode the signed TLV
        byte[] plaintext = Tlv8Codec.encode(subTlv);

        // 4) Encrypt with session write key and fixed nonce
        byte[] writeKey = deriveSessionKeys().getWriteKey();
        return CryptoUtils.encrypt(writeKey, PAIR_NONCE_M5, plaintext);
    }

    /**
     * M6 — Decrypt and store accessory identifier + Curve25519 public key.
     */
    public void verifyAccessoryIdentifiers(byte[] encryptedData) throws Exception {
        // 1) Decrypt using the session's read key and fixed nonce
        byte[] decrypted = CryptoUtils.decrypt(deriveSessionKeys().getReadKey(), PAIR_NONCE_M6, encryptedData);

        // 2) Parse TLV to extract accessory identifier and public key
        Map<Integer, byte[]> tlv = Tlv8Codec.decode(decrypted);
        byte[] idBytes = tlv.get(TlvType.IDENTIFIER.key);
        byte[] pkBytes = tlv.get(TlvType.PUBLIC_KEY.key);

        if (idBytes == null || pkBytes == null) {
            throw new SecurityException("Missing accessory credentials in M6");
        }

        // 3) Store for later use
        this.serverIdentifier = new String(idBytes, StandardCharsets.UTF_8);
        this.serverPublicKey = pkBytes;
    }

    /**
     * After M6 — Derive the 32‐byte session key with HKDF(K, salt, info).
     */
    public SessionKeys deriveSessionKeys() {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(K, PAIR_SALT, PAIR_INFO));

        byte[] sessionKey = new byte[32];
        hkdf.generateBytes(sessionKey, 0, sessionKey.length);

        // HomeKit uses the same key for read/write
        return new SessionKeys(sessionKey, sessionKey);
    }

    /*
     * Returns the stored server identifier after M6.
     *
     * @return the server's identifier string, or null if not yet set.
     */
    public @Nullable String getServerIdentifier() {
        return serverIdentifier;
    }

    /*
     * Returns the stored server SRP public key after M6.
     *
     * @return the server's Curve25519 public key, or null if not yet set.
     */
    public byte @Nullable [] getServerPublicKey() {
        return serverPublicKey;
    }

    // ─── Utility Methods ──────────────────────────────────────────────────────

    private static BigInteger computeK() {
        try {
            byte[] paddedN = toUnsigned(N, N);
            byte[] paddedG = toUnsigned(g, N);
            byte[] hash = sha512(concat(paddedN, paddedG));
            return new BigInteger(1, hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute k", e);
        }
    }

    private static byte[] sha512(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        return md.digest(data);
    }

    private static byte[] concat(byte[]... parts) {
        int total = Arrays.stream(parts).mapToInt(p -> p.length).sum();
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }

    private static byte[] xor(byte[] a, byte[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("xor length mismatch");
        }
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    private static byte[] toUnsigned(BigInteger v, BigInteger N) {
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
}
