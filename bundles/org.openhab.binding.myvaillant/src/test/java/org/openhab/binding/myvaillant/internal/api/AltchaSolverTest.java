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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests {@link AltchaSolver} against solutions computed with Python's {@code hashlib.pbkdf2_hmac} (like myPyllant).
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class AltchaSolverTest {

    private static JsonObject challenge(String algorithm, int keyLength) {
        return JsonParser.parseString("{\"parameters\":{\"algorithm\":\"" + algorithm + "\",\"cost\":50,\"keyLength\":"
                + keyLength
                + ",\"keyPrefix\":\"00\",\"nonce\":\"8f1c2a9b0d4e6f7a\",\"salt\":\"5a17c3e9b2d48f60\"},\"signature\":\"abc123\"}")
                .getAsJsonObject();
    }

    @Test
    void solvesSha256LikeMyPyllant() throws MyVaillantException {
        // python3: json.dumps(payload, separators=(",", ":")) of the same challenge, base64 encoded
        assertEquals(
                "eyJjaGFsbGVuZ2UiOnsicGFyYW1ldGVycyI6eyJhbGdvcml0aG0iOiJQQktERjIvU0hBLTI1NiIsImNvc3QiOjUwLCJrZXlMZW5ndGgiOjMyLCJrZXlQcmVmaXgiOiIwMCIsIm5vbmNlIjoiOGYxYzJhOWIwZDRlNmY3YSIsInNhbHQiOiI1YTE3YzNlOWIyZDQ4ZjYwIn0sInNpZ25hdHVyZSI6ImFiYzEyMyJ9LCJzb2x1dGlvbiI6eyJjb3VudGVyIjo2MDQsImRlcml2ZWRLZXkiOiIwMDJkYTI3NWE0NWE1Y2RiMmUwMzE4ZDZkNDZkYzI3NmExZTEzMGE3ZjJiOTZmZmI1MGEwNDYyOGRmYTJkNzNmIiwidGltZSI6MH19",
                AltchaSolver.solve(challenge("PBKDF2/SHA-256", 32)));
    }

    @Test
    void solvesSha512() throws MyVaillantException {
        String payload = new String(Base64.getDecoder().decode(AltchaSolver.solve(challenge("PBKDF2/SHA-512", 64))),
                StandardCharsets.UTF_8);
        JsonObject solution = JsonParser.parseString(payload).getAsJsonObject().getAsJsonObject("solution");
        assertEquals(207, solution.get("counter").getAsInt());
        assertEquals(
                "009614d21c7d93e5550f5ec289a0eb62d29a0723e8b6f85343df1c8d4ad33fb5fc910fa653a20923dd542a87a3060d9260a2e6140e2a0572441d7e5342d6b07c",
                solution.get("derivedKey").getAsString());
    }

    @Test
    void pbkdf2SpansTwoBlocks() throws Exception {
        // python3: hashlib.pbkdf2_hmac("sha256", b"password", b"salt", 2, 40).hex() spans two blocks
        assertEquals("ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43830651afcb5c862f",
                HexFormat.of().formatHex(AltchaSolver.pbkdf2("HmacSHA256", "password".getBytes(StandardCharsets.UTF_8),
                        "salt".getBytes(StandardCharsets.UTF_8), 2, 40)));
    }

    @Test
    void rejectsIncompleteChallenge() {
        assertThrows(MyVaillantException.class,
                () -> AltchaSolver.solve(JsonParser.parseString("{\"signature\":\"x\"}").getAsJsonObject()));
        assertThrows(MyVaillantException.class, () -> AltchaSolver.solve(JsonParser.parseString(
                "{\"parameters\":{\"algorithm\":\"PBKDF2/SHA-256\",\"cost\":1,\"keyPrefix\":\"zz\",\"nonce\":\"00\",\"salt\":\"00\"},\"signature\":\"x\"}")
                .getAsJsonObject()));
    }
}
