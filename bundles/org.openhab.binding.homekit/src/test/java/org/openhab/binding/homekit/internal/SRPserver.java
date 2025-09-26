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

    // Session state
    public @NonNullByDefault({}) BigInteger A; // client public SRP key
    public final BigInteger b; // server private SRP key
    public final BigInteger B; // server public SRP key
    public @NonNullByDefault({}) byte[] K = null; // session key
    public @NonNullByDefault({}) BigInteger S; // shared secret
    public @NonNullByDefault({}) BigInteger u; // scrambling parameter
    public final BigInteger v; // verifier

    private final String I; // username
    private final byte[] s; // salt
    private final byte[] serverPairingId;
    private final Ed25519PrivateKeyParameters serverLongTermPrivateKey;

    /**
     * Create a SRP server instance with the given parameters.
     *
     * @param password the password to use
     * @param serverSalt the salt to use
     * @param serverPairingId the pairing ID of the server
     * @param serverLongTermPrivateKey the long term private key of the server
     * @param username the username to use (or null for default "Pair-Setup")
     * @param serverPrivateKey optional 32 byte private key to use for b, or null to generate a new one
     *
     * @throws Exception on any error
     */
    public SRPserver(String password, byte[] serverSalt, byte[] serverPairingId,
            Ed25519PrivateKeyParameters serverLongTermPrivateKey, @Nullable String username,
            byte @Nullable [] serverPrivateKey) throws Exception {
        this.serverPairingId = serverPairingId;
        this.serverLongTermPrivateKey = serverLongTermPrivateKey;
        I = username != null ? username : PAIR_SETUP;
        s = serverSalt;

        // x = H(salt || H(username || ":" || password))
        // v = g^x mod N
        byte[] hIP = sha512((I + ":" + password).getBytes(StandardCharsets.UTF_8));
        BigInteger x = new BigInteger(1, sha512(concat(serverSalt, hIP)));
        v = g.modPow(x, N);

        // Apply or create ephemeral b and compute public B
        byte[] serverKey = serverPrivateKey;
        if (serverKey == null) {
            serverKey = new byte[32];
            new SecureRandom().nextBytes(serverKey);
        }
        b = new BigInteger(1, serverKey);
        BigInteger gb = g.modPow(b, N);
        B = k.multiply(v).add(gb).mod(N);
    }

    public byte[] m3CreateServerProof(byte[] clientPublicKeyA) throws Exception {
        BigInteger clientPublicA = new BigInteger(1, clientPublicKeyA);
        if (clientPublicA.mod(N).equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid client public key");
        }
        A = clientPublicA;

        // u = H(PAD(A) || PAD(B))
        byte[] uHash = sha512(concat(toUnsigned(A, 384), toUnsigned(B, 384)));
        u = new BigInteger(1, uHash);
        if (u.equals(BigInteger.ZERO)) {
            throw new SecurityException("Invalid scrambling parameter");
        }

        // S = (A * v^u)^b mod N
        BigInteger vu = v.modPow(u, N);
        BigInteger base = A.multiply(vu).mod(N);
        S = base.modPow(b, N);
        K = sha512(toUnsigned(S, 384));

        // Compute M1 = H(H(N) xor H(g) || H(I) || salt || A || B || K)
        byte[] HN = sha512(toUnsigned(N, 384));
        byte[] Hg = sha512(toUnsigned(g, 1));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));
        byte[] M1 = sha512(concat(Hxor, HI, s, toUnsigned(clientPublicA, 384), toUnsigned(B, 384), K));

        // Compute M2 = H(A || M1 || K)
        return sha512(concat(toUnsigned(clientPublicA, 384), M1, K));
    }

    public byte[] m5EncodeServerInfoAndSign() throws Exception {
        byte[] sharedKey = generateHkdfKey(K, PAIR_ACCESSORY_SIGN_SALT, PAIR_ACCESSORY_SIGN_INFO);
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

    public byte[] getSymmetricKey() {
        return generateHkdfKey(K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
    }
}
