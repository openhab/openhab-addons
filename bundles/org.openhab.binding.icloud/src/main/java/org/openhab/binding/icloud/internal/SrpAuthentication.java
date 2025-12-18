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
import java.util.Objects;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.binding.icloud.internal.utilities.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 *
 * TODO
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class SrpAuthentication {
    private final String passwordRaw;
    private final List<Pair<String, String>> sessionHeaders;

    private static final BigInteger N = new BigInteger(
            "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819");;
    private final static BigInteger g = BigInteger.valueOf(2l);

    private String I; // username

    public SrpAuthentication(String accountName, String passwordRaw, List<Pair<String, String>> sessionHeaders) {
        this.I = accountName;
        this.passwordRaw = passwordRaw;
        this.sessionHeaders = sessionHeaders;
    }

    /**
     * Base64 encode
     */
    private String b64Encode(BigInteger data) {
        return b64Encode(toByteArray(data));
    }

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
     * Base64 encode
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

    // https://asecuritysite.com/bouncy/bc_srp6a
    public void auth(String authEndpoint, ICloudSession httpClient) throws IOException, InterruptedException,
            ICloudApiResponseException, CryptoException, NoSuchAlgorithmException {
        SrpPassword srpPassword = new SrpPassword(passwordRaw);

        // TODO which rfc?
        // BigInteger N = SRP6StandardGroups.rfc5054_2048.getN();
        // BigInteger G = SRP6StandardGroups.rfc5054_2048.getG();

        byte[] client_a = new byte[256];
        new SecureRandom().nextBytes(client_a);

        BigInteger a = new BigInteger(1, client_a);
        BigInteger A = g.modPow(a, N);

        // Prepare initial authentication request
        Map<String, Object> initData = Map.of("a", b64Encode(A), "accountName", I, "protocols",
                new String[] { "s2k", "s2k_fo" });

        // POST to signin/init endpoint
        String initResponse = httpClient.post(authEndpoint + "/signin/init", JsonUtils.toJson(initData),
                sessionHeaders);

        // Parse response
        JsonObject initBody = parseJsonResponse(initResponse);

        BigInteger B = new BigInteger(1, b64Decode(initBody.get("b").getAsString()));
        byte[] s = b64Decode(initBody.get("salt").getAsString());
        String c = initBody.get("c").getAsString();
        int iterations = initBody.get("iteration").getAsInt();
        int keyLength = 32;

        srpPassword.setEncryptInfo(s, iterations, keyLength);

        /*
         * # SRP-6a safety check
         * if (self.B % N) == 0:
         * return None
         */

        // Calculate S
        Digest digest = new SHA256Digest();
        BigInteger x = SRP6Util.calculateX(digest, N, s, "".getBytes(StandardCharsets.UTF_8), srpPassword.encode());
        BigInteger u = SRP6Util.calculateU(digest, N, A, B);

        BigInteger k = SRP6Util.calculateK(digest, N, g);

        BigInteger v = g.modPow(x, N);
        BigInteger S = B.subtract(k.multiply(v)).modPow(a.add(u.multiply(x)), N);

        byte[] K = sha256(toUnsigned(S, 256));
        // BigInteger K2 = SRP6Util.calculateKey(digest, N, S);

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
        requestBody.put("trustTokens", new String[] {});
        httpClient.post(authEndpoint + "/signin/complete?isRememberMeEnabled=true", JsonUtils.toJson(requestBody),
                sessionHeaders);
    }

    /**
     * Parse JSON response using Gson
     */
    private JsonObject parseJsonResponse(String jsonResponse) {
        Gson gson = new Gson();
        return Objects.requireNonNull(gson.fromJson(jsonResponse, JsonObject.class));
    }

    public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
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
    public static byte[] toUnsigned(BigInteger bigInteger, int length) {
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

    public static byte[] concat(byte[]... parts) {
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
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
}
