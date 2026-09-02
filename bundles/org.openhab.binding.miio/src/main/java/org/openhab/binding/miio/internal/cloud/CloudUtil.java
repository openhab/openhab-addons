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
package org.openhab.binding.miio.internal.cloud;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_USERDATA_PATH;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoCryptoException;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link CloudUtil} class is used for supporting functions for Xiaomi cloud access
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CloudUtil {

    private static final Random RANDOM = new SecureRandom();
    private static final String UNEXPECTED = "Unexpected :";

    /**
     * Saves the Xiaomi cloud device info with tokens to file
     *
     * @param data file content
     * @param country county server
     * @param logger
     */
    public static void saveDeviceInfoFile(String data, String country, Logger logger) {
        File folder = new File(BINDING_USERDATA_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File dataFile = new File(folder, "miioTokens-" + country + ".json");
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(data);
            logger.debug("Devices token info saved to {}", dataFile.getAbsolutePath());
        } catch (IOException e) {
            logger.debug("Failed to write token file '{}': {}", dataFile.getName(), e.getMessage());
        }
    }

    /**
     * Generate signature for the request.
     *
     * @param requestUrl the full request url. e.g.: http://api.xiaomi.com/getUser?id=123321
     * @param signedNonce secret key for encryption.
     * @param nonce
     * @param params request params. This should be a TreeMap because the
     *            parameters are required to be in lexicographic order.
     * @return hash value for the values provided
     * @throws MiIoCryptoException
     */
    public static String generateSignature(@Nullable String requestUrl, @Nullable String signedNonce, String nonce,
            @Nullable Map<String, String> params) throws MiIoCryptoException {
        if (signedNonce == null || signedNonce.length() == 0) {
            throw new MiIoCryptoException("key is not nullable");
        }
        List<String> exps = new ArrayList<>();

        if (requestUrl != null) {
            URI uri = URI.create(requestUrl);
            exps.add(uri.getPath());
        }
        exps.add(signedNonce);
        exps.add(nonce);

        if (params != null && !params.isEmpty()) {
            final TreeMap<String, String> sortedParams = new TreeMap<>(params);
            Set<Map.Entry<String, String>> entries = sortedParams.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                exps.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String s : exps) {
            if (!first) {
                sb.append('&');
            } else {
                first = false;
            }
            sb.append(s);
        }
        return CloudCrypto.hMacSha256Encode(Base64.getDecoder().decode(signedNonce),
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate encrypted signature (SHA1, base64) for the request.
     *
     * @param url the full request url
     * @param method HTTP method (GET/POST)
     * @param signedNonce secret key for encryption (Base64 encoded)
     * @param params request params
     * @return BASE64 encoded SHA1 signature
     */
    public static String generateEncSignature(String url, String method, String signedNonce, Map<String, String> params)
            throws MiIoCryptoException {
        String path = url.split("com", 2)[1].replace("/app/", "/");
        List<String> signatureParams = new ArrayList<>();
        signatureParams.add(method.toUpperCase());
        signatureParams.add(path);

        for (Map.Entry<String, String> entry : new TreeMap<>(params).entrySet()) {
            signatureParams.add(entry.getKey() + "=" + entry.getValue());
        }

        signatureParams.add(signedNonce);
        String signatureString = String.join("&", signatureParams);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(signatureString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new MiIoCryptoException("SHA-1 algorithm not found", e);
        }
    }

    /**
     * Generate encrypted parameters for the request.
     *
     * <p>
     * This method creates a copy of the provided params map and performs the following
     * steps:
     * <ol>
     * <li>Compute an rc4_hash__ value used by the Xiaomi API.</li>
     * <li>Encrypt each parameter value using RC4 with the provided signedNonce.</li>
     * <li>Add signature, ssecurity and _nonce fields required by the API.</li>
     * </ol>
     *
     * @param url the full request url
     * @param method HTTP method (GET/POST)
     * @param signedNonce secret key for encryption (Base64 encoded)
     * @param nonce nonce value
     * @param params request params (will not be modified)
     * @param ssecurity ssecurity value
     * @return Map with encrypted parameters and required fields (a new map)
     * @throws MiIoCryptoException when encryption or signature generation fails
     */
    public static Map<String, String> generateEncParams(String url, String method, String signedNonce, String nonce,
            Map<String, String> params, String ssecurity) throws MiIoCryptoException {
        Map<String, String> encParams = new TreeMap<>(params);
        // Step 1: Add rc4_hash__
        String rc4Hash = generateEncSignature(url, method, signedNonce, encParams);
        encParams.put("rc4_hash__", rc4Hash);
        // Step 2: Encrypt all values with RC4
        for (Map.Entry<String, String> entry : new TreeMap<>(encParams).entrySet()) {
            String encrypted = CloudCrypto.encryptRc4(signedNonce, entry.getValue());
            encParams.put(entry.getKey(), encrypted);
        }
        // Step 3: Add signature, ssecurity, _nonce
        encParams.put("signature", generateEncSignature(url, method, signedNonce, encParams));
        encParams.put("ssecurity", ssecurity);
        encParams.put("_nonce", nonce);
        return encParams;
    }

    /**
     * Generate a time-dependent nonce value encoded as BASE64.
     *
     * <p>
     * The generated value embeds a random long and a minute-resolution timestamp
     * (milli / 60000). The result is safe for use with Xiaomi's signing API.
     *
     * @param milli current epoch milliseconds used to derive the timestamp portion
     * @return BASE64 encoded nonce string
     * @throws IOException never thrown in current implementation, kept for API compatibility
     */
    public static String generateNonce(long milli) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(output);
        dataOutputStream.writeLong(RANDOM.nextLong());
        dataOutputStream.writeInt((int) (milli / 60000));
        dataOutputStream.flush();
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    /**
     * Produce a signed nonce by concatenating the decoded ssecret and decoded nonce,
     * then computing the SHA-256 hash and returning it as BASE64.
     *
     * @param ssecret base64-encoded secret (ssecurity)
     * @param nonce base64-encoded nonce (from {@link #generateNonce})
     * @return BASE64 encoded SHA-256 hash of (ssecret || nonce)
     * @throws IOException if base64 decoding fails (propagated for callers)
     * @throws MiIoCryptoException on crypto related errors
     */
    public static String signedNonce(String ssecret, String nonce) throws IOException, MiIoCryptoException {
        byte[] byteArrayS = Base64.getDecoder().decode(ssecret.getBytes(StandardCharsets.UTF_8));
        byte[] byteArrayN = Base64.getDecoder().decode(nonce.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(byteArrayS);
        output.write(byteArrayN);
        return CloudCrypto.sha256Hash(output.toByteArray());
    }

    /**
     * Write a byte array to a filesystem path using NIO.
     *
     * @param bFile data to write
     * @param fileDest destination path as string
     * @throws IOException when writing to the filesystem fails
     */
    public static void writeBytesToFileNio(byte[] bFile, String fileDest) throws IOException {
        Path path = Paths.get(fileDest);
        Files.write(path, bFile);
    }

    /**
     * Parse a Xiaomi JSON response that may contain a non-JSON prefix.
     *
     * <p>
     * Xiaomi sometimes prefixes JSON payloads with the string "&&&START&&&".
     * This helper removes that prefix when present. If the prefix is not found
     * the method returns UNEXPECTED + original data to aid debugging.
     *
     * @param data raw response string
     * @return cleaned JSON string or an UNEXPECTED-prefixed value
     */
    public static String parseJson(String data) {
        if (data.contains("&&&START&&&")) {
            return data.replace("&&&START&&&", "");
        } else {
            return UNEXPECTED.concat(data);
        }
    }

    /**
     * Safely extracts a string value from a JsonObject.
     *
     * @param json The JsonObject to extract from
     * @param key The key to look up
     * @param defaultValue The default value if key is missing or null
     * @return The string value or defaultValue if not available
     */
    public static String getJsonString(@Nullable JsonObject json, String key, String defaultValue) {
        if (json == null || !json.has(key)) {
            return defaultValue;
        }
        JsonElement element = json.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        try {
            return element.getAsString();
        } catch (IllegalStateException e) {
            return defaultValue;
        }
    }

    /**
     * Safely extracts an integer value from a JsonObject.
     *
     * @param json The JsonObject to extract from
     * @param key The key to look up
     * @param defaultValue The default value if key is missing or null
     * @return The integer value or defaultValue if not available
     */
    public static int getJsonInt(@Nullable JsonObject json, String key, int defaultValue) {
        if (json == null || !json.has(key)) {
            return defaultValue;
        }
        JsonElement element = json.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        try {
            return element.getAsInt();
        } catch (IllegalStateException | NumberFormatException e) {
            return defaultValue;
        }
    }
}
