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
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.session.SessionKeys;

/**
 * Manages the SRP (Secure Remote Password) protocol for pairing with a HomeKit accessory.
 * This class handles the SRP steps, including key generation, proof verification,
 * and encryption of identifiers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SrpClient {

    // HomeKit 3072-bit prime from RFC 5054
    public static final String N_HEX =
    //@formatter:off
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74" +
            "020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F1437" +
            "4FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
            "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF05" +
            "98DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB" +
            "9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
            "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF695581718" +
            "3995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33" +
            "A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" +
            "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864" +
            "D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E2" +
            "08E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF";
    //@formatter:on

    private static final BigInteger N = new BigInteger(N_HEX);
    private static final BigInteger g = BigInteger.valueOf(5);

    private static final byte[] PAIR_USER = "Pair-Setup".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_SALT = "Pair-Setup-Encrypt-Salt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PAIR_INFO = "Pair-Setup-Encrypt-Info".getBytes(StandardCharsets.UTF_8);
    private static final byte[] NONCE_M5 = "PS-Msg05".getBytes(StandardCharsets.UTF_8);
    private static final byte[] NONCE_M6 = "PS-Msg06".getBytes(StandardCharsets.UTF_8);

    private final String accessoryPairingCode;
    private final SecureRandom random = new SecureRandom();

    // SRP internals
    private @Nullable BigInteger a = null; // client private exponent
    private @Nullable BigInteger A = null; // client public value
    private @Nullable BigInteger B = null; // server public value
    private @Nullable BigInteger x = null; // private key derived from salt + (username:pin)
    private @Nullable BigInteger k = null; // SRP multiplier
    private @Nullable BigInteger u = null; // scrambling parameter
    private @Nullable BigInteger S = null; // shared secret
    private byte[] K = new byte[0]; // session key (H(S))
    private byte[] M1 = new byte[0]; // client proof
    private byte[] salt = new byte[0]; // server salt

    // Curve25519 key‐pair for identifier exchange
    private final AsymmetricCipherKeyPair x25519KeyPair;

    // Accessory credentials after M6
    private @Nullable String accessoryIdentifier;
    private byte[] accessoryPublicKey = new byte[0];

    public SrpClient(String accessoryPairingCode) {
        this.accessoryPairingCode = accessoryPairingCode;

        // Generate Curve25519 key‐pair once
        X25519KeyPairGenerator gen = new X25519KeyPairGenerator();
        gen.init(new X25519KeyGenerationParameters(random));
        this.x25519KeyPair = gen.generateKeyPair();
    }

    /**
     * M2 — Store salt and accessory public key (B).
     */
    public void processChallenge(byte[] salt, byte[] serverPublicKey) throws NoSuchAlgorithmException {
        this.B = new BigInteger(1, serverPublicKey);
        this.salt = salt;

        // Precompute k = H(N || g)
        this.k = new BigInteger(1, MessageDigest.getInstance("SHA-512")
                .digest(concat(BigIntUtils.toUnsignedByteArray(N), BigIntUtils.toUnsignedByteArray(g))));

        // Precompute x = H(salt || H(username:pin))
        byte[] inner = MessageDigest.getInstance("SHA-512")
                .digest((PAIR_USER + ":" + accessoryPairingCode).getBytes(StandardCharsets.UTF_8));

        this.x = new BigInteger(1, MessageDigest.getInstance("SHA-512").digest(concat(salt, inner)));
    }

    /**
     * M3 — Client public key A.
     */
    public byte[] getPublicKey() {
        BigInteger A = this.A;
        if (A == null) {
            // a = random, A = g^a mod N
            this.a = new BigInteger(N.bitLength(), random).mod(N);
            A = g.modPow(a, N);
            this.A = A;
        }
        return BigIntUtils.toUnsignedByteArray(A);
    }

    /**
     * M3 — Client proof M1 = H( H(N)^H(g) || H(username) || salt || A || B || K ).
     */
    public byte[] getClientProof() throws Exception {
        if (M1.length == 0) {
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

            // u = H(A || B)
            sha512.update(BigIntUtils.toUnsignedByteArray(A));
            sha512.update(BigIntUtils.toUnsignedByteArray(B));
            this.u = new BigInteger(1, sha512.digest());

            BigInteger B = this.B;
            BigInteger k = this.k;
            BigInteger a = this.a;
            BigInteger u = this.u;
            BigInteger x = this.x;
            if (B == null || k == null || a == null || u == null || x == null) {
                throw new IllegalStateException("SRP internal state not initialized");
            }

            // S = ( B - k·g^x )^( a + u·x ) mod N
            BigInteger gx = g.modPow(x, N);
            BigInteger tmp = B.subtract(k.multiply(gx)).mod(N);
            BigInteger exp = a.add(u.multiply(x));
            this.S = tmp.modPow(exp, N);

            // K = H(S)
            this.K = MessageDigest.getInstance("SHA-512").digest(BigIntUtils.toUnsignedByteArray(S));

            // compute proof M1
            byte[] HN = MessageDigest.getInstance("SHA-512").digest(BigIntUtils.toUnsignedByteArray(N));
            byte[] Hg = MessageDigest.getInstance("SHA-512").digest(BigIntUtils.toUnsignedByteArray(g));
            byte[] Hxor = xor(HN, Hg);
            byte[] Hu = MessageDigest.getInstance("SHA-512").digest(PAIR_USER);

            sha512.reset();
            sha512.update(Hxor);
            sha512.update(Hu);
            sha512.update(salt);
            sha512.update(BigIntUtils.toUnsignedByteArray(A));
            sha512.update(BigIntUtils.toUnsignedByteArray(B));
            sha512.update(K);
            this.M1 = sha512.digest();
        }
        return M1;
    }

    /**
     * M4 — Verify server proof M2 = H( A || M1 || K ).
     */
    public void verifyServerProof(byte[] serverProof) throws Exception {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        sha512.update(BigIntUtils.toUnsignedByteArray(A));
        sha512.update(M1);
        sha512.update(K);
        byte[] expected = sha512.digest();

        if (!Arrays.equals(expected, serverProof)) {
            throw new SecurityException("SRP server proof mismatch");
        }
    }

    /**
     * M5 — Encrypt controller identifier + Curve25519 public key.
     */
    public byte[] getEncryptedIdentifiers() throws Exception {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.IDENTIFIER.key, PAIR_USER, //
                TlvType.PUBLIC_KEY.key, ((X25519PublicKeyParameters) x25519KeyPair.getPublic()).getEncoded());
        byte[] plain = Tlv8Codec.encode(tlv);
        return CryptoUtils.encrypt(deriveSessionKeys().getWriteKey(), plain, NONCE_M5);
    }

    /**
     * M6 — Decrypt and store accessory identifier + Curve25519 public key.
     */
    public void verifyAccessoryIdentifiers(byte[] encryptedData) throws Exception {
        byte[] decrypted = CryptoUtils.encrypt(deriveSessionKeys().getReadKey(), encryptedData, NONCE_M6);
        Map<Integer, byte[]> accTlv = Tlv8Codec.decode(decrypted);

        byte[] idBytes = accTlv.get(TlvType.IDENTIFIER.key);
        byte[] pkBytes = accTlv.get(TlvType.PUBLIC_KEY.key);
        if (idBytes == null || pkBytes == null) {
            throw new SecurityException("Missing accessory credentials in M6");
        }
        this.accessoryIdentifier = new String(idBytes, StandardCharsets.UTF_8);
        this.accessoryPublicKey = pkBytes;
    }

    /**
     * After M6, derive the 32‐byte session key using HKDF(S, salt, info).
     */
    public SessionKeys deriveSessionKeys() {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        hkdf.init(new HKDFParameters(K, PAIR_SALT, PAIR_INFO));
        byte[] sessionKey = new byte[32];
        hkdf.generateBytes(sessionKey, 0, sessionKey.length);
        return new SessionKeys(sessionKey, sessionKey);
    }

    // ——— Internals ———————————————————————————————————————————

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] out = new byte[Math.min(a.length, b.length)];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    private static class BigIntUtils {
        static byte[] toUnsignedByteArray(@Nullable BigInteger b) {
            if (b == null) {
                throw new IllegalStateException("BigInteger is null");
            }
            byte[] bytes = b.toByteArray();
            return bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
        }
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public @Nullable String getAccessoryIdentifier() {
        return accessoryIdentifier;
    }

    public byte[] getAccessoryPublicKey() {
        return accessoryPublicKey;
    }
}
