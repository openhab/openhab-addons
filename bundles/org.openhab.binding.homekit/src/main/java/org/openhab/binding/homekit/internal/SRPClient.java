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

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Implements the client side of the Secure Remote Password (SRP) protocol for HomeKit pairing.
 * This class handles the SRP handshake, proof generation, and verification.
 * It also manages the encryption and decryption of identifiers using the shared session key.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class SRPClient {

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

    private final String setupCode;
    private BigInteger a; // private ephemeral
    private BigInteger A; // public ephemeral
    private BigInteger B; // server public
    private byte[] salt;
    private byte[] K; // shared session key

    public SRPClient(String setupCode) {
        this.setupCode = setupCode;
    }

    /**
     * Processes the server's SRP challenge by storing the salt and server public key,
     * and generating the client's ephemeral keys.
     *
     * @param salt The salt provided by the server.
     * @param serverPublicKey The server's public key (B).
     * @throws Exception If an error occurs during processing.
     */
    public void processChallenge(byte[] salt, byte[] serverPublicKey) throws Exception {
        this.salt = salt;
        this.B = new BigInteger(1, serverPublicKey);
        SecureRandom random = new SecureRandom();
        this.a = new BigInteger(256, random);
        this.A = g.modPow(a, N);
    }

    /**
     * Generates the client's proof of knowledge (M1) and returns it along with the client's public key (A).
     *
     * @return A map containing the client's public key (A) and proof (M1).
     * @throws Exception If an error occurs during proof generation.
     */
    public Map<Integer, byte[]> generateClientProof() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] xH = digest.digest((new String(salt) + setupCode).getBytes());
        BigInteger x = new BigInteger(1, xH);

        BigInteger u = computeU(A, B);
        BigInteger S = (B.subtract(g.modPow(x, N))).modPow(a.add(u.multiply(x)), N);
        this.K = digest.digest(S.toByteArray());

        byte[] M1 = computeM1(A, B, K);
        return Map.of(0x03, A.toByteArray(), 0x04, M1);
    }

    /**
     * Verifies the server's proof (M2) against the expected value.
     *
     * @param M2 The server's proof to verify.
     * @throws Exception If an error occurs during verification or if the proof does not match.
     */
    public void verifyServerProof(byte[] M2) throws Exception {
        byte[] expected = computeM2(A, computeM1(A, B, K), K);
        if (!MessageDigest.isEqual(M2, expected)) {
            throw new SecurityException("Server proof mismatch");
        }
    }

    /**
     * Generates encrypted identifiers using the shared session key (K).
     * This includes encrypting the controller's identifier and public key.
     *
     * @return A map containing the nonce and encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    public Map<Integer, byte[]> generateEncryptedIdentifiers() throws Exception {
        // Encrypt controller identifier and public key using shared key K
        byte[] plaintext = "...".getBytes(); // TODO input TLV8 encoded identifiers
        byte[] nonce = generateNonce();
        byte[] encrypted = ChaCha20.encrypt(K, nonce, plaintext);
        return Map.of(0x05, nonce, 0x06, encrypted);
    }

    /**
     * Verifies the accessory's encrypted identifiers using the shared session key (K).
     *
     * @param tlv6 A map containing the nonce and encrypted data from the accessory.
     * @throws Exception If an error occurs during decryption or verification.
     */
    public void verifyAccessoryIdentifiers(Map<Integer, byte[]> tlv6) throws Exception {
        byte[] nonce = tlv6.get(0x05);
        byte[] encrypted = tlv6.get(0x06);
        byte[] decrypted = ChaCha20.decrypt(K, nonce, encrypted);
        // TODO Parse TLV8 and validate accessory identity
    }

    /**
     * Derives session keys for encrypting and decrypting messages between the HomeKit controller and accessory.
     *
     * @return An instance of SessionKeys containing the derived read and write keys.
     */
    public SessionKeys deriveSessionKeys() {
        return new SessionKeys(K);
    }

    private BigInteger computeU(BigInteger A, BigInteger B) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] uH = digest.digest(concat(A.toByteArray(), B.toByteArray()));
        return new BigInteger(1, uH);
    }

    private byte[] computeM1(BigInteger A, BigInteger B, byte[] K) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        return digest.digest(concat(A.toByteArray(), B.toByteArray(), K));
    }

    private byte[] computeM2(BigInteger A, byte[] M1, byte[] K) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        return digest.digest(concat(A.toByteArray(), M1, K));
    }

    private byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] arr : arrays) {
            out.write(arr, 0, arr.length);
        }
        return out.toByteArray();
    }

    private byte[] generateNonce() {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }
}
