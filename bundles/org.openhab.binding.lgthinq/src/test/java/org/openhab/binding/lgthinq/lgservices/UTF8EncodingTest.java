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
package org.openhab.binding.lgthinq.lgservices;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test class to validate UTF-8 encoding fixes using the actual service class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class UTF8EncodingTest {

    @TempDir
    @Nullable

    Path tempDir;

    @Nullable
    private LGThinQTestService service;

    @BeforeEach
    void setUp() {
        Path dir = Objects.requireNonNull(tempDir);
        System.setProperty("THINQ_USER_DATA_FOLDER", dir.toAbsolutePath().toString());
        HttpClient httpClient = Objects.requireNonNull(Mockito.mock(HttpClient.class));
        service = new LGThinQTestService(httpClient);
    }

    @Test
    void testMalformedUTF8InJson() throws IOException, LGThinqApiException {
        Path dir = Objects.requireNonNull(tempDir);
        // malformedJson contains a valid-ish structure with an invalid UTF-8 sequence (0xb0) inside a string value
        // We need a minimal structure that ACCapabilityFactoryV1 accepts
        String part1 = "{\"Info\": {\"productType\": \"AC\", \"modelType\": \"JSON\"}, \"Value\": {"
                + "\"OpMode\": {\"option\": {\"0\": \"@OFF\"}}, "
                + "\"WindStrength\": {\"option\": {\"0\": \"@LOW\"}}, "
                + "\"SupportOpMode\": {\"option\": {\"0\": \"@OFF\"}}, "
                + "\"SupportWindStrength\": {\"option\": {\"0\": \"@LOW\"}}, " + "\"test\": \"Value ";
        String part2 = "\"}}";

        byte[] part1Bytes = part1.getBytes(StandardCharsets.UTF_8);
        byte[] part2Bytes = part2.getBytes(StandardCharsets.UTF_8);
        byte[] malformedJson = new byte[part1Bytes.length + 1 + part2Bytes.length];
        System.arraycopy(part1Bytes, 0, malformedJson, 0, part1Bytes.length);
        malformedJson[part1Bytes.length] = (byte) 0xb0;
        System.arraycopy(part2Bytes, 0, malformedJson, part1Bytes.length + 1, part2Bytes.length);

        // Create a temporary file with the malformed content
        Path malformedFile = dir.resolve("malformed.json");
        Files.write(malformedFile, malformedJson);

        // We use the service to read this file. We provide a file URI.
        ACCapability cap = Objects.requireNonNull(service).getCapability("testDevice", malformedFile.toUri().toString(),
                true);

        assertNotNull(cap);
        // ACCapability for V1 extracts data from the "Value" node.
        @SuppressWarnings("unchecked")
        Map<String, Object> valueNode = (Map<String, Object>) cap.getRawData().get("Value");
        assertNotNull(valueNode);
        String value = Objects.requireNonNull((String) valueNode.get("test"));

        // The invalid byte 0xb0 should be replaced by the Unicode replacement character \uFFFD
        assertTrue(value.contains("\uFFFD"), "Resulting string should contain the Unicode replacement character");
        assertEquals("Value \uFFFD", value);
    }

    @Test
    void testBase64UTF8Decoding() {
        // String with special characters: "Café"
        String original = "Café";
        String encoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));

        // In LGThinQAbstractApiClientService (line 405) and LGThinQACApiV1ClientServiceImpl (line 80),
        // we use: new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8)
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

        assertEquals(original, decoded);
    }

    private static class LGThinQTestService extends LGThinQAbstractApiClientService<ACCapability, ACCanonicalSnapshot> {
        public LGThinQTestService(HttpClient httpClient) {
            super(ACCapability.class, ACCanonicalSnapshot.class, httpClient);
        }

        @Override
        public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
                throws LGThinqApiException {
        }

        @Override
        protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
            return false;
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
                String command, String keyName, String value) throws Exception {
            return new RestResult("", 200);
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
                String command, @Nullable String keyName, @Nullable String value, @Nullable ObjectNode extraNode)
                throws Exception {
            return new RestResult("", 200);
        }

        @Override
        protected Map<String, Object> handleGenericErrorResult(@Nullable RestResult resp) throws LGThinqApiException {
            return Collections.emptyMap();
        }
    }
}
