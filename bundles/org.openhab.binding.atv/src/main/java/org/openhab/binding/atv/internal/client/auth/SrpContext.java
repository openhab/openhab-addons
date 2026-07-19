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
package org.openhab.binding.atv.internal.client.auth;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * SRP-6a core arithmetic, matching the wire format expected by real Apple devices.
 *
 * <p>
 * This class contains no wire logic; it only computes the SRP quantities. The subtle points
 * that must be exact for interop are:
 * <ul>
 * <li>All integers are serialized to their <em>minimal</em> big-endian byte form when hashed;
 * leading zero bytes are dropped.</li>
 * <li>{@code pad(v)} left-pads the minimal bytes of {@code v} to the byte length of N.</li>
 * <li>{@code k = H(N || PAD(g))}, {@code x = H(salt || H(I ":" P))}, {@code u = H(PAD(A) ||
 * PAD(B))}.</li>
 * <li>Client proof {@code M1 = H((H(N) xor H(g)) || H(I) || salt || A || B || K)} (RFC 2945 form —
 * <em>not</em> BouncyCastle's {@code H(A||B||S)}).</li>
 * <li>Server proof {@code M2 = H(A || M1 || K)}.</li>
 * <li>The session-key derivation {@code K = f(S)} is pluggable: HAP uses {@code K = H(S)}, AirPlay
 * legacy uses {@code K = H(S||0x00000000) || H(S||0x00000001)}.</li>
 * </ul>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class SrpContext {

    private final SrpGroup group;
    private final String hashAlgorithm;
    private final Function<BigInteger, byte[]> sessionKeyFn;
    private final BigInteger multiplier;

    /**
     * Creates a new context.
     *
     * @param group the SRP group (N, g)
     * @param hashAlgorithm a {@link MessageDigest} algorithm name, e.g. {@code "SHA-512"}
     * @param sessionKeyFn derives the session key K from the premaster secret S
     */
    public SrpContext(SrpGroup group, String hashAlgorithm, Function<BigInteger, byte[]> sessionKeyFn) {
        this.group = group;
        this.hashAlgorithm = hashAlgorithm;
        this.sessionKeyFn = sessionKeyFn;
        // k = H(N | PAD(g))
        this.multiplier = hashInt(concat(intToBytes(group.prime()), pad(group.generator())));
    }

    /**
     * A HAP context: 3072-bit group, SHA-512, K = H(S).
     */
    public static SrpContext hap() {
        return new SrpContext(SrpGroup.RFC5054_3072, "SHA-512",
                s -> hashRaw(SrpContext.staticDigest("SHA-512"), SrpContext.intToBytesStatic(s)));
    }

    /**
     * A legacy AirPlay context: 2048-bit group, SHA-1, K = H(S||00000000) || H(S||00000001).
     */
    public static SrpContext legacyAirPlay() {
        return new SrpContext(SrpGroup.RFC5054_2048, "SHA-1", s -> {
            byte[] sBytes = intToBytesStatic(s);
            MessageDigest md = staticDigest("SHA-1");
            byte[] k1 = hashRaw(md, sBytes, new byte[] { 0, 0, 0, 0 });
            byte[] k2 = hashRaw(md, sBytes, new byte[] { 0, 0, 0, 1 });
            byte[] out = new byte[k1.length + k2.length];
            System.arraycopy(k1, 0, out, 0, k1.length);
            System.arraycopy(k2, 0, out, k1.length, k2.length);
            return out;
        });
    }

    /**
     * Returns the group.
     */
    public SrpGroup group() {
        return group;
    }

    /**
     * Returns the multiplier {@code k = H(N | PAD(g))}.
     */
    public BigInteger multiplier() {
        return multiplier;
    }

    /**
     * x = H(salt | H(I ":" P)).
     */
    public BigInteger passwordHash(byte[] salt, String username, String password) {
        byte[] inner = digest((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return hashInt(concat(salt, inner));
    }

    /**
     * v = g^x % N.
     */
    public BigInteger verifier(BigInteger passwordHash) {
        return group.generator().modPow(passwordHash, group.prime());
    }

    /**
     * A = g^a % N.
     */
    public BigInteger clientPublic(BigInteger clientPrivate) {
        return group.generator().modPow(clientPrivate, group.prime());
    }

    /**
     * B = (k*v + g^b) % N.
     */
    public BigInteger serverPublic(BigInteger verifier, BigInteger serverPrivate) {
        return multiplier.multiply(verifier).add(group.generator().modPow(serverPrivate, group.prime()))
                .mod(group.prime());
    }

    /**
     * u = H(PAD(A) | PAD(B)).
     */
    public BigInteger commonSecret(BigInteger clientPublic, BigInteger serverPublic) {
        return hashInt(concat(pad(clientPublic), pad(serverPublic)));
    }

    /**
     * S = (B - (k * g^x)) ^ (a + (u * x)) % N.
     */
    public BigInteger clientPremaster(BigInteger passwordHash, BigInteger serverPublic, BigInteger clientPrivate,
            BigInteger commonSecret) {
        BigInteger v = verifier(passwordHash);
        BigInteger base = serverPublic.subtract(multiplier.multiply(v));
        BigInteger exp = clientPrivate.add(commonSecret.multiply(passwordHash));
        return base.modPow(exp, group.prime());
    }

    /**
     * S = (A * v^u) ^ b % N.
     */
    public BigInteger serverPremaster(BigInteger verifier, BigInteger serverPrivate, BigInteger clientPublic,
            BigInteger commonSecret) {
        return clientPublic.multiply(verifier.modPow(commonSecret, group.prime())).modPow(serverPrivate, group.prime());
    }

    /**
     * K = f(S).
     */
    public byte[] sessionKey(BigInteger premaster) {
        return sessionKeyFn.apply(premaster);
    }

    /**
     * M1 = H((H(N) xor H(g)) | H(I) | salt | A | B | K).
     */
    public byte[] clientProof(byte[] sessionKey, byte[] salt, BigInteger serverPublic, BigInteger clientPublic,
            String username) {
        BigInteger hn = hashInt(intToBytes(group.prime()));
        BigInteger hg = hashInt(intToBytes(group.generator()));
        BigInteger hi = hashInt(username.getBytes(StandardCharsets.UTF_8));
        return digest(concat(intToBytes(hn.xor(hg)), intToBytes(hi), salt, intToBytes(clientPublic),
                intToBytes(serverPublic), sessionKey));
    }

    /**
     * M2 = H(A | M1 | K).
     */
    public byte[] serverProof(byte[] sessionKey, byte[] clientProof, BigInteger clientPublic) {
        return digest(concat(intToBytes(clientPublic), clientProof, sessionKey));
    }

    /**
     * PAD(v): minimal big-endian bytes of v, left-padded with zeros to the byte length of N.
     */
    public byte[] pad(BigInteger value) {
        int width = intToBytes(group.prime()).length;
        byte[] raw = intToBytes(value);
        if (raw.length >= width) {
            return raw;
        }
        byte[] out = new byte[width];
        System.arraycopy(raw, 0, out, width - raw.length, raw.length);
        return out;
    }

    /**
     * Minimal unsigned big-endian byte representation: no leading zero bytes, and {@code 0}
     * maps to a single {@code 0x00}.
     */
    public static byte[] intToBytes(BigInteger value) {
        return intToBytesStatic(value);
    }

    static byte[] intToBytesStatic(BigInteger value) {
        if (value.signum() == 0) {
            return new byte[] { 0 };
        }
        byte[] raw = value.toByteArray();
        int start = 0;
        while (start < raw.length - 1 && raw[start] == 0) {
            start++;
        }
        if (start == 0) {
            return raw;
        }
        byte[] out = new byte[raw.length - start];
        System.arraycopy(raw, start, out, 0, out.length);
        return out;
    }

    private byte[] digest(byte[] data) {
        return hashRaw(newDigest(), data);
    }

    private BigInteger hashInt(byte[] data) {
        return new BigInteger(1, digest(data));
    }

    private MessageDigest newDigest() {
        return staticDigest(hashAlgorithm);
    }

    static MessageDigest staticDigest(String algo) {
        try {
            return MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing message digest: " + algo, e);
        }
    }

    static byte[] hashRaw(MessageDigest md, byte[]... parts) {
        md.reset();
        for (byte[] p : parts) {
            md.update(p);
        }
        return md.digest();
    }

    static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] p : parts) {
            out.write(p, 0, p.length);
        }
        return out.toByteArray();
    }
}
