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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.TlvType;

/**
 * Simulated Stanford Secure Remote Protocol test server used for JUnits tests.
 * The implementation is intentionally separate from the Client implementation in order avoid self referencing tests.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SRPserver {

    // Constants (HomeKit SRP-6a)
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

    // HomeKit‐specific constants for HKDF/ChaCha20‐Poly1305
    private static final byte[] PAIR_SALT = "Pair-Setup-Encrypt-Salt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_INFO = "Pair-Setup-Encrypt-Info".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_NONCE_M6 = CryptoUtils.generateNonce("PS-Msg06");

    // Session state
    private final String I; // username
    private final byte[] s; // salt
    private final BigInteger v; // verifier
    private final BigInteger b; // private SRP key ephemeral value
    private final BigInteger B; // public SRP key ephemeral value

    private byte @Nullable [] K = null;
    private byte @Nullable [] clientPublicSigningKey = null;

    public SRPserver(String username, String password, byte[] salt) throws Exception {
        this.I = username;
        this.s = salt;

        // Compute verifier once
        byte[] hIP = sha512((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        BigInteger x = new BigInteger(1, sha512(concat(salt, hIP)));
        this.v = g.modPow(x, N);

        // Generate ephemeral b and compute public B
        this.b = new BigInteger(N.bitLength(), random).mod(N);
        BigInteger gb = g.modPow(b, N);
        this.B = k.multiply(v).add(gb).mod(N);
    }

    /**
     * * M2 — Get server public key B
     *
     * @return Server public key B
     */
    public byte[] getPublicKey() {
        return toUnsigned(B, N);
    }

    /**
     * M4 — Compute server proof M2 = H(A || M1 || K)
     *
     * @param clientPublicSigningKey Client Curve25519 public key A (32 bytes)
     * @return Server proof M2
     */
    public byte[] computeServerProof(byte[] clientPublicSigningKey) throws Exception {
        this.clientPublicSigningKey = clientPublicSigningKey;
        BigInteger clientPublic = new BigInteger(1, clientPublicSigningKey);
        if (clientPublic.mod(N).equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid client public key");
        }

        // Compute u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(clientPublic, N), toUnsigned(B, N)));
        BigInteger u = new BigInteger(1, uHash);
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid scrambling parameter");
        }

        // Compute shared secret S = (A * v^u)^b mod N
        BigInteger vu = v.modPow(u, N);
        BigInteger S = clientPublic.multiply(vu).mod(N).modPow(b, N);
        this.K = sha512(toUnsigned(S, N));

        // Compute M1 = H(H(N) ⊕ H(g) || H(I) || salt || A || B || K)
        byte[] HN = sha512(toUnsigned(N, N));
        byte[] Hg = sha512(toUnsigned(g, N));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));

        byte[] M1 = sha512(concat(Hxor, HI, s, toUnsigned(clientPublic, N), toUnsigned(B, N), K));

        // Compute M2 = H(A || M1 || K)
        return sha512(concat(toUnsigned(clientPublic, N), M1, K));
    }

    /**
     * M6 — Encrypt accessory identifier and Curve25519 public key. And sign the TLV with Ed25519 key.
     *
     * @param serverIdentifier UTF-8 string identifier of the accessory
     * @param serverPublicSigningKey Curve25519 public key (32 bytes)
     * @param accessoryPrivateKey Ed25519 private key for signing the TLV
     * @return encrypted TLV payload for M6
     */
    public byte[] createEncryptedData(String serverIdentifier, byte[] serverPublicSigningKey,
            Ed25519PrivateKeyParameters accessoryPrivateKey) throws Exception {
        if (K == null) {
            throw new IllegalStateException("Session key K not established");
        }
        // 1) Build sub-TLV with controller signing public key, pairing identifier, nd server signing public key
        byte[] complexIdentifier = concat(clientPublicSigningKey, serverIdentifier.getBytes(StandardCharsets.UTF_8),
                serverPublicSigningKey);
        Map<Integer, byte[]> subTlv = Map.of( //
                TlvType.IDENTIFIER.key, complexIdentifier, //
                TlvType.PUBLIC_KEY.key, serverPublicSigningKey);

        // 2) Encode and sign the TLV
        byte[] message = Tlv8Codec.encode(subTlv);
        byte[] signature = CryptoUtils.signVerifyMessage(accessoryPrivateKey, message);
        subTlv.put(TlvType.SIGNATURE.key, signature);

        // 3) Re-encode signed TLV
        byte[] plaintext = Tlv8Codec.encode(subTlv);

        // 4) Derive session key using HKDF(S, salt, info)
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(K, PAIR_SALT, PAIR_INFO));
        byte[] sessionKey = new byte[32];
        hkdf.generateBytes(sessionKey, 0, sessionKey.length);

        // 5) Encrypt using ChaCha20-Poly1305
        return CryptoUtils.encrypt(sessionKey, PAIR_NONCE_M6, plaintext);
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
