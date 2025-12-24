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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
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

    /*
     * ***************************************************************************************
     *
     * DEVELOPER NOTE:
     *
     * Some of the field names in this class follow the Crytographic "Alice and Bob Notation"
     * where for example 'A' (uppercase) is the conventional meaning for "Alice's Public Key"
     * and 'a' (lowercase) is the conventional meaning for "Alice's Private Key". Such names
     * are legal according to Java language syntax, but the openHAB style checker warns about
     * some of them. => Please ignore such warnings.
     *
     * ***************************************************************************************
     */

    // Session state
    public @NonNullByDefault({}) BigInteger A; // client public SRP key
    public final BigInteger b; // server private SRP key
    public final BigInteger B; // server public SRP key
    public @NonNullByDefault({}) byte[] S = null; // shared secret
    public @NonNullByDefault({}) byte[] K = null; // Apple SRP style session key = H(S)
    public @NonNullByDefault({}) BigInteger u; // scrambling parameter
    public final BigInteger v; // verifier

    private final String I; // username
    private final byte[] s; // salt
    private final byte[] accessoryId;
    private final Ed25519PrivateKeyParameters accessoryKey;

    /**
     * Create a SRP server instance with the given parameters.
     *
     * @param password the password to use
     * @param serverSalt the salt to use
     * @param accessoryId the pairing ID of the server
     * @param accessoryKey the long term private key of the server
     * @param username the username to use (or null for default "Pair-Setup")
     * @param accessoryPrivateKey optional 32 byte private key to use for b, or null to generate a new one
     * @throws NoSuchAlgorithmException
     *
     */
    public SRPserver(String password, byte[] serverSalt, byte[] accessoryId, Ed25519PrivateKeyParameters accessoryKey,
            @Nullable String username, byte @Nullable [] accessoryPrivateKey) throws NoSuchAlgorithmException {
        this.accessoryId = accessoryId;
        this.accessoryKey = accessoryKey;
        I = username != null ? username : PAIR_SETUP;
        s = serverSalt;

        // x = H(salt || H(username || ":" || password))
        // v = g^x mod N
        byte[] hIP = sha512((I + ":" + password).getBytes(StandardCharsets.UTF_8));
        BigInteger x = new BigInteger(1, sha512(concat(serverSalt, hIP)));
        v = g.modPow(x, N);

        // Apply or create ephemeral b and compute public B
        byte[] serverKey = accessoryPrivateKey;
        if (serverKey == null) {
            serverKey = new byte[32];
            new SecureRandom().nextBytes(serverKey);
        }
        b = new BigInteger(1, serverKey);
        BigInteger gb = g.modPow(b, N);
        B = k.multiply(v).add(gb).mod(N);
    }

    public byte[] m3CreateServerProof(byte[] clientPublicKeyA) throws NoSuchAlgorithmException {
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

        // S = (A * v^u)^b mod N (384 bytes)
        BigInteger vu = v.modPow(u, N);
        BigInteger base = A.multiply(vu).mod(N);
        S = toUnsigned(base.modPow(b, N), 384);

        // Compute 'Apple SRP style' session key K = H(S) (64 bytes)
        K = sha512(S);

        // Compute M1 = H(H(N) xor H(g) || H(I) || salt || A || B || K)
        byte[] HN = sha512(toUnsigned(N, 384));
        byte[] Hg = sha512(toUnsigned(g, 1));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha512(I.getBytes(StandardCharsets.UTF_8));
        byte[] M1 = sha512(concat(Hxor, HI, s, toUnsigned(clientPublicA, 384), toUnsigned(B, 384), K));

        // Compute M2 = H(A || M1 || K)
        return sha512(concat(toUnsigned(clientPublicA, 384), M1, K));
    }

    public void m5DecodeControllerInfoAndVerify(Map<Integer, byte[]> tlv5)
            throws InvalidCipherTextException, IllegalArgumentException {
        byte[] cipherText = tlv5.get(TlvType.ENCRYPTED_DATA.value);
        if (cipherText == null) {
            throw new IllegalArgumentException("Missing encrypted data");
        }

        byte[] decryptKey = generateHkdfKey(K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
        byte[] plainText = CryptoUtils.decrypt(decryptKey, PS_M5_NONCE, cipherText, new byte[0]);

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] iOSDeviceId = subTlv.get(TlvType.IDENTIFIER.value);
        byte[] iOSDeviceLTPK = subTlv.get(TlvType.PUBLIC_KEY.value);
        byte[] iOSDeviceSignature = subTlv.get(TlvType.SIGNATURE.value);

        if (iOSDeviceId == null || iOSDeviceLTPK == null || iOSDeviceSignature == null) {
            throw new IllegalArgumentException("Missing identifier, public key or signature");
        }

        byte[] iOSDeviceX = generateHkdfKey(K, PAIR_SETUP_CONTROLLER_SIGN_SALT, PAIR_SETUP_CONTROLLER_SIGN_INFO);
        byte[] iOSDeviceInfo = concat(iOSDeviceX, iOSDeviceId, iOSDeviceLTPK);

        Ed25519PublicKeyParameters iOSDeviceLongTermPublicKey = new Ed25519PublicKeyParameters(iOSDeviceLTPK, 0);
        verifySignature(iOSDeviceLongTermPublicKey, iOSDeviceSignature, iOSDeviceInfo);
    }

    public byte[] m6EncodeAccessoryInfoAndSign() throws InvalidCipherTextException {
        byte[] accessoryX = generateHkdfKey(K, PAIR_SETUP_ACCESSORY_SIGN_SALT, PAIR_SETUP_ACCESSORY_SIGN_INFO);
        byte[] accessoryLTPK = accessoryKey.generatePublicKey().getEncoded();
        byte[] accessoryInfo = concat(accessoryX, accessoryId, accessoryLTPK);
        byte[] accessorySignature = signMessage(accessoryKey, accessoryInfo);

        Map<Integer, byte[]> subTlv = Map.of( //
                TlvType.IDENTIFIER.value, accessoryId, //
                TlvType.PUBLIC_KEY.value, accessoryLTPK, //
                TlvType.SIGNATURE.value, accessorySignature);

        byte[] plaintext = Tlv8Codec.encode(subTlv);
        byte[] encryptKey = generateHkdfKey(K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
        return CryptoUtils.encrypt(encryptKey, PS_M6_NONCE, plaintext, new byte[0]);
    }
}
