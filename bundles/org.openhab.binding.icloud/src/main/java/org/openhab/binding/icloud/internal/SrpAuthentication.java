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
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.binding.icloud.internal.utilities.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 *
 * SrpAuthentication implements the SRP authentication for iCloud.
 *
 * @author Simon Spielmann - Initial contribution
 */
public class SrpAuthentication {

    private final @NonNull String password;
    private final List<Pair<@NonNull String, @NonNull String>> sessionHeaders;

    // N and g values from RFC 5054 - 2048 bit group
    private static final BigInteger N = new BigInteger(
            "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819");;
    private final static BigInteger g = BigInteger.valueOf(2l);

    // Username
    private String I;

    /**
     * Implements SRP authentication according to Apple's specifications.
     *
     * @param accountName the account name (username)
     * @param password user password
     * @param sessionHeaders list of session headers
     */
    public SrpAuthentication(String accountName, String password,
            List<Pair<@NonNull String, @NonNull String>> sessionHeaders) {
        this.I = accountName;
        this.password = password;
        this.sessionHeaders = sessionHeaders;
    }

    /**
     * Convert BigInteger to byte array without leading zero byte
     *
     * @param data BigInteger to convert
     * @return byte array representation of BigInteger without leading zero byte
     */
    private byte[] toByteArray(BigInteger data) {
        byte[] signedBytes = data.toByteArray();
        if (signedBytes[0] == 0x00) {
            // Führendes Null-Byte entfernen
            byte[] unsignedBytes = new byte[signedBytes.length - 1];
            System.arraycopy(signedBytes, 1, unsignedBytes, 0, unsignedBytes.length);
            return unsignedBytes;
        } else {
            return signedBytes;
        }
    }

    /**
     * Perform SRP authentication
     *
     * @param authEndpoint the authentication endpoint URL
     * @param httpClient the HTTP client session
     */
    public void auth(String authEndpoint, ICloudSession httpClient) throws IOException, InterruptedException,
            ICloudApiResponseException, CryptoException, NoSuchAlgorithmException {
        SrpPassword srpPassword = new SrpPassword(password);

        byte[] clientA = new byte[256];
        new SecureRandom().nextBytes(clientA);

        BigInteger a = new BigInteger(1, clientA);
        BigInteger A = g.modPow(a, N);

        // Prepare initial authentication request
        Map<String, Object> initData = Map.of("a", b64Encode(A), "accountName", I, "protocols",
                new String[] { "s2k", "s2k_fo" });

        String initResponse = httpClient.post(authEndpoint + "/signin/init", JsonUtils.toJson(initData),
                sessionHeaders);

        // Parse response
        JsonObject initBody = parseJson(initResponse);

        BigInteger B = new BigInteger(1, b64Decode(initBody.get("b").getAsString()));
        byte[] s = b64Decode(initBody.get("salt").getAsString());
        String c = initBody.get("c").getAsString();
        int iterations = initBody.get("iteration").getAsInt();
        int keyLength = 32;

        srpPassword.setEncryptInfo(s, iterations, keyLength);

        // SRP-6a safety check
        if (B.mod(N).equals(BigInteger.ZERO)) {
            throw new CryptoException("Invalid server public value B");
        }

        // Calculate S
        Digest digest = new SHA256Digest();
        BigInteger x = SRP6Util.calculateX(digest, N, s, "".getBytes(StandardCharsets.UTF_8), srpPassword.encode());
        BigInteger u = SRP6Util.calculateU(digest, N, A, B);

        BigInteger k = SRP6Util.calculateK(digest, N, g);

        BigInteger v = g.modPow(x, N);
        BigInteger S = B.subtract(k.multiply(v)).modPow(a.add(u.multiply(x)), N);

        byte[] K = sha256(toUnsigned(S, 256));

        // Compute client proof M1 = H(H(N) xor H(g) || H(I) || s || A || B || K)
        byte[] HN = sha256(toUnsigned(N, 256));
        byte[] Hg = sha256(toUnsigned(g, 256));
        byte[] Hxor = xor(HN, Hg);
        byte[] HI = sha256(I.getBytes(StandardCharsets.UTF_8));
        byte[] M1 = sha256(concat(Hxor, HI, s, toUnsigned(A, 256), toUnsigned(B, 256), K));

        // Compute expected server proof M2 = H(A || M1 || K)
        byte[] M2 = sha256(concat(toUnsigned(A, 256), M1, K));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountName", I);
        requestBody.put("c", c);
        requestBody.put("m1", b64Encode(M1));
        requestBody.put("m2", b64Encode(M2));
        requestBody.put("rememberMe", true);
        requestBody.put("trustTokens", new String[] { httpClient.getTrustToken() });
        httpClient.post(authEndpoint + "/signin/complete?isRememberMeEnabled=true", JsonUtils.toJson(requestBody),
                sessionHeaders);
    }

    /**
     * Parse JSON response using Gson
     *
     * @param jsonString the JSON response string
     * @return the parsed JsonObject
     */
    private JsonObject parseJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, JsonObject.class);
    }

    /**
     * Computes the SHA-256 hash of the given data.
     *
     * @param data the input data.
     * @return the SHA-256 hash as a byte array.
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available.
     */
    private static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    /**
     * Converts a BigInteger to an unsigned byte array of the specified length.
     * If the byte array representation of the BigInteger is shorter than the specified length,
     * it is left-padded with zeros. If it is longer, an exception is thrown.
     *
     * @param bigInteger the BigInteger to convert.
     * @param length the desired length of the resulting byte array.
     * @return a byte array of the given length representing the unsigned BigInteger.
     */
    private static byte[] toUnsigned(BigInteger bigInteger, int length) {
        byte[] raw = bigInteger.toByteArray();
        if (raw.length == length && raw[0] != 0) {
            return raw;
        }

        byte[] unsigned;
        if (raw[0] == 0) {
            // strip leading sign byte
            unsigned = new byte[raw.length - 1];
            System.arraycopy(raw, 1, unsigned, 0, unsigned.length);
        } else {
            unsigned = raw;
        }

        if (unsigned.length == length) {
            return unsigned;
        }

        // pad to fixed length
        byte[] padded = new byte[length];
        System.arraycopy(unsigned, 0, padded, length - unsigned.length, unsigned.length);
        return padded;
    }

    /**
     * Concatenates multiple byte arrays into a single byte array.
     *
     * @param parts the byte arrays to concatenate
     * @return the concatenated byte array
     */
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

    /**
     * XORs two byte arrays of the same length.
     *
     * @param a first byte array
     * @param b second byte array
     * @return the result of XORing the two byte arrays
     */
    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    /**
     * Base64 encode
     */
    private String b64Encode(BigInteger data) {
        return b64Encode(toByteArray(data));
    }

    /**
     * Base64 encode
     *
     * @param data byte array to encode
     */
    private String b64Encode(byte[] data) {
        return Base64.toBase64String(data);
    }

    /**
     * Base64 decode
     */
    private byte[] b64Decode(String data) {
        return Base64.decode(data);
    }
}
