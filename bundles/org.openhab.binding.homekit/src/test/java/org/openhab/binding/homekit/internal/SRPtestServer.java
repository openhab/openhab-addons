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

import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
public class SRPtestServer {

    private final byte[] serverPairingId;
    private final Ed25519PrivateKeyParameters serverLongTermPrivateKey;

    // Session state
    private final String I; // username
    private final byte[] s; // salt
    private final BigInteger v; // verifier
    private final BigInteger b; // private SRP key ephemeral value
    private final BigInteger B; // public SRP key ephemeral value

    private @NonNullByDefault({}) byte[] K = null;
    private @NonNullByDefault({}) BigInteger A;
    private @NonNullByDefault({}) BigInteger u;
    private @NonNullByDefault({}) BigInteger S;

    public SRPtestServer(String password, byte[] serverSalt, byte[] serverPairingId,
            Ed25519PrivateKeyParameters serverLongTermPrivateKey) throws Exception {
        this.serverPairingId = serverPairingId;
        this.serverLongTermPrivateKey = serverLongTermPrivateKey;
        I = PAIR_SETUP;
        s = serverSalt;

        // Compute verifier once
        byte[] hIP = sha512((I + ":" + password).getBytes(StandardCharsets.UTF_8));
        BigInteger x = new BigInteger(1, sha512(concat(serverSalt, hIP)));
        v = g.modPow(x, N);

        // Generate ephemeral b and compute public B
        b = new BigInteger(N.bitLength(), new SecureRandom()).mod(N);
        BigInteger gb = g.modPow(b, N);
        B = k.multiply(v).add(gb).mod(N);
    }

    public byte[] createServerProof(byte[] clientPublicKeyA) throws Exception {
        BigInteger clientPublicA = new BigInteger(1, clientPublicKeyA);
        if (clientPublicA.mod(N).equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid client public key");
        }
        A = clientPublicA;

        // u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(A, N), toUnsigned(B, N)));
        u = new BigInteger(1, uHash);
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid scrambling parameter");
        }

        // S = (A * v^u)^b mod N
        BigInteger vu = v.modPow(u, N);
        BigInteger base = A.multiply(vu).mod(N);
        S = base.modPow(b, N);
        K = sha512(toUnsigned(S, N));

        // Compute M1 = H(H(N) ⊕ H(g) || H(I) || salt || A || B || K)
        byte[] HN = sha512(toUnsigned(N, N));
        byte[] Hg = sha512(toUnsigned(g, N));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));
        byte[] M1 = sha512(concat(Hxor, HI, s, toUnsigned(clientPublicA, N), toUnsigned(B, N), K));

        // Compute M2 = H(A || M1 || K)
        return sha512(concat(toUnsigned(clientPublicA, N), M1, K));
    }

    public byte[] createEncryptedAccessoryInfo() throws Exception {
        byte[] sharedKey = generateHkdfKey(getSharedSecret(), PAIR_CONTROLLER_SIGN_SALT, PAIR_CONTROLLER_SIGN_INFO);
        byte[] signingKey = serverLongTermPrivateKey.generatePublicKey().getEncoded();
        byte[] payload = concat(sharedKey, serverPairingId, signingKey);
        byte[] signature = signMessage(serverLongTermPrivateKey, payload);

        Map<Integer, byte[]> subTlv = Map.of( //
                TlvType.IDENTIFIER.key, serverPairingId, //
                TlvType.PUBLIC_KEY.key, signingKey, //
                TlvType.SIGNATURE.key, signature);

        byte[] plaintext = Tlv8Codec.encode(subTlv);
        return CryptoUtils.encrypt(getSymmetricKey(), PS_M6_NONCE, plaintext);
    }

    public byte[] getPublicKey() {
        return toUnsigned(B, N);
    }

    private byte[] getSharedSecret() {
        return toUnsigned(S, N);
    }

    public byte[] getSymmetricKey() {
        return generateHkdfKey(toUnsigned(S, N), PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
    }
}
