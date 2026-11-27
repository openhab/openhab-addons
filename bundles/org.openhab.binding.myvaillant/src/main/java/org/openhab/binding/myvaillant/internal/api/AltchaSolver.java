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
package org.openhab.binding.myvaillant.internal.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Solves the ALTCHA proof-of-work challenge of Vaillant's Keycloak login page and packages the solution like the
 * browser widget does (base64 encoded JSON, sent as the "altcha" form field). Same algorithm as
 * {@code solve_altcha_challenge} in myPyllant.
 *
 * <p>
 * The PBKDF2 password is raw bytes (nonce followed by the counter), which the JDK's {@code PBKDF2WithHmacSHA256} cannot
 * take because {@code PBEKeySpec} only accepts characters. PBKDF2 is therefore implemented on top of {@link Mac}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
final class AltchaSolver {

    /** Expected attempts are 256 per prefix byte, this bounds a malformed challenge */
    private static final int MAX_COUNTER = 5_000_000;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private AltchaSolver() {
    }

    static String solve(JsonObject challenge) throws MyVaillantException, InterruptedException {
        JsonObject parameters = JsonUtil.getObject(challenge, "parameters");
        JsonElement signature = challenge.get("signature");
        Integer cost = JsonUtil.getInteger(parameters, "cost");
        if (parameters == null || signature == null || cost == null) {
            throw new MyVaillantException("Incomplete ALTCHA challenge");
        }
        Integer keyLengthValue = JsonUtil.getInteger(parameters, "keyLength");
        int keyLength = keyLengthValue == null ? 32 : keyLengthValue;
        String macAlgorithm = switch (JsonUtil.requireString(parameters, "algorithm")) {
            case "PBKDF2/SHA-512" -> "HmacSHA512";
            case "PBKDF2/SHA-384" -> "HmacSHA384";
            default -> "HmacSHA256";
        };
        try {
            HexFormat hex = HexFormat.of();
            byte[] nonce = hex.parseHex(JsonUtil.requireString(parameters, "nonce"));
            byte[] salt = hex.parseHex(JsonUtil.requireString(parameters, "salt"));
            byte[] keyPrefix = hex.parseHex(JsonUtil.requireString(parameters, "keyPrefix"));
            if (keyPrefix.length > keyLength) {
                throw new MyVaillantException("ALTCHA key prefix longer than the key");
            }

            byte[] password = Arrays.copyOf(nonce, nonce.length + Integer.BYTES);
            for (int counter = 0; counter < MAX_COUNTER; counter++) {
                // Solving takes some seconds, e.g. disposing the account must not wait for it
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                ByteBuffer.wrap(password, nonce.length, Integer.BYTES).putInt(counter);
                byte[] derivedKey = pbkdf2(macAlgorithm, password, salt, cost, keyLength);
                if (Arrays.equals(derivedKey, 0, keyPrefix.length, keyPrefix, 0, keyPrefix.length)) {
                    JsonObject solution = new JsonObject();
                    solution.addProperty("counter", counter);
                    solution.addProperty("derivedKey", hex.formatHex(derivedKey));
                    solution.addProperty("time", 0);
                    JsonObject solvedChallenge = new JsonObject();
                    solvedChallenge.add("parameters", parameters);
                    solvedChallenge.add("signature", signature);
                    JsonObject payload = new JsonObject();
                    payload.add("challenge", solvedChallenge);
                    payload.add("solution", solution);
                    return Base64.getEncoder().encodeToString(GSON.toJson(payload).getBytes(StandardCharsets.UTF_8));
                }
            }
            throw new MyVaillantException("No ALTCHA solution found");
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new MyVaillantException("Invalid ALTCHA challenge: " + e.getMessage(), e);
        }
    }

    static byte[] pbkdf2(String macAlgorithm, byte[] password, byte[] salt, int iterations, int keyLength)
            throws GeneralSecurityException {
        Mac mac = Mac.getInstance(macAlgorithm);
        mac.init(new SecretKeySpec(password, macAlgorithm));
        int macLength = mac.getMacLength();
        byte[] result = new byte[keyLength];
        int blocks = (keyLength + macLength - 1) / macLength;
        for (int block = 1; block <= blocks; block++) {
            mac.update(salt);
            mac.update(ByteBuffer.allocate(Integer.BYTES).putInt(block).array());
            byte[] u = mac.doFinal();
            byte[] t = u.clone();
            for (int i = 1; i < iterations; i++) {
                u = mac.doFinal(u);
                for (int j = 0; j < t.length; j++) {
                    t[j] ^= u[j];
                }
            }
            int offset = (block - 1) * macLength;
            System.arraycopy(t, 0, result, offset, Math.min(macLength, keyLength - offset));
        }
        return result;
    }
}
